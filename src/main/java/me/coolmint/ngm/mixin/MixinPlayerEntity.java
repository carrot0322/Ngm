package me.coolmint.ngm.mixin;

import me.coolmint.ngm.event.impl.AttackEvent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static me.coolmint.ngm.util.traits.Util.EVENT_BUS;

@Mixin(value = PlayerEntity.class, priority = 800)
public class MixinPlayerEntity {
    @Inject(method = "attack", at = @At("HEAD"), cancellable = true)
    private void attackAHook2(Entity target, CallbackInfo ci) {
        final AttackEvent event = new AttackEvent(target, false);
        EVENT_BUS.post(event);
        if (event.isCancelled()) {
            ci.cancel();
        }
    }
}
