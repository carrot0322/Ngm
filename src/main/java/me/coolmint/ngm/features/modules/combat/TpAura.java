package me.coolmint.ngm.features.modules.combat;

import me.coolmint.ngm.Ngm;
import me.coolmint.ngm.features.modules.Module;
import me.coolmint.ngm.features.settings.Setting;
import me.coolmint.ngm.util.models.Timer;
import me.coolmint.ngm.util.player.EntityUtil;
import me.coolmint.ngm.util.player.PositionUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.SwordItem;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.Hand;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TpAura extends Module {
    private enum tpType{
        Full,
        Semi
    }
    private enum swingType {
        Full,
        Packet,
        None
    }

    private Setting<Integer> max_range = register(new Setting<>("Max Range", 20, 0, 100));
    private Setting<Boolean> only = register(new Setting<>("OnlyPlayer", true));
    private Setting<Boolean> swordOnly = register(new Setting<>("Sword Only", false));
    private Setting<Boolean> motion = register(new Setting<>("Motion", false));
    private Setting<tpType> type = register(new Setting<>("TPType", tpType.Semi));
    private Setting<Boolean> onGround = register(new Setting<>("onGround", false));
    private Setting<swingType> swing = register(new Setting<>("Swing", swingType.Full));

    public TpAura() {
        super("TpAura", "", Category.COMBAT, true, false, false);
    }

    private static LivingEntity target;
    private final Timer timerUtil = new Timer();

    public void onTick() {
        if (!(Boolean)this.swordOnly.getValue() || mc.player.getInventory().getStack(mc.player.getInventory().selectedSlot).getItem() instanceof SwordItem) {
            Stream var10000;
            List targets;
            if ((Boolean)this.only.getValue()) {
                var10000 = mc.world.getPlayers().stream();
                PlayerEntity.class.getClass();
                targets = (List)var10000.filter(PlayerEntity.class::isInstance).collect(Collectors.toList());
            } else {
                var10000 = mc.world.getPlayers().stream();
                LivingEntity.class.getClass();
                targets = (List)var10000.filter(LivingEntity.class::isInstance).collect(Collectors.toList());
            }

            targets = (List)targets.stream().filter((entity) -> {
                if (entity instanceof PlayerEntity) {
                    return !Ngm.friendManager.isFriend((PlayerEntity)entity);
                } else {
                    return true;
                }
            }).collect(Collectors.toList());
            targets = (List)targets.stream().filter((entity) -> {
                return mc.player.distanceTo((Entity) entity) < (float)((Integer)this.max_range.getValue() * 2) && entity != mc.player;
            }).collect(Collectors.toList());
            targets = (List)targets.stream().filter((entity) -> {
                return entity != null && ((LivingEntity)entity).getHealth() > 0.0F;
            }).collect(Collectors.toList());
            mc.player.getClass();
            targets.sort(Comparator.comparingDouble(mc.player::distanceTo));
            if (!targets.isEmpty()) {
                target = (LivingEntity)targets.get(0);
                if (mc.player.getAttackCooldownProgress(0.5f) == 1f) {
                    this.move(target, () -> {
                        mc.interactionManager.attackEntity(mc.player, target);
                        switch (this.swing.getValue()) {
                            case Full:
                                mc.player.swingHand(Hand.MAIN_HAND);
                                break;
                            case Packet:
                                mc.player.networkHandler.sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
                        }

                    });
                    this.timerUtil.reset();
                }
            }

        }
    }

    public void move(LivingEntity target, Runnable attack) {
        PositionUtil pos = new PositionUtil(mc.player.getX(), mc.player.getY(), mc.player.getZ());
        byte var5;
        if ((Boolean)this.motion.getValue()) {
            EntityUtil.motion(target.getX(), target.getY(), target.getZ());
        } else {
            switch (this.type.getValue()) {
                case Full:
                    mc.player.refreshPositionAfterTeleport(target.getX(), target.getY(), target.getZ());
                    break;
                case Semi:
                    mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(target.getX(), target.getY(), target.getZ(), (Boolean)this.onGround.getValue()));
            }
        }

        attack.run();
        if ((Boolean)this.motion.getValue()) {
            EntityUtil.motion(pos.getX(), pos.getY(), pos.getZ());
        } else {
            switch (this.type.getValue()) {
                case Full:
                    mc.player.refreshPositionAfterTeleport(pos.getX(), pos.getY(), pos.getZ());
                    break;
                case Semi:
                    mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(pos.getX(), pos.getY(), pos.getZ(), (Boolean)this.onGround.getValue()));
            }
        }

    }
}