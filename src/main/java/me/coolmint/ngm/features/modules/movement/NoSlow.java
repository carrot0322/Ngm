package me.coolmint.ngm.features.modules.movement;

import com.google.common.eventbus.Subscribe;
import me.coolmint.ngm.Ngm;
import me.coolmint.ngm.event.impl.*;
import me.coolmint.ngm.features.modules.Module;
import me.coolmint.ngm.features.settings.Setting;
import net.minecraft.block.*;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.DeathScreen;
import net.minecraft.client.gui.screen.ingame.SignEditScreen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.*;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.ArrayList;
import java.util.List;

public class NoSlow extends Module {
    private final Setting<Boolean> strictConfig = register(new Setting<>("StrictNCP", false));
    private final Setting<Boolean> airStrictConfig = register(new Setting<>("AirStrictNCP", false));
    private final Setting<Boolean> grimConfig = register(new Setting<>("Grim", false));
    private final Setting<Boolean> strafeFixConfig = register(new Setting<>("StrafeFix", false));
    private final Setting<Boolean> itemsConfig = register(new Setting<>("Items", true));
    private final Setting<Boolean> shieldsConfig = register(new Setting<>("Shields", false));
    private final Setting<Boolean> soulsandConfig = register(new Setting<>("SoulSand", false));
    private final Setting<Boolean> honeyblockConfig = register(new Setting<>("HoneyBlock", false));
    private final Setting<Boolean> slimeblockConfig = register(new Setting<>("SlimeBlock", false));

    public NoSlow() {
        super("NoSlow", "", Module.Category.MOVEMENT, true, false, false);
    }

    private boolean sneaking;
    private static KeyBinding[] MOVE_KEYBINDS;

    @Override
    public void onEnable() {
        if (MOVE_KEYBINDS != null) {
            return;
        }
        MOVE_KEYBINDS = new KeyBinding[]{mc.options.jumpKey, mc.options.sneakKey, mc.options.forwardKey, mc.options.backKey, mc.options.rightKey, mc.options.leftKey};
    }

    @Override
    public void onDisable() {
        if (airStrictConfig.getValue() && sneaking) {
            mc.getNetworkHandler().sendPacket(new ClientCommandC2SPacket(mc.player,
                    ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY));
        }
        sneaking = false;
        Ngm.tickManager.setClientTick(1.0f);
    }

    @Subscribe
    public void onGameJoin(GameJoinEvent event) {
        onEnable();
    }

    @Subscribe
    public void onSetCurrentHand(SetCurrentHandEvent event) {
        if (airStrictConfig.getValue() && !sneaking && checkSlowed()) {
            sneaking = true;
            mc.getNetworkHandler().sendPacket(new ClientCommandC2SPacket(mc.player,
                    ClientCommandC2SPacket.Mode.PRESS_SHIFT_KEY));
        }
    }

    @Subscribe
    public void onPlayerUpdate(UpdateEvent event) {
        ItemStack offHandStack = mc.player.getOffHandStack();
        if (mc.player.getActiveHand() == Hand.OFF_HAND) {
            mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(mc.player.getInventory().selectedSlot % 8 + 1));
            mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(mc.player.getInventory().selectedSlot));
        } else if (!offHandStack.isFood() && offHandStack.getItem() != Items.BOW && offHandStack.getItem() != Items.CROSSBOW && offHandStack.getItem() != Items.SHIELD) {
            Ngm.networkManager.sendSequencedPacket(id -> new PlayerInteractItemC2SPacket(Hand.OFF_HAND, id));
        }
    }

    @Override
    public void onTick() {
        if (airStrictConfig.getValue() && sneaking && !mc.player.isUsingItem()) {
            sneaking = false;
            mc.getNetworkHandler().sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY));
        }
    }

    @Subscribe
    public void onMovementSlowdown(MovementSlowdownEvent event) {
        if (checkSlowed()) {
            event.input.movementForward *= 5.0f;
            event.input.movementSideways *= 5.0f;
        }
    }

    @Subscribe
    public void onVelocityMultiplier(VelocityMultiplierEvent event) {
        if (event.getBlock() == Blocks.SOUL_SAND && soulsandConfig.getValue() || event.getBlock() == Blocks.HONEY_BLOCK && honeyblockConfig.getValue()) {
            event.cancel();
        }
    }

    @Subscribe
    public void onSteppedOnSlimeBlock(SteppedOnSlimeBlockEvent event) {
        if (slimeblockConfig.getValue()) {
            event.cancel();
        }
    }

    @Subscribe
    public void onBlockSlipperiness(BlockSlipperinessEvent event) {
        if (event.getBlock() == Blocks.SLIME_BLOCK
                && slimeblockConfig.getValue()) {
            event.cancel();
            event.setSlipperiness(0.6f);
        }
    }

    @Subscribe
    public void onPacketOutbound(PacketEvent.Send event) {
        if (mc.player == null || mc.world == null || mc.isInSingleplayer()) {
            return;
        }
        if (event.getPacket() instanceof PlayerMoveC2SPacket packet && packet.changesPosition()
                && strictConfig.getValue() && checkSlowed()) {
            // mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(0));
            // mc.getNetworkHandler().sendSequencedPacket(id -> new PlayerInteractItemC2SPacket(Hand.OFF_HAND, id));
            mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(mc.player.getInventory().selectedSlot));
        } else if (event.getPacket() instanceof ClickSlotC2SPacket && strictConfig.getValue()) {
            if (mc.player.isUsingItem()) {
                mc.player.stopUsingItem();
            }
            if (sneaking || mc.player.isSneaking()) {
                mc.getNetworkHandler().sendPacket(new ClientCommandC2SPacket(mc.player,
                        ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY));
            }
            if (mc.player.isSprinting()) {
                mc.getNetworkHandler().sendPacket(new ClientCommandC2SPacket(mc.player,
                        ClientCommandC2SPacket.Mode.STOP_SPRINTING));
            }
        }
    }

    public boolean checkSlowed() {
        ItemStack offHandStack = mc.player.getOffHandStack();
        if ((offHandStack.isFood() || offHandStack.getItem() == Items.BOW || offHandStack.getItem() == Items.CROSSBOW || offHandStack.getItem() == Items.SHIELD) && grimConfig.getValue()) {
            return false;
        }
        return !mc.player.isRiding() && !mc.player.isSneaking() && !mc.player.isFallFlying()
                && (mc.player.isUsingItem() && itemsConfig.getValue() || mc.player.isBlocking() && shieldsConfig.getValue() && !grimConfig.getValue());
    }

    public boolean checkScreen() {
        return mc.currentScreen != null && !(mc.currentScreen instanceof ChatScreen || mc.currentScreen instanceof SignEditScreen || mc.currentScreen instanceof DeathScreen);
    }

    public boolean getStrafeFix() {
        return strafeFixConfig.getValue();
    }
}