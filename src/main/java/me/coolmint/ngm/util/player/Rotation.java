package me.coolmint.ngm.util.player;

import org.joml.Quaternionf;

import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import static me.coolmint.ngm.util.traits.Util.mc;

public record Rotation(float yaw, float pitch)
{
    public void applyToClientPlayer()
    {
        float adjustedYaw =
                RotationUtils.limitAngleChange(mc.player.getYaw(), yaw);
        mc.player.setYaw(adjustedYaw);
        mc.player.setPitch(pitch);
    }

    public void sendPlayerLookPacket()
    {
        sendPlayerLookPacket(mc.player.isOnGround());
    }

    public void sendPlayerLookPacket(boolean onGround)
    {
        mc.player.networkHandler.sendPacket(
                new PlayerMoveC2SPacket.LookAndOnGround(yaw, pitch, onGround));
    }

    public double getAngleTo(Rotation other)
    {
        float yaw1 = MathHelper.wrapDegrees(yaw);
        float yaw2 = MathHelper.wrapDegrees(other.yaw);
        float diffYaw = MathHelper.wrapDegrees(yaw1 - yaw2);

        float pitch1 = MathHelper.wrapDegrees(pitch);
        float pitch2 = MathHelper.wrapDegrees(other.pitch);
        float diffPitch = MathHelper.wrapDegrees(pitch1 - pitch2);

        return Math.sqrt(diffYaw * diffYaw + diffPitch * diffPitch);
    }

    public Rotation withYaw(float yaw)
    {
        return new Rotation(yaw, pitch);
    }

    public Rotation withPitch(float pitch)
    {
        return new Rotation(yaw, pitch);
    }

    public Vec3d toLookVec()
    {
        float radPerDeg = MathHelper.RADIANS_PER_DEGREE;
        float pi = MathHelper.PI;

        float adjustedYaw = -MathHelper.wrapDegrees(yaw) * radPerDeg - pi;
        float cosYaw = MathHelper.cos(adjustedYaw);
        float sinYaw = MathHelper.sin(adjustedYaw);

        float adjustedPitch = -MathHelper.wrapDegrees(pitch) * radPerDeg;
        float nCosPitch = -MathHelper.cos(adjustedPitch);
        float sinPitch = MathHelper.sin(adjustedPitch);

        return new Vec3d(sinYaw * nCosPitch, sinPitch, cosYaw * nCosPitch);
    }

    public Quaternionf toQuaternion()
    {
        float radPerDeg = MathHelper.RADIANS_PER_DEGREE;
        float yawRad = -MathHelper.wrapDegrees(yaw) * radPerDeg;
        float pitchRad = MathHelper.wrapDegrees(pitch) * radPerDeg;

        float sinYaw = MathHelper.sin(yawRad / 2);
        float cosYaw = MathHelper.cos(yawRad / 2);
        float sinPitch = MathHelper.sin(pitchRad / 2);
        float cosPitch = MathHelper.cos(pitchRad / 2);

        float x = sinPitch * cosYaw;
        float y = cosPitch * sinYaw;
        float z = -sinPitch * sinYaw;
        float w = cosPitch * cosYaw;

        return new Quaternionf(x, y, z, w);
    }

    public static Rotation wrapped(float yaw, float pitch)
    {
        return new Rotation(MathHelper.wrapDegrees(yaw),
                MathHelper.wrapDegrees(pitch));
    }
}
