package me.coolmint.ngm.mixin;

import me.coolmint.ngm.event.Stage;
import me.coolmint.ngm.event.impl.EntityOutlineEvent;
import me.coolmint.ngm.event.impl.GameLeftEvent;
import me.coolmint.ngm.event.impl.TickEvent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static me.coolmint.ngm.util.traits.Util.EVENT_BUS;


@Mixin(value = MinecraftClient.class, priority = 1001)
public abstract class MixinMinecraftClient {

    @Shadow public ClientWorld world;


    @Shadow @Nullable public ClientPlayerInteractionManager interactionManager;

    @Shadow @Nullable public ClientPlayerEntity player;
    @Unique
    private boolean leftClick;
    @Unique
    private boolean rightClick;
    @Unique
    private boolean doAttackCalled;
    @Unique
    private boolean doItemUseCalled;
    @Shadow
    protected abstract void doItemUse();
    @Shadow
    protected abstract boolean doAttack();

    @Inject(method = "disconnect(Lnet/minecraft/client/gui/screen/Screen;)V", at = @At("HEAD"))
    private void onDisconnect(Screen screen, CallbackInfo info) {
        if (world != null) {
            EVENT_BUS.post(GameLeftEvent.get());
        }
    }

    @Inject(method = "hasOutline", at = @At(value = "HEAD"), cancellable = true)
    private void hookHasOutline(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        EntityOutlineEvent entityOutlineEvent = new EntityOutlineEvent(entity);
        EVENT_BUS.post(entityOutlineEvent);
        if (entityOutlineEvent.isCancelled()) {
            cir.cancel();
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "tick", at = @At(value = "HEAD"))
    private void hookTickPre(CallbackInfo ci) {
        doAttackCalled = false;
        doItemUseCalled = false;
        if (player != null && world != null) {
            TickEvent tickPreEvent = new TickEvent();
            //tickPreEvent.setStage(Stage.PRE);
            EVENT_BUS.post(tickPreEvent);
        }
        if (interactionManager == null) {
            return;
        }
        if (leftClick && !doAttackCalled) {
            doAttack();
        }
        if (rightClick && !doItemUseCalled) {
            doItemUse();
        }
        leftClick = false;
        rightClick = false;
    }

    @Inject(method = "tick", at = @At(value = "TAIL"))
    private void hookTickPost(CallbackInfo ci) {
        if (player != null && world != null) {
            TickEvent tickPostEvent = new TickEvent();
            //tickPostEvent.setStage(Stage.POST);
            EVENT_BUS.post(tickPostEvent);
        }
    }
}