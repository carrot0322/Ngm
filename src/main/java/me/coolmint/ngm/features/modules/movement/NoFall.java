package me.coolmint.ngm.features.modules.movement;

import com.google.common.eventbus.Subscribe;
import me.coolmint.ngm.features.modules.Module;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import me.coolmint.ngm.event.impl.EventSync;
import me.coolmint.ngm.event.impl.PacketEvent;
import me.coolmint.ngm.mixin.IPlayerMoveC2SPacket;
import me.coolmint.ngm.features.settings.Setting;

public class NoFall extends Module {
    public NoFall() {
        super("NoFall", "", Module.Category.MOVEMENT, true, false, false);
    }

    private final Setting<NoFall.Mode> mode = register(new Setting<>("Mode", NoFall.Mode.Vanilla));
    public final Setting<FallDistance> fallDistance = register(new Setting<>("FallDistance", FallDistance.Calc));
    public final Setting<Integer> fallDistanceValue = register(new Setting<>("FallDistanceVal", 10, 2, 100, v -> fallDistance.getValue() == FallDistance.Custom));
    private me.coolmint.ngm.util.models.Timer pearlCooldown = new me.coolmint.ngm.util.models.Timer();

    private enum Mode {
        MatrixOffGround, Vanilla, Grim2b2t
    }

    private enum FallDistance {
        Calc, Custom
    }

    private boolean cancelGround = false;

    @Override
    public void onUpdate() {
        if (fullNullCheck())
            return;
        if (isFalling()) {
            switch (mode.getValue()) {

                case Grim2b2t -> {
                    mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.Full(mc.player.getX(), mc.player.getY() + 0.000000001, mc.player.getZ(), mc.player.getYaw(), mc.player.getPitch(), false));
                    mc.player.onLanding();
                }

                case MatrixOffGround, Vanilla -> {
                    cancelGround = true;
                }
            }
        }
    }

    public boolean isFalling() {
        if(fullNullCheck())
            return false;

        if (mc.player.isFallFlying())
            return false;

        switch (fallDistance.getValue()) {
            case Custom -> {
                return mc.player.fallDistance > fallDistanceValue.getValue();
            }
            case Calc -> {
                return (((mc.player.fallDistance - 3) / 2F) + 3.5F) > mc.player.getHealth() / 3f;
            }
        }
        return false;
    }

    @Override
    public String getDisplayInfo() {
        return mode.getValueAsString() + " " + (isFalling() ? "Ready" : "");
    }

    @Subscribe
    public void onPacketSend(PacketEvent.Send e) {
        if (e.getPacket() instanceof PlayerMoveC2SPacket pac) {
            if (cancelGround)
                ((IPlayerMoveC2SPacket) pac).setOnGround(false);
        }
    }

    @Override
    public void onEnable() {
        cancelGround = false;
    }
}