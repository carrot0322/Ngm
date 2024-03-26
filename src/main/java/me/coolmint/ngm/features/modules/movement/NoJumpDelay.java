package me.coolmint.ngm.features.modules.movement;

import me.coolmint.ngm.features.modules.Module;
import me.coolmint.ngm.mixin.ILivingEntity;
import me.coolmint.ngm.features.settings.Setting;

public class NoJumpDelay extends Module {
    public NoJumpDelay() {
        super("NoJumpDelay", "", Module.Category.MOVEMENT, true, false, false);
    }

    private final Setting<Integer> delay = register(new Setting<>("Delay", 1, 0, 4));

    @Override
    public void onUpdate() {
        if (((ILivingEntity)mc.player).getLastJumpCooldown() > delay.getValue()) {
            ((ILivingEntity)mc.player).setLastJumpCooldown(delay.getValue());
        }
    }
}