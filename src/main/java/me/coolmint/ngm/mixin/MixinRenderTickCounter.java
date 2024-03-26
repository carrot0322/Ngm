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

    @Inject(method = "beginRenderTick", at = @At(value = "FIELD", target = "Lnet/minecraft/client/render/RenderTickCounter;prevTimeMillis:J"))
    public void beginRenderTick(long timeMillis, CallbackInfoReturnable<Integer> cir) {
        this.lastFrameDuration *= Ngm.TIMER;
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