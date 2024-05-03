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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class HudModule extends Module {
    private final Setting<Boolean> Watermark = register(new Setting<>("Watermark", true));
    private final Setting<Boolean> Arraylist = register(new Setting<>("Arraylist", true));
    private final Setting<Boolean> Info = register(new Setting<>("Info", true));
    private final Setting<Boolean> Coordinate = register(new Setting<>("Coordinate", true, v -> Info.getValue()));
    private final Setting<Boolean> Fps = register(new Setting<>("Fps", true, v -> Info.getValue()));
    private final Setting<Boolean> Tps = register(new Setting<>("Tps", true, v -> Info.getValue()));
    private final Setting<Boolean> Speed = register(new Setting<>("Speed", true, v -> Info.getValue()));
    public Setting<Boolean> smoothFont = register(new Setting<>("SmoothFont", true));
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
            if(smoothFont.getValue()){
                if(rainbow.getValue())
                    FontRenderers.Hud.drawString(
                            event.getContext().getMatrices(), Ngm.NAME + " " + Ngm.VERSION,
                            2, 3,  getRainbow(alpha.getValue()), true
                    );
                else
                    FontRenderers.Hud.drawString(
                            event.getContext().getMatrices(), Ngm.NAME + " " + Ngm.VERSION,
                            2, 3,  new Color(red.getValue(), green.getValue(), blue.getValue(), alpha.getValue()).getRGB(), true
                    );
            } else {
                if(rainbow.getValue()){
                    event.getContext().drawTextWithShadow(
                            mc.textRenderer, Ngm.NAME + " " + Ngm.VERSION,
                            2, 3, getRainbow(alpha.getValue())
                    );
                } else {
                    event.getContext().drawTextWithShadow(
                            mc.textRenderer, Ngm.NAME + " " + Ngm.VERSION,
                            2, 3, new Color(red.getValue(), green.getValue(), blue.getValue(), alpha.getValue()).getRGB()
                    );
                }
            }
        }

        if (Arraylist.getValue()) {
            List<Module> modules = Ngm.moduleManager.sortedModules;

            for (int i = 0; i == modules.size(); i++) {
                if (!modules.get(i).drawn.getValue())
                    modules.remove(i);
            }

            modules.sort(Comparator.comparingDouble(mod -> smoothFont.getValue() ? -FontRenderers.Hud.getStringWidth(mod.getDisplayName()) : -mc.textRenderer.getWidth(mod.getDisplayName())));

            int y = 2;

            for (Module mod : modules) {
                String displayName = mod.getDisplayName();
                float stringWidth = FontRenderers.Hud.getStringWidth(displayName);
                float stringWidthMC = mc.textRenderer.getWidth(displayName);

                float x = smoothFont.getValue() ? mc.getWindow().getScaledWidth() - stringWidth - 2 : mc.getWindow().getScaledWidth() - stringWidthMC -2;
                float yOffset = mc.textRenderer.fontHeight + 2;

                if(smoothFont.getValue()){
                    if(rainbow.getValue())
                        FontRenderers.Hud.drawString(
                                event.getContext().getMatrices(), displayName,
                                x, y, getRainbow(alpha.getValue()), true
                        );
                    else
                        FontRenderers.Hud.drawString(
                                event.getContext().getMatrices(), displayName,
                                x, y, new Color(red.getValue(), green.getValue(), blue.getValue(), alpha.getValue()).getRGB(), true
                        );
                } else {
                    if(rainbow.getValue()){
                        event.getContext().drawTextWithShadow(
                                mc.textRenderer, displayName,
                                (int) x, y, getRainbow(alpha.getValue())
                        );
                    } else {
                        event.getContext().drawTextWithShadow(
                                mc.textRenderer, displayName,
                                (int) x, y, new Color(red.getValue(), green.getValue(), blue.getValue(), alpha.getValue()).getRGB()
                        );
                    }
                }

                y += yOffset;
            }
        }

        if(Info.getValue()){
            if(mc.player == null) return;
            List<String> items = new ArrayList<>();

            if(Coordinate.getValue())
                items.add("X: " + String.format("%.2f", mc.player.getX()) + " Y: " + String.format("%.2f", mc.player.getY()) + " Z: " + String.format("%.2f", mc.player.getZ()));

            if(Fps.getValue())
                items.add(mc.getCurrentFps() + " FPS");

            if(Tps.getValue())
                items.add(Ngm.serverManager.getTPS() + " TPS");

            if(Speed.getValue())
                items.add(Ngm.speedManager.getSpeedKpH() + " KPH");

            items.sort(Comparator.comparingDouble(mod -> smoothFont.getValue() ? -FontRenderers.Hud.getStringWidth(mod) : -mc.textRenderer.getWidth(mod)));

            int y = mc.getWindow().getScaledHeight() - 10;

            for (String mod : items) {
                float stringWidth = FontRenderers.Hud.getStringWidth(mod);
                float stringWidthMC = mc.textRenderer.getWidth(mod);
                float x = smoothFont.getValue() ? mc.getWindow().getScaledWidth() - stringWidth - 2 : mc.getWindow().getScaledWidth() - stringWidthMC -2;

                float yOffset = smoothFont.getValue() ? FontRenderers.Hud.getFontHeight() - 2 : mc.textRenderer.fontHeight;

                if(smoothFont.getValue()){
                    if(rainbow.getValue())
                        FontRenderers.Hud.drawString(
                                event.getContext().getMatrices(), mod,
                                x, y, getRainbow(alpha.getValue()), true
                        );
                    else
                        FontRenderers.Hud.drawString(
                                event.getContext().getMatrices(), mod,
                                x, y, new Color(red.getValue(), green.getValue(), blue.getValue(), alpha.getValue()).getRGB(), true
                        );
                } else {
                    if(rainbow.getValue()){
                        event.getContext().drawTextWithShadow(
                                mc.textRenderer, mod,
                                (int) x, y, getRainbow(alpha.getValue())
                        );
                    } else {
                        event.getContext().drawTextWithShadow(
                                mc.textRenderer, mod,
                                (int) x, y, new Color(red.getValue(), green.getValue(), blue.getValue(), alpha.getValue()).getRGB()
                        );
                    }
                }

                y -= yOffset;
            }
        }
    }
}
