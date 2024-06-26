package me.coolmint.ngm.features.gui.items.buttons;

import me.coolmint.ngm.Ngm;
import me.coolmint.ngm.features.gui.ClickGui;
import me.coolmint.ngm.features.gui.fonts.FontRenderers;
import me.coolmint.ngm.features.modules.client.ClickGuiModule;
import me.coolmint.ngm.features.settings.Setting;
import me.coolmint.ngm.util.client.RenderUtil;
import me.coolmint.ngm.util.models.Timer;
import net.minecraft.SharedConstants;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Formatting;
import org.lwjgl.glfw.GLFW;

import java.awt.*;

public class StringButton
        extends Button {
    private static final Timer idleTimer = new Timer();
    private static boolean idle;
    private final Setting<String> setting;
    public boolean isListening;
    private CurrentString currentString = new CurrentString("");

    public StringButton(Setting<String> setting) {
        super(setting.getName());
        this.setting = setting;
        this.width = 15;
    }

    public static String removeLastChar(String str) {
        String output = "";
        if (str != null && str.length() > 0) {
            output = str.substring(0, str.length() - 1);
        }
        return output;
    }

    @Override
    public void drawScreen(DrawContext context, int mouseX, int mouseY, float partialTicks) {
        RenderUtil.rect(context.getMatrices(), this.x, this.y, this.x + (float) this.width + 7.4f, this.y + (float) this.height - 0.5f, this.getState() ? (!this.isHovering(mouseX, mouseY) ? Ngm.colorManager.getColorWithAlpha(Ngm.moduleManager.getModuleByClass(ClickGuiModule.class).alpha.getValue()) : Ngm.colorManager.getColorWithAlpha(Ngm.moduleManager.getModuleByClass(ClickGuiModule.class).hoverAlpha.getValue())) : (!this.isHovering(mouseX, mouseY) ? new Color(0, 0, 0, 0).getRGB() : -2007673515));
        if (this.isListening) {
            FontRenderers.Main.drawString(context.getMatrices(), this.currentString.string() + "_", this.x + 2.3f, this.y - 1.7f - (float) ClickGui.getClickGui().getTextOffset(), this.getState() ? -1 : -5592406, true);
            //drawString(this.currentString.string() + "_", this.x + 2.3f, this.y - 1.7f - (float) ClickGui.getClickGui().getTextOffset(), this.getState() ? -1 : -5592406);
        } else {
            FontRenderers.Main.drawString(context.getMatrices(), this.setting.getName().equals("Buttons") ? "Buttons " : (this.setting.getName().equals("Prefix") ? "Prefix  " + Formatting.GRAY : "") + this.setting.getValue(), this.x + 2.3f, this.y - 1.7f - (float) ClickGui.getClickGui().getTextOffset(), this.getState() ? -1 : -5592406, true);
            //drawString((this.setting.getName().equals("Buttons") ? "Buttons " : (this.setting.getName().equals("Prefix") ? "Prefix  " + Formatting.GRAY : "")) + this.setting.getValue(), this.x + 2.3f, this.y - 1.7f - (float) ClickGui.getClickGui().getTextOffset(), this.getState() ? -1 : -5592406);
        }
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (this.isHovering(mouseX, mouseY)) {
            mc.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1f));
        }
    }

    @Override
    public void onKeyTyped(char typedChar, int keyCode) {
        if (this.isListening) {
            if (isValidChar(typedChar) && typedChar != GLFW.GLFW_KEY_BACKSPACE)
                this.setString(this.currentString.string() + typedChar);

            if(typedChar == GLFW.GLFW_KEY_BACKSPACE)
                this.setString(this.currentString.string().substring(0, this.currentString.string().length() - 1));
        }
    }

    public static boolean isValidChar(char chr) {
        return chr != 167 && chr >= ' ' && chr != 127;
    }

    @Override public void onKeyPressed(int key) {
        if (isListening) {
            switch (key) {
                case 1: {
                    return;
                }
                case 28: {
                    this.enterString();
                }
                case 14: {
                    this.setString(StringButton.removeLastChar(this.currentString.string()));
                }
            }
        }
    }

    @Override
    public void update() {
        this.setHidden(!this.setting.isVisible());
    }

    private void enterString() {
        if (this.currentString.string().isEmpty()) {
            this.setting.setValue(this.setting.getDefaultValue());
        } else {
            this.setting.setValue(this.currentString.string());
        }
        this.setString("");
        this.onMouseClick();
    }

    @Override
    public int getHeight() {
        return 14;
    }

    @Override
    public void toggle() {
        this.isListening = !this.isListening;
    }

    @Override
    public boolean getState() {
        return !this.isListening;
    }

    public void setString(String newString) {
        this.currentString = new CurrentString(newString);
    }

    public static String getIdleSign() {
        if (idleTimer.passedMs(500)) {
            idle = !idle;
            idleTimer.reset();
        }
        if (idle) return "_";
        return "";
    }

    public record CurrentString(String string) {
    }
}