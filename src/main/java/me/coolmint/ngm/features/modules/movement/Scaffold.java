package me.coolmint.ngm.features.modules.movement;

import com.google.common.eventbus.Subscribe;
import javafx.scene.input.KeyCode;
import me.coolmint.ngm.Ngm;
import me.coolmint.ngm.event.Stage;
import me.coolmint.ngm.event.impl.*;
import me.coolmint.ngm.features.modules.Module;
import me.coolmint.ngm.features.settings.Setting;
import me.coolmint.ngm.util.ChatUtil.sendInfoUtil;
import me.coolmint.ngm.util.MathUtil;
import me.coolmint.ngm.util.MovementUtility;
import me.coolmint.ngm.util.models.Timer;
import me.coolmint.ngm.util.player.InventoryUtility;
import me.coolmint.ngm.util.player.Rotation;
import me.coolmint.ngm.util.player.RotationUtils;
import net.minecraft.block.AirBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.Keyboard;
import net.minecraft.client.network.SequencedPacketCreator;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import org.apache.commons.lang3.RandomUtils;
import org.joml.Vector3f;

import java.util.LinkedList;
import java.util.concurrent.LinkedBlockingQueue;

import static me.coolmint.ngm.util.MovementUtility.getSpeed;

public class Scaffold extends Module {
    private Setting<Boolean> allowTower = register(new Setting<>("EnableTower", true));
    private Setting<towerMove1> towerMove = register(new Setting<>("TowerWhen", towerMove1.Always, v -> allowTower.getValue()));
    enum towerMove1 {
        Always, Moving, Standing
    }
    private Setting<towerMode> towerModeValue = register(new Setting<>("TowerMode", towerMode.ConstantMotion, v -> allowTower.getValue()));
    enum towerMode {
        Jump, Motion, StableMotion, ConstantMotion, MotionTP, Teleport, AAC339, AAC364, BlocksMC, Watchdog, Float
    }
    private Setting<Float> towerTimerValue = register(new Setting<>("TowerTimer", 1f, 0.1f, 1.4f, v -> allowTower.getValue()));

    // Watchdog
    private Setting<Boolean> watchdogTowerBoostValue = register(new Setting<>("WatchdogTowerBoost", true, v -> allowTower.getValue() && towerModeValue.getValue() == towerMode.Watchdog));
    private Setting<Float> watchdogTowerSpeed = register(new Setting<>("Watchdog-TowerSpeed", 1.7f, 1.5f, 2.5f, v -> allowTower.getValue() && towerModeValue.getValue() == towerMode.Watchdog && watchdogTowerBoostValue.getValue()));

    // Jump mode
    private Setting<Float> jumpMotionValue = register(new Setting<>("JumpMotion", 0.42f, 0.3681289f, 0.79f, v -> allowTower.getValue() && towerModeValue.getValue() == towerMode.Jump));
    private Setting<Integer> jumpDelayValue = register(new Setting<>("JumpDelay", 0, 0, 20, v -> allowTower.getValue() && towerModeValue.getValue() == towerMode.Jump));

    // StableMotion
    private Setting<Float> stableMotionValue = register(new Setting<>("StableMotion", 0.41982f, 0.1f, 1f, v -> allowTower.getValue() && towerModeValue.getValue() == towerMode.StableMotion));
    private Setting<Boolean> stableFakeJumpValue = register(new Setting<>("StableFakeJump", false, v -> allowTower.getValue() && towerModeValue.getValue() == towerMode.StableMotion));
    private Setting<Boolean> stableStopValue = register(new Setting<>("StableStop", false, v -> allowTower.getValue() && towerModeValue.getValue() == towerMode.StableMotion));
    private Setting<Integer> stableStopDelayValue = register(new Setting<>("StableStopDelay", 1500, 0, 5000, v -> allowTower.getValue() && towerModeValue.getValue() == towerMode.StableMotion && stableStopValue.getValue()));

    // ConstantMotion
    private Setting<Float> constantMotionValue = register(new Setting<>("ConstantMotion", 0.42f, 0.1f, 1f, v -> allowTower.getValue() && towerModeValue.getValue() == towerMode.ConstantMotion));
    private Setting<Float> constantMotionJumpGroundValue = register(new Setting<>("ConstantMotionJumpGround", 0.79f, 0.76f, 1f, v -> allowTower.getValue() && towerModeValue.getValue() == towerMode.ConstantMotion));

    // Teleport
    private Setting<Float> teleportHeightValue = register(new Setting<>("TeleportHeight", 1.15f, 0.1f, 5f, v -> allowTower.getValue() && towerModeValue.getValue() == towerMode.Teleport));
    private Setting<Integer> teleportDelayValue = register(new Setting<>("TeleportDelay", 0, 0, 20, v -> allowTower.getValue() && towerModeValue.getValue() == towerMode.Teleport));
    private Setting<Boolean> teleportGroundValue = register(new Setting<>("TeleportGround", true, v -> allowTower.getValue() && towerModeValue.getValue() == towerMode.Teleport));
    private Setting<Boolean> teleportNoMotionValue = register(new Setting<>("TeleportNoMotion", false, v -> allowTower.getValue() && towerModeValue.getValue() == towerMode.Teleport));

    // Timing
    public Setting<sprintModes> sprintModeValue = register(new Setting<>("SprintMode", sprintModes.Same));
    enum sprintModes {
        Same, Silent, Ground, Air, Off
    }
    private Setting<placeConditions> placeConditionValue = register(new Setting<>("Place-Condition", placeConditions.Always));
    enum placeConditions {
        Air, FallDown, NegativeMotion, Always
    }

