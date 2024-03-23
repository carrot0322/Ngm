package me.coolmint.ngm.features.modules.misc;

import me.coolmint.ngm.features.modules.Module;
import me.coolmint.ngm.features.settings.Setting;

public class AntiPacketKick extends Module {
    public Setting<Boolean> catchExceptions = register(new Setting<>("catchExceptions", false));
    public Setting<Boolean> logExceptions = register(new Setting<>("logExceptions", false));

    public AntiPacketKick() {
        super("AntiPacketKick", "Attempts to prevent you from being disconnected by large packets.", Category.MISC, true, false, false);
    }

    public boolean catchExceptions() {
        return isEnabled() && catchExceptions.getValue();
    }
}
