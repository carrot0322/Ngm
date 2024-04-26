package me.coolmint.ngm.features.modules.misc;

import com.google.common.eventbus.Subscribe;
import me.coolmint.ngm.features.modules.Module;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import me.coolmint.ngm.event.impl.PacketEvent;

public class XCarry extends Module {
    public XCarry() {
        super("XCarry", "", Module.Category.MISC, true, false, false);
    }

    @Subscribe
    public void onPacketSend(PacketEvent.Send e) {
        if (e.getPacket() instanceof CloseHandledScreenC2SPacket) e.cancel();
    }
}