    private Setting<rotationModes> rotationModeValue = register(new Setting<>("RotationMode", rotationModes.Normal));
    enum rotationModes {
        Normal,
        Smooth
    }
    private Setting<waitRotationMods> preRotationValue = register(new Setting<>("WaitRotationMode", waitRotationMods.Normal));
    enum waitRotationMods {
        Normal,
        Lock,
        None
    }
    private Setting<autoJumps> autoJumpValue = register(new Setting<>("AutoJumpMode", autoJumps.Off));
    enum autoJumps {
        Off,
        Normal,
        KeepY,
        Breezily
    }
    private Setting<Integer> breezilyDelayValue = register(new Setting<>("Breezily-Delay", 7, 2, 12, v -> autoJumpValue.getValue() == autoJumps.Breezily));

    private Setting<Float> timerValue = register(new Setting<>("Timer", 1f, 0.1f, 1.4f));

    // Delay
    private Setting<Boolean> placeableDelay = register(new Setting<>("PlaceableDelay", false));
    private Setting<Integer> maxDelayValue = register(new Setting<>("MaxDelay", 50, 0, 1000, v -> placeableDelay.getValue()));
    private Setting<Integer> minDelayValue = register(new Setting<>("MinDelay", 50, 0, 1000, v -> placeableDelay.getValue()));

    private Setting<Boolean> startPlaceDelayValue = register(new Setting<>("StartPlaceChecks", false));
    private Setting<Integer> startPlaceDelay = register(new Setting<>("StartPlace-Delay", 5, 5, 30, v -> startPlaceDelayValue.getValue()));
    private Setting<Boolean> placeSlowDownValue = register(new Setting<>("Place-SlowDown", false));
    private Setting<Float> speedModifierValue = register(new Setting<>("Speed-Multiplier", 0.8f, 0f, 1.4f, v -> placeSlowDownValue.getValue()));
    private Setting<Boolean> slowDownValue = register(new Setting<>("SlowDown", false));
    private Setting<Float> xzMultiplier = register(new Setting<>("XZ-Multiplier", 0.6f, 0f, 1.1f, v -> slowDownValue.getValue()));
    private Setting<Boolean> noSpeedPotValue = register(new Setting<>("NoSpeedPot", false));
    private Setting<Float> speedSlowDown = register(new Setting<>("SpeedPot-SlowDown", 0.8f, 0.0f, 1.1f, v -> noSpeedPotValue.getValue()));
    private Setting<Boolean> customSpeedValue = register(new Setting<>("CustomSpeed", false));
    private Setting<Float> customMoveSpeedValue = register(new Setting<>("CustomMoveSpeed", 0.2f, 0f, 5f, v -> customSpeedValue.getValue()));
    private Setting<Boolean> autoSneakValue = register(new Setting<>("AutoSneak", false));
    private Setting<Boolean> smartSpeedValue = register(new Setting<>("SpeedKeepY", true));
    private Setting<Boolean> safeWalkValue = register(new Setting<>("SafeWalk", false));
    private Setting<Boolean> airSafeValue = register(new Setting<>("AirSafe", false, v -> safeWalkValue.getValue()));
    private Setting<Boolean> autoDisableSpeedValue = register(new Setting<>("AutoDisable-Speed", false));
    private Setting<Boolean> desyncValue = register(new Setting<>("Desync", false));
    private Setting<Integer> desyncDelayValue = register(new Setting<>("DesyncDelay", 400, 10, 810, v -> desyncValue.getValue()));
    private Setting<Float> maxTurnSpeed = register(new Setting<>("MaxTurnSpeed", 120f, 0f, 180f));
    private Setting<Float> minTurnSpeed = register(new Setting<>("MinTurnSpeed", 80f, 0f, 180f));

    @Subscribe
    public void onSettingChange(ClientEvent event) {
        if (event.getStage() == 2 && event.getSetting().getFeature().equals(this)) {
            if (event.getSetting().equals(this.maxDelayValue) && this.maxDelayValue.getPlannedValue() < this.minDelayValue.getValue()) {
                maxDelayValue.setValue(minDelayValue.getValue() + 1);
            }
            if (event.getSetting().equals(this.minDelayValue) && this.minDelayValue.getPlannedValue() > this.maxDelayValue.getValue()) {
                minDelayValue.setValue(maxDelayValue.getValue() - 1);
            }

            if (event.getSetting().equals(this.maxTurnSpeed) && this.maxTurnSpeed.getPlannedValue() < this.minTurnSpeed.getValue()) {
                maxTurnSpeed.setValue(minTurnSpeed.getValue() + 1f);
            }
            if (event.getSetting().equals(this.minTurnSpeed) && this.minTurnSpeed.getPlannedValue() > this.maxTurnSpeed.getValue()) {
                minTurnSpeed.setValue(maxTurnSpeed.getValue() - 1f);
            }
        }
    }

    public Scaffold() {
        super("Scaffold", "", Category.MOVEMENT, true, false, false);
    }

    // Delay
    private Timer delayTimer = new Timer();
    private Timer towerDelayTimer = new Timer();

    // Mode stuff
    private Timer timer = new Timer();
    private Timer startPlaceTimer = new Timer();

    /**
     * MODULE
     */
    // Target block
    private PlaceInfo targetPlace = null;

