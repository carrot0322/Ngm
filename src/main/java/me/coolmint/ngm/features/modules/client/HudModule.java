package me.coolmint.ngm.features.modules.client;

import me.coolmint.ngm.Ngm;
import me.coolmint.ngm.event.impl.Render2DEvent;
import me.coolmint.ngm.features.gui.Component;
import me.coolmint.ngm.features.gui.fonts.FontRenderers;
import me.coolmint.ngm.features.modules.Module;
import me.coolmint.ngm.features.settings.Setting;
import me.coolmint.ngm.util.client.ColorUtil;
import net.minecraft.client.MinecraftClient;

import java.awt.*;
import java.util.Comparator;
import java.util.List;

public class HudModule extends Module {
    private final Setting<Boolean> Watermark = register(new Setting<>("Watermark", true));
    private final Setting<Boolean> Arraylist = register(new Setting<>("Arraylist", true));
    public Setting<Boolean> rainbow = register(new Setting<>("Rainbow", false));
    public Setting<Integer> rainbowHue = register(new Setting<>("Delay", 240, 0, 600, v -> rainbow.getValue()));
    public Setting<Integer> red = register(new Setting<>("Red", 20, 0, 255, v -> !rainbow.getValue()));
    public Setting<Integer> green = register(new Setting<>("Green", 255, 0, 255, v -> !rainbow.getValue()));
    public Setting<Integer> blue = register(new Setting<>("Blue", 80, 0, 255, v -> !rainbow.getValue()));
    public Setting<Integer> alpha = register(new Setting<>("Alpha", 255, 0, 255));

    public HudModule() {
        super("Hud", "hud", Category.CLIENT, true, false, false);
    }

    private int getRainbow(int alpha){
        return ColorUtil.rainbow(Component.counter1[0] * this.rainbowHue.getValue()).getRGB();
    }

    @Override
    public void onRender2D(Render2DEvent event) {
        MinecraftClient mc = MinecraftClient.getInstance();

        if (Watermark.getValue()) {
            if(rainbow.getValue())
                FontRenderers.Hud.drawString(event.getContext().getMatrices(), Ngm.NAME + " " + Ngm.VERSION, 2, 3,  getRainbow(alpha.getValue()), true);
            else
                FontRenderers.Hud.drawString(event.getContext().getMatrices(), Ngm.NAME + " " + Ngm.VERSION, 2, 3,  new Color(red.getValue(), green.getValue(), blue.getValue(), alpha.getValue()).getRGB(), true);
        }

        if (Arraylist.getValue()) {
            List<Module> modules = Ngm.moduleManager.sortedModules;

            for (int i = 0; i == modules.size(); i++) {
                if (!modules.get(i).drawn.getValue())
                    modules.remove(i);
            }

            modules.sort(Comparator.comparingDouble(mod -> -FontRenderers.Hud.getStringWidth(mod.getDisplayName())));

            int y = 2;

            for (Module mod : modules) {
                String displayName = mod.getDisplayName();
                float stringWidth = FontRenderers.Hud.getStringWidth(displayName);

                float yOffset = mc.textRenderer.fontHeight + 2;

                if(rainbow.getValue())
                    FontRenderers.Hud.drawString(event.getContext().getMatrices(), displayName, mc.getWindow().getScaledWidth() - stringWidth - 2, y,  getRainbow(alpha.getValue()), true);
                else
                    FontRenderers.Hud.drawString(event.getContext().getMatrices(), displayName, mc.getWindow().getScaledWidth() - stringWidth - 2, y, new Color(red.getValue(), green.getValue(), blue.getValue(), alpha.getValue()).getRGB(), true);

                y += yOffset;
            }
        }
    }
}
