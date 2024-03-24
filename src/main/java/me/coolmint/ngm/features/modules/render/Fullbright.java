package me.coolmint.ngm.features.modules.render;

import me.coolmint.ngm.features.modules.Module;
import me.coolmint.ngm.features.settings.Setting;

public class Fullbright extends Module {
    public Fullbright() {
        super("Fullbright", "", Category.RENDER, true, false, false);
    }

    public static Setting<Integer> brightness = new Setting<>("Brightness", 15, 0, 15);
}