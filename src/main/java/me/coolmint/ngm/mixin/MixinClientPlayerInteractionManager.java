package me.coolmint.ngm.mixin;

import me.coolmint.ngm.event.impl.AttackBlockEvent;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static me.coolmint.ngm.util.traits.Util.EVENT_BUS;

@Mixin(ClientPlayerInteractionManager.class)
public class MixinClientPlayerInteractionManager {

    @Inject(method = "attackBlock", at = @At("HEAD"), cancellable = true)
    private void attackBlockHook(BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> cir) {
        AttackBlockEvent event = new AttackBlockEvent(pos, direction);
        EVENT_BUS.post(event);
        if (event.isCancelled())
            cir.setReturnValue(false);
    }

}
