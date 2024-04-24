package me.coolmint.ngm.features.modules.movement;

import me.coolmint.ngm.features.modules.Module;

public class Jetpack extends Module {
    public Jetpack() {
        super("Jetpack", "", Category.MOVEMENT, true, false, false);
    }

    @Override
    public void onUpdate() {
        if(mc.options.jumpKey.isPressed())
            mc.player.jump();
    }
}