package me.coolmint.ngm.features.modules.combat;

import com.google.common.eventbus.Subscribe;
import me.coolmint.ngm.features.modules.Module;
import net.minecraft.block.Blocks;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.TntEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.TntMinecartEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SwordItem;
import net.minecraft.network.packet.c2s.play.*;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import me.coolmint.ngm.Ngm;
import me.coolmint.ngm.event.impl.EventSync;
import me.coolmint.ngm.event.impl.PacketEvent;
import me.coolmint.ngm.features.settings.Setting;
import me.coolmint.ngm.features.modules.Module;
import me.coolmint.ngm.util.player.InventoryUtility;
import me.coolmint.ngm.util.player.SearchInvResult;

public final class AutoTotem extends Module {
    private final Setting<Mode> mode = new Setting<>("Mode", Mode.Matrix);
    private final Setting<OffHand> offhand = new Setting<>("Item", OffHand.Totem);
    private final Setting<Float> healthF = new Setting<>("HP", 16f, 0f, 36f);
    private final Setting<Float> healthS = new Setting<>("ShieldGappleHp", 16f, 0f, 20f, v -> offhand.getValue() == OffHand.Shield);
    private final Setting<Boolean> calcAbsorption = new Setting<>("CalcAbsorption", true);
    private final Setting<Boolean> stopMotion = new Setting<>("stopMotion", false);
    private final Setting<Boolean> resetAttackCooldown = new Setting<>("ResetAttackCooldown", false);
    private final Setting<Boolean> safety = new Setting<>("Safety", false);
    private final Setting<Boolean> hotbarFallBack = new Setting<>("HotbarFallback", false, v -> safety.getValue());
    private final Setting<Boolean> onElytra = new Setting<>("OnElytra", true, v -> safety.getValue());
    private final Setting<Boolean> onFall = new Setting<>("OnFall", true, v -> safety.getValue());
    private final Setting<Boolean> onObsidianPlace = new Setting<>("OnObsidianPlace", false, v -> safety.getValue());
    private final Setting<Boolean> onMinecartTnt = new Setting<>("OnMinecartTNT", true, v -> safety.getValue());
    private final Setting<Boolean> onCreeper = new Setting<>("OnCreeper", true, v -> safety.getValue());
    private final Setting<Boolean> onAnchor = new Setting<>("OnAnchor", true, v -> safety.getValue());

    private final Setting<Boolean> onTnt = new Setting<>("OnTNT", true, v -> safety.getValue());
    public final Setting<Boolean> rcGap = new Setting<>("RCGap", false);
    private final Setting<Boolean> crappleSpoof = new Setting<>("CrappleSpoof", true, v -> offhand.getValue() == OffHand.GApple);

    private enum OffHand {Totem, GApple, Shield}

    private enum Mode {Default, Matrix, NewVersion}

    private static AutoTotem instance;

    private int delay;

    private Item prevItem;

    public AutoTotem() {
        super("AutoTotem", "", Module.Category.COMBAT, true, false, false);
    }

    @Subscribe
    public void onSync(EventSync e) {
        swapTo(getItemSlot());
        delay--;
    }

    @Subscribe
    public void onPacketReceive(PacketEvent.@NotNull Receive e) {
        if (e.getPacket() instanceof EntitySpawnS2CPacket spawn && hotbarFallBack.getValue()) {
            if (spawn.getEntityType() == EntityType.END_CRYSTAL) {
                if (mc.player.squaredDistanceTo(spawn.getX(), spawn.getY(), spawn.getZ()) < 36) {
                    runInstant();
                }
            }
        }
        if (e.getPacket() instanceof BlockUpdateS2CPacket blockUpdate) {
            if (blockUpdate.getState().getBlock() == Blocks.OBSIDIAN && onObsidianPlace.getValue()) {
                if (mc.player.squaredDistanceTo(blockUpdate.getPos().toCenterPos()) < 36 && delay <= 0) {
                    runInstant();
                }
            }
        }
    }

    private float getTriggerHealth() {
        return mc.player.getHealth() + (calcAbsorption.getValue() ? mc.player.getAbsorptionAmount() : 0f);
    }

    private void runInstant() {
        SearchInvResult hotbarResult = InventoryUtility.findItemInHotBar(Items.TOTEM_OF_UNDYING);
        SearchInvResult invResult = InventoryUtility.findItemInInventory(Items.TOTEM_OF_UNDYING);
        if (hotbarResult.found()) {
            hotbarResult.switchTo();
            delay = 20;
        } else if (invResult.found()) {
            int slot = invResult.slot() >= 36 ? invResult.slot() - 36 : invResult.slot();
            if (!hotbarFallBack.getValue()) swapTo(slot);
            else mc.interactionManager.pickFromInventory(slot);
            delay = 20;
        }
    }

