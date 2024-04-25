package me.coolmint.ngm.features.modules.combat;

import com.google.common.eventbus.Subscribe;
import me.coolmint.ngm.Ngm;
import me.coolmint.ngm.event.impl.PacketEvent;
import me.coolmint.ngm.event.impl.SyncEvent;
import me.coolmint.ngm.features.modules.Module;
import me.coolmint.ngm.features.settings.Setting;
import me.coolmint.ngm.util.ChatUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.util.Hand;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Aura extends Module {
    public Setting<Boolean> onlyPlayer = register(new Setting<>("Only Player", false));
    public Setting<Float> attackRange = register(new Setting<>("Attack Range", 3.00f, 2.00f, 6.00f));
    public Setting<Float> rotateRange = register(new Setting<>("Rotate Range", 4.2f, 3.0f, 12.0f));
    public Setting<rotations> rotateMode = register(new Setting<>("Rotate Mode", rotations.Grim));
    public Setting<Float> targetRange = register(new Setting<>("Target Range", 8.0f, 6.0f, 12.0f));
    public Setting<Boolean> tpDisable = register(new Setting<>("Disable on tp", false));
    public Setting<Boolean> deathDisable = register(new Setting<>("Disable on death", true));

    enum rotations{
        Grim
    }

    public Aura() {
        super("Aura", "", Category.COMBAT, true, false, false);
    }

    public static LivingEntity target;
    public float rotationYaw, rotationPitch;

    @Override
    public void onEnable(){
        rotationYaw = mc.player.getYaw();
        rotationPitch = mc.player.getPitch();
    }

    @Override
    public void onTick(){
        Stream players;
        List targets;
        if (this.onlyPlayer.getValue()) {
            players = mc.world.getPlayers().stream();
            PlayerEntity.class.getClass();
            targets = (List)players.filter(PlayerEntity.class::isInstance).collect(Collectors.toList());
        } else {
            players = mc.world.getPlayers().stream();
            LivingEntity.class.getClass();
            targets = (List)players.filter(LivingEntity.class::isInstance).collect(Collectors.toList());
        }

        targets = (List)targets.stream().filter((entity) -> {
            if (entity instanceof PlayerEntity) {
                return !Ngm.friendManager.isFriend((PlayerEntity)entity);
            } else {
                return true;
            }
        }).collect(Collectors.toList());

        targets = (List) targets.stream().filter((entity) -> {
            return mc.player.distanceTo((Entity) entity) < (float)(this.targetRange.getValue() * 2) && entity != mc.player;
        }).collect(Collectors.toList());

        targets = (List) targets.stream().filter((entity) -> {
            return entity != null && ((LivingEntity)entity).getHealth() > 0.0F;
        }).collect(Collectors.toList());

        mc.player.getClass();
        targets.sort(Comparator.comparingDouble(mc.player::distanceTo));

        if(targets.isEmpty())
            return;

        target = (LivingEntity) targets.get(0);

        float distance = mc.player.distanceTo(target);

        if(distance <= targetRange.getValue()){
            /*
            if(distance <= rotateRange.getValue()){
                switch (rotateMode.getValue()){
                    case Grim -> mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.Full(mc.player.getX(), mc.player.getY(), mc.player.getZ(), rotationYaw, rotationPitch, mc.player.isOnGround()));
                }
            }
             */

            if(distance <= attackRange.getValue()){
                if (mc.player.getAttackCooldownProgress(0) >= 1) {
                    mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.Full(mc.player.getX(), mc.player.getY(), mc.player.getZ(), rotationYaw, rotationPitch, mc.player.isOnGround()));
                    mc.interactionManager.attackEntity(mc.player, target);
                    mc.player.swingHand(Hand.MAIN_HAND);
                }
            }
        }
    }

    @Subscribe
    public void onSync(SyncEvent e){
        if(target != null && rotateMode.getValue() != rotations.Grim){
            mc.player.setYaw(rotationYaw);
            mc.player.setPitch(rotationPitch);
        } else {
            rotationYaw = mc.player.getYaw();
            rotationPitch = mc.player.getPitch();
        }
    }

    @Subscribe
    public void onPacketReceive(PacketEvent.Receive e) {
        if (e.getPacket() instanceof PlayerPositionLookS2CPacket && tpDisable.getValue()) {
            ChatUtil.sendInfo("Aura has been disabled due tp");
            disable();
        }

        if (e.getPacket() instanceof EntityStatusS2CPacket pac && pac.getStatus() == 3 && pac.getEntity(mc.world) == mc.player && deathDisable.getValue()) {
            ChatUtil.sendInfo("Aura has been disabled due death");
            disable();
        }
    }
}
