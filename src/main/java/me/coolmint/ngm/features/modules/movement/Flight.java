package me.coolmint.ngm.features.modules.movement;

import com.google.common.eventbus.Subscribe;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import me.coolmint.ngm.features.settings.Setting;
import me.coolmint.ngm.Ngm;
import me.coolmint.ngm.event.impl.EventMove;
import me.coolmint.ngm.event.impl.EventSync;
import me.coolmint.ngm.event.impl.PacketEvent;
import me.coolmint.ngm.features.modules.Module;
import me.coolmint.ngm.util.MovementUtility;

public class Flight extends Module {

    private final Setting<Mode> mode = register(new Setting<>("Mode", Mode.Vanilla));
    public Setting<Float> hSpeed = register(new Setting<>("Horizontal", 1f, 0.0f, 10.0f));
    public Setting<Float> vSpeed = register(new Setting<>("Vertical", 0.78F, 0.0F, 5F));
    private double prevX, prevY, prevZ;
    public boolean onPosLook = false;

    public Flight() {
        super("Flight", "", Category.MOVEMENT, true, false, false);
    }

    public void onUpdate() {
        if (mode.getValue() != Mode.Vanilla) return;
        mc.player.getAbilities().flying = false;
        mc.player.setVelocity(0.0, 0.0, 0.0);

        if (mc.options.jumpKey.isPressed()) mc.player.setVelocity(mc.player.getVelocity().add(0, vSpeed.getValue(), 0));
        if (mc.options.sneakKey.isPressed()) mc.player.setVelocity(mc.player.getVelocity().add(0, -vSpeed.getValue(), 0));

        final double[] dir = MovementUtility.forward(hSpeed.getValue());
        mc.player.setVelocity(dir[0], mc.player.getVelocity().getY(), dir[1]);

        if (mode.getValue() == Mode.Groundspoof) {
            if (MovementUtility.isMoving() && mc.world.getBlockCollisions(mc.player, mc.player.getBoundingBox().expand(0.5, 0.0, 0.5).offset(0.0, -1.0, 0.0)).iterator().hasNext()) {
                mc.player.setOnGround(true);
            }
        }
    }

    @Subscribe
    public void onPacketSend(PacketEvent.Send e) {
        if (mode.getValue() == Mode.Vanilla) {
            if (e.getPacket() instanceof PlayerMoveC2SPacket.Full) {
                if (onPosLook) {
                    mc.player.setVelocity(prevX, prevY, prevZ);
                    onPosLook = false;
                }
            }
        }
    }

    @Subscribe
    public void onSync(EventSync e) {
        if (mode.getValue() == Mode.Vanilla) {
            if (mc.player.isSneaking()) {
                mc.player.getAbilities().flying = true;
            }
        }
    }

    private enum Mode {
        Vanilla, Groundspoof
    }
}
