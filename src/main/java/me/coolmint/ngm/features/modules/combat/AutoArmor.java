package me.coolmint.ngm.features.modules.combat;

import com.google.common.collect.Lists;
import com.google.common.eventbus.Subscribe;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import me.coolmint.ngm.event.Stage;
import me.coolmint.ngm.event.impl.TickEvent;
import me.coolmint.ngm.features.modules.Module;
import me.coolmint.ngm.features.settings.Setting;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.registry.Registries;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;

import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.Queue;

import static me.coolmint.ngm.util.player.InvUtils.click;

public class AutoArmor extends Module {
    Setting<Priority> priorityConfig = register(new Setting<>("Priority", Priority.BLAST_PROTECTION));
    Setting<Float> minDurabilityConfig = register(new Setting<>("MinDurability", 0.0f, 0.0f, 20.0f));
    Setting<Boolean> elytraPriorityConfig = register(new Setting<>("ElytraPriority", true));
    Setting<Boolean> blastLeggingsConfig = register(new Setting<>("Leggings-BlastPriority",true));
    Setting<Boolean> noBindingConfig = register(new Setting<>("NoBinding", true));
    Setting<Boolean> inventoryConfig = register(new Setting<>("AllowInventory", false));

    public AutoArmor() {
        super("AutoArmor", "", Category.COMBAT, true, false, false);
    }

    private final Queue<ArmorSlot> helmet = new PriorityQueue<>();
    private final Queue<ArmorSlot> chestplate = new PriorityQueue<>();
    private final Queue<ArmorSlot> leggings = new PriorityQueue<>();
    private final Queue<ArmorSlot> boots = new PriorityQueue<>();

    @Override
    public void onTick() {
        if (mc.currentScreen != null && !(mc.currentScreen instanceof InventoryScreen
                && inventoryConfig.getValue())) {
            return;
        }
        //
        helmet.clear();
        chestplate.clear();
        leggings.clear();
        boots.clear();
        for (int j = 9; j < 45; j++) {
            ItemStack stack = mc.player.getInventory().getStack(j);
            if (stack.isEmpty() || !(stack.getItem() instanceof ArmorItem armor)) {
                continue;
            }
            if (noBindingConfig.getValue() && EnchantmentHelper.hasBindingCurse(stack)) {
                continue;
            }
            int index = armor.getSlotType().getEntitySlotId();
            float dura = (stack.getMaxDamage() - stack.getDamage()) / (float) stack.getMaxDamage();
            if (dura < minDurabilityConfig.getValue()) {
                continue;
            }
            ArmorSlot data = new ArmorSlot(index, j, stack);
            switch (index) {
                case 0 -> helmet.add(data);
                case 1 -> chestplate.add(data);
                case 2 -> leggings.add(data);
                case 3 -> boots.add(data);
            }
        }
        for (int i = 0; i < 4; i++) {
            ItemStack armorStack = mc.player.getInventory().getArmorStack(i);
            if (elytraPriorityConfig.getValue() && armorStack.getItem() == Items.ELYTRA) {
                continue;
            }
            float armorDura = (armorStack.getMaxDamage() - armorStack.getDamage()) / (float) armorStack.getMaxDamage();
            if (!armorStack.isEmpty() || armorDura >= minDurabilityConfig.getValue()) {
                continue;
            }
            switch (i) {
                case 0 -> {
                    if (!helmet.isEmpty()) {
                        ArmorSlot helmetSlot = helmet.poll();
                        swapArmor(helmetSlot.getType(), helmetSlot.getSlot());
                    }
                }
                case 1 -> {
                    if (!chestplate.isEmpty()) {
                        ArmorSlot chestSlot = chestplate.poll();
                        swapArmor(chestSlot.getType(), chestSlot.getSlot());
                    }
                }
                case 2 -> {
                    if (!leggings.isEmpty()) {
                        ArmorSlot leggingsSlot = leggings.poll();
                        swapArmor(leggingsSlot.getType(), leggingsSlot.getSlot());
                    }
                }
                case 3 -> {
                    if (!boots.isEmpty()) {
                        ArmorSlot bootsSlot = boots.poll();
                        swapArmor(bootsSlot.getType(), bootsSlot.getSlot());
                    }
                }
            }
        }
    }

