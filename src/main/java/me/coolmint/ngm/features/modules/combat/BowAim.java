package me.coolmint.ngm.features.modules.combat;

import com.google.common.eventbus.Subscribe;
import me.coolmint.ngm.features.modules.Module;
import me.coolmint.ngm.features.settings.Setting;
import me.coolmint.ngm.util.entity.EntityUtil;
import me.coolmint.ngm.util.player.RotationUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.BowItem;

import java.util.List;
import java.util.stream.Collectors;

public class BowAim extends Module {
    private Setting<Boolean> throughWallsValue = register(new Setting<>("ThroughWalls", false));
    private Entity target = null;

    public BowAim() {
        super("BowAim", "", Category.COMBAT, true, false, false);
    }

    public void onDisable() {
        target = null;
    }

    @Override
    public void onUpdate() {
        target = null;

        if (mc.player.isUsingItem() && mc.player.getActiveItem().getItem() instanceof BowItem) {
            Entity entity = getTarget(throughWallsValue.getValue());
            if (entity != null) {
                target = entity;
                RotationUtils.faceBow(target, true, false, 5f);
            }
        }
    }

    private Entity getTarget(boolean throughWalls) {
        List<Entity> targets = mc.world.getPlayers().stream()
                .filter(entity -> entity instanceof LivingEntity &&
                        EntityUtil.isSelected((LivingEntity) entity, true) &&
                        (throughWalls || mc.player.canSee(entity)))
                .collect(Collectors.toList());

        return targets.stream()
                .min((entity1, entity2) -> {
                    double dist1 = mc.player.distanceTo(entity1);
                    double dist2 = mc.player.distanceTo(entity2);
                    return Double.compare(dist1, dist2);
                })
                .orElse(null);
    }

    public boolean hasTarget() {
        return target != null && mc.player.canSee(target);
    }
}
