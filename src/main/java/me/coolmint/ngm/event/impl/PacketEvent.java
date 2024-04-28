package me.coolmint.ngm.event.impl;

import me.coolmint.ngm.event.Event;
import net.minecraft.network.packet.Packet;

public class PacketEvent extends Event {
    public final Packet<?> packet;

    public PacketEvent(Packet<?> packet) {
        this.packet = packet;
    }

    public <T extends Packet<?>> T getPacket() {
        return (T) this.packet;
    }

    public static class SendPRE extends PacketEvent {
        public SendPRE(Packet<?> packet) {
            super(packet);
        }
    }

    public static class ReceivePRE extends PacketEvent {
        public ReceivePRE(Packet<?> packet) {
            super(packet);
        }
    }

    public static class SendPost extends PacketEvent {
        public SendPost(Packet<?> packet) {
            super(packet);
        }
    }

    public static class ReceivePost extends PacketEvent {
        public ReceivePost(Packet<?> packet) {
            super(packet);
        }
    }
}