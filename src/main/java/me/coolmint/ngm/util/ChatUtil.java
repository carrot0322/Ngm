package me.coolmint.ngm.util;

import me.coolmint.ngm.Ngm;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import static me.coolmint.ngm.features.Feature.nullCheck;
import static me.coolmint.ngm.util.traits.Util.mc;

public class ChatUtil {
    public static String MsgPrefix(){
        return Formatting.WHITE + "[" + Formatting.GREEN + Ngm.NAME + Formatting.WHITE + "]" + Formatting.RESET + " ";
    }

    public static void sendInfo(String message) {
        sendSilentMessage(MsgPrefix() + Formatting.GRAY + message);
    }

    public static void sendWarning(String message) {
        sendSilentMessage(MsgPrefix() + Formatting.YELLOW + message);
    }

    public static void sendError(String message) {
        sendSilentMessage(MsgPrefix() + Formatting.RED + message);
    }

    public static void sendSilentMessage(String message) {
        if (nullCheck())
            return;

        mc.inGameHud.getChatHud().addMessage(Text.literal(message));
    }
}