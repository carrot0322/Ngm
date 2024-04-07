package me.coolmint.ngm.util;

import me.coolmint.ngm.Ngm;
import me.coolmint.ngm.event.impl.EventMove;
import me.coolmint.ngm.features.modules.Module;
import me.coolmint.ngm.util.player.RotationUtils;
import net.minecraft.block.AirBlock;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

import static me.coolmint.ngm.util.traits.Util.mc;

public final class MovementUtility {

    public static boolean isMoving() {
        return mc.player.input.movementForward != 0.0 || mc.player.input.movementSideways != 0.0;
    }

    public static double[] forward(final double d) {
        float f = mc.player.input.movementForward;
        float f2 = mc.player.input.movementSideways;
        float f3 =  mc.player.getYaw();
        if (f != 0.0f) {
            if (f2 > 0.0f) {
                f3 += ((f > 0.0f) ? -45 : 45);
            } else if (f2 < 0.0f) {
                f3 += ((f > 0.0f) ? 45 : -45);
            }
            f2 = 0.0f;
            if (f > 0.0f) {
                f = 1.0f;
            } else if (f < 0.0f) {
                f = -1.0f;
            }
        }
        final double d2 = Math.sin(Math.toRadians(f3 + 90.0f));
        final double d3 = Math.cos(Math.toRadians(f3 + 90.0f));
        final double d4 = f * d * d3 + f2 * d * d2;
        final double d5 = f * d * d2 - f2 * d * d3;
        return new double[]{d4, d5};
    }

    public static void setMotion(double speed) {
        double forward = mc.player.input.movementForward;
        double strafe = mc.player.input.movementSideways;
        float yaw = mc.player.getYaw();
        if (forward == 0 && strafe == 0) {
            mc.player.setVelocity(0, mc.player.getVelocity().y,0);
        } else {
            if (forward != 0) {
                if (strafe > 0) {
                    yaw += (float) (forward > 0 ? -45 : 45);
                } else if (strafe < 0) {
                    yaw += (float) (forward > 0 ? 45 : -45);
                }
                strafe = 0;
                if (forward > 0) {
                    forward = 1;
                } else if (forward < 0) {
                    forward = -1;
                }
            }
            double sin = MathHelper.sin((float) Math.toRadians(yaw + 90));
            double cos = MathHelper.cos((float) Math.toRadians(yaw + 90));
            mc.player.setVelocity(forward * speed * cos + strafe * speed * sin, mc.player.getVelocity().y,forward * speed * sin - strafe * speed * cos);
        }
    }

    public static double getJumpSpeed() {
        double jumpSpeed = 0.3999999463558197;
        if (mc.player.hasStatusEffect(StatusEffects.JUMP_BOOST)) {
            double amplifier = mc.player.getStatusEffect(StatusEffects.JUMP_BOOST).getAmplifier();
            jumpSpeed += (amplifier + 1) * 0.1;
        }
        return jumpSpeed;
    }

    public static void modifyEventSpeed(EventMove event, double d) {
        double d2 = mc.player.input.movementForward;
        double d3 = mc.player.input.movementSideways;
        float f = mc.player.getYaw();
        if (d2 == 0.0 && d3 == 0.0) {
            event.setX(0.0);
            event.setZ(0.0);
        } else {
            if (d2 != 0.0) {
                if (d3 > 0.0) {
                    f += (float) (d2 > 0.0 ? -45 : 45);
                } else if (d3 < 0.0) {
                    f += (float) (d2 > 0.0 ? 45 : -45);
                }

                d3 = 0.0;
                if (d2 > 0.0) {
                    d2 = 1.0;
                } else if (d2 < 0.0) {
                    d2 = -1.0;
                }
            }
            double sin = Math.sin(Math.toRadians(f + 90.0F));
            double cos = Math.cos(Math.toRadians(f + 90.0F));

            event.setX(d2 * d * cos + d3 * d * sin);
            event.setZ(d2 * d * sin - d3 * d * cos);
        }
    }

    public static double getBaseMoveSpeed() {
        int n;
        double d = 0.2873;

        if (Module.fullNullCheck()) return d;

        if (mc.player.hasStatusEffect(StatusEffects.SPEED)) {
            n = mc.player.getStatusEffect(StatusEffects.SPEED).getAmplifier();
            d *= 1.0 + 0.2 * (n + 1);
        }
        if (mc.player.hasStatusEffect(StatusEffects.JUMP_BOOST)) {
            n = mc.player.getStatusEffect(StatusEffects.JUMP_BOOST).getAmplifier();
            d /= 1.0 + 0.2 * (n + 1);
        }
        if (mc.player.hasStatusEffect(StatusEffects.SLOWNESS)) {
            n = mc.player.getStatusEffect(StatusEffects.SLOWNESS).getAmplifier();
            d /= 1.0 + (0.2 * (n + 1));
        }
        return d;
    }

