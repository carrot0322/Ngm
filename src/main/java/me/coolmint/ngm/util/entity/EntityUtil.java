package me.coolmint.ngm.util.entity;

import me.coolmint.ngm.Ngm;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.mob.*;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.passive.SquidEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.entity.vehicle.ChestMinecartEntity;
import net.minecraft.entity.vehicle.FurnaceMinecartEntity;
import net.minecraft.entity.vehicle.MinecartEntity;

import java.util.Objects;

import static me.coolmint.ngm.util.traits.Util.mc;

public class EntityUtil {
    public static boolean isMonster(Entity e) {
        return e instanceof Monster;
    }

    public static boolean isNeutral(Entity e) {
        return e instanceof Angerable && !((Angerable) e).hasAngerTime();
    }

    public static boolean isPassive(Entity e) {
        return e instanceof PassiveEntity || e instanceof AmbientEntity
                || e instanceof SquidEntity;
    }

    public static boolean isVehicle(Entity e) {
        return e instanceof BoatEntity || e instanceof MinecartEntity
                || e instanceof FurnaceMinecartEntity
                || e instanceof ChestMinecartEntity;
    }

    public static boolean targetDead = false;
    public static boolean targetInvisible = true;
    public static boolean targetPlayer = true;
    public static boolean targetMobs = true;
    public static boolean targetAnimals = true;

    public static boolean isSelected(final Entity entity, final boolean canAttackCheck) {
        if (entity instanceof LivingEntity && (targetDead || entity.isAlive()) && entity != mc.player) {
            if (targetInvisible || !entity.isInvisible()) {
                if (targetPlayer && entity instanceof PlayerEntity) {
                    final PlayerEntity entityPlayer = (PlayerEntity) entity;

                    if (canAttackCheck) {
                        if (Ngm.friendManager.isFriend(entityPlayer))
                            return false;

                        if (entityPlayer.isSpectator())
                            return false;
                    }

                    return true;
                }

                return targetMobs && isMob(entity) || targetAnimals;

            }
        }
        return false;
    }

    public static boolean isMob(final Entity entity) {
        return entity instanceof MobEntity || entity instanceof VillagerEntity || entity instanceof SlimeEntity ||
                entity instanceof GhastEntity || entity instanceof EnderDragonEntity;
    }
}
