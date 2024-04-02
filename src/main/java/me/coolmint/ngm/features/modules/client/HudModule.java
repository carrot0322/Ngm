package me.coolmint.ngm.features.modules.client;

import me.coolmint.ngm.Ngm;
import me.coolmint.ngm.event.impl.Render2DEvent;
import me.coolmint.ngm.features.modules.Module;
import me.coolmint.ngm.features.settings.Setting;
import net.minecraft.client.MinecraftClient;

import java.util.Comparator;
import java.util.List;

public class HudModule extends Module {
    private final Setting<Boolean> Watermark = register(new Setting<>("Watermark", true));
    private final Setting<Boolean> Arraylist = register(new Setting<>("Arraylist", true));

    public HudModule() {
        super("Hud", "hud", Category.CLIENT, true, false, false);
    }

    @Override
    public void onRender2D(Render2DEvent event) {
        MinecraftClient mc = MinecraftClient.getInstance();

        if (Watermark.getValue()) {
            event.getContext().drawTextWithShadow(
                    mc.textRenderer,
                    Ngm.NAME + " " + Ngm.VERSION,
                    2, 2,
                    -1
            );
        }

        if (Arraylist.getValue()) {
            List<me.coolmint.ngm.features.modules.Module> modules = Ngm.moduleManager.sortedModules;

            for (int i = 0; i == modules.size(); i++){
                if (!modules.get(i).drawn.getValue())
                    modules.remove(i);
            }

            int y = 2;

            for (me.coolmint.ngm.features.modules.Module mod : modules) {
                String displayName = mod.getDisplayName();
                int stringWidth = mc.textRenderer.getWidth(displayName);

                int yOffset = mc.textRenderer.fontHeight + 2;

                event.getContext().drawTextWithShadow(
                        mc.textRenderer,
                        displayName,
                        mc.getWindow().getScaledWidth() - stringWidth - 2,
                        y,
                        -1
                );
                y += yOffset;
            }
        }
    }
}