    private void click(int slot, int button, SlotActionType type) {
        ScreenHandler screenHandler = mc.player.currentScreenHandler;
        DefaultedList<Slot> defaultedList = screenHandler.slots;
        int i = defaultedList.size();
        ArrayList<ItemStack> list = Lists.newArrayListWithCapacity(i);
        for (Slot slot1 : defaultedList) {
            list.add(slot1.getStack().copy());
        }
        screenHandler.onSlotClick(slot, button, type, mc.player);
        Int2ObjectOpenHashMap<ItemStack> int2ObjectMap = new Int2ObjectOpenHashMap<>();
        for (int j = 0; j < i; ++j) {
            ItemStack itemStack2;
            ItemStack itemStack = list.get(j);
            if (ItemStack.areEqual(itemStack, itemStack2 = defaultedList.get(j).getStack())) continue;
            int2ObjectMap.put(j, itemStack2.copy());
        }
        mc.player.networkHandler.sendPacket(new ClickSlotC2SPacket(screenHandler.syncId, screenHandler.getRevision(), slot, button, type, screenHandler.getCursorStack().copy(), int2ObjectMap));
    }

    public void pickupSlot(final int slot) {
        click(slot, 0, SlotActionType.PICKUP);
    }

    public void swapArmor(int armorSlot, int slot) {
        ItemStack stack = mc.player.getInventory().getArmorStack(armorSlot);

        armorSlot = 8 - armorSlot;
        pickupSlot(slot);
        boolean rt = !stack.isEmpty();
        pickupSlot(armorSlot);
        if (rt) {
            pickupSlot(slot);
        }
    }

    public float getPriority(int i, ItemStack armorStack) {
        return 1.0f;
    }

    public enum Priority {
        BLAST_PROTECTION(Enchantments.BLAST_PROTECTION),
        PROTECTION(Enchantments.PROTECTION),
        PROJECTILE_PROTECTION(Enchantments.PROJECTILE_PROTECTION);

        private final Enchantment enchant;

        Priority(Enchantment enchant) {
            this.enchant = enchant;
        }

        public Enchantment getEnchantment() {
            return enchant;
        }
    }

    public class ArmorSlot implements Comparable<ArmorSlot> {
        private final int armorType;
        private final int slot;
        private final ItemStack armorStack;

        public ArmorSlot(int armorType, int slot, ItemStack armorStack) {
            this.armorType = armorType;
            this.slot = slot;
            this.armorStack = armorStack;
        }

        @Override
        public int compareTo(ArmorSlot other) {
            final ItemStack otherStack = other.getArmorStack();
            ArmorItem armorItem = (ArmorItem) armorStack.getItem();
            ArmorItem otherItem = (ArmorItem) otherStack.getItem();
            float durabilityDiff = otherItem.getMaterial().getDurability(otherItem.getType())
                    - armorItem.getMaterial().getDurability(armorItem.getType());
            if (durabilityDiff != 0.0f) {
                return (int) durabilityDiff;
            }
            Enchantment enchantment = priorityConfig.getValue().getEnchantment();
            if (blastLeggingsConfig.getValue() && armorType == 2
                    && hasEnchantment(Enchantments.BLAST_PROTECTION)) {
                return -1;
            }
            if (hasEnchantment(enchantment)) {
                return other.hasEnchantment(enchantment) ? 0 : -1;
            } else {
                return other.hasEnchantment(enchantment) ? 1 : 0;
            }
        }

        public boolean hasEnchantment(Enchantment enchantment) {
            Object2IntMap<Enchantment> enchants =
                    getEnchantments(armorStack);
            return enchants.containsKey(enchantment);
        }

        public ItemStack getArmorStack() {
            return armorStack;
        }

        public int getType() {
            return armorType;
        }

        public int getSlot() {
            return slot;
        }

        public static Object2IntMap<Enchantment> getEnchantments(ItemStack itemStack) {
            Object2IntMap<Enchantment> enchants = new Object2IntOpenHashMap<>();
            NbtList list = itemStack.getEnchantments();
            for (int i = 0; i < list.size(); i++) {
                NbtCompound tag = list.getCompound(i);
                Registries.ENCHANTMENT.getOrEmpty(Identifier.tryParse(tag.getString("id"))).ifPresent((enchantment) -> enchants.put(enchantment, tag.getInt("lvl")));
            }
            return enchants;
        }
    }
}
