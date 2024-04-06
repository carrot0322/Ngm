package me.coolmint.ngm.mixin;

import me.coolmint.ngm.event.impl.SlowMovementEvent;
import me.coolmint.ngm.event.impl.TeamColorEvent;
import me.coolmint.ngm.event.impl.VelocityMultiplierEvent;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static me.coolmint.ngm.util.traits.Util.EVENT_BUS;
import static me.coolmint.ngm.util.traits.Util.mc;

@Mixin(Entity.class)
public abstract class MixinEntity {
    @Inject(method = "slowMovement", at = @At(value = "HEAD"), cancellable = true)
    private void hookSlowMovement(BlockState state, Vec3d multiplier, CallbackInfo ci) {
        if ((Object) this != mc.player) {
            return;
        }
        SlowMovementEvent slowMovementEvent = new SlowMovementEvent(state);
        EVENT_BUS.post(slowMovementEvent);
        if (slowMovementEvent.isCancelled()) {
            ci.cancel();
        }
    }

    @Redirect(method = "getVelocityMultiplier", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;getBlock()" + "Lnet/minecraft/ block/Block;"))
    private Block hookGetVelocityMultiplier(BlockState instance) {
        if ((Object) this != mc.player) {
            return instance.getBlock();
        }
        VelocityMultiplierEvent velocityMultiplierEvent = new VelocityMultiplierEvent(instance);
        EVENT_BUS.post(velocityMultiplierEvent);
        if (velocityMultiplierEvent.isCancelled()) {
            return Blocks.DIRT;
        }
        return instance.getBlock();
    }

    @Inject(method = "getTeamColorValue", at = @At(value = "HEAD"),
            cancellable = true)
    private void hookGetTeamColorValue(CallbackInfoReturnable<Integer> cir) {
        TeamColorEvent teamColorEvent = new TeamColorEvent((Entity) (Object) this);
        EVENT_BUS.post(teamColorEvent);
        if (teamColorEvent.isCancelled()) {
            cir.setReturnValue(teamColorEvent.getColor());
            cir.cancel();
        }
    }
}