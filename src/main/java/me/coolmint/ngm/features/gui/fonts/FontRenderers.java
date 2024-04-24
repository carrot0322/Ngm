package me.coolmint.ngm.features.gui.fonts;

import me.coolmint.ngm.Ngm;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.io.IOException;
import java.util.Objects;

public class FontRenderers {
    public static FontAdapter Main;
    public static FontAdapter Hud;

    public static @NotNull RendererFontAdapter createDefault(float size, String font) throws IOException, FontFormatException {
        return new RendererFontAdapter(Font.createFont(Font.TRUETYPE_FONT, Objects.requireNonNull(Ngm.class.getClassLoader().getResourceAsStream("assets/client/fonts/" + font + ".ttf"))).deriveFont(Font.PLAIN, size / 2f), size / 2f);
    }

    public static @NotNull RendererFontAdapter createIcons(float size) throws IOException, FontFormatException {
        return new RendererFontAdapter(Font.createFont(Font.TRUETYPE_FONT, Objects.requireNonNull(Ngm.class.getClassLoader().getResourceAsStream("assets/client/fonts/icons.ttf"))).deriveFont(Font.PLAIN, size / 2f), size / 2f);
    }
}