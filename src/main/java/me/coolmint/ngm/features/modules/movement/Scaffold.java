package me.coolmint.ngm.features.modules.movement;

import com.google.common.eventbus.Subscribe;
import me.coolmint.ngm.Ngm;
import me.coolmint.ngm.event.impl.ClientEvent;
import me.coolmint.ngm.event.impl.PacketEvent;
import me.coolmint.ngm.features.command.Command;
import me.coolmint.ngm.features.modules.Module;
import me.coolmint.ngm.features.settings.Setting;
import me.coolmint.ngm.util.MathUtil;
import me.coolmint.ngm.util.MovementUtility;
import me.coolmint.ngm.util.models.Timer;
import me.coolmint.ngm.util.player.Rotation;
import me.coolmint.ngm.util.player.RotationUtils;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import org.apache.commons.lang3.RandomUtils;

import java.util.LinkedList;
import java.util.concurrent.LinkedBlockingQueue;

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
        if (!faceBlock && startPlaceplayer.passedMs(1))
            startPlaceTimer.reset();

        if (blocksAmount <= 0) {
            faceBlock = false;
            return;
        }

        if (lockRotation != null) {
            Rotation serverRotation = RotationUtils.getServerRotation();
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
                            RotationUtils.getServerRotation(),
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
                new BlockPos(mc.player.getX(), mc.player.getY() - 1.0, mc.player.getZ())
        ).getBlock() == Blocks.AIR;

        if (!canTower && (autoSneakValue.getValue() && mc.player.isOnGround() && shouldEagle || Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)))
            mc.gameSettings.keyBindSneak.pressed = true;
        else if (!Keyboard.isKeyDown(Keyboard.KEY_LSHIFT))
            mc.gameSettings.keyBindSneak.pressed = false;

        if (!canTower && towerModeValue.getValue().equalsIgnoreCase("watchdog") && mc.player.ticksExisted % 2 == 0) {
            wdTick = 5;
            towerTick = 0;
            wdSpoof = false;
        }

        if (allowTower.getValue() && Keyboard.isKeyDown(Keyboard.KEY_SPACE) && !Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) && blocksAmount > 0 && MovementUtility.isRidingBlock() &&
                (towerMove.getValue().equalsIgnoreCase("always") ||
                        (!MovementUtility.isMoving() && towerMove.getValue().equalsIgnoreCase("standing")) ||
                        (MovementUtility.isMoving() && towerMove.getValue().equalsIgnoreCase("moving")))) {
            
            canTower = true;
            String towerMode = towerModeValue.getValue().toString().toLowerCase();

            switch (towerMode) {
                case "jump":
                    if (mc.player.isOnGround() && timer.passedMs(jumpDelayValue.getValue())) {
                        fakeJump();
                        mc.player.motionY = jumpMotionValue.getValue();
                        timer.reset();
                    }
                    break;

                case "motion":
                    if (mc.player.isOnGround()) {
                        fakeJump();
                        mc.player.motionY = 0.42;
                    } else if (mc.player.motionY < 0.1) {
                        mc.player.motionY = -0.3;
                    }
                    break;

                case "motiontp":
                    if (mc.player.isOnGround()) {
                        fakeJump();
                        mc.player.motionY = 0.42;
                    } else if (mc.player.motionY < 0.23) {
                        mc.player.setPosition(mc.player.getX(), mc.player.getY(), mc.player.getZ());
                    }
                    break;

                case "teleport":
                    if (teleportNoMotionValue.getValue()) {
                        mc.player.motionY = 0.0;
                    }
                    if ((mc.player.isOnGround() || !teleportGroundValue.getValue()) && player.passedMs(teleportDelayValue.getValue())) {
                        fakeJump();
                        mc.player.setPositionAndUpdate(
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
                    mc.player.motionY = stableMotionValue.getValue();
                    if (stableStopValue.getValue() && towerDelayplayer.passedMs(stableStopDelayValue.getValue())) {
                        mc.player.motionY = -0.28;
                        towerDelayTimer.reset();
                    }
                    break;

                case "constantmotion":
                    if (mc.player.isOnGround()) {
                        fakeJump();
                        jumpGround = mc.player.getY();
                        mc.player.motionY = constantMotionValue.getValue();
                    }
                    if (mc.player.getY() > jumpGround + constantMotionJumpGroundValue.getValue()) {
                        fakeJump();
                        mc.player.setPosition(mc.player.getX(), mc.player.getY(), mc.player.getZ());
                        mc.player.motionY = constantMotionValue.getValue();
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
                            mc.player.motionX *= 0.9f;
                            mc.player.motionZ *= 0.9f;
                        }
                        if (towerTick > 16) {
                            towerTick = 0;
                        }
                    }
                    if (mc.player.isOnGround()) {
                        if (towerTick == 0 || towerTick == 5) {
                            if (watchdogTowerBoostValue.getValue()) {
                                mc.player.motionX *= watchdogTowerSpeed.getValue();
                                mc.player.motionZ *= watchdogTowerSpeed.getValue();
                            }
                            mc.player.motionY = 0.42;
                            towerTick = 1;
                        }
                    }
                    if (mc.player.motionY > -0.0784000015258789) {
                        if (!mc.player.isOnGround()) {
                            switch ((int) (mc.player.getY() % 1.0 * 100.0)) {
                                case 42:
                                    mc.player.motionY = 0.33;
                                    break;
                                case 75:
                                    mc.player.motionY = 1.0 - (mc.player.getY() % 1.0);
                                    wdSpoof = true;
                                    break;
                                case 0:
                                    if (MovementUtility.isRidingBlock()) {
                                        mc.player.motionY = -0.0784000015258789;
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
                        mc.player.motionY = 0.42;
                    } else if (mc.player.getY() % 1 < 0.1 && offGroundTicks != 0) {
                        mc.player.setPosition(mc.player.getX(), Math.floor(mc.player.getY()), mc.player.getZ());
                    }
                    break;

                case "aac339":
                    if (mc.player.isOnGround()) {
                        fakeJump();
                        mc.player.motionY = 0.4001;
                    }
                    mc.timer.timerSpeed = 1f;
                    if (mc.player.motionY < 0) {
                        mc.player.motionY -= 0.00000945;
                        mc.timer.timerSpeed = 1.6f;
                    }
                    break;

                case "aac364":
                    if (mc.player.ticksExisted % 4 == 1) {
                        mc.player.motionY = 0.4195464;
                        mc.player.setPosition(mc.player.getX() - 0.035, mc.player.getY(), mc.player.getZ());
                    } else if (mc.player.ticksExisted % 4 == 0) {
                        mc.player.motionY = -0.5;
                        mc.player.setPosition(mc.player.getX() + 0.035, mc.player.getY(), mc.player.getZ());
                    }
                    break;

                default:
                    break;
            }
        } else {
            canTower = false;
        }

        if (autoDisableSpeedValue.getValue() && Launch.moduleManager.getModule(Speed.class).getState()) {
            Launch.moduleManager.getModule(Speed.class).setState(false);
            chat("Speed was disabled");
        }

        mc.timer.timerSpeed = timerValue.getValue();

        if (mc.player.isOnGround()) {
            offGroundTicks = 0;
        } else {
            offGroundTicks++;
        }

        if (desyncValue.getValue()) {
            synchronized (positions) {
                positions.add(new double[]{mc.player.getX(), mc.player.entityBoundingBox.minY, mc.player.getZ()});
            }
            if (pulseplayer.passedMs(desyncDelayValue.getValue())) {
                blink();
                pulseTimer.reset();
            }
        }

        // scaffold custom speed if enabled
        if (customSpeedValue.getValue()) {
            MovementUtility.strafe(customMoveSpeedValue.getValue());
        }

        if (sprintModeValue.getValue().equalsIgnoreCase("off") ||
                (sprintModeValue.getValue().equalsIgnoreCase("ground") && !mc.player.isOnGround()) ||
                (sprintModeValue.getValue().equalsIgnoreCase("air") && mc.player.isOnGround())) {
            mc.player.setSprinting(false);
        }

        if (!autoJumpValue.getValue().equalsIgnoreCase("keepy") &&
                !(smartSpeedValue.getValue() && Launch.moduleManager.getModule(Speed.class).getState()) ||
                Keyboard.isKeyDown(Keyboard.KEY_SPACE) || mc.player.getY() < launchY) {
            launchY = mc.player.getY();
        }

        if (Keyboard.isKeyDown(Keyboard.KEY_SPACE) && mc.player.isOnGround()) {
            placeCount = 0;
        }

        if ((autoJumpValue.getValue().equalsIgnoreCase("keepy") && !Launch.moduleManager.getModule(Speed.class).getState()) ||
                (autoJumpValue.getValue().equalsIgnoreCase("normal") && faceBlock) ||
                (autoJumpValue.getValue().equalsIgnoreCase("breezily") && placeCount >= breezilyDelayValue.getValue()) &&
                        MovementUtility.isMoving() && mc.player.isOnGround()) {
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
            placementPacket.setFacingX(MathHelper.clamp_float(placementPacket.getFacingX(), -1.0F, 1.0F));
            placementPacket.setFacingY(MathHelper.clamp_float(placementPacket.getFacingY(), -1.0F, 1.0F));
            placementPacket.setFacingZ(MathHelper.clamp_float(placementPacket.getFacingZ(), -1.0F, 1.0F));
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

    @Subscribe
    public void onStrafe(StrafeEvent event) {
        if (blocksAmount <= 0 || RotationUtils.targetRotation == null)
            return;

        boolean silentRotationsState = Launch.moduleManager.getModule(SilentRotations.class).state;
        boolean customStrafe = Launch.moduleManager.getModule(SilentRotations.class).customStrafe.getValue();

        if (silentRotationsState && !customStrafe) {
            event.yaw = RotationUtils.targetRotation.yaw - 180f;
        }
    }

    private boolean shouldPlace() {
        String placeCondition = placeConditionValue.getValue().toLowerCase();
        boolean placeWhenAir = placeCondition.equals("air");
        boolean placeWhenFall = placeCondition.equals("falldown");
        boolean placeWhenNegativeMotion = placeCondition.equals("negativemotion");
        boolean alwaysPlace = placeCondition.equals("always");

        return alwaysPlace || canTower || (placeWhenAir && !mc.player.isOnGround()) || (placeWhenFall && mc.player.fallDistance > 0) || (placeWhenNegativeMotion && mc.player.motionY < 0);
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
        if (blocksAmount <= 0)
            return;

        // Handle tower movement
        if (canTower && !MovementUtility.isMoving() && event.eventState == EventState.POST) {
            mc.player.motionX = 0.0;
            mc.player.motionZ = 0.0;
        }

        // Decrease watchdog tick if in watchdog mode
        if (towerModeValue.getValue().equalsIgnoreCase("watchdog")) {
            if (wdTick > 0) {
                wdTick--;
            }
        }

        // Check if should face block
        boolean shouldEagle = mc.theWorld.getBlockState(new BlockPos(mc.player.getX(), mc.player.getY() - 1.0, mc.player.getZ())).getBlock() == Blocks.air;
        if ((!mc.player.isOnGround() || shouldEagle) && mc.player.ticksExisted % 2 == 0 && !faceBlock) {
            faceBlock = true;
        }

        try {
            if (faceBlock) {
                place();
                mc.player.isSwingInProgress = false;
            } else if (slot != mc.player.inventoryContainer.getSlot(InventoryUtils.findAutoBlockBlock()).slotNumber) {
                mc.player.inventory.currentItem = InventoryUtils.findAutoBlockBlock() - 36;
                mc.playerController.updateController();
            }
        } catch (Exception ignored) {
        }

        // No SpeedPot
        if (noSpeedPotValue.getValue() && mc.player.isPotionActive(Potion.moveSpeed) && !canTower && mc.player.isOnGround()) {
            mc.player.motionX *= speedSlowDown.getValue();
            mc.player.motionZ *= speedSlowDown.getValue();
        }

        // XZReducer
        if (mc.player.isOnGround() && slowDownValue.getValue()) {
            mc.player.motionX *= xzMultiplier.getValue();
            mc.player.motionZ *= xzMultiplier.getValue();
        }

        // Handle event state
        EventState eventState = event.eventState;
        for (int i = 0; i <= 7; i++) {
            if (mc.player.inventory.mainInventory[i] != null && mc.player.inventory.mainInventory[i].stackSize <= 0) {
                mc.player.inventory.mainInventory[i] = null;
            }
        }

        // Handle placement logic
        if (eventState == EventState.PRE) {
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
            mc.timer.timerSpeed = towerTimerValue.getValue();
            if (eventState == EventState.POST && towerModeValue.getValue().equalsIgnoreCase("float")) {
                if (BlockUtils.getBlock(new BlockPos(mc.player.getX(), mc.player.getY() + 2, mc.player.getZ())) instanceof BlockAir) {
                    floatUP(event);
                }
            }
        } else {
            verusState = 0;
        }
    }

    private void floatUP(MotionEvent event) {
        if (!mc.theWorld.getCollidingBoundingBoxes(mc.player, mc.player.getEntityBoundingBox().offset(0.0, -0.01, 0.0)).isEmpty()
                && mc.player.isOnGround() && mc.player.isCollidedVertically) {
            verusState = 0;
            verusJumped = true;
        }

        if (verusJumped) {
            MovementUtility.strafe();

            switch (verusState) {
                case 0:
                    fakeJump();
                    mc.player.motionY = 0.41999998688697815;
                    verusState++;
                    break;

                case 1:
                    verusState++;
                    break;

                case 2:
                    verusState++;
                    break;

                case 3:
                    event.onGround = true;
                    mc.player.motionY = 0.0;
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

        if (!canTower && ((autoJumpValue.getValue().equalsIgnoreCase("keepy")
                || (smartSpeedValue.getValue() && Launch.moduleManager.getModule(Speed.class).getState()))
                && !GameSettings.isKeyDown(mc.gameSettings.keyBindJump))
                && launchY <= mc.player.getY()) {
            blockPosition = new BlockPos(mc.player.getX(), launchY - 1.0, mc.player.getZ());
        } else if (mc.player.getY() == (int) mc.player.getY() + 0.5) {
            blockPosition = new BlockPos(mc.player);
        } else {
            blockPosition = new BlockPos(mc.player.getX(), mc.player.getY(), mc.player.getZ()).down();
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
        if (startPlaceDelayValue.getValue() && faceBlock && !startPlaceplayer.passedMs(startPlaceDelay.getValue())) {
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
        if (!canTower && (!delayplayer.passedMs(delay)
                || ((autoJumpValue.getValue().equalsIgnoreCase("keepy")
                || (smartSpeedValue.getValue() && Launch.moduleManager.getModule(Speed.class).getState()))
                && !GameSettings.isKeyDown(mc.gameSettings.keyBindJump))
                && launchY - 1 != (int) targetPlace.vec3.yCoord)
        ) {
            return;
        }

        // Check if the held item is a valid block for placement
        if (mc.player.heldItem != null && mc.player.heldItem.getItem() instanceof BlockItem) {
            Block block = ((BlockItem) mc.player.heldItem.getItem()).getBlock();
            if (InventoryUtils.BLOCK_BLACKLIST.contains(block)
                    || !block.isFullCube()
                    || mc.player.heldItem.stackSize <= 0) {
                return;
            }
        }

        // Simulate right-click action to place the block
        if (mc.player.heldItem != null && mc.player.heldItem.getItem() instanceof BlockItem) {
            KeyBinding.onTick(mc.gameSettings.keyBindUseItem.getKeyCode());
        }

        // Reset delay timer and set a new delay if necessary
        delayTimer.reset();
        delay = (!placeableDelay.getValue()) ? 0L : TimeUtils.randomDelay(minDelayValue.getValue(), maxDelayValue.getValue());

        // Slow down the player's motion on ground if enabled
        if (mc.player.isOnGround() && placeSlowDownValue.getValue()) {
            float modifier = speedModifierValue.getValue();
            mc.player.motionX *= modifier;
            mc.player.motionZ *= modifier;
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
        mc.gameSettings.keyBindSneak.pressed = GameSettings.isKeyDown(mc.gameSettings.keyBindSneak);

        // Handle silent sprint mode if enabled
        if (sprintModeValue.getValue().equalsIgnoreCase("silent")) {
            if (mc.player.isSprinting()) {
                PacketUtils.sendPacketNoEvent(new C0BPacketEntityAction(mc.player, C0BPacketEntityAction.Action.STOP_SPRINTING));
                PacketUtils.sendPacketNoEvent(new C0BPacketEntityAction(mc.player, C0BPacketEntityAction.Action.START_SPRINTING));
            }
        }

        // Release movement keys if they were held down
        if (!GameSettings.isKeyDown(mc.gameSettings.keyBindRight)) mc.gameSettings.keyBindRight.pressed = false;
        if (!GameSettings.isKeyDown(mc.gameSettings.keyBindLeft)) mc.gameSettings.keyBindLeft.pressed = false;

        lockRotation = null;
        lookupRotation = null;
        mc.timer.timerSpeed = 1.0f;
        faceBlock = false;

        // Reset the selected item slot to the last used slot
        if (slot != mc.player.inventory.currentItem)
            mc.netHandler.addToSendQueue(new C09PacketHeldItemChange(mc.player.inventory.currentItem));

        // Reset the player's selected item slot to the original slot
        if (lastSlot != mc.player.inventory.currentItem) {
            mc.player.inventory.currentItem = lastSlot;
            mc.playerController.updateController();
        }
    }

    @Subscribe
    public void onMove(MoveEvent event) {
        if (!safeWalkValue.getValue()) return;
        if (airSafeValue.getValue() || mc.player.isOnGround()) {
            event.setSafeWalk(true);
        }
    }

    @Subscribe
    public void onJump(JumpEvent event) {
        if (blocksAmount <= 0 || RotationUtils.targetRotation == null) return;
        boolean silentRotationsState = Launch.moduleManager.getModule(SilentRotations.class).getState();
        boolean customStrafeState = Launch.moduleManager.getModule(SilentRotations.class).customStrafe.getValue();

        if (silentRotationsState && !customStrafeState) {
            event.setYaw(RotationUtils.targetRotation.yaw - 180.0f);
        }

        if (canTower) {
            event.cancelEvent();
        }
    }

    @Subscribe
    public void onWorld(WorldEvent event) {
        faceBlock = false;
        startPlaceTimer.reset();
    }

    @Subscribe
    public void onRender2D(Render2DEvent event) {
        ScaledResolution scaledResolution = new ScaledResolution(mc);
        String counter = blocksAmount + " Blocks";
        int infoWidth = FontLoaders.SF20.getStringWidth(counter);
        FontLoaders.SF20.drawStringWithShadow(
                counter,
                (scaledResolution.getScaledWidth() / 2 - infoWidth + 21),
                (scaledResolution.getScaledHeight() / 2 - 30),
                -0x1111111
        );
    }

    private boolean search(BlockPos blockPosition) {
        if (!isReplaceable(blockPosition)) return false;
        Vec3 eyesPos = new Vec3(
                mc.player.getX(),
                mc.player.getEntityBoundingBox().minY + mc.player.getEyeHeight(),
                mc.player.getZ()
        );
        PlaceRotation placeRotation = null;
        for (EnumFacing side : EnumFacing.VALUES) {
            BlockPos neighbor = blockPosition.offset(side);
            if (!canBeClicked(neighbor)) continue;
            Vec3 dirVec = new Vec3(side.getDirectionVec());
            double xSearch = 0.1;
            while (xSearch < 0.9) {
                double ySearch = 0.1;
                while (ySearch < 0.9) {
                    double zSearch = 0.1;
                    while (zSearch < 0.9) {
                        Vec3 posVec = new Vec3(blockPosition).addVector(xSearch, ySearch, zSearch);
                        double distanceSqPosVec = eyesPos.squareDistanceTo(posVec);
                        Vec3 hitVec = posVec.addVector(dirVec.xCoord * 0.5, dirVec.yCoord * 0.5, dirVec.zCoord * 0.5);
                        if (eyesPos.squareDistanceTo(hitVec) > 18.0 ||
                                distanceSqPosVec > eyesPos.squareDistanceTo(posVec.add(dirVec)) ||
                                mc.theWorld.rayTraceBlocks(eyesPos, hitVec, false, true, false) != null) {
                            zSearch += 0.1;
                            continue;
                        }
                        for (int i = 0; i < (rotationModeValue.getValue().equalsIgnoreCase("Smooth") ? 2 : 1); i++) {
                            double diffX = (rotationModeValue.getValue().equalsIgnoreCase("Smooth") && i == 0) ? 0.0 : hitVec.xCoord - eyesPos.xCoord;
                            double diffY = hitVec.yCoord - eyesPos.yCoord;
                            double diffZ = (rotationModeValue.getValue().equalsIgnoreCase("Smooth") && i == 1) ? 0.0 : hitVec.zCoord - eyesPos.zCoord;
                            double diffXZ = MathHelper.sqrt_double(diffX * diffX + diffZ * diffZ);
                            Rotation rotation = new Rotation(
                                    MathHelper.wrapAngleTo180_float((float) (Math.toDegrees(Math.atan2(diffZ, diffX)) - 90f)),
                                    MathHelper.wrapAngleTo180_float((float) (-Math.toDegrees(Math.atan2(diffY, diffXZ))))
                            );
                            lookupRotation = rotation;
                            Vec3 rotationVector = RotationUtils.getVectorForRotation(rotation);
                            Vec3 vector = eyesPos.addVector(rotationVector.xCoord * 4, rotationVector.yCoord * 4, rotationVector.zCoord * 4);
                            MovingObjectPosition obj = mc.theWorld.rayTraceBlocks(eyesPos, vector, false, false, true);
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
        if (Math.round(10 * MathHelper.wrapAngleTo180_float(limitedRotation.getYaw())) == Math.round(10 * MathHelper.wrapAngleTo180_float(placeRotation.rotation.getYaw())) &&
                Math.round(10 * MathHelper.wrapAngleTo180_float(limitedRotation.getPitch())) == Math.round(10 * MathHelper.wrapAngleTo180_float(placeRotation.rotation.getPitch()))) {
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
            ItemStack itemStack = mc.player.inventoryContainer.getSlot(i).getStack();
            if (itemStack != null && itemStack.getItem() instanceof BlockItem) {
                Block block = ((BlockItem) itemStack.getItem()).getBlock();
                if (!InventoryUtils.BLOCK_BLACKLIST.contains(block) && block.isFullCube()) {
                    amount += itemStack.stackSize;
                }
            }
        }
        return amount;
    }
}