    public static boolean sprintIsLegit(float yaw) {
        return (Math.abs(Math.abs(MathHelper.wrapDegrees(yaw)) - Math.abs(MathHelper.wrapDegrees(Ngm.playerManager.yaw))) < 40);
    }

    public static boolean isRidingBlock() {
        return !(mc.world.getBlockState(new BlockPos((int) mc.player.getX(), (int) (mc.player.getY() - 1.8), (int) mc.player.getZ())).getBlock() instanceof AirBlock) || !(mc.world.getBlockState(new BlockPos((int) mc.player.getX(), (int) (mc.player.getY() - 1), (int) mc.player.getZ())).getBlock() instanceof AirBlock) || !(mc.world.getBlockState(new BlockPos((int) mc.player.getX(), (int) (mc.player.getY() - 0.5), (int) mc.player.getZ())).getBlock() instanceof AirBlock) || !(mc.world.getBlockState(new BlockPos((int) (mc.player.getX() + 0.5), (int) (mc.player.getY() - 1.8), (int) (mc.player.getZ() + 0.5))).getBlock() instanceof AirBlock) || !(mc.world.getBlockState(new BlockPos((int) (mc.player.getX() - 0.5), (int) (mc.player.getY() - 1.8), (int) (mc.player.getZ() + 0.5))).getBlock() instanceof AirBlock) || !(mc.world.getBlockState(new BlockPos((int) (mc.player.getX() + 0.5), (int) (mc.player.getY() - 1.8), (int) (mc.player.getZ() - 0.5))).getBlock() instanceof AirBlock) || !(mc.world.getBlockState(new BlockPos((int) (mc.player.getX() - 0.5), (int) (mc.player.getY() - 1.8), (int) (mc.player.getZ() - 0.5))).getBlock() instanceof AirBlock) || !(mc.world.getBlockState(new BlockPos((int) (mc.player.getX() + 0.5), (int) (mc.player.getY() - 1), (int) (mc.player.getZ() + 0.5))).getBlock() instanceof AirBlock) || !(mc.world.getBlockState(new BlockPos((int) (mc.player.getX() - 0.5), (int) (mc.player.getY() - 1), (int) (mc.player.getZ() + 0.5))).getBlock() instanceof AirBlock) || !(mc.world.getBlockState(new BlockPos((int) (mc.player.getX() + 0.5), (int) (mc.player.getY() - 1), (int) (mc.player.getZ() - 0.5))).getBlock() instanceof AirBlock) || !(mc.world.getBlockState(new BlockPos((int) (mc.player.getX() - 0.5), (int) (mc.player.getY() - 1), (int) (mc.player.getZ() - 0.5))).getBlock() instanceof AirBlock) || !(mc.world.getBlockState(new BlockPos((int) (mc.player.getX() + 0.5), (int) (mc.player.getY() - 0.5), (int) (mc.player.getZ() + 0.5))).getBlock() instanceof AirBlock) || !(mc.world.getBlockState(new BlockPos((int) (mc.player.getX() - 0.5), (int) (mc.player.getY() - 0.5), (int) (mc.player.getZ() + 0.5))).getBlock() instanceof AirBlock) || !(mc.world.getBlockState(new BlockPos((int) (mc.player.getX() + 0.5), (int) (mc.player.getY() - 0.5), (int) (mc.player.getZ() - 0.5))).getBlock() instanceof AirBlock) || !(mc.world.getBlockState(new BlockPos((int) (mc.player.getX() - 0.5), (int) (mc.player.getY() - 0.5), (int) (mc.player.getZ() - 0.5))).getBlock() instanceof AirBlock);
    }

    public static float getSpeed() {
        return (float) getSpeed(mc.player.getVelocity().x, mc.player.getVelocity().z);
    }

    public static double getSpeed(double motionX, double motionZ) {
        return Math.sqrt(motionX * motionX + motionZ * motionZ);
    }

    public static void strafe(float speed) {
        strafe(getSpeed());
    }

    public static float getRawDirection() {
        float rotationYaw = RotationUtils.cameraYaw;

        if (mc.player.forwardSpeed < 0F)
            rotationYaw += 180F;

        float forward = 1F;
        if (mc.player.forwardSpeed < 0F)
            forward = -0.5F;
        else if (mc.player.forwardSpeed > 0F)
            forward = 0.5F;

        if (mc.player.sidewaysSpeed > 0F)
            rotationYaw -= 90F * forward;

        if (mc.player.sidewaysSpeed < 0F)
            rotationYaw += 90F * forward;

        return rotationYaw;
    }
}