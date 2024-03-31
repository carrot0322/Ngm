package me.coolmint.ngm.features.modules.combat;

import me.coolmint.ngm.features.modules.Module;
import me.coolmint.ngm.features.settings.Setting;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;

public class KillAura extends Module {

    private final Setting<Integer> range = register(new Setting<>("Range", 3, 0, 6));
    public KillAura() {
        super("KillAura", "", Category.COMBAT, true, false, false);
    }

    @Override
    public void onTick() {
        if (fullNullCheck()) return;
        if (getClosetPlayer() == null) return;
        if (mc.player.getAttackCooldownProgress(0) >= 1) {
            mc.interactionManager.attackEntity(mc.player, getClosetPlayer());
            mc.player.swingHand(Hand.MAIN_HAND);
        }
        super.onTick();
    }


    public PlayerEntity getClosetPlayer() {
        for (PlayerEntity e : mc.world.getPlayers()) {
            if (!(e instanceof ClientPlayerEntity) && e.distanceTo(mc.player) <= range.getValue() && e.isAlive()) return e;
        }
        return null;
    }
}
