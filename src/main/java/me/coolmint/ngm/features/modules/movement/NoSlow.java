package me.coolmint.ngm.features.modules.movement;

import me.coolmint.ngm.util.player.PlayerUtility;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.util.Hand;
import me.coolmint.ngm.features.modules.Module;
import me.coolmint.ngm.features.settings.Setting;

public class NoSlow extends Module {
    public NoSlow() {
        super("NoSlow", "", Module.Category.MOVEMENT, true, false, false);
    }

    public final Setting<Mode> mode = register(new Setting<>("Mode", Mode.NCP));
    private final Setting<Boolean> mainHand = register(new Setting<>("MainHand", true));
    private final Setting<Boolean> food = register(new Setting<>("Food", true));
    private final Setting<Boolean> projectiles = register(new Setting<>("Projectiles", true));
    private final Setting<Boolean> shield = register(new Setting<>("Shield", true));
    public final Setting<Boolean> soulSand = register(new Setting<>("SoulSand", true));
    public final Setting<Boolean> honey = register(new Setting<>("Honey", true));
    public final Setting<Boolean> slime = register(new Setting<>("Slime", true));
    public final Setting<Boolean> ice = register(new Setting<>("Ice", true));
    public final Setting<Boolean> sweetBerryBush = register(new Setting<>("SweetBerryBush", true));

    private boolean returnSneak;

    @Override
    public void onUpdate() {
        if (returnSneak) {
            mc.options.sneakKey.setPressed(false);
            returnSneak = false;
        }

        if (mc.player.isUsingItem() && !mc.player.isRiding() && !mc.player.isFallFlying()) {
            switch (mode.getValue()) {
                case StrictNCP -> mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(mc.player.getInventory().selectedSlot));
                case MusteryGrief -> {
                    if (mc.player.isOnGround() && mc.options.jumpKey.isPressed()) {
                        mc.options.sneakKey.setPressed(true);
                        returnSneak = true;
                    }
                }
                case Grim -> {
                    if (mc.player.getActiveHand() == Hand.OFF_HAND) {
                        mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(mc.player.getInventory().selectedSlot % 8 + 1));
                        mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(mc.player.getInventory().selectedSlot));
                    } else if (mainHand.getValue()) {
                        mc.player.networkHandler.sendPacket(new PlayerInteractItemC2SPacket(Hand.OFF_HAND, PlayerUtility.getWorldActionId(mc.world)));
                    }
                }
                case Matrix -> {
                    if (mc.player.isOnGround() && !mc.options.jumpKey.isPressed()) {
                        mc.player.setVelocity(mc.player.getVelocity().x * 0.3, mc.player.getVelocity().y, mc.player.getVelocity().z * 0.3);
                    } else if (mc.player.fallDistance > 0.2f)
                        mc.player.setVelocity(mc.player.getVelocity().x * 0.95f, mc.player.getVelocity().y, mc.player.getVelocity().z * 0.95f);
                }
                case GrimNew -> {
                    if (mc.player.getActiveHand() == Hand.OFF_HAND) {
                        mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(mc.player.getInventory().selectedSlot % 8 + 1));
                        mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(mc.player.getInventory().selectedSlot));
                    } else if (mainHand.getValue() && (mc.player.getItemUseTime() <= 3 || mc.player.age % 2 == 0)) {
                        mc.player.networkHandler.sendPacket(new PlayerInteractItemC2SPacket(Hand.OFF_HAND, PlayerUtility.getWorldActionId(mc.world)));
                    }
                }
                case Matrix2 -> {
                    if (mc.player.isOnGround())
                        if (mc.player.age % 2 == 0)
                            mc.player.setVelocity(mc.player.getVelocity().x * 0.5f, mc.player.getVelocity().y, mc.player.getVelocity().z * 0.5f);
                        else
                            mc.player.setVelocity(mc.player.getVelocity().x * 0.95f, mc.player.getVelocity().y, mc.player.getVelocity().z * 0.95f);
                }
            }
        }
    }

    public boolean canNoSlow() {
        if (!food.getValue() && mc.player.getActiveItem().isFood())
            return false;

        if (!shield.getValue() && mc.player.getActiveItem().getItem() == Items.SHIELD)
            return false;

        if (!projectiles.getValue()
                && (mc.player.getActiveItem().getItem() == Items.CROSSBOW || mc.player.getActiveItem().getItem() == Items.BOW || mc.player.getActiveItem().getItem() == Items.TRIDENT))
            return false;

        if (mode.getValue() == Mode.MusteryGrief && mc.player.isOnGround() && !mc.options.jumpKey.isPressed())
            return false;

        if (!mainHand.getValue() && mc.player.getActiveHand() == Hand.MAIN_HAND)
            return false;

        if ((mc.player.getOffHandStack().isFood() || mc.player.getOffHandStack().getItem() == Items.SHIELD)
                && (mode.getValue() == Mode.GrimNew || mode.getValue() == Mode.Grim) && mc.player.getActiveHand() == Hand.MAIN_HAND)
            return false;

        return true;
    }

    public enum Mode {
        NCP, StrictNCP, Matrix, Grim, MusteryGrief, GrimNew, Matrix2
    }
}