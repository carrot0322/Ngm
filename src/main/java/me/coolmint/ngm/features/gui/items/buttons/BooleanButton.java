package me.coolmint.ngm.features.gui.items.buttons;


import me.coolmint.ngm.Ngm;
import me.coolmint.ngm.features.gui.ClickGui;
import me.coolmint.ngm.features.gui.fonts.FontRenderers;
import me.coolmint.ngm.features.modules.client.ClickGuiModule;
import me.coolmint.ngm.features.settings.Setting;
import me.coolmint.ngm.util.RenderUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvents;

import java.awt.*;

public class BooleanButton
        extends Button {
    private final Setting<Boolean> setting;

    public BooleanButton(Setting<Boolean> setting) {
        super(setting.getName());
        this.setting = setting;
        this.width = 15;
    }

    @Override
    public void drawScreen(DrawContext context, int mouseX, int mouseY, float partialTicks) {
        RenderUtil.rect(context.getMatrices(), this.x, this.y, this.x + (float) this.width + 7.4f, this.y + (float) this.height - 0.5f, this.getState() ? (!this.isHovering(mouseX, mouseY) ? Ngm.colorManager.getColorWithAlpha(Ngm.moduleManager.getModuleByClass(ClickGuiModule.class).hoverAlpha.getValue()) : Ngm.colorManager.getColorWithAlpha(Ngm.moduleManager.getModuleByClass(ClickGuiModule.class).alpha.getValue())) : (!this.isHovering(mouseX, mouseY) ? new Color(0, 0, 0, 0).getRGB() : -2007673515));
        //drawString(this.getName(), this.x + 2.3f, this.y - 1.7f - (float) ClickGui.getClickGui().getTextOffset(), this.getState() ? -1 : -5592406);
        FontRenderers.Main.drawString(context.getMatrices(), this.getName(), this.x + 2.3f, this.y - 1.7f - (float) ClickGui.getClickGui().getTextOffset(), this.getState() ? -1 : -5592406, true);
    }

    @Override
    public void update() {
        this.setHidden(!this.setting.isVisible());
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (this.isHovering(mouseX, mouseY)) {
            mc.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1f));
        }
    }

    @Override
    public int getHeight() {
        return 14;
    }

    @Override
    public void toggle() {
        this.setting.setValue(!this.setting.getValue());
    }

    @Override
    public boolean getState() {
        return this.setting.getValue();
    }
}