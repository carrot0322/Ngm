package me.coolmint.ngm.mixin;

import me.coolmint.ngm.Ngm;
import me.coolmint.ngm.event.impl.TickCounterEvent;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static me.coolmint.ngm.util.traits.Util.EVENT_BUS;

@Mixin( RenderTickCounter.class )
public class MixinRenderTickCounter {
    @Shadow public float lastFrameDuration;
    @Shadow
    private float tickDelta;
    @Shadow
    private long prevTimeMillis;
    @Shadow
    private float tickTime;

    @Inject(method = "beginRenderTick", at = @At("HEAD"), cancellable = true)
    private void beginRenderTick(long timeMillis, CallbackInfoReturnable<Integer> cir) {
        this.lastFrameDuration = ((timeMillis - this.prevTimeMillis) / this.tickTime) * Ngm.TICK_TIMER;
        this.prevTimeMillis = timeMillis;
        this.tickDelta += this.lastFrameDuration;
        int i = (int) this.tickDelta;
        this.tickDelta -= i;
        cir.setReturnValue(i);
    }

    @Inject(method = "beginRenderTick", at = @At(value = "HEAD"),
            cancellable = true)
    private void hookBeginRenderTick(long timeMillis, CallbackInfoReturnable<Integer> cir) {
        TickCounterEvent tickCounterEvent = new TickCounterEvent();
        EVENT_BUS.post(tickCounterEvent);
        if (tickCounterEvent.isCancelled()) {
            lastFrameDuration = ((timeMillis - prevTimeMillis) / tickTime) * tickCounterEvent.getTicks();
            prevTimeMillis = timeMillis;
            tickDelta += lastFrameDuration;
            int i = (int) tickDelta;
            tickDelta -= i;
            cir.setReturnValue(i);
        }
    }
}