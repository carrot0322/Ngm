package me.coolmint.ngm.mixin;

import me.coolmint.ngm.Ngm;
import net.minecraft.client.render.LightmapTextureManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(LightmapTextureManager.class)
public class MixinLightmapTextureManager {
    @ModifyArgs(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/texture/NativeImage;setColor(III)V"))
    private void update(Args args) {
        if (Ngm.moduleManager.isModuleEnabled("Fullbright")) {
            args.set(2, 0xFFFFFFFF);
        }
    }
}