    // Desync
    private LinkedBlockingQueue<Packet<?>> packets = new LinkedBlockingQueue<>();
    private LinkedList<double[]> positions = new LinkedList<>();
    private Timer pulseTimer = new Timer();
    private boolean disableLogger = false;

    // Launch position
    private int launchY = 0;
    private int placeCount = 0;
    private boolean faceBlock = false;

    // Rotation lock
    private Rotation lockRotation = null;
    private Rotation lookupRotation = null;

    // Auto block slot
    private int slot = 0;
    private int lastSlot = 0;

    private long delay = 0;

    // Tower
    private int offGroundTicks = 0;
    private boolean verusJumped = false;
    private int wdTick = 0;
    private boolean wdSpoof = false;
    private int towerTick = 0;

    // Render thingy
    private boolean canTower = false;
    private float firstPitch = 0f;
    private float firstRotate = 0f;
    private float progress = 0f;
    private float spinYaw = 0f;
    private long lastMS = 0L;
    private double jumpGround = 0.0;
    private int verusState = 0;

    @Override
    public void onEnable() {
        if (mc.player == null) return;
        progress = 0f;
        spinYaw = 0f;
        wdTick = 5;
        placeCount = 0;
        firstPitch = mc.player.getPitch();
        firstRotate = mc.player.getYaw();
        launchY = (int) mc.player.getY();
        lastSlot = mc.player.getInventory().selectedSlot;
        slot = mc.player.getInventory().selectedSlot;
        canTower = false;
        lastMS = System.currentTimeMillis();
        if (desyncValue.getValue()) {
            synchronized(positions) {
                positions.add(new double[]{
                        mc.player.getX(),
                        mc.player.getBoundingBox().minY + mc.player.getEyeY() / 2,
                        mc.player.getZ()
                });
                positions.add(new double[]{mc.player.getX(), mc.player.getBoundingBox().minY, mc.player.getZ()});
            }
            pulseTimer.reset();
        }
    }

    private void fakeJump() {
        if (mc.player != null) {
            mc.player.setAir(1);
            mc.player.addStat(StatList.jumpStat);
        }
    }
    
