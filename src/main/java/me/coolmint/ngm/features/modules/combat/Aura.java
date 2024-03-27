package me.coolmint.ngm.features.modules.combat;

import com.google.common.eventbus.Subscribe;
import me.coolmint.ngm.Ngm;
import me.coolmint.ngm.event.impl.EventSync;
import me.coolmint.ngm.event.impl.PacketEvent;
import me.coolmint.ngm.features.modules.Module;
import me.coolmint.ngm.features.settings.Setting;
import me.coolmint.ngm.mixin.ILivingEntity;
import me.coolmint.ngm.util.ChatUtil;
import me.coolmint.ngm.util.models.Timer;
import me.coolmint.ngm.util.player.InventoryUtility;
import me.coolmint.ngm.util.player.PlayerUtility;
import me.coolmint.ngm.util.player.SearchInvResult;
import net.minecraft.block.Blocks;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.SlimeEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.CatEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FireballEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.ShulkerBulletEntity;
import net.minecraft.item.*;
import net.minecraft.network.packet.c2s.play.*;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import static me.coolmint.ngm.util.MathUtil.random;
import static net.minecraft.util.UseAction.BLOCK;
import static net.minecraft.util.math.MathHelper.wrapDegrees;

public class Aura extends Module {
    public Setting<Float> aimRange = register(new Setting<>("AimRange", 3.1f, 2f, 6.0f));
    public Setting<Float> attackRange = register(new Setting<>("Range", 3.1f, 2f, 6.0f));
    public Setting<Float> wallRange = register(new Setting<>("WallRange", 3.1f, 0f, 6.0f));
    public Setting<Boolean> wallsBypass = register(new Setting<>("WallsBypass", false, v -> wallRange.getValue() > 0));
    public Setting<Integer> fov = register(new Setting<>("Fov", 180, 0, 180));
    public Setting<Rotation> rotationMode = register(new Setting<>("Range", Rotation.Universal));
    public Setting<Integer> minYawStep = register(new Setting<>("MinYawStep", 65, 1, 180));
    public Setting<Integer> maxYawStep = register(new Setting<>("MaxYawStep", 75, 1, 180));
    public Setting<RandomHitDelay> randomHitDelay = register(new Setting<>("RandomHitDelay", RandomHitDelay.Off));
    public Setting<Switch> switchMode = register(new Setting<>("Switch", Switch.Silent));
    public Setting<Boolean> onlyWeapon = register(new Setting<>("OnlyWeapon", true, v -> switchMode.getValue() != Switch.Silent));
    public Setting<Boolean> smartCrit = register(new Setting<>("SmartCritical", true));
    public Setting<Boolean> onlySpace = register(new Setting<>("OnlySpace", false, v -> smartCrit.getValue()));
    public Setting<Boolean> shieldBreaker = register(new Setting<>("ShieldBreaker", true));
    public Setting<Boolean> unpressShield = register(new Setting<>("AutoBlock", false));
    public Setting<Boolean> clientLook = register(new Setting<>("ClientLook", false));
    public Setting<Boolean> oldDelay = register(new Setting<>("1.8", false));
    public Setting<Integer> minCPS = register(new Setting<>("MinCPS", 7, 1, 15, v -> oldDelay.getValue()));
    public Setting<Integer> maxCPS = register(new Setting<>("MaxCPS", 12, 1, 15, v -> oldDelay.getValue()));
    public Setting<Sort> sort = register(new Setting<>("Sort", Sort.LowestDistance));
    public Setting<Boolean> lockTarget = register(new Setting<>("LockTarget", true));
    public Setting<RayTrace> rayTrace = register(new Setting<>("RayTrace", RayTrace.OnlyTarget));
    public Setting<Boolean> pauseWhileEating = register(new Setting<>("PauseWhileEating", false));
    public Setting<Boolean> pauseInInventory = register(new Setting<>("PauseInInventory", false));
    public Setting<Boolean> deathDisable = register(new Setting<>("DeathDisable", true));
    public Setting<Boolean> ignoreInvisible = register(new Setting<>("IgnoreInvis", false));

    boolean ignoreTeam = true;
    boolean ignoreCreative = true;
    boolean ignoreShield = false;

    public Aura() {
        super("Aura", "", Category.COMBAT, true, false, false);
    }

    public static Entity target;

