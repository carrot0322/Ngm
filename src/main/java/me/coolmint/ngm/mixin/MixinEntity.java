package me.coolmint.ngm.mixin;

import me.coolmint.ngm.event.impl.TeamColorEvent;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static me.coolmint.ngm.util.traits.Util.EVENT_BUS;

@Mixin(Entity.class)
public abstract class MixinEntity {
    @Inject(method = "getTeamColorValue", at = @At(value = "HEAD"), cancellable = true)
    private void hookGetTeamColorValue(CallbackInfoReturnable<Integer> cir) {
        TeamColorEvent teamColorEvent = new TeamColorEvent((Entity) (Object) this);
        EVENT_BUS.post(teamColorEvent);
        if (teamColorEvent.isCancelled()) {
            cir.setReturnValue(teamColorEvent.getColor());
            cir.cancel();
        }
    }
}
