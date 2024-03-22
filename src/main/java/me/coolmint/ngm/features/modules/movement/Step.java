package me.coolmint.ngm.features.modules.movement;

import me.coolmint.ngm.features.modules.Module;
import me.coolmint.ngm.features.settings.Setting;

public class Step extends Module {
    public Setting<Boolean> up = this.register(new Setting<>("Up", true));
    private final Setting<Float> upHeight = register(new Setting<>("Height", 2f, 1f, 10f, v -> true));
    public Setting<Boolean> down = this.register(new Setting<>("Down", false));
    private final Setting<Float> downVelocity = register(new Setting<>("DownPower", 1f, 1f, 10f, v -> true));
    public Step() {
        super("Step", "step..", Category.MOVEMENT, true, false, false);
    }

    @Override public void onDisable() {
        if (nullCheck()) return;
        mc.player.setStepHeight(0.6f);
    }

    @Override public void onUpdate() {
        if (nullCheck()) return;

        if(up.getValue())
            mc.player.setStepHeight(upHeight.getValue());

        if(down.getValue()){
            if (mc.player.isInLava() || mc.player.isTouchingWater() || !mc.player.isOnGround()) return;
            mc.player.addVelocity(0, downVelocity.getValue() * -1, 0);
        }
    }
}
