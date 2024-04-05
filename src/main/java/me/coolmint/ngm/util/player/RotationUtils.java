package me.coolmint.ngm.util.player;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import static me.coolmint.ngm.util.traits.Util.mc;

public enum RotationUtils
{
    ;

    public static Vec3d getEyesPos()
    {
        ClientPlayerEntity player = mc.player;
        float eyeHeight = player.getEyeHeight(player.getPose());
        return player.getPos().add(0, eyeHeight, 0);
    }

    public static Vec3d getClientLookVec(float partialTicks)
    {
        float yaw = mc.player.getYaw(partialTicks);
        float pitch = mc.player.getPitch(partialTicks);
        return new Rotation(yaw, pitch).toLookVec();
    }

    public static Vec3d getServerLookVec()
    {
        return new Rotation(mc.player.getYaw(), mc.player.getPitch()).toLookVec();
    }

    public static Rotation getNeededRotations(Vec3d vec)
    {
        Vec3d eyes = getEyesPos();

        double diffX = vec.x - eyes.x;
        double diffZ = vec.z - eyes.z;
        double yaw = Math.toDegrees(Math.atan2(diffZ, diffX)) - 90F;

        double diffY = vec.y - eyes.y;
        double diffXZ = Math.sqrt(diffX * diffX + diffZ * diffZ);
        double pitch = -Math.toDegrees(Math.atan2(diffY, diffXZ));

        return Rotation.wrapped((float)yaw, (float)pitch);
    }

    public static double getAngleToLookVec(Vec3d vec)
    {
        ClientPlayerEntity player = mc.player;
        Rotation current = new Rotation(player.getYaw(), player.getPitch());
        Rotation needed = getNeededRotations(vec);
        return current.getAngleTo(needed);
    }

    public static float getHorizontalAngleToLookVec(Vec3d vec)
    {
        float currentYaw = MathHelper.wrapDegrees(mc.player.getYaw());
        float neededYaw = getNeededRotations(vec).yaw();
        return MathHelper.wrapDegrees(currentYaw - neededYaw);
    }

    public static boolean isAlreadyFacing(Rotation rotation)
    {
        return getAngleToLastReportedLookVec(rotation) <= 1.0;
    }

    public static double getAngleToLastReportedLookVec(Vec3d vec)
    {
        Rotation needed = getNeededRotations(vec);
        return getAngleToLastReportedLookVec(needed);
    }

    public static double getAngleToLastReportedLookVec(Rotation rotation)
    {
        ClientPlayerEntity player = mc.player;
        Rotation lastReported = new Rotation(player.lastRenderYaw, player.lastRenderPitch);
        return lastReported.getAngleTo(rotation);
    }

    public static boolean isFacingBox(Box box, double range)
    {
        Vec3d start = getEyesPos();
        Vec3d end = start.add(getServerLookVec().multiply(range));
        return box.raycast(start, end).isPresent();
    }

    public static Rotation slowlyTurnTowards(Rotation end, float maxChange)
    {
        float startYaw = mc.player.prevYaw;
        float startPitch = mc.player.prevPitch;
        float endYaw = end.yaw();
        float endPitch = end.pitch();

        float yawChange = Math.abs(MathHelper.wrapDegrees(endYaw - startYaw));
        float pitchChange =
                Math.abs(MathHelper.wrapDegrees(endPitch - startPitch));

        float maxChangeYaw =
                Math.min(maxChange, maxChange * yawChange / pitchChange);
        float maxChangePitch =
                Math.min(maxChange, maxChange * pitchChange / yawChange);

        float nextYaw = limitAngleChange(startYaw, endYaw, maxChangeYaw);
        float nextPitch =
                limitAngleChange(startPitch, endPitch, maxChangePitch);

        return new Rotation(nextYaw, nextPitch);
    }

    public static float limitAngleChange(float current, float intended,
                                         float maxChange)
    {
        float currentWrapped = MathHelper.wrapDegrees(current);
        float intendedWrapped = MathHelper.wrapDegrees(intended);

        float change = MathHelper.wrapDegrees(intendedWrapped - currentWrapped);
        change = MathHelper.clamp(change, -maxChange, maxChange);

        return current + change;
    }

    public static float limitAngleChange(float current, float intended)
    {
        float currentWrapped = MathHelper.wrapDegrees(current);
        float intendedWrapped = MathHelper.wrapDegrees(intended);

        float change = MathHelper.wrapDegrees(intendedWrapped - currentWrapped);

        return current + change;
    }
}