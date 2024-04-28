package me.coolmint.ngm.features.modules.movement;

import com.google.common.eventbus.Subscribe;
import me.coolmint.ngm.Ngm;
import me.coolmint.ngm.event.Stage;
import me.coolmint.ngm.event.impl.SyncEvent;
import me.coolmint.ngm.event.impl.TickEvent;
import me.coolmint.ngm.event.impl.PacketEvent;
import me.coolmint.ngm.features.modules.Module;
import me.coolmint.ngm.features.settings.Setting;
import me.coolmint.ngm.mixin.IClientPlayerEntity;
import me.coolmint.ngm.util.player.MovementUtility;
import net.minecraft.block.AbstractBlock;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

public class Flight extends Module {
    private final Setting<Mode> mode = register(new Setting<>("Mode", Mode.Vanilla));
    public Setting<Float> hSpeed = register(new Setting<>("Horizontal", 1f, 0.0f, 10.0f, v -> !mode.getValue().equals(Mode.VulcanGlide)));
    public Setting<Float> vSpeed = register(new Setting<>("Vertical", 0.78F, 0.0F, 5F, v -> !mode.getValue().equals(Mode.VulcanGlide)));

    public Flight() {
        super("Flight", "", Category.MOVEMENT, true, false, false);
    }

    private int ticks = 0;
    private double prevX, prevY, prevZ;
    public boolean onPosLook = false;
    private int delayLeft = 20;
    private int offLeft = 1;
    private double lastPacketY = Double.MAX_VALUE;

    public void onUpdate() {
        if (mode.getValue() == Mode.GroundSpoof) {
            mc.player.getAbilities().flying = false;
            mc.player.setVelocity(0.0, 0.0, 0.0);

            if (mc.options.jumpKey.isPressed()) mc.player.setVelocity(mc.player.getVelocity().add(0, vSpeed.getValue(), 0));
            if (mc.options.sneakKey.isPressed()) mc.player.setVelocity(mc.player.getVelocity().add(0, -vSpeed.getValue(), 0));

            final double[] dir = MovementUtility.forward(hSpeed.getValue());
            mc.player.setVelocity(dir[0], mc.player.getVelocity().getY(), dir[1]);

            if (MovementUtility.isMoving() && mc.world.getBlockCollisions(mc.player, mc.player.getBoundingBox().expand(0.5, 0.0, 0.5).offset(0.0, -1.0, 0.0)).iterator().hasNext()) {
                mc.player.setOnGround(true);
            }
        }
    }

    @Subscribe
    public void onPacketSendPRE(PacketEvent.SendPRE e) {
        if(nullCheck()) return;

        if (mode.getValue() == Mode.Vanilla) {
            if (e.getPacket() instanceof PlayerMoveC2SPacket.Full) {
                antiKickPacket(e.getPacket(), mc.player.getY());
                if (onPosLook) {
                    mc.player.setVelocity(prevX, prevY, prevZ);
                    onPosLook = false;
                }
            }
        }

        if (e.getPacket() instanceof PlayerMoveC2SPacket pac) {

            if(mode.getValue().equals(Mode.VulcanGlide)){
                if (!mc.player.isOnGround()) {
                    Ngm.TICK_TIMER = 1f;
                    mc.player.setVelocity(mc.player.getVelocity().x, ticks % 2 == 0 ? -0.17 : -0.10, mc.player.getVelocity().z);

                    if (ticks == 0)
                        mc.player.setVelocity(mc.player.getVelocity().x, -0.16, mc.player.getVelocity().z);

                    ticks++;
                }
            }

        }
    }



    @Subscribe
    public void onSync(SyncEvent e) {
        if (mode.getValue() == Mode.Vanilla) {
            if (mc.player.isSneaking()) {
                mc.player.getAbilities().flying = true;
            }
        }
    }

    @Subscribe
    public void onPostTick(TickEvent.Post event){
        if(nullCheck()) return;

        if (offLeft <= 0 && delayLeft <= 0) {
            // Resend movement packets
            ((IClientPlayerEntity) mc.player).setTicksSinceLastPositionPacketSent(20);
        } else if (delayLeft <= 0) {
            boolean shouldReturn = false;

            if (offLeft == 1) {
                // Resend movement packets
                ((IClientPlayerEntity) mc.player).setTicksSinceLastPositionPacketSent(20);
            }

            offLeft--;

            if (shouldReturn) return;
        }
    }

    private void antiKickPacket(PlayerMoveC2SPacket packet, double currentY) {
        if (this.delayLeft <= 0 && this.lastPacketY != Double.MAX_VALUE && shouldFlyDown(currentY, this.lastPacketY) && isEntityOnAir(mc.player))
            mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), lastPacketY - 0.03130D, mc.player.getZ(), mc.player.isOnGround()));
        else
            lastPacketY = currentY;
    }

    private boolean shouldFlyDown(double currentY, double lastY) {
        if (currentY >= lastY) {
            return true;
        } else return lastY - currentY < 0.03130D;
    }

    private boolean isEntityOnAir(Entity entity) {
        return entity.getWorld().getStatesInBox(entity.getBoundingBox().expand(0.0625).stretch(0.0, -0.55, 0.0)).allMatch(AbstractBlock.AbstractBlockState::isAir);
    }

    enum Mode {
        Vanilla,
        GroundSpoof,
        VulcanGlide
    }
}
