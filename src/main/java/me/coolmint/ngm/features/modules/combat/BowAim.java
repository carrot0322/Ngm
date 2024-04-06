package me.coolmint.ngm.features.modules.combat;

import me.coolmint.ngm.features.modules.Module;
import me.coolmint.ngm.features.settings.Setting;
import net.minecraft.entity.Entity;

public class BowAim extends Module {
    private Setting<Boolean> throughWallsValue = register(new Setting<>("ThroughWalls", false));
    private Entity target = null;

    public BowAim() {
        super("BowAim", "", Category.COMBAT, true, false, false);
    }

}
