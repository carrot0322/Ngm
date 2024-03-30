package me.coolmint.ngm.features.modules.misc;

import me.coolmint.ngm.features.modules.Module;
import net.minecraft.item.Items;

public class FastPlace extends Module {
    public FastPlace() {
        super("FastPlace", "", Category.MISC, true, false, false);
    }

    @Override public void onUpdate() {
        if (nullCheck()) return;

        if (mc.player.isHolding(Items.EXPERIENCE_BOTTLE)) {
            mc.itemUseCooldown = 0;
        }
    }
}
