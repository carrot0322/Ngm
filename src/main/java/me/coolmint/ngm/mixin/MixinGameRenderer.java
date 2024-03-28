package me.coolmint.ngm.mixin;

import me.coolmint.ngm.event.impl.ReachEvent;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

import static me.coolmint.ngm.util.traits.Util.EVENT_BUS;

@Mixin(GameRenderer.class)
public class MixinGameRenderer{
    @ModifyConstant(method = "updateTargetedEntity", constant = @Constant(doubleValue = 9))
    private double updateTargetedEntityModifySquaredMaxReach(double d) {
        ReachEvent reachEvent = new ReachEvent();
        EVENT_BUS.post(reachEvent);
        double reach = reachEvent.getReach() + 3.0;
        return reachEvent.isCancelled() ? reach * reach : 9.0;
    }
}