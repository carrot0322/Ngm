package me.coolmint.ngm.features.modules.misc;

import me.coolmint.ngm.features.modules.Module;
import me.coolmint.ngm.event.impl.PacketEvent;
import com.google.common.eventbus.Subscribe;
import me.coolmint.ngm.util.ChatUtil;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.world.GameMode;

public class GameModeNotifier extends Module {
    public GameModeNotifier() {
        super("GameMode Notifier", "", Module.Category.MISC, true, false, false);
    }

    @Subscribe
    public void onPacket(PacketEvent.Receive event) {
        if (this.mc.getNetworkHandler() == null) {
            return;
        }
        Packet<?> packet = event.packet;
        if (packet instanceof PlayerListS2CPacket packet2) {
            for (PlayerListS2CPacket.Entry entry : packet2.getEntries()) {
                for (PlayerListS2CPacket.Action action : packet2.getActions()) {
                    if (!action.equals(PlayerListS2CPacket.Action.UPDATE_GAME_MODE) || packet2.getPlayerAdditionEntries().contains(entry)) continue;
                    GameMode newGameMode = entry.gameMode();
                    String player = this.mc.getNetworkHandler().getPlayerListEntry(entry.profileId()).getProfile().getName();
                    ChatUtil.sendInfo(player + " has switched to " + newGameMode.getName() + " mode!");
                }
            }
        }
    }
}