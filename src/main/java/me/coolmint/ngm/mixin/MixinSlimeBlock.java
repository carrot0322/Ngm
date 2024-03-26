package me.coolmint.ngm.mixin;

import me.coolmint.ngm.event.impl.SteppedOnSlimeBlockEvent;
import net.minecraft.block.BlockState;
import net.minecraft.block.SlimeBlock;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static me.coolmint.ngm.util.traits.Util.EVENT_BUS;
import static me.coolmint.ngm.util.traits.Util.mc;

@Mixin(SlimeBlock.class)
public class MixinSlimeBlock {
    @Inject(method = "onSteppedOn", at = @At(value = "HEAD"),
            cancellable = true)
    private void hookOnSteppedOn(World world, BlockPos pos, BlockState state, Entity entity, CallbackInfo ci) {
        SteppedOnSlimeBlockEvent steppedOnSlimeBlockEvent = new SteppedOnSlimeBlockEvent();
        EVENT_BUS.post(steppedOnSlimeBlockEvent);
        if (steppedOnSlimeBlockEvent.isCancelled() && entity == mc.player) {
            ci.cancel();
        }
    }
}