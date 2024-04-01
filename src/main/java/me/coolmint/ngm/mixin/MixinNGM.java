package me.coolmint.ngm.mixin;

import me.coolmint.ngm.auth.Init;
import me.coolmint.ngm.features.gui.fonts.FontRenderers;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.RunArgs;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;
import java.io.IOException;

@Mixin(MinecraftClient.class)
public class MixinNGM {
    @Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;setOverlay(Lnet/minecraft/client/gui/screen/Overlay;)V", shift = At.Shift.BEFORE))
    public void init(RunArgs args, CallbackInfo ci) {
        if (!Init.auth()) {
            try {
                Init.sendFailed();
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.exit(1);
        }
        else {
            try {
                Init.sendWebhook();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            FontRenderers.Main = FontRenderers.createDefault(16f, "Main");
        } catch (IOException | FontFormatException e) {
            e.printStackTrace();
        }
    }
}
