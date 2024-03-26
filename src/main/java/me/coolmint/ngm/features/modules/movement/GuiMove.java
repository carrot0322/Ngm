package me.coolmint.ngm.features.modules.movement;

import com.google.common.eventbus.Subscribe;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import me.coolmint.ngm.features.settings.Setting;
import me.coolmint.ngm.event.impl.PacketEvent;
import me.coolmint.ngm.features.modules.Module;
import me.coolmint.ngm.util.MovementUtility;

public class GuiMove extends Module {

    public Setting<Boolean> rotateOnArrows = new Setting<>("RotateOnArrows", true);
    public Setting<Boolean> clickBypass = new Setting<>("strict", false);
    public Setting<Boolean> sneak = new Setting<>("sneak", false);

    public GuiMove() {
        super("GuiMove", "", Module.Category.MOVEMENT, true, false, false);
    }

    public static boolean pause = false;

    @Override
    public void onUpdate() {
        if (mc.currentScreen != null) {
            if (!(mc.currentScreen instanceof ChatScreen)) {

                for (KeyBinding k : new KeyBinding[]{mc.options.forwardKey, mc.options.backKey, mc.options.leftKey, mc.options.rightKey, mc.options.jumpKey, mc.options.sprintKey}) {
                    k.setPressed(InputUtil.isKeyPressed(mc.getWindow().getHandle(), InputUtil.fromTranslationKey(k.getBoundKeyTranslationKey()).getCode()));
                }
                if (rotateOnArrows.getValue()) {
                    if (InputUtil.isKeyPressed(mc.getWindow().getHandle(), 264))
                        mc.player.setPitch(mc.player.getPitch() + 4);

                    if (InputUtil.isKeyPressed(mc.getWindow().getHandle(), 265))
                        mc.player.setPitch(mc.player.getPitch() - 4);

                    if (InputUtil.isKeyPressed(mc.getWindow().getHandle(), 262))
                        mc.player.setYaw(mc.player.getYaw() + 4);

                    if (InputUtil.isKeyPressed(mc.getWindow().getHandle(), 263))
                        mc.player.setYaw(mc.player.getYaw() - 4);

                    if (mc.player.getPitch() > 90) mc.player.setYaw(90);
                    if (mc.player.getPitch() < -90) mc.player.setYaw(-90);
                }
                if (sneak.getValue()) mc.options.sneakKey.setPressed(InputUtil.isKeyPressed(mc.getWindow().getHandle(), InputUtil.fromTranslationKey(mc.options.sneakKey.getBoundKeyTranslationKey()).getCode()));
            }
        }
    }

    @Subscribe
    public void onPacketSend(PacketEvent.Send e) {
        if (pause) {
            pause = false;
            return;
        }
        if (e.getPacket() instanceof ClickSlotC2SPacket) {
            if (clickBypass.getValue() && mc.player.isOnGround() && MovementUtility.isMoving() && !mc.world.getBlockCollisions(mc.player, mc.player.getBoundingBox().offset(0.0, 0.0656, 0.0)).iterator().hasNext()) {
                if (mc.player.isSprinting()) mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.STOP_SPRINTING));

                mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() + 0.0656, mc.player.getZ(), false));
            }
        }
    }

    @Subscribe
    public void onPacketSendPost(PacketEvent.SendPost e) {
        if (e.getPacket() instanceof ClickSlotC2SPacket) {
            if (mc.player.isSprinting()) mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.START_SPRINTING));
        }
    }
}