package me.coolmint.ngm.mixin;

import me.coolmint.ngm.Ngm;
import me.coolmint.ngm.features.gui.ClickGui;
import net.minecraft.client.Keyboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static me.coolmint.ngm.util.traits.Util.mc;

@Mixin(Keyboard.class)
public class MixinKeyboard {

    @Inject(method = "onKey", at = @At("HEAD"), cancellable = true)
    private void onKey(long windowPointer, int key, int scanCode, int action, int modifiers, CallbackInfo ci) {
        boolean whitelist = mc.currentScreen == null || mc.currentScreen instanceof ClickGui;
        if (!whitelist) return;

        if (action == 0) Ngm.moduleManager.onKeyReleased(key);
        if (action == 1) Ngm.moduleManager.onKeyPressed(key);
        if (action == 2) action = 1;
    }
}