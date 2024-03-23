package me.coolmint.ngm.util.player;

import net.minecraft.block.BlockState;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.Entity;
import net.minecraft.item.PotionItem;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.Colors;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;

import static me.coolmint.ngm.util.traits.Util.mc;

public class PlayerUtils {
    private static final double diagonal = 1 / Math.sqrt(2);
    private static final Vec3d horizontalVelocity = new Vec3d(0, 0, 0);

    private static final Colors color = new Colors();

    public static void centerPlayer() {
        double x = MathHelper.floor(mc.player.getX()) + 0.5;
        double z = MathHelper.floor(mc.player.getZ()) + 0.5;
        mc.player.setPosition(x, mc.player.getY(), z);
        mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY(), mc.player.getZ(), mc.player.isOnGround()));
    }

    public static float[] calculateAngle(Vec3d target) {
        Vec3d eyesPos = new Vec3d(mc.player.getX(), mc.player.getY() + mc.player.getEyeHeight(mc.player.getPose()), mc.player.getZ());

        double dX = target.x - eyesPos.x;
        double dY = (target.y - eyesPos.y) * -1.0D;
        double dZ = target.z - eyesPos.z;

        double dist = Math.sqrt(dX * dX + dZ * dZ);

        return new float[]{(float) MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(dZ, dX)) - 90.0D), (float) MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(dY, dist)))};
    }

    public static boolean shouldPause(boolean ifBreaking, boolean ifEating, boolean ifDrinking) {
        if (ifBreaking && mc.interactionManager.isBreakingBlock()) return true;
        if (ifEating && (mc.player.isUsingItem() && (mc.player.getMainHandStack().getItem().isFood() || mc.player.getOffHandStack().getItem().isFood()))) return true;
        return ifDrinking && (mc.player.isUsingItem() && (mc.player.getMainHandStack().getItem() instanceof PotionItem || mc.player.getOffHandStack().getItem() instanceof PotionItem));
    }

    public static boolean isMoving() {
        return mc.player.forwardSpeed != 0 || mc.player.sidewaysSpeed != 0;
    }

    public static boolean isSprinting() {
        return mc.player.isSprinting() && (mc.player.forwardSpeed != 0 || mc.player.sidewaysSpeed != 0);
    }

    public static boolean isInHole(boolean doubles) {
        if (mc.player == null || mc.world == null) return false;

        BlockPos blockPos = mc.player.getBlockPos();
        int air = 0;

        for (Direction direction : Direction.values()) {
            if (direction == Direction.UP) continue;

            BlockState state = mc.world.getBlockState(blockPos.offset(direction));

            if (state.getBlock().getBlastResistance() < 600) {
                if (!doubles || direction == Direction.DOWN) return false;

                air++;

                for (Direction dir : Direction.values()) {
                    if (dir == direction.getOpposite() || dir == Direction.UP) continue;

                    BlockState blockState1 = mc.world.getBlockState(blockPos.offset(direction).offset(dir));

                    if (blockState1.getBlock().getBlastResistance() < 600) {
                        return false;
                    }
                }
            }
        }

        return air < 2;
    }

    public static double distanceTo(Entity entity) {
        return distanceTo(entity.getX(), entity.getY(), entity.getZ());
    }

    public static double distanceTo(BlockPos blockPos) {
        return distanceTo(blockPos.getX(), blockPos.getY(), blockPos.getZ());
    }

    public static double distanceTo(Vec3d vec3d) {
        return distanceTo(vec3d.getX(), vec3d.getY(), vec3d.getZ());
    }

    public static double distanceTo(double x, double y, double z) {
        return Math.sqrt(squaredDistanceTo(x, y, z));
    }

    public static double squaredDistanceTo(Entity entity) {
        return squaredDistanceTo(entity.getX(), entity.getY(), entity.getZ());
    }

    public static double squaredDistanceTo(BlockPos blockPos) {
        return squaredDistanceTo(blockPos.getX(), blockPos.getY(), blockPos.getZ());
    }

    public static double squaredDistanceTo(double x, double y, double z) {
        return squaredDistance(mc.player.getX(), mc.player.getY(), mc.player.getZ(), x, y, z);
    }

    public static double squaredDistance(double x1, double y1, double z1, double x2, double y2, double z2) {
        float f = (float) (x1 - x2);
        float g = (float) (y1 - y2);
        float h = (float) (z1 - z2);
        return org.joml.Math.fma(f, f, org.joml.Math.fma(g, g, h * h));
    }

    public static boolean isWithin(Entity entity, double r) {
        return squaredDistanceTo(entity.getX(), entity.getY(), entity.getZ()) <= r * r;
    }

    public static boolean isWithin(Vec3d vec3d, double r) {
        return squaredDistanceTo(vec3d.getX(), vec3d.getY(), vec3d.getZ()) <= r * r;
    }

    public static boolean isWithin(BlockPos blockPos, double r) {
        return squaredDistanceTo(blockPos.getX(), blockPos.getY(), blockPos.getZ()) <= r * r;
    }

    public static boolean isWithin(double x, double y, double z, double r) {
        return squaredDistanceTo(x, y, z) <= r * r;
    }

    public static double distanceToCamera(double x, double y, double z) {
        return Math.sqrt(squaredDistanceToCamera(x, y, z));
    }

    public static double distanceToCamera(Entity entity) {
        return distanceToCamera(entity.getX(), entity.getY() + entity.getEyeHeight(entity.getPose()), entity.getZ());
    }

    public static double squaredDistanceToCamera(double x, double y, double z) {
        Vec3d cameraPos = mc.gameRenderer.getCamera().getPos();
        return squaredDistance(cameraPos.x, cameraPos.y, cameraPos.z, x, y, z);
    }

    public static double squaredDistanceToCamera(Entity entity) {
        return squaredDistanceToCamera(entity.getX(), entity.getY() + entity.getEyeHeight(entity.getPose()), entity.getZ());
    }

    public static boolean isWithinCamera(Entity entity, double r) {
        return squaredDistanceToCamera(entity.getX(), entity.getY(), entity.getZ()) <= r * r;
    }

    public static boolean isWithinCamera(Vec3d vec3d, double r) {
        return squaredDistanceToCamera(vec3d.getX(), vec3d.getY(), vec3d.getZ()) <= r * r;
    }

    public static boolean isWithinCamera(BlockPos blockPos, double r) {
        return squaredDistanceToCamera(blockPos.getX(), blockPos.getY(), blockPos.getZ()) <= r * r;
    }

    public static boolean isWithinCamera(double x, double y, double z, double r) {
        return squaredDistanceToCamera(x, y, z) <= r * r;
    }

    public static boolean isWithinReach(Entity entity) {
        return isWithinReach(entity.getX(), entity.getY(), entity.getZ());
    }

    public static boolean isWithinReach(Vec3d vec3d) {
        return isWithinReach(vec3d.getX(), vec3d.getY(), vec3d.getZ());
    }

    public static boolean isWithinReach(BlockPos blockPos) {
        return isWithinReach(blockPos.getX(), blockPos.getY(), blockPos.getZ());
    }

    public static boolean isWithinReach(double x, double y, double z) {
        return squaredDistance(mc.player.getX(), mc.player.getEyeY(), mc.player.getZ(), x, y, z) <= mc.interactionManager.getReachDistance() * mc.interactionManager.getReachDistance();
    }

    public static GameMode getGameMode() {
        PlayerListEntry playerListEntry = mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid());
        if (playerListEntry == null) return GameMode.SPECTATOR;
        return playerListEntry.getGameMode();
    }

    public static float getTotalHealth() {
        return mc.player.getHealth() + mc.player.getAbsorptionAmount();
    }

    public static boolean isAlive() {
        return mc.player.isAlive() && !mc.player.isDead();
    }

    public static int getPing() {
        if (mc.getNetworkHandler() == null) return 0;

        PlayerListEntry playerListEntry = mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid());
        if (playerListEntry == null) return 0;
        return playerListEntry.getLatency();
    }
}