    public void swapTo(int slot) {
        if (slot != -1 && delay <= 0) {
            if (mc.currentScreen instanceof GenericContainerScreen) return;

            if (stopMotion.getValue()) mc.player.setVelocity(0, mc.player.getVelocity().getY(), 0);

            int nearest_slot = findNearestCurrentItem();
            int prevCurrentItem = mc.player.getInventory().selectedSlot;
            if (slot >= 9) {
                switch (mode.getValue()) {
                    case Default -> {
                        mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.STOP_SPRINTING));
                        clickSlot(slot);
                        clickSlot(45);
                        clickSlot(slot);
                        mc.player.networkHandler.sendPacket(new CloseHandledScreenC2SPacket(mc.player.currentScreenHandler.syncId));
                    }
                    case Matrix -> {
                        mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.STOP_SPRINTING));
                        mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, slot, nearest_slot, SlotActionType.SWAP, mc.player);
                        mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(nearest_slot));
                        mc.player.getInventory().selectedSlot = nearest_slot;
                        ItemStack itemstack = mc.player.getOffHandStack();
                        mc.player.setStackInHand(Hand.OFF_HAND, mc.player.getMainHandStack());
                        mc.player.setStackInHand(Hand.MAIN_HAND, itemstack);
                        mc.player.networkHandler.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.SWAP_ITEM_WITH_OFFHAND, BlockPos.ORIGIN, Direction.DOWN));
                        mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(prevCurrentItem));
                        mc.player.getInventory().selectedSlot = prevCurrentItem;
                        mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, slot, nearest_slot, SlotActionType.SWAP, mc.player);
                        mc.player.networkHandler.sendPacket(new CloseHandledScreenC2SPacket(mc.player.currentScreenHandler.syncId));
                        if (resetAttackCooldown.getValue())
                            mc.player.resetLastAttackedTicks();
                    }
                    case NewVersion -> {
                        mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, slot, 40, SlotActionType.SWAP, mc.player);
                        mc.player.networkHandler.sendPacket(new CloseHandledScreenC2SPacket(mc.player.currentScreenHandler.syncId));
                    }
                }
            } else {
                mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(slot));
                mc.player.getInventory().selectedSlot = slot;
                mc.player.networkHandler.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.SWAP_ITEM_WITH_OFFHAND, BlockPos.ORIGIN, Direction.DOWN));
                mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(prevCurrentItem));
                mc.player.getInventory().selectedSlot = prevCurrentItem;
                if (resetAttackCooldown.getValue())
                    mc.player.resetLastAttackedTicks();
            }
            delay = 5;
        }
    }

    public static int findNearestCurrentItem() {
        int i = mc.player.getInventory().selectedSlot;
        if (i == 8) return 7;
        if (i == 0) return 1;
        return i - 1;
    }

    public int getItemSlot() {
        if (mc.player == null || mc.world == null) return -1;

        SearchInvResult gapple = InventoryUtility.findItemInInventory(Items.ENCHANTED_GOLDEN_APPLE);
        SearchInvResult crapple = InventoryUtility.findItemInInventory(Items.GOLDEN_APPLE);
        SearchInvResult shield = InventoryUtility.findItemInInventory(Items.SHIELD);

        int itemSlot = -1;
        Item item = null;

        switch (offhand.getValue()) {
            case Totem -> {
                if (mc.player.getOffHandStack().getItem() != Items.TOTEM_OF_UNDYING && !mc.player.getOffHandStack().isEmpty())
                    prevItem = mc.player.getOffHandStack().getItem();
                item = prevItem;
            }

            case GApple -> {
                if (crappleSpoof.getValue()) {
                    if (mc.player.hasStatusEffect(StatusEffects.ABSORPTION) && mc.player.getStatusEffect(StatusEffects.ABSORPTION).getAmplifier() > 2) {
                        if (crapple.found() || mc.player.getOffHandStack().getItem() == Items.GOLDEN_APPLE)
                            item = Items.GOLDEN_APPLE;
                        else if (gapple.found() || mc.player.getOffHandStack().getItem() == Items.ENCHANTED_GOLDEN_APPLE)
                            item = Items.ENCHANTED_GOLDEN_APPLE;
                    } else {
                        if (gapple.found() || mc.player.getOffHandStack().getItem() == Items.ENCHANTED_GOLDEN_APPLE)
                            item = Items.ENCHANTED_GOLDEN_APPLE;
                        else if (crapple.found() || mc.player.getOffHandStack().getItem() == Items.GOLDEN_APPLE)
                            item = Items.GOLDEN_APPLE;
                    }
                } else {
                    if (crapple.found() || mc.player.getOffHandStack().getItem() == Items.GOLDEN_APPLE)
                        item = Items.GOLDEN_APPLE;
                    else if (gapple.found() || mc.player.getOffHandStack().getItem() == Items.ENCHANTED_GOLDEN_APPLE)
                        item = Items.ENCHANTED_GOLDEN_APPLE;
                }
            }

            case Shield -> {
                if (shield.found() || mc.player.getOffHandStack().getItem() == Items.SHIELD) {
                    if (getTriggerHealth() <= healthS.getValue()) {
                        if (crapple.found() || mc.player.getOffHandStack().getItem() == Items.GOLDEN_APPLE)
                            item = Items.GOLDEN_APPLE;
                        else if (gapple.found() || mc.player.getOffHandStack().getItem() == Items.ENCHANTED_GOLDEN_APPLE)
                            item = Items.ENCHANTED_GOLDEN_APPLE;
                    } else {
                        if (!mc.player.getItemCooldownManager().isCoolingDown(Items.SHIELD)) item = Items.SHIELD;
                        else {
                            if (crapple.found() || mc.player.getOffHandStack().getItem() == Items.GOLDEN_APPLE)
                                item = Items.GOLDEN_APPLE;
                            else if (gapple.found() || mc.player.getOffHandStack().getItem() == Items.ENCHANTED_GOLDEN_APPLE)
                                item = Items.ENCHANTED_GOLDEN_APPLE;
                        }
                    }
                } else if (crapple.found() || mc.player.getOffHandStack().getItem() == Items.GOLDEN_APPLE)
                    item = Items.GOLDEN_APPLE;
            }
        }


        if (getTriggerHealth() <= healthF.getValue() && (InventoryUtility.findItemInInventory(Items.TOTEM_OF_UNDYING).found() || mc.player.getOffHandStack().getItem() == Items.TOTEM_OF_UNDYING))
            item = Items.TOTEM_OF_UNDYING;

        if (rcGap.getValue() && (mc.player.getMainHandStack().getItem() instanceof SwordItem) && mc.options.useKey.isPressed()) {
            if (crapple.found() || mc.player.getOffHandStack().getItem() == Items.GOLDEN_APPLE)
                item = Items.GOLDEN_APPLE;
            if (gapple.found() || mc.player.getOffHandStack().getItem() == Items.ENCHANTED_GOLDEN_APPLE)
                item = Items.ENCHANTED_GOLDEN_APPLE;
        }

        if (onFall.getValue() && (getTriggerHealth()) - (((mc.player.fallDistance - 3) / 2F) + 3.5F) < 0.5)
            item = Items.TOTEM_OF_UNDYING;

        if (onElytra.getValue() && mc.player.isFallFlying())
            item = Items.TOTEM_OF_UNDYING;
        {
        }

        for (Entity entity : mc.world.getEntities()) {
            if (entity == null || !entity.isAlive()) continue;
            if (mc.player.squaredDistanceTo(entity) > 36) continue;


            if (onTnt.getValue()) {
                if (entity instanceof TntEntity) {
                    item = Items.TOTEM_OF_UNDYING;
                    break;
                }
            }

            if (onMinecartTnt.getValue()) {
                if (entity instanceof TntMinecartEntity) {
                    item = Items.TOTEM_OF_UNDYING;
                    break;
                }
            }

            if (onCreeper.getValue()) {
                if (entity instanceof CreeperEntity) {
                    item = Items.TOTEM_OF_UNDYING;
                    break;
                }
            }
        }

        if (onAnchor.getValue()) {
            for (int x = -6; x <= 6; x++)
                for (int y = -6; y <= 6; y++)
                    for (int z = -6; z <= 6; z++) {
                        BlockPos bp = new BlockPos(x, y, z);
                        if (mc.world.getBlockState(bp).getBlock() == Blocks.RESPAWN_ANCHOR) {
                            item = Items.TOTEM_OF_UNDYING;
                            break;
                        }
                    }
        }

        for (int i = 9; i < 45; i++) {
            if (mc.player.getOffHandStack().getItem() == item) return -1;
            if (mc.player.getInventory().getStack(i >= 36 ? i - 36 : i).getItem().equals(item)) {
                itemSlot = i >= 36 ? i - 36 : i;
                break;
            }
        }

        if (item == mc.player.getMainHandStack().getItem() && mc.options.useKey.isPressed()) return -1;

        return itemSlot;
    }

    public static AutoTotem getInstance() {
        return instance;
    }
}