    @Override
    public void onUpdate() {
        if (!faceBlock && startPlaceTimer.passedMs(1))
            startPlaceTimer.reset();

        if (getBlocksAmount() <= 0) {
            faceBlock = false;
            return;
        }

        if (lockRotation != null) {
            Rotation serverRotation = RotationUtils.getServerLookVec();
            if (serverRotation != null) {
                Rotation targetRotation = RotationUtils.limitAngleChange(
                        serverRotation,
                        lockRotation,
                        RandomUtils.nextFloat(minTurnSpeed.getValue(), maxTurnSpeed.getValue())
                );
                RotationUtils.setTargetRotation(targetRotation);
            }
        } else {
            faceBlock = false;
            String preRotation = preRotationValue.getValue().toString().toLowerCase();

            switch (preRotation) {
                case "normal":
                    float yaw = MovementUtility.getRawDirection() - 180f;
                    float pitch = 83f;
                    Rotation targetRotation = RotationUtils.limitAngleChange(
                            RotationUtils.getServerLookVec(),
                            new Rotation(yaw, pitch),
                            RandomUtils.nextFloat(minTurnSpeed.getValue(), maxTurnSpeed.getValue())
                    );
                    RotationUtils.setTargetRotation(targetRotation);
                    break;

                case "lock":
                    RotationUtils.setTargetRotation(new Rotation(firstRotate, firstPitch));
                    break;

                default:
                    break;
            }
        }

        boolean shouldEagle = mc.world.getBlockState(
                new BlockPos((int) mc.player.getX(), (int) (mc.player.getY() - 1.0), (int) mc.player.getZ())
        ).getBlock() == Blocks.AIR;

        if (!canTower && (autoSneakValue.getValue() && mc.player.isOnGround() && shouldEagle || mc.options.sneakKey.isPressed()))
            mc.options.sneakKey.setPressed(true);
        else if (!mc.options.sneakKey.isPressed())
            mc.options.sneakKey.setPressed(false);

        if (!canTower && towerModeValue.getValue() == towerMode.Watchdog && mc.player.ticksExisted % 2 == 0) {
            wdTick = 5;
            towerTick = 0;
            wdSpoof = false;
        }

        if (allowTower.getValue() && mc.options.jumpKey.isPressed() && !mc.options.sneakKey.isPressed() && getBlocksAmount() > 0 && MovementUtility.isRidingBlock() &&
                (towerMove.getValue() == towerMove1.Always ||
                        (!MovementUtility.isMoving() && towerMove.getValue() == towerMove1.Standing) ||
                        (MovementUtility.isMoving() && towerMove.getValue() == towerMove1.Moving))) {

            canTower = true;
            String towerMode = towerModeValue.getValue().toString().toLowerCase();

            switch (towerMode) {
                case "jump":
                    if (mc.player.isOnGround() && timer.passedMs(jumpDelayValue.getValue())) {
                        fakeJump();
                        mc.player.setVelocity(mc.player.getVelocity().x, jumpMotionValue.getValue(), mc.player.getVelocity().z);
                        timer.reset();
                    }
                    break;

                case "motion":
                    if (mc.player.isOnGround()) {
                        fakeJump();
                        mc.player.setVelocity(mc.player.getVelocity().x, 0.42, mc.player.getVelocity().z);
                    } else if (mc.player.getVelocity().y < 0.1) {
                        mc.player.setVelocity(mc.player.getVelocity().x, -0.3, mc.player.getVelocity().z);
                    }
                    break;

                case "motiontp":
                    if (mc.player.isOnGround()) {
                        fakeJump();
                        mc.player.setVelocity(mc.player.getVelocity().x, 0.42, mc.player.getVelocity().z);
                    } else if (mc.player.getVelocity().y < 0.23) {
                        mc.player.setPosition(mc.player.getX(), mc.player.getY(), mc.player.getZ());
                    }
                    break;

                case "teleport":
                    if (teleportNoMotionValue.getValue()) {
                        mc.player.setVelocity(mc.player.getVelocity().x, 0, mc.player.getVelocity().z);
                    }
                    if ((mc.player.isOnGround() || !teleportGroundValue.getValue()) && delayTimer.passedMs(teleportDelayValue.getValue())) {
                        fakeJump();
                        mc.player.refreshPositionAfterTeleport(
                                mc.player.getX(),
                                mc.player.getY() + teleportHeightValue.getValue(),
                                mc.player.getZ()
                        );
                        timer.reset();
                    }
                    break;

                case "stablemotion":
                    if (stableFakeJumpValue.getValue()) {
                        fakeJump();
                    }
                    mc.player.setVelocity(mc.player.getVelocity().x, stableMotionValue.getValue(), mc.player.getVelocity().z);
                    if (stableStopValue.getValue() && towerDelayTimer.passedMs(stableStopDelayValue.getValue())) {
                        mc.player.setVelocity(mc.player.getVelocity().x, -0.28, mc.player.getVelocity().z);
                        towerDelayTimer.reset();
                    }
                    break;

                case "constantmotion":
                    if (mc.player.isOnGround()) {
                        fakeJump();
                        jumpGround = mc.player.getY();
                        mc.player.setVelocity(mc.player.getVelocity().x, constantMotionValue.getValue(), mc.player.getVelocity().z);
                    }
                    if (mc.player.getY() > jumpGround + constantMotionJumpGroundValue.getValue()) {
                        fakeJump();
                        mc.player.setPosition(mc.player.getX(), mc.player.getY(), mc.player.getZ());
                        mc.player.setVelocity(mc.player.getVelocity().x, constantMotionValue.getValue(), mc.player.getVelocity().z);
                        jumpGround = mc.player.getY();
                    }
                    break;

                case "watchdog":
                    if (wdTick != 0) {
                        towerTick = 0;
                        return;
                    }
                    if (towerTick > 0) {
                        towerTick++;
                        if (towerTick > 6) {
                            mc.player.setVelocity(mc.player.getVelocity().x * 0.9f, mc.player.getVelocity().y, mc.player.getVelocity().z * 0.9f);
                        }
                        if (towerTick > 16) {
                            towerTick = 0;
                        }
                    }
                    if (mc.player.isOnGround()) {
                        if (towerTick == 0 || towerTick == 5) {
                            if (watchdogTowerBoostValue.getValue()) {
                                mc.player.setVelocity(mc.player.getVelocity().x * watchdogTowerSpeed.getValue(), mc.player.getVelocity().y, mc.player.getVelocity().z * watchdogTowerSpeed.getValue());
                            }
                            mc.player.setVelocity(mc.player.getVelocity().x, 0.42, mc.player.getVelocity().z);
                            towerTick = 1;
                        }
                    }
                    if (mc.player.getVelocity().y > -0.0784000015258789) {
                        if (!mc.player.isOnGround()) {
                            switch ((int) (mc.player.getY() % 1.0 * 100.0)) {
                                case 42:
                                    mc.player.setVelocity(mc.player.getVelocity().x, 0.33, mc.player.getVelocity().z);
                                    break;
                                case 75:
                                    mc.player.setVelocity(mc.player.getVelocity().x, 1.0 - (mc.player.getY() % 1.0), mc.player.getVelocity().z);
                                    wdSpoof = true;
                                    break;
                                case 0:
                                    if (MovementUtility.isRidingBlock()) {
                                        mc.player.setVelocity(mc.player.getVelocity().x, -0.0784000015258789, mc.player.getVelocity().z);
                                    }
                                    break;
                            }
                        }
                    } else {
                        mc.player.jump();
                    }
                    break;

                case "blocksmc":
                    if (mc.player.getY() % 1 <= 0.00153598) {
                        mc.player.setPosition(mc.player.getX(), Math.floor(mc.player.getY()), mc.player.getZ());
                        mc.player.setVelocity(mc.player.getVelocity().x, 0.42, mc.player.getVelocity().z);
                    } else if (mc.player.getY() % 1 < 0.1 && offGroundTicks != 0) {
                        mc.player.setPosition(mc.player.getX(), Math.floor(mc.player.getY()), mc.player.getZ());
                    }
                    break;

                case "aac339":
                    if (mc.player.isOnGround()) {
                        fakeJump();
                        mc.player.setVelocity(mc.player.getVelocity().x, 0.4001, mc.player.getVelocity().z);
                    }
                    Ngm.TICK_TIMER = 1f;
                    if (mc.player.getVelocity().y < 0) {
                        mc.player.setVelocity(mc.player.getVelocity().x, 0.00000945, mc.player.getVelocity().z);
                        Ngm.TICK_TIMER = 1.6f;
                    }
                    break;

                case "aac364":
                    if (mc.player.ticksExisted % 4 == 1) {
                        mc.player.setVelocity(mc.player.getVelocity().x, 0.4195464, mc.player.getVelocity().z);
                        mc.player.setPosition(mc.player.getX() - 0.035, mc.player.getY(), mc.player.getZ());
                    } else if (mc.player.ticksExisted % 4 == 0) {
                        mc.player.setVelocity(mc.player.getVelocity().x, -0.5, mc.player.getVelocity().z);
                        mc.player.setPosition(mc.player.getX() + 0.035, mc.player.getY(), mc.player.getZ());
                    }
                    break;

                default:
                    break;
            }
        } else {
            canTower = false;
        }

        /* 스피드 아직 없다
        if (autoDisableSpeedValue.getValue() && Launch.moduleManager.getModule(Speed.class).getState()) {
            Launch.moduleManager.getModule(Speed.class).setState(false);
            ChatUtil.sendInfo("Speed was disabled");
        }

         */

        Ngm.TICK_TIMER = timerValue.getValue();

        if (mc.player.isOnGround()) {
            offGroundTicks = 0;
        } else {
            offGroundTicks++;
        }

        if (desyncValue.getValue()) {
            synchronized (positions) {
                positions.add(new double[]{mc.player.getX(), mc.player.getBoundingBox().minY, mc.player.getZ()});
            }
            if (pulseTimer.passedMs(desyncDelayValue.getValue())) {
                blink();
                pulseTimer.reset();
            }
        }

        // scaffold custom speed if enabled
        if (customSpeedValue.getValue()) {
            MovementUtility.strafe(customMoveSpeedValue.getValue());
        }

        if (sprintModeValue.getValue() == sprintModes.Off ||
                (sprintModeValue.getValue() == sprintModes.Ground && !mc.player.isOnGround()) ||
                (sprintModeValue.getValue() == sprintModes.Air && mc.player.isOnGround())) {
            mc.player.setSprinting(false);
        }

        if (autoJumpValue.getValue() != autoJumps.KeepY /*&&
                !(smartSpeedValue.getValue() && Launch.moduleManager.getModule(Speed.class).getState())*/ ||
                mc.options.jumpKey.isPressed() || mc.player.getY() < launchY) {
            launchY = (int) mc.player.getY();
        }

        if (mc.options.jumpKey.isPressed() && mc.player.isOnGround()) {
            placeCount = 0;
        }

        if ((autoJumpValue.getValue() == autoJumps.KeepY /*&& !Launch.moduleManager.getModule(Speed.class).getState())*/ ||
                (autoJumpValue.getValue() == autoJumps.Normal && faceBlock) ||
                (autoJumpValue.getValue() == autoJumps.Breezily && placeCount >= breezilyDelayValue.getValue()) &&
                        MovementUtility.isMoving() && mc.player.isOnGround())) {
            mc.player.jump();
            placeCount = 0;
        }
    }

