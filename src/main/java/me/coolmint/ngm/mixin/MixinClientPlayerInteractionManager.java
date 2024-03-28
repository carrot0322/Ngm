package me.coolmint.ngm.mixin;

import me.coolmint.ngm.event.impl.ReachEvent;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.world.GameMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static me.coolmint.ngm.util.traits.Util.EVENT_BUS;

@Mixin(ClientPlayerInteractionManager.class)
public abstract class MixinClientPlayerInteractionManager{
    @Shadow
    private GameMode gameMode;

    @Inject(method = "getReachDistance", at = @At(value = "HEAD"),
            cancellable = true)
    private void hookGetReachDistance(CallbackInfoReturnable<Float> cir) {
        final ReachEvent reachEvent = new ReachEvent();
        EVENT_BUS.post(reachEvent);
        if (reachEvent.isCancelled()) {
            cir.cancel();
            float reach = gameMode.isCreative() ? 5.0f : 4.5f;
            ;
            cir.setReturnValue(reach + reachEvent.getReach());
        }
    }
}