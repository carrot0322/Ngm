package me.coolmint.ngm.features.modules.misc;

import com.google.common.eventbus.Subscribe;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import me.coolmint.ngm.event.impl.PacketEvent;
import me.coolmint.ngm.features.modules.Module;
import me.coolmint.ngm.features.settings.Setting;
import me.coolmint.ngm.util.MathUtil;
import me.coolmint.ngm.util.models.Timer;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.network.packet.s2c.play.InventoryS2CPacket;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class ChestStealer extends Module {
    public Setting<Integer> maxDelay = register(new Setting<>("MaxDelay", 80, 0, 400));
    public Setting<Integer> minDelay = register(new Setting<>("MinDelay", 60, 0, 400));
    public Setting<Boolean> instant = register(new Setting<>("Instant", true));
    public Setting<Boolean> autoClose = register(new Setting<>("AutoClose", true));
    public Setting<Integer> autoCloseMaxDelay = register(new Setting<>("MaxCloseDelay", 80, 0, 400, v -> autoClose.getValue()));
    public Setting<Integer> autoCloseMinDelay = register(new Setting<>("MinCloseDelay", 60, 0, 400, v -> autoClose.getValue()));
    public Setting<Boolean> noDuplicate = register(new Setting<>("NoDuplicate", false));
    public Setting<Boolean> closeOnFull = register(new Setting<>("CloseOnFull", true));
    public Setting<Boolean> takeRandomized = register(new Setting<>("TakeRandomized", false));

    public ChestStealer() {
        super("ChestStealer", "", Category.MISC, true, false, false);
    }

    private Timer delayTimer = new Timer();
    private Integer nextDelay = MathUtil.getRandom(minDelay.getValue(), maxDelay.getValue());
    private Timer closeTimer = new Timer();
    private Integer nextClose = MathUtil.getRandom(autoCloseMinDelay.getValue(), autoCloseMaxDelay.getValue());
    private Integer contentReceived = 0;
    private Boolean once = false;

    @Override
    public void onDisable(){
        once = false;
    }

    @Override
    public void onUpdate(){
        if(mc.currentScreen instanceof GenericContainerScreen){
            if(instant.getValue()){
                GenericContainerScreen chest = (GenericContainerScreen) mc.currentScreen;
                int rows = chest.getScreenHandler().getRows() * 9;

                var handler = mc.player.currentScreenHandler;
                Int2ObjectArrayMap<ItemStack> itemMap = new Int2ObjectArrayMap<>();
                itemMap.put(0, new ItemStack(Items.ACACIA_BOAT, 1));

                for (int i = 0; i < rows; i++) {
                    Slot slot = chest.getScreenHandler().getSlot(i);
                    if (slot.hasStack()) {
                        mc.player.networkHandler.sendPacket(
                                new ClickSlotC2SPacket(
                                        handler.syncId,
                                        handler.getRevision(),
                                        i,
                                        0,
                                        SlotActionType.QUICK_MOVE,
                                        handler.getCursorStack().copy(),
                                        itemMap
                                )
                        );
                    }
                }
            }
        } else
            steal((GenericContainerScreen) mc.currentScreen);

    }

    private void steal(GenericContainerScreen screen){
        if (!isEmpty(screen) && !(closeOnFull.getValue() && mc.player.getInventory().main.isEmpty())) {
            closeTimer.reset();

            // Randomized
            if (takeRandomized.getValue()) {
                boolean noLoop = false;
                List<Slot> items;
                do {
                    items = new ArrayList<>();

                    for (int slotIndex = 0; slotIndex < screen.getScreenHandler().getRows() * 9; slotIndex++) {
                        Slot slot = screen.getScreenHandler().getSlot(slotIndex);

                        if (slot.getStack() != null &&
                                (!noDuplicate.getValue() || !slot.getStack().isStackable() ||
                                        !Arrays.stream(mc.player.getInventory().main.toArray())
                                                .filter(itemStack -> itemStack != null && itemStack != null)
                                                .map(itemStack -> itemStack)
                                                .anyMatch(item -> item == slot.getStack().getItem()))) {
                            items.add(slot);
                        }
                    }

                    if (!items.isEmpty()) {
                        int randomSlot = new Random().nextInt(items.size());
                        Slot slot = items.get(randomSlot);

                        move(screen, slot);
                        if (nextDelay == 0L || delayTimer.passedMs(nextDelay)) {
                            noLoop = true;
                        }
                    } else {
                        noLoop = true;
                    }
                } while (delayTimer.passedMs(nextDelay) && !items.isEmpty() && !noLoop);
                return;
            }

            // Non randomized
            for (int slotIndex = 0; slotIndex < screen.getScreenHandler().getRows() * 9; slotIndex++) {
                Slot slot = screen.getScreenHandler().getSlot(slotIndex);

                if (delayTimer.passedMs(nextDelay) && slot.getStack() != null &&
                        (!noDuplicate.getValue() || !slot.getStack().isStackable() ||
                                !Arrays.stream(mc.player.getInventory().main.toArray())
                                        .filter(itemStack -> itemStack != null)
                                        .anyMatch(item -> item == slot.getStack().getItem()))) {
                    move(screen, slot);
                }
            }
        } else if (autoClose.getValue() && screen.getScreenHandler().syncId == contentReceived &&
                closeTimer.passedMs(nextClose)) {
            mc.player.closeScreen();
            nextClose = (int) MathUtil.random(autoCloseMinDelay.getValue(), autoCloseMaxDelay.getValue());

            if (once) {
                once = false;
                return;
            }
        }
    }

    @Subscribe
    private void onPacket(PacketEvent.Receive event) {
        Packet packet = event.getPacket();

        if (packet instanceof InventoryS2CPacket) {
            InventoryS2CPacket windowItemsPacket = (InventoryS2CPacket) packet;
            contentReceived = windowItemsPacket.getSyncId();
        }
    }


    private void move(GenericContainerScreen screen, Slot slot) {
        screen.mouseClicked(slot.x, slot.y, 0);
        delayTimer.reset();
        nextDelay = (int) MathUtil.random(minDelay.getValue(), maxDelay.getValue());
    }


    private boolean isEmpty(GenericContainerScreen chest) {
        for (int i = 0; i < chest.getScreenHandler().getRows() * 9; i++) {
            Slot slot = chest.getScreenHandler().getSlot(i);

            if (slot.getStack() != null &&
                    (!noDuplicate.getValue() || !slot.getStack().isStackable() ||
                            !Arrays.stream(mc.player.getInventory().main.toArray())
                                    .filter(itemStack -> itemStack != null && itemStack != null)
                                    .map(itemStack -> itemStack)
                                    .anyMatch(item -> item == slot.getStack().getItem()))) {
                return false;
            }
        }
        return true;
    }

}
