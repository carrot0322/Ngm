package me.coolmint.ngm.mixin;

import me.coolmint.ngm.Ngm;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import me.coolmint.ngm.features.modules.player.Reach;

@Mixin(ClientPlayerInteractionManager.class)
public class MixinClientPlayerInteractionManager {
    @Inject(method = "getReachDistance", at = @At("HEAD"), cancellable = true)
    private void getReachDistanceHook(CallbackInfoReturnable<Float> cir) {
        if (Ngm.moduleManager.isModuleEnabled("Reach")) {
            Reach reach = new Reach();
            cir.setReturnValue(reach.range.getValue());
        }
    }
}