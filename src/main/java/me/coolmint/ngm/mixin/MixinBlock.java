package me.coolmint.ngm.mixin;

import me.coolmint.ngm.event.impl.BlockSlipperinessEvent;
import net.minecraft.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static me.coolmint.ngm.util.traits.Util.EVENT_BUS;

@Mixin(Block.class)
public class MixinBlock {
    @Inject(method = "getSlipperiness", at = @At(value = "RETURN"), cancellable = true)
    private void hookGetSlipperiness(CallbackInfoReturnable<Float> cir) {
        BlockSlipperinessEvent blockSlipperinessEvent = new BlockSlipperinessEvent((Block) (Object) this, cir.getReturnValueF());
        EVENT_BUS.post(blockSlipperinessEvent);
        if (blockSlipperinessEvent.isCancelled()) {
            cir.cancel();
            cir.setReturnValue(blockSlipperinessEvent.getSlipperiness());
        }
    }
}