    @Subscribe
    public void onPacket(PacketEvent event) {
        if (mc.player == null)
            return;

        Packet packet = event.getPacket();

        if (towerModeValue.getValue() == towerMode.Watchdog) {
            if (packet instanceof PlayerMoveC2SPacket) {
                if (wdSpoof) {
                    mc.player.setOnGround(true);
                    wdSpoof = false;
                }
            }
        }

        if (packet instanceof PlayerInteractBlockC2SPacket) {
            PlayerInteractBlockC2SPacket placementPacket = (PlayerInteractBlockC2SPacket) packet;
            placementPacket.setFacingX(MathHelper.clamp(placementPacket.getFacingX(), -1.0F, 1.0F));
            placementPacket.setFacingY(MathHelper.clamp(placementPacket.getFacingY(), -1.0F, 1.0F));
            placementPacket.setFacingZ(MathHelper.clamp(placementPacket.getFacingZ(), -1.0F, 1.0F));
        }

        if (packet instanceof PlayerInteractBlockC2SPacket && !mc.isIntegratedServerRunning()) {
            event.cancel();
            PlayerInteractBlockC2SPacket placementPacket = (PlayerInteractBlockC2SPacket) packet;

            Ngm.networkManager.sendSequencedPacket(new PlayerInteractBlockC2SPacket(
                    placementPacket.getPosition(),
                    placementPacket.getPlacedBlockDirection(),
                    null,
                    placementPacket.getPlacedBlockOffsetX(),
                    placementPacket.getPlacedBlockOffsetY(),
                    placementPacket.getPlacedBlockOffsetZ()
            ));
        }
    }

    /*
    @Subscribe
    public void onStrafe(StrafeEvent event) {
        if (getBlocksAmount() <= 0 || RotationUtils.targetRotation == null)
            return;

        boolean silentRotationsState = Launch.moduleManager.getModule(SilentRotations.class).state;
        boolean customStrafe = Launch.moduleManager.getModule(SilentRotations.class).customStrafe.getValue();

        if (silentRotationsState && !customStrafe) {
            event.yaw = RotationUtils.targetRotation.yaw - 180f;
        }
    }
     */

    private boolean shouldPlace() {
        String placeCondition = placeConditionValue.getValue().toString().toLowerCase();
        boolean placeWhenAir = placeCondition.equals("air");
        boolean placeWhenFall = placeCondition.equals("falldown");
        boolean placeWhenNegativeMotion = placeCondition.equals("negativemotion");
        boolean alwaysPlace = placeCondition.equals("always");

        return alwaysPlace || canTower || (placeWhenAir && !mc.player.isOnGround()) || (placeWhenFall && mc.player.fallDistance > 0) || (placeWhenNegativeMotion && mc.player.getVelocity().y < 0);
    }

