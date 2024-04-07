package me.coolmint.ngm.util.player;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.PendingUpdateManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.*;
import org.jetbrains.annotations.NotNull;
import me.coolmint.ngm.mixin.IClientWorldMixin;
import me.coolmint.ngm.util.MathUtil;

import static me.coolmint.ngm.features.modules.Module.mc;

public final class PlayerUtility {
    public static boolean isEating() {
        if (mc.player == null) return false;

        return (mc.player.getMainHandStack().isFood() || mc.player.getOffHandStack().isFood())
                && mc.player.isUsingItem();
    }

    public static boolean isMining() {
        if (mc.interactionManager == null) return false;

        return mc.interactionManager.isBreakingBlock();
    }

    public static float squaredDistanceFromEyes(@NotNull Vec3d vec) {
        if (mc.player == null) return 0;

        double d0 = vec.x - mc.player.getX();
        double d1 = vec.z - mc.player.getZ();
        double d2 = vec.y - (mc.player.getY() + mc.player.getEyeHeight(mc.player.getPose()));
        return (float) (d0 * d0 + d1 * d1 + d2 * d2);
    }

    public static float squaredDistance2d(@NotNull Vec2f point) {
        if (mc.player == null) return 0f;

        double d = mc.player.getX() - point.x;
        double f = mc.player.getZ() - point.y;
        return (float) (d * d + f * f);
    }

    public static ClientPlayerEntity getPlayer() {
        return mc.player;
    }

    public static int getWorldActionId(ClientWorld world) {
        PendingUpdateManager pum = getUpdateManager(world);
        int p = pum.getSequence();
        pum.close();
        return p;
    }

    public static float calculatePercentage(@NotNull ItemStack stack) {
        float durability = stack.getMaxDamage() - stack.getDamage();
        return (durability / (float) stack.getMaxDamage()) * 100F;
    }

    private static PendingUpdateManager getUpdateManager(ClientWorld world) {
        return ((IClientWorldMixin) world).acquirePendingUpdateManager();
    }

    public static float fixAngle(float angle) {
        return Math.round(angle / ((float) (getGCD() * 0.15D))) * (float) (getGCD() * 0.15D);
    }

    public static float getGCD() {
        return (float) (Math.pow((float) (mc.options.getMouseSensitivity().getValue() * 0.6D + 0.2D), 3) * 8.0F);
    }

    public static float squaredDistance2d(double x, double z) {
        if (mc.player == null) return 0f;

        double d = mc.player.getX() - x;
        double f = mc.player.getZ() - z;
        return (float) (d * d + f * f);
    }

    public static BlockPos GetLocalPlayerPosFloored() {
        return new BlockPos((int) Math.floor(mc.player.getX()), (int) Math.floor(mc.player.getY()), (int) Math.floor(mc.player.getZ()));
    }

    public static void PacketFacePitchAndYaw(float pitch, float yaw) {
        boolean isSprinting = mc.player.isSprinting();

        if (isSprinting != mc.player.isSprinting()) {
            if (isSprinting) {
                mc.getNetworkHandler().sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.START_SPRINTING));
            } else {
                mc.getNetworkHandler().sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.STOP_SPRINTING));
            }

            mc.player.setSprinting(isSprinting);
        }

        boolean isSneaking = mc.player.isSneaking();

        if (isSneaking != mc.player.isSneaking()) {
            if (isSneaking) {
                mc.player.setSneaking(true);
            } else {
                mc.player.setSneaking(false);
            }

            mc.player.setSneaking(isSneaking);
        }

        if (PlayerUtility.isCurrentViewEntity()) {
            Box axisalignedbb = mc.player.getBoundingBox();
            double posXDifference = mc.player.getX() - mc.player.lastRenderX;
            double posYDifference = axisalignedbb.minY - mc.player.lastRenderY;
            double posZDifference = mc.player.getZ() - mc.player.lastRenderZ;
            double yawDifference = yaw - mc.player.lastRenderYaw;
            double rotationDifference = pitch - mc.player.lastRenderPitch;
            ++mc.player.ticksSinceLastPositionPacketSent;
            boolean movedXYZ = posXDifference * posXDifference + posYDifference * posYDifference + posZDifference * posZDifference > 9.0E-4D || mc.player.ticksSinceLastPositionPacketSent >= 20;
            boolean movedRotation = yawDifference != 0.0D || rotationDifference != 0.0D;

            if (mc.player.isRiding()) {
                mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.Full(mc.player.getVelocity().x, -999.0D, mc.player.getVelocity().z, yaw, pitch, mc.player.isOnGround()));
                movedXYZ = false;
            } else if (movedXYZ && movedRotation) {
                mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.Full(mc.player.getX(), axisalignedbb.minY, mc.player.getZ(), yaw, pitch, mc.player.isOnGround()));
            } else if (movedXYZ) {
                mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), axisalignedbb.minY, mc.player.getZ(), mc.player.isOnGround()));
            } else if (movedRotation) {
                mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(yaw, pitch, mc.player.isOnGround()));
            } else if (mc.player.lastOnGround != mc.player.isOnGround()) {
                mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.OnGroundOnly(mc.player.isOnGround()));
            }

            if (movedXYZ) {
                mc.player.lastRenderX = mc.player.getX();
                mc.player.lastRenderY = axisalignedbb.minY;
                mc.player.lastRenderX = mc.player.getZ();
                mc.player.ticksSinceLastPositionPacketSent = 0;
            }

            if (movedRotation) {
                mc.player.lastRenderYaw = yaw;
                mc.player.lastRenderPitch = pitch;
            }

            mc.player.lastOnGround = mc.player.isOnGround();
            mc.player.autoJumpEnabled = mc.options.getAutoJump().getValue();
        }
    }

    public static boolean isCurrentViewEntity() {
        return mc.getCameraEntity() == mc.player;
    }
}