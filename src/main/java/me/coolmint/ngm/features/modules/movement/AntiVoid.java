package me.coolmint.ngm.features.modules.movement;

import com.google.common.eventbus.Subscribe;
import me.coolmint.ngm.Ngm;
import me.coolmint.ngm.features.modules.Module;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.BlockPos;
import me.coolmint.ngm.event.impl.EventMove;
import me.coolmint.ngm.features.settings.Setting;

public class AntiVoid extends Module {

    public AntiVoid() {
        super("AntiVoid", "", Module.Category.MOVEMENT, true, false, false);
    }

    private static final Setting<Mode> mode = new Setting<>("Mode", Mode.NCP);
    private final Setting<Boolean> sendPacket = new Setting<>("SendPacket", true, v-> mode.getValue() == Mode.NCP);

    private enum Mode {
        NCP, Timer
    }

    boolean timerFlag;

    @Override
    public void onDisable(){
        if(timerFlag)
            Ngm.TICK_TIMER = 1f;
    }

    @Subscribe
    public void onMove(EventMove e) {
        if (fullNullCheck())
            return;

        if (fallingToVoid()) {
            if (mode.getValue() == Mode.NCP) {
                e.cancel();
                e.setY(0);
                if (sendPacket.getValue())
                    mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY(), mc.player.getZ(), true));
            } else {
                Ngm.TICK_TIMER = 0.2f;
                timerFlag = true;
            }
        } else if (timerFlag) {
            Ngm.TICK_TIMER = 1f;
            timerFlag = false;
        }
    }

    private boolean fallingToVoid() {
        for (int i = (int) mc.player.getY(); i >= -64; i--)
            if (!mc.world.isAir(BlockPos.ofFloored(mc.player.getX(), i, mc.player.getZ())))
                return false;
        return mc.player.fallDistance > 0;
    }
}