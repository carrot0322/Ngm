package me.coolmint.ngm.features.gui.items.buttons;

import me.coolmint.ngm.Ngm;
import me.coolmint.ngm.features.gui.Component;
import me.coolmint.ngm.features.gui.ClickGui;
import me.coolmint.ngm.features.gui.fonts.FontRenderers;
import me.coolmint.ngm.features.gui.items.Item;
import me.coolmint.ngm.features.modules.client.ClickGuiModule;
import me.coolmint.ngm.util.RenderUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvents;

import java.awt.*;

public class Button
        extends Item {
    private boolean state;

    public Button(String name) {
        super(name);
        this.height = 15;
    }

    @Override
    public void drawScreen(DrawContext context, int mouseX, int mouseY, float partialTicks) {
        RenderUtil.rect(context.getMatrices(), this.x, this.y, this.x + (float) this.width, this.y + (float) this.height - 0.5f, this.getState() ? (!this.isHovering(mouseX, mouseY) ? Ngm.colorManager.getColorWithAlpha(Ngm.moduleManager.getModuleByClass(ClickGuiModule.class).alpha.getValue()) : Ngm.colorManager.getColorWithAlpha(Ngm.moduleManager.getModuleByClass(ClickGuiModule.class).hoverAlpha.getValue())) : (!this.isHovering(mouseX, mouseY) ? new Color(0, 0, 0, 0).getRGB() : -2007673515));
        FontRenderers.Main.drawString(context.getMatrices(), this.getName(), this.x + 2.3f, this.y - 2.0f - (float) ClickGui.getClickGui().getTextOffset(), this.getState() ? -1 : -5592406, true);
        //drawString(this.getName(), this.x + 2.3f, this.y - 2.0f - (float) ClickGui.getClickGui().getTextOffset(), this.getState() ? -1 : -5592406);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton == 0 && this.isHovering(mouseX, mouseY)) {
            this.onMouseClick();
        }
    }

    public void onMouseClick() {
        this.state = !this.state;
        this.toggle();
        mc.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1f));
    }

    public void toggle() {
    }

    public boolean getState() {
        return this.state;
    }

    @Override
    public int getHeight() {
        return 14;
    }

    public boolean isHovering(int mouseX, int mouseY) {
        for (Component component : ClickGui.getClickGui().getComponents()) {
            if (!component.drag) continue;
            return false;
        }
        return (float) mouseX >= this.getX() && (float) mouseX <= this.getX() + (float) this.getWidth() && (float) mouseY >= this.getY() && (float) mouseY <= this.getY() + (float) this.height;
    }
}