    public float rotationYaw, rotationPitch, pitchAcceleration = 1f, prevYaw;

    private Vec3d rotationPoint = Vec3d.ZERO;
    private Vec3d rotationMotion = Vec3d.ZERO;

    private int hitTicks;
    private int trackticks;
    private boolean lookingAtHitbox;

    private final Timer delayTimer = new Timer();
    private final Timer pauseTimer = new Timer();

    public void auraLogic() {
        Item handItem = mc.player.getMainHandStack().getItem();

        if ((switchMode.getValue() != Switch.Silent && onlyWeapon.getValue() && !(handItem instanceof SwordItem || handItem instanceof AxeItem || handItem instanceof TridentItem))) {
            target = null;
            return;
        }

        handleKill();
        updateTarget();

        if (target == null) {
            return;
        }
        boolean readyForAttack;

            if (switchMode.getValue() == Switch.None && onlyWeapon.getValue() && !(handItem instanceof SwordItem || handItem instanceof AxeItem || handItem instanceof TridentItem))
                return;

            boolean[] playerState = preAttack();
            if (!(target instanceof PlayerEntity pl) || !(pl.isUsingItem() && pl.getOffHandStack().getItem() == Items.SHIELD) || ignoreShield)
                attack();

            postAttack(playerState[0], playerState[1]);
        }

    private boolean skipRayTraceCheck() {
        return rotationMode.getValue() == Rotation.None || rayTrace.getValue() == RayTrace.OFF;
    }

    public void attack() {
        int prevSlot = switchMethod();
        mc.interactionManager.attackEntity(mc.player, target);
        Criticals.cancelCrit = false;
        mc.player.swingHand(Hand.MAIN_HAND);
        hitTicks = getHitTicks();
        if (prevSlot != -1)
            InventoryUtility.switchTo(prevSlot);
    }

