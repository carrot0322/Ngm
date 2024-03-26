package me.coolmint.ngm.features.modules.combat;

import com.google.common.eventbus.Subscribe;
import me.coolmint.ngm.event.impl.GameLeftEvent;
import me.coolmint.ngm.features.modules.Module;
import me.coolmint.ngm.features.settings.Setting;
import me.coolmint.ngm.util.entity.EntityFilterList;
import me.coolmint.ngm.util.entity.EntityUtils;
import me.coolmint.ngm.util.player.RotationUtils;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Hand;

import java.util.*;
import java.util.function.ToDoubleFunction;
import java.util.stream.Stream;

public class TpAura extends Module {
    private final Setting<Integer> attackTime = register(new Setting<>("AttackTime", 1, 1, 20));
    private final Setting<Float> range = register(new Setting<>("Range", 3.2f, 1.0f, 6.0f));
    private final Setting<Priority> priority = register(new Setting<>("Priority", Priority.HEALTH));
    private final Setting<Boolean> autoDisable = register(new Setting<>("Auto Disable", true));

    public TpAura() {
        super("TpAura", "", Category.COMBAT, true, false, false);
    }

    private int tickTimer;

    public void resetTimer()
    {
        tickTimer = 0;
    }

    public void updateTimer()
    {
        tickTimer += 50;
    }

    public boolean isTimeToAttack()
    {
        if(attackTime.getValue() > 0)
            return tickTimer >= 1000 / attackTime.getValue();

        return mc.player.getAttackCooldownProgress(0) >= 1;
    }

    private final Random random = new Random();

    private final EntityFilterList entityFilters = EntityFilterList.genericCombat();

    @Override
    public void onUpdate() {
        assert mc.player != null;

        updateTimer();
        if (!isTimeToAttack())
            return;

        ClientPlayerEntity player = mc.player;

        // set entity
        Stream<Entity> stream = EntityUtils.getAttackableEntities();
        double rangeSq = Math.pow(range.getValue(), 2);
        stream = stream.filter(e -> mc.player.squaredDistanceTo(e) <= rangeSq);

        stream = entityFilters.applyTo(stream);

        Entity entity = stream.min(priority.getValue().comparator).orElse(null);
        if (entity == null || entity == mc.player)
            return;

        // teleport
        player.setPosition(entity.getX() + random.nextInt(3) * 2 - 2,
                entity.getY(), entity.getZ() + random.nextInt(3) * 2 - 2);

        // check cooldown
        if (player.getAttackCooldownProgress(0) < 1)
            return;

        // attack entity
        RotationUtils.getNeededRotations(entity.getBoundingBox().getCenter()).sendPlayerLookPacket();

        mc.interactionManager.attackEntity(player, entity);
        player.swingHand(Hand.MAIN_HAND);
        resetTimer();
    }

    private enum Priority
    {
        DISTANCE("Distance", e -> mc.player.squaredDistanceTo(e)),

        ANGLE("Angle",
                e -> RotationUtils
                        .getAngleToLookVec(e.getBoundingBox().getCenter())),

        HEALTH("Health", e -> e instanceof LivingEntity
                ? ((LivingEntity)e).getHealth() : Integer.MAX_VALUE);

        private final String name;
        private final Comparator<Entity> comparator;

        private Priority(String name, ToDoubleFunction<Entity> keyExtractor)
        {
            this.name = name;
            comparator = Comparator.comparingDouble(keyExtractor);
        }

        @Override
        public String toString()
        {
            return name;
        }
    }
    @Subscribe
    private void onGameLeft(GameLeftEvent event) {
        if (autoDisable.getValue()) toggle();
    }
}
