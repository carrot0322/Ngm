package me.coolmint.ngm.features.modules.legit;

import me.coolmint.ngm.Ngm;
import me.coolmint.ngm.features.modules.Module;
import me.coolmint.ngm.features.settings.Setting;
import me.coolmint.ngm.util.MathUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.mob.MagmaCubeEntity;
import net.minecraft.entity.mob.SlimeEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;

public class Trigger extends Module {
    public Setting<TargetMode> target = register(new Setting<>("Target", TargetMode.PLAYER));
    public Setting<Integer> chance = register(new Setting<>("Chance", 100, 0, 100));

    enum TargetMode {
        PLAYER,
        CRYSTAL,
        ALL
    }

    public Trigger() {
        super("Trigger", "", Category.LEGIT, true, false, false);
    }

    @Override
    public void onUpdate(){
        if(fullNullCheck() && !mc.isWindowFocused()) return;

        if (mc.crosshairTarget instanceof EntityHitResult entityHitResult) {
            if (MathUtil.random(0, 100) <= chance.getValue() && mc.player.getAttackCooldownProgress(0.5f) == 1f) {
                Entity entity = entityHitResult.getEntity();
                attack(entity);
            }
        }
    }

    private void attack(Entity entity){
        mc.interactionManager.attackEntity(mc.player, entity);
        mc.player.swingHand(Hand.MAIN_HAND);
    }
}
