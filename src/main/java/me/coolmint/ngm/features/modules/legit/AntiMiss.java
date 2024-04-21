package me.coolmint.ngm.features.modules.legit;

import me.coolmint.ngm.features.modules.Module;
import com.google.common.eventbus.Subscribe;
import net.minecraft.util.hit.EntityHitResult;
import me.coolmint.ngm.event.impl.EventAttack;
import me.coolmint.ngm.event.impl.EventHandleBlockBreaking;

public class AntiMiss extends Module {

    public AntiMiss() {
        super("AntiMiss", "", Module.Category.LEGIT, true, false, false);
    }

    @Subscribe
    public void onAttack(EventAttack e) {
        if (!(mc.crosshairTarget instanceof EntityHitResult) && e.isPre())
            e.cancel();
    }

    @Subscribe
    public void onBlockBreaking(EventHandleBlockBreaking e) {
        if (!(mc.crosshairTarget instanceof EntityHitResult))
            e.cancel();
    }
}