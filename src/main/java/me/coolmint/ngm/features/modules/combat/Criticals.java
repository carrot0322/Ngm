package me.coolmint.ngm.features.modules.combat;

import com.google.common.eventbus.Subscribe;
import me.coolmint.ngm.event.impl.PacketEvent;
import me.coolmint.ngm.features.modules.Module;
import me.coolmint.ngm.util.models.Timer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

public class Criticals extends Module {
    private final Timer timer = new Timer();
    public static boolean cancelCrit;
    public Criticals() {
        super("Criticals", "", Category.COMBAT, true, false, false);
    }
    @Subscribe private void onPacketSend(PacketEvent.Send event) {
        if (event.getPacket() instanceof PlayerInteractEntityC2SPacket packet && packet.type.getType() == PlayerInteractEntityC2SPacket.InteractType.ATTACK) {
            Entity entity = mc.world.getEntityById(packet.entityId);
            if (cancelCrit || entity == null || entity instanceof EndCrystalEntity || !mc.player.isOnGround() || !(entity instanceof LivingEntity) || !timer.passedMs(0)) return;

            doCrit(entity);
        }
    }

    public void doCrit(Entity entity){
        mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() + (double) 0.1f, mc.player.getZ(), false));
        mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY(), mc.player.getZ(), false));
        mc.player.addCritParticles(entity);
        timer.reset();
    }

    @Override public String getDisplayInfo() {
        return "Packet";
    }
}
