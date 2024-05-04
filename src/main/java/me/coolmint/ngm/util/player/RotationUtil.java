package me.coolmint.ngm.util.player;

import me.coolmint.ngm.event.impl.FixVelocityEvent;
import me.coolmint.ngm.event.impl.KeyEvent;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import static me.coolmint.ngm.util.traits.Util.mc;

public class RotationUtil {
    public float fixRotation, prevRotation;

    public void onPlayerMove(FixVelocityEvent event) {
        if (Float.isNaN(fixRotation)) return;
        event.setVelocity(fix(fixRotation, event.getMovementInput(), event.getSpeed()));
    }

    public void onKeyInput(KeyEvent e) {
        if (Float.isNaN(fixRotation))
            return;

        float mF = mc.player.input.movementForward;
        float mS = mc.player.input.movementSideways;
        float delta = (mc.player.getYaw() - fixRotation) * MathHelper.RADIANS_PER_DEGREE;
        float cos = MathHelper.cos(delta);
        float sin = MathHelper.sin(delta);
        mc.player.input.movementSideways = Math.round(mS * cos - mF * sin);
        mc.player.input.movementForward = Math.round(mF * cos + mS * sin);
    }

    private Vec3d fix(float yaw, Vec3d movementInput, float speed) {
        double d = movementInput.lengthSquared();
        if (d < 1.0E-7)
            return Vec3d.ZERO;
        Vec3d vec3d = (d > 1.0 ? movementInput.normalize() : movementInput).multiply(speed);
        float f = MathHelper.sin(yaw * MathHelper.RADIANS_PER_DEGREE);
        float g = MathHelper.cos(yaw * MathHelper.RADIANS_PER_DEGREE);
        return new Vec3d(vec3d.x * (double) g - vec3d.z * (double) f, vec3d.y, vec3d.z * (double) g + vec3d.x * (double) f);
    }

    public static Vec3d getEyesPos() {
        ClientPlayerEntity player = mc.player;
        float eyeHeight = player.getEyeHeight(player.getPose());
        return player.getPos().add(0, eyeHeight, 0);
    }
}
