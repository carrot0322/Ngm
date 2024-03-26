package me.coolmint.ngm.features.modules.player;

import me.coolmint.ngm.features.modules.Module;
import me.coolmint.ngm.features.settings.Setting;

public class Reach extends Module {
    public Reach() {
        super("Reach", "", Category.PLAYER, true, false, false);
    }

    public Setting<Float> range = register(new Setting<>("range", 3f, 3.0f, 6.0f));

    @Override
    public String getDisplayInfo() {
        return String.valueOf(range.getValue());
    }
}