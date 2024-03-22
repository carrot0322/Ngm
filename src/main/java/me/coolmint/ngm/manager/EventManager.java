package me.coolmint.ngm.manager;

import com.google.common.eventbus.Subscribe;
import me.coolmint.ngm.Ngm;
import me.coolmint.ngm.event.Stage;
import me.coolmint.ngm.event.impl.*;
import me.coolmint.ngm.features.Feature;
import me.coolmint.ngm.util.models.Timer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.WorldTimeUpdateS2CPacket;

public class EventManager extends Feature {
    private final Timer logoutTimer = new Timer();

    public void init() {
        EVENT_BUS.register(this);
    }

    public void onUnload() {
        EVENT_BUS.unregister(this);
    }

    @Subscribe
    public void onUpdate(UpdateEvent event) {
        // mc.getWindow().setTitle("OyVey 0.0.3");
        if (!fullNullCheck()) {
//            OyVey.inventoryManager.update();
            Ngm.moduleManager.onUpdate();
            Ngm.moduleManager.sortModules(true);
            onTick();
//            if ((HUD.getInstance()).renderingMode.getValue() == HUD.RenderingMode.Length) {
//                OyVey.moduleManager.sortModules(true);
//            } else {
//                OyVey.moduleManager.sortModulesABC();
//            }
        }
    }

    public void onTick() {
        if (fullNullCheck())
            return;
        Ngm.moduleManager.onTick();
        for (PlayerEntity player : mc.world.getPlayers()) {
            if (player == null || player.getHealth() > 0.0F)
                continue;
            EVENT_BUS.post(new DeathEvent(player));
//            PopCounter.getInstance().onDeath(player);
        }
    }

    @Subscribe
    public void onUpdateWalkingPlayer(UpdateWalkingPlayerEvent event) {
        if (fullNullCheck())
            return;
        if (event.getStage() == Stage.PRE) {
            Ngm.speedManager.updateValues();
            Ngm.rotationManager.updateRotations();
            Ngm.positionManager.updatePosition();
        }
        if (event.getStage() == Stage.POST) {
            Ngm.rotationManager.restoreRotations();
            Ngm.positionManager.restorePosition();
        }
    }

    @Subscribe
    public void onPacketReceive(PacketEvent.Receive event) {
        Ngm.serverManager.onPacketReceived();
        if (event.getPacket() instanceof WorldTimeUpdateS2CPacket)
            Ngm.serverManager.update();
    }

    @Subscribe
    public void onWorldRender(Render3DEvent event) {
        Ngm.moduleManager.onRender3D(event);
    }

    @Subscribe public void onRenderGameOverlayEvent(Render2DEvent event) {
        Ngm.moduleManager.onRender2D(event);
    }
}