package me.coolmint.ngm.util.player;

import me.coolmint.ngm.Ngm;
import me.coolmint.ngm.mixin.IInteractionManager;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PickaxeItem;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;

import static me.coolmint.ngm.util.traits.Util.mc;

public class InventoryUtil {
    public static SearchInvResult getPickAxeHotbar() {
        if (mc.player == null) return SearchInvResult.notFound();

        int slot = -1;
        float f = 1.0F;
        for (int b1 = 0; b1 < 9; b1++) {
            ItemStack itemStack = mc.player.getInventory().getStack(b1);
            if (itemStack != null && itemStack.getItem() instanceof PickaxeItem) {
                float f1 = 0;
                f1 += EnchantmentHelper.getLevel(Enchantments.EFFICIENCY, itemStack);
                if (f1 > f) {
                    f = f1;
                    slot = b1;
                }
            }
        }

        if (slot == -1) return SearchInvResult.notFound();
        return new SearchInvResult(slot, true, mc.player.getInventory().getStack(slot));
    }

    public static SearchInvResult getPickAxeHotBar() {
        if (mc.player == null) return SearchInvResult.notFound();

        int slot = -1;
        float f = 1.0F;
        for (int b1 = 0; b1 < 9; b1++) {
            ItemStack itemStack = mc.player.getInventory().getStack(b1);
            if (itemStack != null && itemStack.getItem() instanceof PickaxeItem) {
                float f1 = 0;
                f1 += EnchantmentHelper.getLevel(Enchantments.EFFICIENCY, itemStack);
                if (f1 > f) {
                    f = f1;
                    slot = b1;
                }
            }
        }

        if (slot == -1) return SearchInvResult.notFound();
        return new SearchInvResult(slot, true, mc.player.getInventory().getStack(slot));
    }

    public static void switchTo(int slot) {
        if (mc.player == null || mc.getNetworkHandler() == null) return;
        if (mc.player.getInventory().selectedSlot == slot && Ngm.playerManager.serverSideSlot == slot)
            return;
        mc.player.getInventory().selectedSlot = slot;
        ((IInteractionManager) mc.interactionManager).syncSlot();
    }

    public static void switchToSilent(int slot) {
        if (mc.player == null || mc.getNetworkHandler() == null) return;
        mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(slot));
    }
}
