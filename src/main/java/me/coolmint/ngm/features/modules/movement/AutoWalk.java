package me.coolmint.ngm.features.modules.movement;

import com.google.common.eventbus.Subscribe;
import me.coolmint.ngm.event.impl.TickEvent;
import me.coolmint.ngm.features.modules.Module;
public class AutoWalk extends Module {

    public AutoWalk() {
        super("AutoWalk", "", Module.Category.MOVEMENT, true, false, false);
    }

    @Override
    public void onDisable() {
        mc.options.forwardKey.setPressed(false);
    }

    @Subscribe
    public void onTick(TickEvent e) {
        mc.options.forwardKey.setPressed(true);
    }
}