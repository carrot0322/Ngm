package me.coolmint.ngm.mixin;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.coolmint.ngm.Ngm;
import me.coolmint.ngm.event.impl.ChatEvent;
import me.coolmint.ngm.event.impl.GameLeftEvent;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.EnterReconfigurationS2CPacket;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static me.coolmint.ngm.features.Feature.fullNullCheck;
import static me.coolmint.ngm.util.traits.Util.EVENT_BUS;

@Mixin(ClientPlayNetworkHandler.class)
public class MixinClientPlayNetworkHandler {
    @Inject(method = "sendChatMessage", at = @At("HEAD"), cancellable = true)
    private void sendChatMessageHook(String content, CallbackInfo ci) {
        ChatEvent event = new ChatEvent(content);
        EVENT_BUS.post(event);
        if (event.isCancelled()) ci.cancel();
    }

    @Inject(method = "sendChatMessage", at = @At("HEAD"), cancellable = true)
    private void onSendChatMessage(@NotNull String message, CallbackInfo ci) {
        if (message.startsWith(Ngm.commandManager.getPrefix())) {
            try {
                Ngm.commandManager.getDispatcher().execute(
                        message.substring(Ngm.commandManager.getPrefix().length()),
                        Ngm.commandManager.getSource()
                );
            } catch (CommandSyntaxException ignored) {}

            ci.cancel();
        }
    }

    @Inject(method = "onGameJoin", at = @At("TAIL"))
    private void onGameJoinTail(GameJoinS2CPacket packet, CallbackInfo info) {
        if (fullNullCheck()) {
            EVENT_BUS.post(GameLeftEvent.get());
        }
    }

    @Inject(method = "onEnterReconfiguration", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/NetworkThreadUtils;forceMainThread(Lnet/minecraft/network/packet/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/util/thread/ThreadExecutor;)V", shift = At.Shift.AFTER))
    private void onEnterReconfiguration(EnterReconfigurationS2CPacket packet, CallbackInfo info) {
        EVENT_BUS.post(GameLeftEvent.get());
    }
}