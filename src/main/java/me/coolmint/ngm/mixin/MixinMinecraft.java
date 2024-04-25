package me.coolmint.ngm.mixin;

import me.coolmint.ngm.event.impl.AttackEvent;
import me.coolmint.ngm.event.impl.GameLeftEvent;
import me.coolmint.ngm.event.impl.TickEvent;
import me.coolmint.ngm.features.gui.fonts.FontRenderers;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.RunArgs;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.awt.*;
import java.io.IOException;

import static me.coolmint.ngm.util.traits.Util.EVENT_BUS;

@Mixin(MinecraftClient.class)
public class MixinMinecraft {
    @Shadow
    public ClientWorld world;

    @Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;setOverlay(Lnet/minecraft/client/gui/screen/Overlay;)V", shift = At.Shift.BEFORE))
    public void init(RunArgs args, CallbackInfo ci) {
        try {
            FontRenderers.Main = FontRenderers.createDefault(16f, "Main");
            FontRenderers.Hud = FontRenderers.createDefault(20f, "Lexend");
        } catch (IOException | FontFormatException e) {
            e.printStackTrace();
        }
    }

    @Inject(at = @At("HEAD"), method = "tick")
    private void onPreTick(CallbackInfo info) {
        EVENT_BUS.post(TickEvent.Pre.get());
    }

    @Inject(at = @At("TAIL"), method = "tick")
    private void onTick(CallbackInfo info) {
        EVENT_BUS.post(TickEvent.Post.get());
    }

    @Inject(method = "disconnect(Lnet/minecraft/client/gui/screen/Screen;)V", at = @At("HEAD"))
    private void onDisconnect(Screen screen, CallbackInfo info) {
        if (world != null) {
            EVENT_BUS.post(GameLeftEvent.get());
        }
    }

    @Inject(method = "doAttack", at = @At("HEAD"), cancellable = true)
    private void doAttackHook(CallbackInfoReturnable<Boolean> cir) {
        final AttackEvent event = new AttackEvent(null, true);
        EVENT_BUS.post(event);
        if (event.isCancelled()) {
            cir.setReturnValue(false);
        }
    }
}