package me.coolmint.ngm.features.modules.combat;

import com.google.common.eventbus.Subscribe;
import me.coolmint.ngm.features.modules.Module;
import me.coolmint.ngm.event.impl.EventSync;
import me.coolmint.ngm.features.settings.Setting;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import me.coolmint.ngm.util.player.PlayerUtility;

public class BowSpam extends Module {
    private final Setting<Integer> ticks = register(new Setting<>("Delay", 3, 0, 20));

    private static BowSpam instance;

    public BowSpam() {
        super("BowSpam", "", Module.Category.COMBAT, true, false, false);
    }

    @Subscribe
    public void onSync(EventSync event) {
        if ((mc.player.getOffHandStack().getItem() == Items.BOW || mc.player.getMainHandStack().getItem() == Items.BOW) && mc.player.isUsingItem()) {
            if (mc.player.getItemUseTime() >= this.ticks.getValue()) {
                mc.player.networkHandler.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, mc.player.getHorizontalFacing()));
                mc.player.networkHandler.sendPacket(new PlayerInteractItemC2SPacket(mc.player.getOffHandStack().getItem() == Items.BOW ? Hand.OFF_HAND : Hand.MAIN_HAND, PlayerUtility.getWorldActionId(mc.world)));
                mc.player.stopUsingItem();
            }
        }
    }

    public static BowSpam getInstance() {
        return instance;
    }
}