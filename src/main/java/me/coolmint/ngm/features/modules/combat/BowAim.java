package me.coolmint.ngm.features.modules.combat;

import com.google.common.eventbus.Subscribe;
import me.coolmint.ngm.event.impl.Render3DEvent;
import me.coolmint.ngm.features.modules.Module;
import me.coolmint.ngm.features.settings.Setting;
import me.coolmint.ngm.util.entity.EntityUtil;
import me.coolmint.ngm.util.entity.EntityUtils;
import me.coolmint.ngm.util.entity.SortPriority;
import me.coolmint.ngm.util.player.InvUtils;
import me.coolmint.ngm.util.player.PlayerUtils;
import me.coolmint.ngm.util.player.Rotation;
import me.coolmint.ngm.util.player.RotationUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArrowItem;
import net.minecraft.item.Items;
import net.minecraft.util.math.Vec3d;

public class BowAim extends Module {
    private Setting<Integer> range = register(new Setting<>("Range", 50, 0, 100));
    private Setting<SortPriority> priority = register(new Setting<>("priority", SortPriority.LowestDistance));
    private Setting<Boolean> babies = register(new Setting<>("babies", true));
    private Setting<Boolean> nametagged = register(new Setting<>("nametagged", false));
    private Setting<Boolean> pauseOnCombat = register(new Setting<>("pauseOnCombat", false));

    public BowAim() {
        super("BowAim", "", Category.COMBAT, true, false, false);
    }

    private boolean wasPathing;
    private Entity target;

    @Override
    public void onDisable() {
        target = null;
        wasPathing = false;
    }

    /*
    @Subscribe
    private void onRender(Render3DEvent event) {
        if (!PlayerUtils.isAlive() || !itemInHand()) return;
        if (!mc.player.getAbilities().creativeMode && !InvUtils.find(itemStack -> itemStack.getItem() instanceof ArrowItem).found()) return;

        target = TargetUtils.get(entity -> {
            if (entity == mc.player || entity == mc.cameraEntity) return false;
            if ((entity instanceof LivingEntity && ((LivingEntity) entity).isDead()) || !entity.isAlive()) return false;
            if (!PlayerUtils.isWithin(entity, range.getValue())) return false;
            if (!entities.get().contains(entity.getType())) return false;
            if (!nametagged.getValue() && entity.hasCustomName()) return false;
            if (!PlayerUtils.canSeeEntity(entity)) return false;
            if (entity instanceof PlayerEntity) {
                if (((PlayerEntity) entity).isCreative()) return false;
                if (!Friends.get().shouldAttack((PlayerEntity) entity)) return false;
            }
            return !(entity instanceof AnimalEntity) || babies.getValue() || !((AnimalEntity) entity).isBaby();
        }, priority.getValue());

        if (target == null) {
            if (wasPathing) {
                PathManagers.get().resume();
                wasPathing = false;
            }
            return;
        }

        if (mc.options.useKey.isPressed() && itemInHand()) {
            if (pauseOnCombat.getValue() && PathManagers.get().isPathing() && !wasPathing) {
                PathManagers.get().pause();
                wasPathing = true;
            }
            aim(event.tickDelta);
        }
    }
     */

    private boolean itemInHand() {
        return InvUtils.testInMainHand(Items.BOW, Items.CROSSBOW);
    }

    private void aim(double tickDelta) {
        // Velocity based on bow charge.
        float velocity = (mc.player.getItemUseTime() - mc.player.getItemUseTimeLeft()) / 20f;
        velocity = (velocity * velocity + velocity * 2) / 3;
        if (velocity > 1) velocity = 1;

        // Positions
        double posX = target.getPos().getX() + (target.getPos().getX() - target.prevX) * tickDelta;
        double posY = target.getPos().getY() + (target.getPos().getY() - target.prevY) * tickDelta;
        double posZ = target.getPos().getZ() + (target.getPos().getZ() - target.prevZ) * tickDelta;

        // Adjusting for hitbox heights
        posY -= 1.9f - target.getHeight();

        double relativeX = posX - mc.player.getX();
        double relativeY = posY - mc.player.getY();
        double relativeZ = posZ - mc.player.getZ();

        // Calculate the pitch
        double hDistance = Math.sqrt(relativeX * relativeX + relativeZ * relativeZ);
        double hDistanceSq = hDistance * hDistance;
        float g = 0.006f;
        float velocitySq = velocity * velocity;
        float pitch = (float) -Math.toDegrees(Math.atan((velocitySq - Math.sqrt(velocitySq * velocitySq - g * (g * hDistanceSq + 2 * relativeY * velocitySq))) / (g * hDistance)));

        // Set player rotation
        if (Float.isNaN(pitch)) {
            RotationUtils.rotate(RotationUtils.getYaw(target), RotationUtils.getPitch(target));
        } else {
            RotationUtils.rotate(RotationUtils.getYaw(new Vec3d(posX, posY, posZ)), pitch);
        }
    }
}
