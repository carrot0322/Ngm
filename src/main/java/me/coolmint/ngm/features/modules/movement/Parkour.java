package me.coolmint.ngm.features.modules.movement;

import me.coolmint.ngm.features.modules.Module;

public class Parkour extends Module {
    public Parkour() {
        super("Parkour", "Automatically jumps at the edge of blocks", Category.MOVEMENT, true, false, false);
    }

    @Override
    public void onTick() {
        if (mc.player.isOnGround() && !mc.player.isSneaking() && mc.world.isSpaceEmpty(mc.player.getBoundingBox().offset(0.0, -0.5, 0.0).expand(-0.001, 0.0, -0.001))) {
            mc.player.jump();
        }
    }
}