    private void blink() {
        try {
            disableLogger = true;
            while (!packets.isEmpty()) {
                mc.getNetworkHandler().sendPacket(packets.take());
            }
            disableLogger = false;
        } catch (Exception e) {
            e.printStackTrace();
            disableLogger = false;
        }
        synchronized(positions) {
            positions.clear();
        }
    }

    public void onMotion(MotionEvent event) {
        if (getBlocksAmount() <= 0)
            return;

        // Handle tower movement
        if (canTower && !MovementUtility.isMoving() && event.eventState == Stage.POST) {
            mc.player.setVelocity(0.0, mc.player.getVelocity().y, 0.0);
        }

        // Decrease watchdog tick if in watchdog mode
        if (towerModeValue.getValue() == towerMode.Watchdog) {
            if (wdTick > 0) {
                wdTick--;
            }
        }

        // Check if should face block
        boolean shouldEagle = mc.world.getBlockState(new BlockPos((int) mc.player.getX(), (int) (mc.player.getY() - 1.0), (int) mc.player.getZ())).getBlock() == Blocks.AIR;
        if ((!mc.player.isOnGround() || shouldEagle) && mc.player.ticksExisted % 2 == 0 && !faceBlock) {
            faceBlock = true;
        }

        try {
            if (faceBlock) {
                place();
                mc.player.handSwinging = false;
            } else if (slot != mc.player.getInventory().getSlotWithStack(InventoryUtility.findAutoBlockBlock().slotNumber)) {
                mc.player.getInventory().selectedSlot = InventoryUtils.findAutoBlockBlock() - 36;
                mc.interactionManager.tick();
            }
        } catch (Exception ignored) {
        }

        // No SpeedPot
        if (noSpeedPotValue.getValue() && mc.player.isPotionActive(Potion.moveSpeed) && !canTower && mc.player.isOnGround()) {
            mc.player.setVelocity(mc.player.getVelocity().x * speedSlowDown.getValue(), mc.player.getVelocity().y, mc.player.getVelocity().z * speedSlowDown.getValue());
        }

        // XZReducer
        if (mc.player.isOnGround() && slowDownValue.getValue()) {
            mc.player.setVelocity(mc.player.getVelocity().x * xzMultiplier.getValue(), mc.player.getVelocity().y, mc.player.getVelocity().z * xzMultiplier.getValue());
        }

        // Handle event state
        Stage eventState = event.eventState;
        for (int i = 0; i <= 7; i++) {
            if (mc.player.getInventory().main.get(i) != null && mc.player.getInventory().main.get(i).getCount() <= 0) {
                mc.player.getInventory().main.set(i, null);
            }
        }

        // Handle placement logic
        if (eventState == Stage.PRE) {
            if (!shouldPlace() || InventoryUtils.findAutoBlockBlock() == -1) {
                return;
            }
            findBlock();
        }

        if (targetPlace == null) {
            if (placeableDelay.getValue()) {
                delayTimer.reset();
            }
        }

        // Adjust timer speed and handle floating mode
        if (canTower) {
            Ngm.TICK_TIMER = towerTimerValue.getValue();
            if (eventState == Stage.POST && towerModeValue.getValue() == towerMode.Float) {
                if (BlockUtils.getBlock(new BlockPos(mc.player.getX(), mc.player.getY() + 2, mc.player.getZ())) instanceof AirBlock) {
                    floatUP(event);
                }
            }
        } else {
            verusState = 0;
        }
    }

    @Subscribe
    private void floatUP(MotionEvent event) {
        if (!mc.world.getEntityCollisions(mc.player, mc.player.getBoundingBox().offset(0.0, -0.01, 0.0)).isEmpty()
                && mc.player.isOnGround() && mc.player.isCollidable()) {
            verusState = 0;
            verusJumped = true;
        }

        if (verusJumped) {
            MovementUtility.strafe(getSpeed());

            switch (verusState) {
                case 0:
                    fakeJump();
                    mc.player.setVelocity(mc.player.getVelocity().x, 0.41999998688697815, mc.player.getVelocity().z);
                    verusState++;
                    break;

                case 1:
                    verusState++;
                    break;

                case 2:
                    verusState++;
                    break;

                case 3:
                    mc.player.setOnGround(true);
                    mc.player.setVelocity(mc.player.getVelocity().x, 0.0, mc.player.getVelocity().z);
                    verusState++;
                    break;

                case 4:
                    verusState++;
                    break;
            }

            verusJumped = false;
        }

        verusJumped = true;
    }