    private boolean @NotNull [] preAttack() {
        boolean blocking = mc.player.isUsingItem() && mc.player.getActiveItem().getItem().getUseAction(mc.player.getActiveItem()) == BLOCK;
        if (blocking && unpressShield.getValue())
            mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, Direction.DOWN));

        boolean sprint = mc.player.isSprinting();
        if (sprint)
            disableSprint();
        return new boolean[]{blocking, sprint};
    }

    public void postAttack(boolean block, boolean sprint) {
        if (sprint)
            enableSprint();
        if (block && unpressShield.getValue())
            mc.getNetworkHandler().sendPacket(new PlayerInteractItemC2SPacket(Hand.OFF_HAND, PlayerUtility.getWorldActionId(mc.world)));
    }

    private void disableSprint() {
        mc.player.setSprinting(false);
        mc.options.sprintKey.setPressed(false);
        mc.getNetworkHandler().sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.STOP_SPRINTING));
    }

    private void enableSprint() {
        mc.player.setSprinting(true);
        mc.options.sprintKey.setPressed(true);
        mc.getNetworkHandler().sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.START_SPRINTING));
    }

    public void handleKill() {
        if (target instanceof LivingEntity && (((LivingEntity) target).getHealth() <= 0 || ((LivingEntity) target).isDead()))
            ChatUtil.sendInfo("Target successfully neutralized!");
    }

    private int switchMethod() {
        int prevSlot = -1;
        SearchInvResult swordResult = InventoryUtility.getSwordHotBar();
        if (swordResult.found() && switchMode.getValue() != Switch.None) {
            if (switchMode.getValue() == Switch.Silent)
                prevSlot = mc.player.getInventory().selectedSlot;
            swordResult.switchTo();
        }

        return prevSlot;
    }

    private int getHitTicks() {
        return oldDelay.getValue() ? 1 + (int) (20f / random(minCPS.getValue(), maxCPS.getValue())) : (randomHitDelay.getValue().equals(RandomHitDelay.Delay) ? (int) random(11, 13) : 11);
    }

    @Override
    public void onUpdate() {
        if (!pauseTimer.passedMs(1000))
            return;

        if (mc.player.isUsingItem() && pauseWhileEating.getValue());
    }

    @Subscribe
    public void onSync(EventSync e) {

        if (!pauseTimer.passedMs(1000))
            return;

        if (mc.player.isUsingItem() && pauseWhileEating.getValue())
            return;

        Item handItem = mc.player.getMainHandStack().getItem();
        if ((onlyWeapon.getValue() && !(handItem instanceof SwordItem || handItem instanceof AxeItem || handItem instanceof TridentItem)) && switchMode.getValue() != Switch.Silent)
            return;

        if (target != null && rotationMode.getValue() != Rotation.None) {
            mc.player.setYaw(rotationYaw);
            mc.player.setPitch(rotationPitch);
        } else {
            rotationYaw = mc.player.getYaw();
            rotationPitch = mc.player.getPitch();
        }

        if (oldDelay.getValue())
            if (minCPS.getValue() > maxCPS.getValue())
                minCPS.setValue(maxCPS.getValue());
    }

    @Subscribe
    public void onPacketReceive(PacketEvent.@NotNull Receive e) {
        if (e.getPacket() instanceof EntityStatusS2CPacket status)
            if (status.getStatus() == 30 && status.getEntity(mc.world) != null && target != null && status.getEntity(mc.world) == target)
                ChatUtil.sendInfo("Succesfully destroyed " + target.getName().getString() + "'s shield");

        if (e.getPacket() instanceof EntityStatusS2CPacket pac && pac.getStatus() == 3 && pac.getEntity(mc.world) == mc.player && deathDisable.getValue())
            ChatUtil.sendInfo("Disabling due to death!");
        disable();
    }

    @Override
    public void onEnable() {
        target = null;
        lookingAtHitbox = false;
        rotationPoint = Vec3d.ZERO;
        rotationMotion = Vec3d.ZERO;
        rotationYaw = mc.player.getYaw();
        rotationPitch = mc.player.getPitch();
        delayTimer.reset();
    }

    private boolean shieldBreaker(boolean instant) {
        int axeSlot = InventoryUtility.getAxe().slot();
        if (axeSlot == -1) return false;
        if (!shieldBreaker.getValue()) return false;
        if (!(target instanceof PlayerEntity)) return false;
        if (!((PlayerEntity) target).isUsingItem() && !instant) return false;
        if (((PlayerEntity) target).getOffHandStack().getItem() != Items.SHIELD && ((PlayerEntity) target).getMainHandStack().getItem() != Items.SHIELD)
            return false;

        if (axeSlot >= 9) {
            mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, axeSlot, mc.player.getInventory().selectedSlot, SlotActionType.SWAP, mc.player);
            mc.getNetworkHandler().sendPacket(new CloseHandledScreenC2SPacket(mc.player.currentScreenHandler.syncId));
            mc.interactionManager.attackEntity(mc.player, target);
            mc.player.swingHand(Hand.MAIN_HAND);
            mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, axeSlot, mc.player.getInventory().selectedSlot, SlotActionType.SWAP, mc.player);
            mc.getNetworkHandler().sendPacket(new CloseHandledScreenC2SPacket(mc.player.currentScreenHandler.syncId));
        } else {
            mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(axeSlot));
            mc.interactionManager.attackEntity(mc.player, target);
            mc.player.swingHand(Hand.MAIN_HAND);
            mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(mc.player.getInventory().selectedSlot));
        }
        hitTicks = 10;
        return true;
    }

    public boolean isAboveWater() {
        return mc.player.isSubmergedInWater() || mc.world.getBlockState(BlockPos.ofFloored(mc.player.getPos().add(0, -0.4, 0))).getBlock() == Blocks.WATER;
    }

    public float getAttackCooldownProgressPerTick() {
        return (float) (1.0 / mc.player.getAttributeValue(EntityAttributes.GENERIC_ATTACK_SPEED) * (20.0 * Ngm.TICK_TIMER * (tpsSync.getValue() ? ThunderHack.serverManager.getTPSFactor() : 1f)));
    }

    public float getAttackCooldown() {
        return MathHelper.clamp(((float) ((ILivingEntity) mc.player).getLastAttackedTicks() + attackBaseTime.getValue()) / getAttackCooldownProgressPerTick(), 0.0F, 1.0F);
    }

    private void updateTarget() {
        Entity candidat = findTarget();

        if (target == null) {
            target = candidat;
            return;
        }

        if (sort.getValue() == Sort.FOV || !lockTarget.getValue())
            target = candidat;

        if (candidat instanceof ProjectileEntity)
            target = candidat;

        if (skipEntity(target))
            target = null;
    }

    private void calcRotations(boolean ready) {
        if (ready) {
            trackticks = interactTicks.getValue();
        } else if (trackticks > 0) {
            trackticks--;
        }

        if (target == null)
            return;

        switch (rotationMode.getValue()) {
            case Universal -> {
                Vec3d targetVec;

                if (mc.player.isFallFlying()) targetVec = target.getEyePos();
                else targetVec = getLegitLook(target);

                if (targetVec == null)
                    return;

                pitchAcceleration = lookingAtHitbox ? aimedPitchStep.getValue() : pitchAcceleration < maxPitchStep.getValue() ? pitchAcceleration * pitchAccelerate.getValue() : maxPitchStep.getValue();

                float prevYaw = rotationYaw;
                float prevPitch = rotationPitch;

                float delta_yaw = wrapDegrees((float) wrapDegrees(Math.toDegrees(Math.atan2(targetVec.z - mc.player.getZ(), (targetVec.x - mc.player.getX()))) - 90) - rotationYaw);
                float delta_pitch = ((float) (-Math.toDegrees(Math.atan2(targetVec.y - (mc.player.getPos().y + mc.player.getEyeHeight(mc.player.getPose())), Math.sqrt(Math.pow((targetVec.x - mc.player.getX()), 2) + Math.pow(targetVec.z - mc.player.getZ(), 2))))) - rotationPitch);

                float yawStep = mode.getValue() == Mode.Interact ? 360f : random(minYawStep.getValue(), maxYawStep.getValue());
                float pitchStep = mode.getValue() == Mode.Interact ? 180f : pitchAcceleration + random(-1f, 1f);

                if (accelerateOnHit.getValue() && ready) {
                    yawStep = 180f;
                    pitchStep = 90f;
                }

                if (delta_yaw > 180)
                    delta_yaw = delta_yaw - 180;

                float deltaYaw = MathHelper.clamp(MathHelper.abs(delta_yaw), -yawStep, yawStep);

                float deltaPitch = MathHelper.clamp(delta_pitch, -pitchStep, pitchStep);

                float newYaw = rotationYaw + (delta_yaw > 0 ? deltaYaw : -deltaYaw);
                float newPitch = MathHelper.clamp(rotationPitch + deltaPitch, -90.0F, 90.0F);

                double gcdFix = (Math.pow(mc.options.getMouseSensitivity().getValue() * 0.6 + 0.2, 3.0)) * 1.2;

                if (trackticks > 0 || mode.getValue() == Mode.Track) {
                    rotationYaw = (float) (newYaw - (newYaw - rotationYaw) % gcdFix);
                    rotationPitch = (float) (newPitch - (newPitch - rotationPitch) % gcdFix);
                } else {
                    rotationYaw = mc.player.getYaw();
                    rotationPitch = mc.player.getPitch();
                }

                ModuleManager.rotations.fixRotation = rotationYaw;

                lookingAtHitbox = Ngm.playerManager.checkRtx(
                        rayTraceAngle.getValue() == RayTraceAngle.Calculated ? rotationYaw : prevYaw,
                        rayTraceAngle.getValue() == RayTraceAngle.Calculated ? rotationPitch : prevPitch,
                        attackRange.getValue(), wallRange.getValue(), rayTrace.getValue());
            }
            case SunRise -> {
                Vec3d ent = getSunriseVector(target);
                if (ent == null)
                    ent = target.getEyePos();
                double deltaX = ent.getX() - mc.player.getX();
                double deltaZ = ent.getZ() - mc.player.getZ();
                float yawDelta = MathHelper.wrapDegrees((float) MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(deltaZ, deltaX)) - 90.0) - rotationYaw) / 1.0001f;
                float pitchDelta = ((float) -Math.toDegrees(Math.atan2(ent.getY() - mc.player.getEyePos().getY(), Math.hypot(deltaX, deltaZ))) - rotationPitch) / 1.0001f;
                float additionYaw = Math.min(Math.max((int) Math.abs(yawDelta), 1), 80);
                float additionPitch = Math.max(ready ? Math.abs(pitchDelta) : 1.0f, 2.0f);
                if (Math.abs(additionYaw - prevYaw) <= 3.0f)
                    additionYaw = prevYaw + 3.1f;

                if (trackticks > 0 || mode.getValue() == Mode.Track) {
                    rotationYaw = rotationYaw + (yawDelta > 0.0f ? additionYaw : -additionYaw) * 1.0001f;
                    rotationPitch = MathHelper.clamp(rotationPitch + (pitchDelta > 0.0f ? additionPitch : -additionPitch) * 1.0001f, -90.0f, 90.0f);
                } else {
                    rotationYaw = mc.player.getYaw();
                    rotationPitch = mc.player.getPitch();
                }

                prevYaw = additionYaw;
                lookingAtHitbox = Ngm.playerManager.checkRtx(rotationYaw, rotationPitch, attackRange.getValue(), wallRange.getValue(), rayTrace.getValue());
                Ngm.moduleManager.fixRotation = rotationYaw;
            }
        }
    }

    private Vec3d getSunriseVector(Entity trgt) {
        if (!mc.player.canSee(trgt) && wallsBypass.getValue())
            return trgt.getPos().add(random(-0.15, 0.15), trgt.getBoundingBox().getLengthY(), random(-0.15, 0.15));

        return new ArrayList<>(Arrays.asList(trgt.getEyePos(), trgt.getPos().add(0, trgt.getEyeHeight(trgt.getPose()) / 2f, 0f), trgt.getPos().add(0, 0.05f, 0f)))
                .stream()
                .min(Comparator.comparing(p -> getPitchDelta(rotationPitch, p)))
                .orElse(null);
    }

    private double getPitchDelta(float currentY, Vec3d v) {
        return Math.abs(-Math.toDegrees(Math.atan2(v.y - mc.player.getEyePos().getY(), Math.hypot(v.x - mc.player.getX(), v.z - mc.player.getZ()))) - currentY);
    }

    public void onRender3D(MatrixStack stack) {
        Item handItem = mc.player.getMainHandStack().getItem();
        if (target == null
                || (switchMode.getValue() != Switch.Silent
                && onlyWeapon.getValue()
                && !(handItem instanceof SwordItem || handItem instanceof AxeItem || handItem instanceof TridentItem)));
        }

        if (clientLook.getValue() && rotationMode.getValue() != Rotation.None) {
            mc.player.setYaw((float) Render2DEngine.interpolate(mc.player.prevYaw, rotationYaw, mc.getTickDelta()));
            mc.player.setPitch((float) Render2DEngine.interpolate(mc.player.prevPitch, rotationPitch, mc.getTickDelta()));
        }

    @Override
    public void onDisable() {
        target = null;
    }

    public Vec3d getLegitLook(Entity target) {

        float minMotionXZ = 0.003f;
        float maxMotionXZ = 0.03f;

        float minMotionY = 0.001f;
        float maxMotionY = 0.03f;

        double lenghtX = target.getBoundingBox().getLengthX();
        double lenghtY = target.getBoundingBox().getLengthY();
        double lenghtZ = target.getBoundingBox().getLengthZ();


        if (rotationMotion.equals(Vec3d.ZERO))
            rotationMotion = new Vec3d(random(-0.05f, 0.05f), random(-0.05f, 0.05f), random(-0.05f, 0.05f));

        rotationPoint = rotationPoint.add(rotationMotion);

        if (rotationPoint.x >= (lenghtX - 0.05) / 2f)
            rotationMotion = new Vec3d(-random(minMotionXZ, maxMotionXZ), rotationMotion.getY(), rotationMotion.getZ());

        if (rotationPoint.y >= lenghtY)
            rotationMotion = new Vec3d(rotationMotion.getX(), -random(minMotionY, maxMotionY), rotationMotion.getZ());

        if (rotationPoint.z >= (lenghtZ - 0.05) / 2f)
            rotationMotion = new Vec3d(rotationMotion.getX(), rotationMotion.getY(), -random(minMotionXZ, maxMotionXZ));

        if (rotationPoint.x <= -(lenghtX - 0.05) / 2f)
            rotationMotion = new Vec3d(random(minMotionXZ, 0.03f), rotationMotion.getY(), rotationMotion.getZ());

        if (rotationPoint.y <= 0.05)
            rotationMotion = new Vec3d(rotationMotion.getX(), random(minMotionY, maxMotionY), rotationMotion.getZ());

        if (rotationPoint.z <= -(lenghtZ - 0.05) / 2f)
            rotationMotion = new Vec3d(rotationMotion.getX(), rotationMotion.getY(), random(minMotionXZ, maxMotionXZ));

        rotationPoint.add(random(-0.03f, 0.03f), 0f, random(-0.03f, 0.03f));

        if (!mc.player.canSee(target) && wallsBypass.getValue())
            return target.getPos().add(random(-0.15, 0.15), lenghtY, random(-0.15, 0.15));

        float[] rotation;

        if (!Ngm.playerManager.checkRtx(rotationYaw, rotationPitch, attackRange.getValue(), wallRange.getValue(), rayTrace.getValue())) {
            float[] rotation1 = Ngm.PlayerManager.calcAngle(target.getPos().add(0, target.getEyeHeight(target.getPose()) / 2f, 0));

            if (PlayerUtility.squaredDistanceFromEyes(target.getPos().add(0, target.getEyeHeight(target.getPose()) / 2f, 0)) <= attackRange.getPow2Value()
                    && Ngm.playerManager.checkRtx(rotation1[0], rotation1[1], attackRange.getValue(), 0, rayTrace.getValue())) {
                rotationPoint = new Vec3d(random(-0.1f, 0.1f), target.getEyeHeight(target.getPose()) / (random(1.8f, 2.5f)), random(-0.1f, 0.1f));
            } else {
                float halfBox = (float) (lenghtX / 2f);

                for (float x1 = -halfBox; x1 <= halfBox; x1 += 0.05f) {
                    for (float z1 = -halfBox; z1 <= halfBox; z1 += 0.05f) {
                        for (float y1 = 0.05f; y1 <= target.getBoundingBox().getLengthY(); y1 += 0.15f) {

                            Vec3d v1 = new Vec3d(target.getX() + x1, target.getY() + y1, target.getZ() + z1);

                            if (PlayerUtility.squaredDistanceFromEyes(v1) > attackRange.getPow2Value()) continue;

                            rotation = Ngm.calcAngle(v1);
                            if (Ngm.playerManager.checkRtx(rotation[0], rotation[1], attackRange.getValue(), 0, rayTrace.getValue())) {
                                rotationPoint = new Vec3d(x1, y1, z1);
                                break;
                            }
                        }
                    }
                }
            }
        }
        return target.getPos().add(rotationPoint);
    }

    public boolean isInRange(Entity target) {

        if (PlayerUtility.squaredDistanceFromEyes(target.getPos().add(0, target.getEyeHeight(target.getPose()), 0 + 4)) {
            return false;
        }

        float[] rotation;
        float halfBox = (float) (target.getBoundingBox().getLengthX() / 2f);

        for (float x1 = -halfBox; x1 <= halfBox; x1 += 0.15f) {
            for (float z1 = -halfBox; z1 <= halfBox; z1 += 0.15f) {
                for (float y1 = 0.05f; y1 <= target.getBoundingBox().getLengthY(); y1 += 0.25f) {
                    if (PlayerUtility.squaredDistanceFromEyes(new Vec3d(target.getX() + x1, target.getY() + y1, target.getZ() + z1));
                        continue;

                    rotation = Ngm.PlayerManager.calcAngle(new Vec3d(target.getX() + x1, target.getY() + y1, target.getZ() + z1));
                    if (Ngm.playerManager.checkRtx(rotation[0], rotation[1], (float) Math.sqrt(getSquaredRotateDistance()), wallRange.getValue(), rayTrace.getValue())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public Entity findTarget() {
        List<LivingEntity> first_stage = new CopyOnWriteArrayList<>();
        for (Entity ent : mc.world.getEntities()) {
            if ((ent instanceof ShulkerBulletEntity || ent instanceof FireballEntity)
                    && ent.isAlive()
                    && isInRange(ent)
                    && Projectiles.getValue()) {
                return ent;
            }
            if (skipEntity(ent)) continue;
            if (!(ent instanceof LivingEntity)) continue;
            first_stage.add((LivingEntity) ent);
        }

        return switch (sort.getValue()) {
            case LowestDistance ->
                    first_stage.stream().min(Comparator.comparing(e -> (mc.player.squaredDistanceTo(e.getPos())))).orElse(null);

            case HighestDistance ->
                    first_stage.stream().max(Comparator.comparing(e -> (mc.player.squaredDistanceTo(e.getPos())))).orElse(null);

            case FOV -> first_stage.stream().min(Comparator.comparing(this::getFOVAngle)).orElse(null);

            case LowestHealth ->
                    first_stage.stream().min(Comparator.comparing(e -> (e.getHealth() + e.getAbsorptionAmount()))).orElse(null);

            case HighestHealth ->
                    first_stage.stream().max(Comparator.comparing(e -> (e.getHealth() + e.getAbsorptionAmount()))).orElse(null);

            case LowestDurability -> first_stage.stream().min(Comparator.comparing(e -> {
                        float v = 0;
                        for (ItemStack armor : target.getArmorItems())
                            if (armor != null && !armor.getItem().equals(Items.AIR)) {
                                v += ((armor.getMaxDamage() - armor.getDamage()) / (float) armor.getMaxDamage());
                            }
                        return v;
                    }
            )).orElse(null);

            case HighestDurability -> first_stage.stream().max(Comparator.comparing(e -> {
                        float v = 0;
                        for (ItemStack armor : target.getArmorItems())
                            if (armor != null && !armor.getItem().equals(Items.AIR)) {
                                v += ((armor.getMaxDamage() - armor.getDamage()) / (float) armor.getMaxDamage());
                            }
                        return v;
                    }
            )).orElse(null);
        };
    }

    private boolean skipEntity(Entity entity) {
        if (isBullet(entity)) return false;
        if (!(entity instanceof LivingEntity ent)) return true;
        if (ent.isDead() || !entity.isAlive()) return true;
        if (entity instanceof ArmorStandEntity) return true;
        if (entity instanceof CatEntity) return true;
        if (skipNotSelected(entity)) return true;
        if (!isInFOV(ent)) return true;

        if (entity instanceof PlayerEntity player) {
            if (player == mc.player || Ngm.friendManager.isFriend(player))
                return true;
            if (player.isCreative() && ignoreCreative.getValue())
                return true;
            if (player.isInvisible() && ignoreInvisible.getValue())
                return true;
            if (player.getTeamColorValue() == mc.player.getTeamColorValue() && ignoreTeam.getValue() && mc.player.getTeamColorValue() != 16777215)
                return true;
        }

        return !isInRange(entity);
    }

    private boolean isBullet(Entity entity) {
        return (entity instanceof ShulkerBulletEntity || entity instanceof FireballEntity)
                && entity.isAlive()
                && PlayerUtility.squaredDistanceFromEyes(entity.getPos())
                && Projectiles.getValue();
    }

    private boolean skipNotSelected(Entity entity) {
        if (entity instanceof SlimeEntity && !Slimes.getValue()) return true;
        if (entity instanceof HostileEntity he && !he.isAngryAt(mc.player) && onlyAngry.getValue()) return true;
        if (entity instanceof PlayerEntity && !Players.getValue()) return true;
        if (entity instanceof VillagerEntity && !Villagers.getValue()) return true;
        if (entity instanceof MobEntity && !Mobs.getValue()) return true;
        return entity instanceof AnimalEntity && !Animals.getValue();
    }

    private boolean isInFOV(@NotNull LivingEntity e) {
        double deltaX = e.getX() - mc.player.getX();
        double deltaZ = e.getZ() - mc.player.getZ();
        float yawDelta = MathHelper.wrapDegrees((float) MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(deltaZ, deltaX)) - 90.0) - MathHelper.wrapDegrees(mc.player.getYaw()));
        return Math.abs(yawDelta) <= fov.getValue();
    }

    private float getFOVAngle(@NotNull LivingEntity e) {
        double difX = e.getX() - mc.player.getX();
        double difZ = e.getZ() - mc.player.getZ();
        float yaw = (float) MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(difZ, difX)) - 90.0);
        return Math.abs(yaw - MathHelper.wrapDegrees(mc.player.getYaw()));
    }

    public enum Rotation {
        Universal, SunRise, None
    }

    public enum RayTrace {
        OFF, OnlyTarget, AllEntities
    }

    public enum Sort {
        LowestDistance, HighestDistance, LowestHealth, HighestHealth, LowestDurability, HighestDurability, FOV
    }

    public enum Switch {
        Normal, None, Silent
    }

    public enum RayTraceAngle {
        Calculated, Real
    }

    public enum Mode {
        Interact, Track
    }

    public enum RandomHitDelay {
        Off, Delay, FallDistance
    }
}