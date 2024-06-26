package me.coolmint.ngm.features.modules.combat;

import com.google.common.eventbus.Subscribe;
import me.coolmint.ngm.Ngm;
import me.coolmint.ngm.event.impl.PacketEvent;
import me.coolmint.ngm.event.impl.SyncEvent;
import me.coolmint.ngm.features.modules.Module;
import me.coolmint.ngm.features.settings.Setting;
import me.coolmint.ngm.util.client.ChatUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.util.Hand;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Aura extends Module {
    public Setting<Boolean> onlyPlayer = register(new Setting<>("Only Player", false));
    public Setting<rotations> rotateMode = register(new Setting<>("Rotate Mode", rotations.Grim));
    public Setting<Float> attackRange = register(new Setting<>("Attack Range", 3.0f, 1.0f, 6.0f));
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
            List<Entity> entities = new ArrayList<>();
            mc.world.getEntities().forEach(Entity -> {
                entities.add(Entity);
            });
            players = entities.stream();
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
            return mc.player.distanceTo((Entity) entity) < (float)(this.attackRange.getValue() * 2) && entity != mc.player;
        }).collect(Collectors.toList());

        targets = (List) targets.stream().filter((entity) -> {
            return entity != null && ((LivingEntity)entity).getHealth() > 0.0F;
        }).collect(Collectors.toList());

        mc.player.getClass();
        targets.sort(Comparator.comparingDouble(mc.player::distanceTo));

        if(targets.isEmpty())
            return;

        target = (LivingEntity) targets.get(0);

        if (mc.player.getAttackCooldownProgress(0) >= 1) {
            //TODO: 로테이션 구현
            mc.interactionManager.attackEntity(mc.player, target);
            mc.player.swingHand(Hand.MAIN_HAND);
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
    public void onPacketReceive(PacketEvent.ReceivePRE e) {
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
