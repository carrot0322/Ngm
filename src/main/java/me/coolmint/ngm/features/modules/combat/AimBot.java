package me.coolmint.ngm.features.modules.combat;

import me.coolmint.ngm.Ngm;
import me.coolmint.ngm.features.modules.Module;
import com.google.common.eventbus.Subscribe;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BowItem;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import me.coolmint.ngm.Ngm;
import me.coolmint.ngm.event.impl.EventSync;
import me.coolmint.ngm.features.settings.Setting;
import thunder.hack.utility.math.MathUtility;
import thunder.hack.utility.math.PredictUtility;
import thunder.hack.utility.player.PlayerUtility;

import java.awt.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static net.minecraft.util.hit.HitResult.Type.ENTITY;

public final class AimBot extends Module {
    private final Setting<Mode> mode = register(new Setting<>("Mode", Mode.BowAim));
    private final Setting<Rotation> rotation = register(new Setting<>("Rotation", Rotation.Silent));
    public final Setting<Float> aimRange = register(new Setting<>("Range", 20f, 1f, 30f));
    public final Setting<Boolean> ignoreInvisible = register(new Setting<>("IgnoreInvis", false));

    public static Entity target;
    public static float ppx, ppy, ppz, pmx, pmy, pmz;

    private float rotationYaw, rotationPitch;
    private Box debug_box;
    private float assistAcceleration;

    public AimBot() {
        super("AimBot", "", Module.Category.COMBAT, true, false, false);
    }

    @Subscribe
    public void onSync(EventSync event) {
        if (mode.getValue() == Mode.BowAim) {
            if (!(mc.player.getActiveItem().getItem() instanceof BowItem)) return;

            PlayerEntity nearestTarget = Ngm.combatManager.getTargetByFOV(128);

            if (nearestTarget == null) return;

            float currentDuration = (float) (mc.player.getActiveItem().getMaxUseTime() - mc.player.getItemUseTime()) / 20.0f;

            currentDuration = (currentDuration * currentDuration + currentDuration * 2.0f) / 3.0f;

            if (currentDuration >= 1.0f) currentDuration = 1.0f;

            float pitch = (float) (-Math.toDegrees(calculateArc(nearestTarget, currentDuration * 3.0f)));

            if (Float.isNaN(pitch)) return;

            PlayerEntity predictedEntity = PredictUtility.predictPlayer(nearestTarget, predictTicks.getValue());
            double iX = predictedEntity.getX() - predictedEntity.prevX;
            double iZ = predictedEntity.getZ() - predictedEntity.prevZ;
            double distance = mc.player.distanceTo(predictedEntity);
            distance -= distance % 2.0;
            iX = distance / 2.0 * iX * (mc.player.isSprinting() ? 1.3 : 1.1);
            iZ = distance / 2.0 * iZ * (mc.player.isSprinting() ? 1.3 : 1.1);
            rotationYaw = (float) Math.toDegrees(Math.atan2(predictedEntity.getZ() + iZ - mc.player.getZ(), predictedEntity.getX() + iX - mc.player.getX())) - 90.0f;
            rotationPitch = pitch;
            mc.player.networkHandler.sendPacket(new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, PlayerUtility.getWorldActionId(mc.world)));
        } else {
            if (mc.crosshairTarget.getType() == ENTITY) {
                assistAcceleration = 0;
                return;
            }
        }

        if (target != null || (mode.getValue() == Mode.BowAim && mc.player.getActiveItem().getItem() instanceof BowItem)) {
            if (rotation.getValue() == Rotation.Silent) {
                mc.player.setYaw(rotationYaw);
                mc.player.setPitch(rotationPitch);
            }
        }
    }

    @Override
    public void onEnable() {
        target = null;
        debug_box = null;
        ppx = ppy = ppz = pmx = pmz = pmy = 0;
        rotationYaw = mc.player.getYaw();
        rotationPitch = mc.player.getPitch();
    }

    private float calculateArc(@NotNull PlayerEntity target, double duration) {
        double yArc = target.getY() + (double) (target.getEyeHeight(target.getPose())) - (mc.player.getY() + (double) mc.player.getEyeHeight(mc.player.getPose()));
        double dX = target.getX() - mc.player.getX();
        double dZ = target.getZ() - mc.player.getZ();
        double dirRoot = Math.sqrt(dX * dX + dZ * dZ);
        return calculateArc(duration, dirRoot, yArc);
    }

    private float calculateArc(double d, double dr, double y) {
        y = 2.0 * y * (d * d);
        y = 0.05000000074505806 * ((0.05000000074505806 * (dr * dr)) + y);
        y = Math.sqrt(d * d * d * d - y);
        d = d * d - y;
        y = Math.atan2(d * d + y, 0.05000000074505806 * dr);
        d = Math.atan2(d, 0.05000000074505806 * dr);
        return (float) Math.min(y, d);
    }

    private void calcThread() {
        if (target == null) {
            findTarget();
            return;
        }
        if (skipEntity(target)) {
            target = null;
            return;
        }
    }

    public void findTarget() {
        List<Entity> first_stage = new CopyOnWriteArrayList<>();
        for (Entity entity : mc.world.getEntities()) {
            if (skipEntity(entity)) continue;
            first_stage.add(entity);
        }

        float best_distance = 144;
        Entity best_entity = null;

        for (Entity ent : first_stage) {
            float temp_dst = (float) Math.sqrt(mc.player.squaredDistanceTo(getResolvedPos(ent)));
            if (temp_dst < best_distance) {
                best_entity = ent;
                best_distance = temp_dst;
            }
        }
        target = best_entity;
    }

    private boolean skipEntity(Entity entity) {
        if (!(entity instanceof LivingEntity ent)) return true;
        if (ent.isDead()) return true;
        if (!entity.isAlive()) return true;
        if (entity instanceof ArmorStandEntity) return true;
        if (!(entity instanceof PlayerEntity)) return true;
        if (entity == mc.player) return true;
        if (entity.isInvisible() && ignoreInvisible.getValue()) return true;
        if (Ngm.friendManager.isFriend((PlayerEntity) entity)) return true;
        if (Math.abs(getYawToEntityNew(entity)) > fov.getValue()) return true;
        return mc.player.squaredDistanceTo(getResolvedPos(entity)) > aimRange.getPow2Value();
    }

    public float getYawToEntityNew(@NotNull Entity entity) {
        return getYawBetween(mc.player.getYaw(), mc.player.getX(), mc.player.getZ(), entity.getX(), entity.getZ());
    }

    public float getYawBetween(float yaw, double srcX, double srcZ, double destX, double destZ) {
        double xDist = destX - srcX;
        double zDist = destZ - srcZ;
        float yaw1 = (float) (StrictMath.atan2(zDist, xDist) * 180.0 / 3.141592653589793) - 90.0f;
        return yaw + MathHelper.wrapDegrees(yaw1 - yaw);
    }

    private Vec3d getResolvedPos(@NotNull Entity pl) {
        return new Vec3d(pl.getX() + pl.getVelocity().x * predict.getValue(), pl.getY(), pl.getZ() + pl.getVelocity().z * predict.getValue());
    }

    private enum Part {
        Chest, Head, Neck, Leggings, Boots
    }

    private enum Rotation {
        Client, Silent
    }

    private enum Mode {
        BowAim
    }
}