    private void findBlock() {
        BlockPos blockPosition;

        if (!canTower && ((autoJumpValue.getValue() == autoJumps.KeepY
                /*|| (smartSpeedValue.getValue() && Launch.moduleManager.getModule(Speed.class).getState())*/)
                && !mc.options.jumpKey.isPressed())
                && launchY <= mc.player.getY()) {
            blockPosition = new BlockPos((int) mc.player.getX(), (int) (launchY - 1.0), (int) mc.player.getZ());
        } else if (mc.player.getY() == (int) mc.player.getY() + 0.5) {
            blockPosition = new BlockPos(mc.player.getBlockPos());
        } else {
            blockPosition = new BlockPos((int) mc.player.getX(), (int) mc.player.getY(), (int) mc.player.getZ()).down();
        }

        if (!isReplaceable(blockPosition) || search(blockPosition)) {
            return;
        }

        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                if (search(blockPosition.add(x, 0, z))) {
                    return;
                }
            }
        }
    }

    private void place() {
        // Check if the current held item slot matches the desired block slot
        if (slot != mc.player.inventoryContainer.getSlot(InventoryUtils.findAutoBlockBlock()).slotIndex) {
            mc.player.inventory.currentItem = InventoryUtils.findAutoBlockBlock() - 36;
            mc.playerController.updateController();
        }

        // Handle start place delay if enabled
        if (startPlaceDelayValue.getValue() && faceBlock && !startPlaceTimer.passedMs(startPlaceDelay.getValue())) {
            if (!mc.player.isOnGround()) {
                startPlaceTimer.tick = startPlaceDelay.getValue();
            } else {
                startPlaceTimer.update();
            }
            return;
        }

        // Check if there's a target place and handle delay timer
        if (targetPlace == null) {
            if (placeableDelay.getValue()) {
                delayTimer.reset();
            }
            return;
        }

        // Check conditions before placing
        if (!canTower && (!delayTimer.passedMs(delay)
                || ((autoJumpValue.getValue() == autoJumps.KeepY
                /*|| (smartSpeedValue.getValue() && Launch.moduleManager.getModule(Speed.class).getState())*/)
                && !mc.options.jumpKey.isPressed())
                && launchY - 1 != (int) targetPlace.vec3.yCoord)
        ) {
            return;
        }

        // Check if the held item is a valid block for placement
        if (mc.player.getMainHandStack() != null && mc.player.getMainHandStack().getItem() instanceof BlockItem) {
            Block block = ((BlockItem) mc.player.getMainHandStack().getItem()).getBlock();
            if (InventoryUtility.BLOCK_BLACKLIST.contains(block)
                    || mc.player.getMainHandStack().getCount() <= 0) {
                return;
            }
        }

        // Simulate right-click action to place the block
        if (mc.player.getMainHandStack() != null && mc.player.getMainHandStack().getItem() instanceof BlockItem) {
            KeyBinding.onTick(KeyCode.getKeyCode(mc.options.useKey.toString()));
        }

        // Reset delay timer and set a new delay if necessary
        delayTimer.reset();
        delay = (!placeableDelay.getValue()) ? 0L : MathUtil.getRandom(minDelayValue.getValue(), maxDelayValue.getValue());

        // Slow down the player's motion on ground if enabled
        if (mc.player.isOnGround() && placeSlowDownValue.getValue()) {
            float modifier = speedModifierValue.getValue();
            mc.player.setVelocity(mc.player.getVelocity().x * modifier, mc.player.getVelocity().y, mc.player.getVelocity().z * modifier);
        }

        // Increment place count and reset targetPlace
        placeCount += 1;
        targetPlace = null;
    }

    @Override
    public void onDisable() {
        if (mc.player == null) return;

        // Perform necessary cleanup actions when the module is disabled
        blink();
        startPlaceTimer.reset();
        faceBlock = false;
        placeCount = 0;
        firstPitch = 0.0f;
        firstRotate = 0.0f;
        wdTick = 5;
        canTower = false;

        // Handle silent sprint mode if enabled
        if (sprintModeValue.getValue() == sprintModes.Silent) {
            if (mc.player.isSprinting()) {
                Ngm.networkManager.sendSequencedPacket((SequencedPacketCreator) new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.STOP_SPRINTING));
                Ngm.networkManager.sendSequencedPacket((SequencedPacketCreator) new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.START_SPRINTING));
            }
        }

        // Release movement keys if they were held down
        if (!mc.options.rightKey.isPressed()) mc.options.rightKey.setPressed(false);
        if (!mc.options.leftKey.isPressed()) mc.options.leftKey.setPressed(false);

        lockRotation = null;
        lookupRotation = null;
        Ngm.TICK_TIMER = 1.0f;
        faceBlock = false;

        // Reset the selected item slot to the last used slot
        if (slot != mc.player.getInventory().selectedSlot)
            mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(mc.player.getInventory().selectedSlot));

        // Reset the player's selected item slot to the original slot
        if (lastSlot != mc.player.getInventory().selectedSlot) {
            mc.player.getInventory().selectedSlot = lastSlot;
                mc.interactionManager.tick();
        }
    }

    @Subscribe
    public void onMove(MovementSlowdownEvent event) {
        if (!safeWalkValue.getValue()) return;
        if (airSafeValue.getValue() || mc.player.isOnGround()) {
            event.setSafeWalk(true);
        }
    }

    @Subscribe
    public void onJump(JumpEvent event) {
        if (getBlocksAmount() <= 0 || RotationUtils.targetRotation == null) return;
        /* 사일런트 로테이션이 뭐죠??????
        boolean silentRotationsState = Launch.moduleManager.getModule(SilentRotations.class).getState();
        boolean customStrafeState = Launch.moduleManager.getModule(SilentRotations.class).customStrafe.getValue();

        if (silentRotationsState && !customStrafeState) {
            event.setYaw(RotationUtils.targetRotation.yaw - 180.0f);
        }
         */

        if (canTower) {
            event.cancel();
        }
    }

    @Subscribe
    public void onWorld(WorldEvent event) {
        faceBlock = false;
        startPlaceTimer.reset();
    }

    /* 음 역시 렌더는 어려워
    @Override
    public void onRender2D(Render2DEvent event) {
        Window scaledResolution = new Window(mc);
        String counter = getBlocksAmount() + " Blocks";
        int infoWidth = FontLoaders.SF20.getStringWidth(counter);
        FontLoaders.SF20.drawStringWithShadow(
                counter,
                (scaledResolution.getScaledWidth() / 2 - infoWidth + 21),
                (scaledResolution.getScaledHeight() / 2 - 30),
                -0x1111111
        );
    }

     */

    private boolean search(BlockPos blockPosition) {
        if (!isReplaceable(blockPosition)) return false;
        Vec3d eyesPos = new Vec3d(
                mc.player.getX(),
                mc.player.getBoundingBox().minY + mc.player.getEyeY(),
                mc.player.getZ()
        );
        PlaceRotation placeRotation = null;
        for (EnumFacing side : EnumFacing.VALUES) {
            BlockPos neighbor = blockPosition.offset(side);
            if (!canBeClicked(neighbor)) continue;
            Vec3d dirVec = new Vec3d(side.getDirectionVec());
            double xSearch = 0.1;
            while (xSearch < 0.9) {
                double ySearch = 0.1;
                while (ySearch < 0.9) {
                    double zSearch = 0.1;
                    while (zSearch < 0.9) {
                        Vec3d posVec = new BlockPos(blockPosition).add((int) xSearch,(int) ySearch,(int) zSearch).toCenterPos();
                        double distanceSqPosVec = eyesPos.squareDistanceTo(posVec);
                        Vec3d hitVec = posVec.add(dirVec.x * 0.5, dirVec.y * 0.5, dirVec.z * 0.5);
                        if (eyesPos.squaredDistanceTo(hitVec) > 18.0 ||
                                distanceSqPosVec > eyesPos.squaredDistanceTo(posVec.add(dirVec)) ||
                                mc.world.raycastBlock(eyesPos, hitVec, false, true, false) != null) {
                            zSearch += 0.1;
                            continue;
                        }
                        for (int i = 0; i < (rotationModeValue.getValue() == rotationModes.Smooth ? 2 : 1); i++) {
                            double diffX = (rotationModeValue.getValue()  == rotationModes.Smooth && i == 0) ? 0.0 : hitVec.x - eyesPos.z;
                            double diffY = hitVec.y - eyesPos.y;
                            double diffZ = (rotationModeValue.getValue() == rotationModes.Smooth && i == 1) ? 0.0 : hitVec.z - eyesPos.z;
                            double diffXZ = MathHelper.sqrt((float) (diffX * diffX + diffZ * diffZ));
                            Rotation rotation = new Rotation(
                                    MathHelper.wrapDegrees((float) (Math.toDegrees(Math.atan2(diffZ, diffX)) - 90f)),
                                    MathHelper.wrapDegrees((float) (-Math.toDegrees(Math.atan2(diffY, diffXZ))))
                            );
                            lookupRotation = rotation;
                            Vec3d rotationVector = RotationUtils.getVectorForRotation(rotation);
                            Vec3d vector = eyesPos.add(rotationVector.x * 4, rotationVector.y * 4, rotationVector.z * 4);
                            BlockHitResult obj = mc.world.raycastBlock(eyesPos, vector, false, false, true);
                            if (obj != null && obj.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK && obj.blockPos.equals(neighbor)) {
                                if (placeRotation == null || RotationUtils.getRotationDifference(rotation) < RotationUtils.getRotationDifference(placeRotation.rotation)) {
                                    placeRotation = new PlaceRotation(new PlaceInfo(neighbor, side.getOpposite(), hitVec), rotation);
                                }
                            }
                        }
                        zSearch += 0.1;
                    }
                    ySearch += 0.1;
                }
                xSearch += 0.1;
            }
        }
        if (placeRotation == null) {
            faceBlock = false;
            return false;
        }
        Rotation limitedRotation = RotationUtils.limitAngleChange(RotationUtils.serverRotation, placeRotation.rotation, RandomUtils.nextFloat(minTurnSpeed.getValue(), maxTurnSpeed.getValue()));
        if (Math.round(10 * MathHelper.wrapDegrees(limitedRotation.yaw())) == Math.round(10 * MathHelper.wrapDegrees(placeRotation.rotation.getYaw())) &&
                Math.round(10 * MathHelper.wrapDegrees(limitedRotation.pitch())) == Math.round(10 * MathHelper.wrapDegrees(placeRotation.rotation.getPitch()))) {
            RotationUtils.setTargetRotation(placeRotation.rotation, 0);
            lockRotation = placeRotation.rotation;
            faceBlock = true;
        } else {
            RotationUtils.setTargetRotation(limitedRotation, 0);
            lockRotation = limitedRotation;
        }
        lookupRotation = lockRotation;
        targetPlace = placeRotation.placeInfo;
        return true;
    }

    private int getBlocksAmount() {
        int amount = 0;
        for (int i = 36; i <= 44; i++) {
            ItemStack itemStack = mc.player.getInventory().getStack(i);
            if (itemStack != null && itemStack.getItem() instanceof BlockItem) {
                Block block = ((BlockItem) itemStack.getItem()).getBlock();
                if (!InventoryUtility.BLOCK_BLACKLIST.contains(block) && block.isFullCube()) {
                    amount += itemStack.stackSize;
                }
            }
        }
        return amount;
    }
}
