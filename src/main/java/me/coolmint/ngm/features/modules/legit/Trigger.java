package me.coolmint.ngm.features.modules.legit;

import me.coolmint.ngm.features.modules.Module;
import me.coolmint.ngm.features.settings.Setting;
import me.coolmint.ngm.util.client.MathUtil;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;

public class Trigger extends Module {
    public Setting<Integer> chance = register(new Setting<>("Chance", 100, 0, 100));
    public Setting<Boolean> SwordOnly = register(new Setting<>("Sword Only", false));

    public Trigger() {
        super("Trigger", "", Category.LEGIT, true, false, false);
    }

    @Override
    public void onUpdate() {
        if (fullNullCheck() && !mc.isWindowFocused()) {
            return;
        }
        if (SwordOnly.getValue() && !SwordCheck()) {
            return;
        }

        if (mc.crosshairTarget instanceof EntityHitResult entityHitResult) {
            if (MathUtil.getRandom(0, 100) <= chance.getValue() && mc.player.getAttackCooldownProgress(0.5f) == 1) {
                Entity entity = entityHitResult.getEntity();
                attack(entity);
            }
        }
    }

    private void attack(Entity entity) {
        mc.interactionManager.attackEntity(mc.player, entity);
        mc.player.swingHand(Hand.MAIN_HAND);
    }
    public boolean SwordCheck() {
        ItemStack getItem = mc.player.getMainHandStack();
        return (getItem.isOf(Items.NETHERITE_SWORD) || getItem.isOf(Items.DIAMOND_SWORD) || getItem.isOf(Items.GOLDEN_SWORD) || getItem.isOf(Items.IRON_SWORD) || getItem.isOf(Items.STONE_SWORD) || getItem.isOf(Items.WOODEN_SWORD));
    }
}