package me.coolmint.ngm.features.modules.client;

import com.google.common.eventbus.Subscribe;
import me.coolmint.ngm.Ngm;
import me.coolmint.ngm.event.impl.ClientEvent;
import me.coolmint.ngm.features.command.Command;
import me.coolmint.ngm.features.modules.Module;
import me.coolmint.ngm.features.settings.Setting;
import me.coolmint.ngm.util.ChatUtil;
import net.minecraft.util.Formatting;
import org.lwjgl.glfw.GLFW;

public class ClickGuiModule
        extends Module {
    private static ClickGuiModule INSTANCE = new ClickGuiModule();
    public Setting<String> prefix = this.register(new Setting<>("Prefix", "."));
    public Setting<Boolean> rainbow = this.register(new Setting<>("Rainbow", false));
    public Setting<Integer> rainbowHue = this.register(new Setting<>("Delay", 240, 0, 600, v -> rainbow.getValue()));
    public Setting<Float> rainbowBrightness = this.register(new Setting<>("Brightness ", 150.0f, 1.0f, 255.0f, v -> rainbow.getValue()));
    public Setting<Float> rainbowSaturation = this.register(new Setting<>("Saturation", 150.0f, 1.0f, 255.0f, v -> rainbow.getValue()));
    public Setting<Integer> red = this.register(new Setting<>("Red", 20, 0, 255, v -> !rainbow.getValue()));
    public Setting<Integer> green = this.register(new Setting<>("Green", 255, 0, 255, v -> !rainbow.getValue()));
    public Setting<Integer> blue = this.register(new Setting<>("Blue", 80, 0, 255, v -> !rainbow.getValue()));
    public Setting<Integer> alpha = this.register(new Setting<>("Alpha", 90, 0, 255));
    public Setting<Integer> hoverAlpha = this.register(new Setting<>("HoverAlpha", 40, 0, 255, v -> !rainbow.getValue()));

    private me.coolmint.ngm.features.gui.ClickGui click;

    public ClickGuiModule() {
        super("ClickGui", "Opens the ClickGui", Module.Category.CLIENT, true, false, false);
        setBind(GLFW.GLFW_KEY_Y);
        this.setInstance();
    }

    public static ClickGuiModule getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ClickGuiModule();
        }
        return INSTANCE;
    }

    private void setInstance() {
        INSTANCE = this;
    }

    @Subscribe
    public void onSettingChange(ClientEvent event) {
        if (event.getStage() == 2 && event.getSetting().getFeature().equals(this)) {
            if (event.getSetting().equals(this.prefix)) {
                Ngm.commandManager.setPrefix(this.prefix.getPlannedValue());
                ChatUtil.sendInfo("Prefix set to " + Formatting.DARK_GRAY + Ngm.commandManager.getPrefix());
            }
            Ngm.colorManager.setColor(this.red.getPlannedValue(), this.green.getPlannedValue(), this.blue.getPlannedValue(), this.alpha.getPlannedValue());
        }
    }

    @Override
    public void onEnable() {
        mc.setScreen(me.coolmint.ngm.features.gui.ClickGui.getClickGui());
    }

    @Override
    public void onLoad() {
        Ngm.colorManager.setColor(this.red.getValue(), this.green.getValue(), this.blue.getValue(), this.alpha.getValue());
        Ngm.commandManager.setPrefix(this.prefix.getValue());
    }

    @Override
    public void onTick() {
        if (!(ClickGuiModule.mc.currentScreen instanceof me.coolmint.ngm.features.gui.ClickGui)) {
            this.disable();
        }
    }
}