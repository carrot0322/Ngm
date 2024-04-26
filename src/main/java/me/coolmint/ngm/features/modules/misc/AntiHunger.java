package me.coolmint.ngm.features.modules.misc;

import com.google.common.eventbus.Subscribe;
import me.coolmint.ngm.features.modules.Module;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import org.jetbrains.annotations.NotNull;
import me.coolmint.ngm.event.impl.PacketEvent;
import me.coolmint.ngm.mixin.IPlayerMoveC2SPacket;

public class AntiHunger extends Module {
    public AntiHunger() {
        super("AntiHunger", "", Category.MISC, true, false, false);
    }

    @Subscribe
    public void onPacketSend(PacketEvent.@NotNull Send e) {
        if (e.getPacket() instanceof PlayerMoveC2SPacket pac) {
            ((IPlayerMoveC2SPacket) pac).setOnGround(false);
        }

        if (e.getPacket() instanceof ClientCommandC2SPacket pac) {
            if (pac.getMode() == ClientCommandC2SPacket.Mode.START_SPRINTING) {
                e.cancel();
                mc.player.setSprinting(false);
            }
        }
    }
}