package me.coolmint.ngm.util.client;

import me.coolmint.ngm.mixin.IClientWorldMixin;
import net.minecraft.client.network.PendingUpdateManager;
import net.minecraft.client.network.SequencedPacketCreator;

import static me.coolmint.ngm.util.traits.Util.mc;

public class NetworkUtil {
    public static void sendSequencedPacket(SequencedPacketCreator packetCreator) {
        if (mc.getNetworkHandler() == null || mc.world == null) return;
        try (PendingUpdateManager pendingUpdateManager = ((IClientWorldMixin) mc.world).acquirePendingUpdateManager().incrementSequence();){
            int i = pendingUpdateManager.getSequence();
            mc.getNetworkHandler().sendPacket(packetCreator.predict(i));
        }
    }
}
