package me.coolmint.ngm.features.modules.client;

import me.coolmint.ngm.features.modules.Module;
import me.coolmint.ngm.features.settings.Setting;

public class Notification extends Module {
    public Setting<Boolean> module = register(new Setting<>("Module Toggle Msg", true));

    public Notification() {
        super("Notification", "", Category.CLIENT, true, false, false);
    }
}