package me.coolmint.ngm.mixin;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.TimeoutException;
import me.coolmint.ngm.Ngm;
import me.coolmint.ngm.event.impl.PacketEvent;
import me.coolmint.ngm.features.modules.misc.AntiPacketKick;
import me.coolmint.ngm.util.ChatUtil;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.PacketCallbacks;
import net.minecraft.network.handler.PacketEncoderException;
import net.minecraft.network.packet.Packet;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static me.coolmint.ngm.util.traits.Util.EVENT_BUS;

@Mixin( ClientConnection.class )
public class MixinClientConnection {

    @Shadow private Channel channel;
    @Shadow @Final private NetworkSide side;

    @Inject(method = "channelRead0", at = @At("HEAD"), cancellable = true)
    public void channelRead0(ChannelHandlerContext chc, Packet<?> packet, CallbackInfo ci) {
        if (this.channel.isOpen() && packet != null) {
            try {
                PacketEvent.ReceivePRE event = new PacketEvent.ReceivePRE(packet);
                EVENT_BUS.post(event);
                if (event.isCancelled())
                    ci.cancel();
            } catch (Exception e) {
            }
        }
    }

    @Inject(method = "sendImmediately", at = @At("HEAD"), cancellable = true)
    private void sendImmediately(Packet<?> packet, PacketCallbacks callbacks, boolean flush, CallbackInfo ci) {
        if (this.side != NetworkSide.CLIENTBOUND) return;
        try {
            PacketEvent.SendPRE event = new PacketEvent.SendPRE(packet);
            EVENT_BUS.post(event);
            if (event.isCancelled()) ci.cancel();
        } catch (Exception e) {
        }
    }

    @Inject(method = "exceptionCaught", at = @At("HEAD"), cancellable = true)
    private void exceptionCaught(ChannelHandlerContext context, Throwable throwable, CallbackInfo ci) {
        AntiPacketKick apk = Ngm.moduleManager.getModuleByClass(AntiPacketKick.class);
        if (!(throwable instanceof TimeoutException) && !(throwable instanceof PacketEncoderException) && apk.catchExceptions()) {
            if (apk.logExceptions.getValue()) ChatUtil.sendWarning("Caught exception: " + throwable);
            ci.cancel();
        }
    }
}