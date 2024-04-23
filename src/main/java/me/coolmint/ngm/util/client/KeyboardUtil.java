package me.coolmint.ngm.util.client;

import me.coolmint.ngm.features.modules.Module;
import me.coolmint.ngm.features.settings.Bind;
import org.jetbrains.annotations.NotNull;

public class KeyboardUtil {
    public static String getKeyName(int key) {
        String str = new Bind(key).toString().toUpperCase();
        str = str.replace("KEY.KEYBOARD", "").replace(".", " ");
        return str;
    }

    @NotNull
    public static String getShortKeyName(Module feature) {
        String sbind = feature.getBind().toString();
        return switch (feature.getBind().toString()) {
            case "LEFT_CONTROL" -> "LCtrl";
            case "RIGHT_CONTROL" -> "RCtrl";
            case "LEFT_SHIFT" -> "LShift";
            case "RIGHT_SHIFT" -> "RShift";
            case "LEFT_ALT" -> "LAlt";
            case "RIGHT_ALT" -> "RAlt";
            default -> sbind.toUpperCase();
        };
    }
}
