package me.coolmint.ngm.manager;

import me.coolmint.ngm.mixin.IClientWorld;
import net.minecraft.client.network.PendingUpdateManager;
import net.minecraft.client.network.SequencedPacketCreator;
import net.minecraft.network.listener.ServerPlayPacketListener;
import net.minecraft.network.packet.Packet;

import static me.coolmint.ngm.features.modules.Module.fullNullCheck;
import static me.coolmint.ngm.util.traits.Util.mc;

public class NetworkManager {
    public void sendSequencedPacket(final SequencedPacketCreator p) {
        if (fullNullCheck()) {
            PendingUpdateManager updater =
                    ((IClientWorld) mc.world).hookGetPendingUpdateManager().incrementSequence();
            try {
                int i = updater.getSequence();
                Packet<ServerPlayPacketListener> packet = p.predict(i);
                mc.getNetworkHandler().sendPacket(packet);
            } catch (Throwable e) {
                e.printStackTrace();
                if (updater != null) {
                    try {
                        updater.close();
                    } catch (Throwable e1) {
                        e1.printStackTrace();
                        e.addSuppressed(e1);
                    }
                }
                throw e;
            }
            if (updater != null) {
                updater.close();
            }
        }
    }
}
