package me.coolmint.ngm.features.modules.client;

import me.coolmint.ngm.Ngm;
import me.coolmint.ngm.event.impl.Render2DEvent;
import me.coolmint.ngm.features.modules.Module;

public class HudModule extends Module {
    public HudModule() {
        super("Hud", "hud", Category.CLIENT, true, false, false);
    }

    @Override public void onRender2D(Render2DEvent event) {
        event.getContext().drawTextWithShadow(
                mc.textRenderer,
                Ngm.NAME + " " + Ngm.VERSION,
                2, 2,
                -1
        );
    }
}
