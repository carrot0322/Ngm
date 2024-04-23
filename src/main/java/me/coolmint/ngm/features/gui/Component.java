package me.coolmint.ngm.features.gui;

import me.coolmint.ngm.Ngm;
import me.coolmint.ngm.features.Feature;
import me.coolmint.ngm.features.gui.fonts.FontRenderers;
import me.coolmint.ngm.features.gui.items.Item;
import me.coolmint.ngm.features.gui.items.buttons.Button;
import me.coolmint.ngm.features.modules.Module;
import me.coolmint.ngm.features.modules.client.ClickGuiModule;
import me.coolmint.ngm.util.client.RenderUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Component
        extends Feature {
    public static int[] counter1 = new int[]{1};
    protected DrawContext context;
    private final List<Item> items = new ArrayList<>();
    public boolean drag;
    private int x;
    private int y;
    private int x2;
    private int y2;
    private int width;
    private int height;
    private boolean open;
    private boolean hidden = false;
    public Module.Category category;

    public static final Identifier bomb = new Identifier("textures/bomb.png");
    public static final Identifier ghost = new Identifier("textures/ghost.png");
    public static final Identifier bulb = new Identifier("textures/bulb.png");
    public static final Identifier settings = new Identifier("textures/settings.png");
    public static final Identifier eye = new Identifier("textures/eye.png");
    public static final Identifier running = new Identifier("textures/running.png");
    public static final Identifier mace = new Identifier("textures/mace.png");
    public static final Identifier dots = new Identifier("textures/more.png");

    public Component(Module.Category category, String name, int x, int y, boolean open) {
        super(name);
        this.category = category;
        this.x = x;
        this.y = y + 25;
        this.width = 100;
        this.height = 18;
        this.open = open;
        this.setupItems();
    }

    public void setupItems() {
    }

    private void drag(int mouseX, int mouseY) {
        if (!this.drag) {
            return;
        }
        this.x = this.x2 + mouseX;
        this.y = this.y2 + mouseY;
    }

    public void drawScreen(DrawContext context, int mouseX, int mouseY, float partialTicks) {
        this.context = context;
        this.drag(mouseX, mouseY);
        counter1 = new int[]{1};
        float totalItemHeight = this.open ? this.getTotalItemHeight() - 2.0f : 0.0f;
        FontRenderers.Main.drawString(context.getMatrices(), "N", 3, 3,  Ngm.colorManager.getColorWithAlpha(Ngm.moduleManager.getModuleByClass(ClickGuiModule.class).hoverAlpha.getValue()), true);
        FontRenderers.Main.drawString(context.getMatrices(), "GM Client", 3 + FontRenderers.Main.getStringWidth("N"), 3,  new Color(255, 255, 255, 255).getRGB(), true);
        RenderUtil.renderRoundedQuad(context, this.x, this.y - 1, this.x + this.width, this.y + this.height + totalItemHeight, 4, 20, new Color(50 ,50, 50, 255));

        //Render Category Icons
        if (category.getName().equals("Combat")) context.drawTexture(mace, this.x + 85, this.y + 1, 0, 0, 9, 9, 9, 9);
        if (category.getName().equals("Movement")) context.drawTexture(running, this.x + 85, this.y + 1, 0, 0, 9, 9, 9, 9);
        if (category.getName().equals("Render")) context.drawTexture(eye, this.x + 85, this.y + 1, 0, 0, 9, 9, 9, 9);
        if (category.getName().equals("Client")) context.drawTexture(settings, this.x + 85, this.y + 1, 0, 0, 9, 9, 9, 9);
        if (category.getName().equals("Exploit")) context.drawTexture(bomb, this.x + 85, this.y + 1, 0, 0, 9, 9, 9, 9);
        if (category.getName().equals("Misc")) context.drawTexture(bulb, this.x + 85, this.y + 1, 0, 0, 9, 9, 9, 9);
        if (category.getName().equals("Legit")) context.drawTexture(ghost, this.x + 85, this.y + 1, 0, 0, 9, 9, 9, 9);

        RenderUtil.renderRoundedQuadMatrix(context.getMatrices(), this.x, (float) this.y + 12.5f, this.x + this.width, (float) (this.y + this.height) + totalItemHeight, 4 , 20, new Color(30, 30, 30, 255));
        RenderUtil.rect(context.getMatrices(), this.x, (float) this.y + 12.5f, this.x + this.width, (float) (this.y + this.height), new Color(30, 30, 30, 255).getRGB());

        FontRenderers.Main.drawString(context.getMatrices(), this.getName(), (float) this.x + 3.0f, (float) this.y - 2.0f - (float) ClickGui.getClickGui().getTextOffset(), this.isEnabled() ? Color.GREEN.getRGB() : Color.GRAY.getRGB(), true);
        if (this.open) {
            float y = (float) (this.getY() + this.getHeight()) - 3.0f;
            for (Item item : this.getItems()) {
                Component.counter1[0] = counter1[0] + 1;
                if (item.isHidden()) continue;
                item.setLocation((float) this.x + 2.0f, y);
                item.setWidth(this.getWidth() - 4);
                item.drawScreen(context, mouseX, mouseY, partialTicks);
                context.drawTexture(dots, this.x + 90, (int) y, 0, 0, 10, 10, 10, 10);
                y += (float) item.getHeight() + 1.5f;
            }
        }
    }

    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton == 0 && this.isHovering(mouseX, mouseY)) {
            this.x2 = this.x - mouseX;
            this.y2 = this.y - mouseY;
            ClickGui.getClickGui().getComponents().forEach(component -> {
                if (component.drag) {
                    component.drag = false;
                }
            });
            this.drag = true;
            return;
        }
        if (mouseButton == 1 && this.isHovering(mouseX, mouseY)) {
            this.open = !this.open;
            mc.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1f));
            return;
        }
        if (!this.open) {
            return;
        }
        this.getItems().forEach(item -> item.mouseClicked(mouseX, mouseY, mouseButton));
    }

    public void mouseReleased(int mouseX, int mouseY, int releaseButton) {
        if (releaseButton == 0) {
            this.drag = false;
        }
        if (!this.open) {
            return;
        }
        this.getItems().forEach(item -> item.mouseReleased(mouseX, mouseY, releaseButton));
    }

    public void onKeyTyped(char typedChar, int keyCode) {
        if (!this.open) {
            return;
        }
        this.getItems().forEach(item -> item.onKeyTyped(typedChar, keyCode));
    }

    public void onKeyPressed(int key) {
        if (!open) return;
        this.getItems().forEach(item -> item.onKeyPressed(key));
    }

    public void addButton(Button button) {
        this.items.add(button);
    }

    public int getX() {
        return this.x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return this.y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getWidth() {
        return this.width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return this.height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public boolean isHidden() {
        return this.hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    public boolean isOpen() {
        return this.open;
    }

    public final List<Item> getItems() {
        return this.items;
    }

    private boolean isHovering(int mouseX, int mouseY) {
        return mouseX >= this.getX() && mouseX <= this.getX() + this.getWidth() && mouseY >= this.getY() && mouseY <= this.getY() + this.getHeight() - (this.open ? 2 : 0);
    }

    private float getTotalItemHeight() {
        float height = 0.0f;
        for (Item item : this.getItems()) {
            height += (float) item.getHeight() + 1.5f;
        }
        return height;
    }

    protected void drawString(String text, double x, double y, Color color) {
        drawString(text, x, y, color.hashCode());
    }

    protected void drawString(String text, double x, double y, int color) {
        context.drawTextWithShadow(mc.textRenderer, text, (int) x, (int) y, color);
    }
}