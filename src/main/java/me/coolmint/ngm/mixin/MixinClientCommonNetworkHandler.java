package me.coolmint.ngm.mixin;

import me.coolmint.ngm.Ngm;
import me.coolmint.ngm.util.ChatUtil;
import net.minecraft.client.network.ClientCommonNetworkHandler;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static me.coolmint.ngm.util.traits.Util.mc;

@Mixin(ClientCommonNetworkHandler.class)
public class MixinClientCommonNetworkHandler {
    @Inject(method = "onDisconnected", at = @At("HEAD"), cancellable = true)
    private void onDisconnected(Text reason, CallbackInfo info) {
        if (Ngm.moduleManager.isModuleEnabled("SilentDisconnect") && mc.world != null && mc.player != null) {
            ChatUtil.sendWarning(Text.translatable("disconnect.lost").getString() + " : " + reason.getString());
            info.cancel();
        }
    }
}