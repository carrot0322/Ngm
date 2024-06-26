package me.coolmint.ngm.mixin;

import com.mojang.authlib.GameProfile;
import me.coolmint.ngm.event.Stage;
import me.coolmint.ngm.event.impl.MoveEvent;
import me.coolmint.ngm.event.impl.SyncEvent;
import me.coolmint.ngm.event.impl.UpdateEvent;
import me.coolmint.ngm.event.impl.UpdateWalkingPlayerEvent;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.MovementType;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static me.coolmint.ngm.features.Feature.fullNullCheck;
import static me.coolmint.ngm.util.traits.Util.EVENT_BUS;

@Mixin(ClientPlayerEntity.class)
public class MixinClientPlayerEntity extends AbstractClientPlayerEntity {
    public MixinClientPlayerEntity(ClientWorld world, GameProfile profile) {
        super(world, profile);
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void tickHook(CallbackInfo ci) {
        EVENT_BUS.post(new UpdateEvent());
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/AbstractClientPlayerEntity;tick()V", shift = At.Shift.AFTER))
    private void tickHook2(CallbackInfo ci) {
        EVENT_BUS.post(new UpdateWalkingPlayerEvent(Stage.PRE));
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;sendMovementPackets()V", shift = At.Shift.AFTER))
    private void tickHook3(CallbackInfo ci) {
        EVENT_BUS.post(new UpdateWalkingPlayerEvent(Stage.POST));
    }

    @Inject(method = "sendMovementPackets", at = @At("HEAD"), cancellable = true)
    private void sendMovementPacketsHook(CallbackInfo info) {
        if (fullNullCheck()) return;
        SyncEvent event = new SyncEvent(getYaw(), getPitch());
        EVENT_BUS.post(event);

        if (event.isCancelled()) info.cancel();
    }

    @Inject(method = "move", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/AbstractClientPlayerEntity;move(Lnet/minecraft/entity/MovementType;Lnet/minecraft/util/math/Vec3d;)V"), cancellable = true)
    public void onMoveHook(MovementType movementType, Vec3d movement, CallbackInfo ci) {
        MoveEvent event = new MoveEvent(movement.x, movement.y, movement.z, movement);
        EVENT_BUS.post(event);
        if (event.isCancelled()) {
            super.move(movementType, new Vec3d(event.getX(), event.getY(), event.getZ()));
            ci.cancel();
        }
    }
}