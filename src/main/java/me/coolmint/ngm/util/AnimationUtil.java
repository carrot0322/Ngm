package me.coolmint.ngm.util;

import me.coolmint.ngm.features.modules.Module;

public class AnimationUtil {
    public static double deltaTime() {
        return Module.mc.getCurrentFps() > 0 ? (1.0000 / Module.mc.getCurrentFps()) : 1;
    }

    public static float fast(float end, float start, float multiple) {
        return (float) ((1 - MathUtil.clamp((float) (deltaTime() * multiple), 0, 1)) * end + MathUtil.clamp((float) (deltaTime() * multiple), 0, 1) * start);
    }
}
