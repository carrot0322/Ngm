package me.coolmint.ngm.features.modules.misc;

import com.google.common.eventbus.Subscribe;
import me.coolmint.ngm.Ngm;
import me.coolmint.ngm.event.impl.AttackBlockEvent;
import me.coolmint.ngm.event.impl.PacketEvent;
import me.coolmint.ngm.event.impl.SyncEvent;
import me.coolmint.ngm.features.modules.Module;
import me.coolmint.ngm.features.settings.Setting;
import me.coolmint.ngm.manager.PlayerManager;
import me.coolmint.ngm.util.models.Timer;
import me.coolmint.ngm.util.player.InventoryUtil;
import me.coolmint.ngm.util.player.PlayerUtil;
import me.coolmint.ngm.util.player.RotationUtil;
import net.minecraft.block.AirBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.Objects;

public class SpeedMine extends Module {
    enum categories {
        Mine,
        Packet,
        Render
    }

    // Mine
    enum Mode {
        Packet,
        GrimInstant
    }

    enum RenderMode {
        Block,
        Shrink,
        Grow
    }

    enum SwitchMode {
        Silent,
        Normal,
        Alternative
    }

    enum StartMode {
        StartAbort,
        StartStop
    }

    // Color
    enum color {
        StartLine,
        EndLine,
        StartFill,
        EndFill
    }

    public Setting<categories> category = register(new Setting<>("Category", categories.Mine));

    boolean cCate(categories checkCategory) {
        return category.getValue() == checkCategory;
    }
    
    // Mine
    public Setting<Mode> mode = register(new Setting<>("Mode", Mode.Packet, v -> cCate(categories.Mine)));
    public Setting<StartMode> startMode = register(new Setting<>("StartMode", StartMode.StartAbort, v -> mode.getValue() == Mode.Packet && cCate(categories.Mine)));
    public Setting<SwitchMode> switchMode = register(new Setting<>("SwitchMode", SwitchMode.Alternative, v -> cCate(categories.Mine)));
    public Setting<Integer> swapDelay = register(new Setting<>("SwapDelay", 50, 0, 1000, v -> switchMode.getValue() == SwitchMode.Alternative && cCate(categories.Mine)));
    public Setting<Float> factor = register(new Setting<>("Factor", 1f, 0.5f, 2f, v -> cCate(categories.Mine)));
    public Setting<Float> rebreakfactor = register(new Setting<>("RebreakFactor", 7f, 0.5f, 20f, v -> mode.getValue() == Mode.GrimInstant && cCate(categories.Mine)));
    public Setting<Float> range = register(new Setting<>("Range", 4.2f, 3.0f, 10.0f, v -> cCate(categories.Mine)));
    public Setting<Boolean> rotate = register(new Setting<>("Rotate", false, v -> cCate(categories.Mine)));
    public Setting<Boolean> resetOnSwitch = register(new Setting<>("ResetOnSwitch", true, v -> cCate(categories.Mine)));
    public Setting<Integer> breakAttempts = register(new Setting<>("BreakAttempts", 10, 1, 50, v -> mode.getValue() == Mode.Packet && cCate(categories.Mine)));

    // Packet
    public Setting<Boolean> stop = register(new Setting<>("stop", true, v -> cCate(categories.Packet)));
    public Setting<Boolean> abort = register(new Setting<>("abort", true, v -> cCate(categories.Packet)));
    public Setting<Boolean> start = register(new Setting<>("start", true, v -> cCate(categories.Packet)));
    public Setting<Boolean> stop2 = register(new Setting<>("stop2", true, v -> cCate(categories.Packet)));

    // Render
    public Setting<Boolean> render = register(new Setting<>("Render", true, v -> cCate(categories.Render)));
    public Setting<Boolean> smooth = register(new Setting<>("Smooth", true, v -> cCate(categories.Render) && render.getValue()));
    public Setting<RenderMode> renderMode = register(new Setting<>("Render Mode", RenderMode.Shrink, v -> cCate(categories.Render) && render.getValue()));

    // Color
    public Setting<color> colorCategory = register(new Setting<>("", color.StartLine, v -> cCate(categories.Render) & render.getValue()));
    public Setting<Integer> lineWidth = register(new Setting<>("Line Width", 2, 1, 10, v -> cCate(categories.Render) && render.getValue() && colorCategory.getValue() == color.StartLine || colorCategory.getValue() == color.EndLine));

    // Start Line
    public Setting<Integer> startLineRed = register(new Setting<>("Start Line Red", 255, 0, 255, v -> cCate(categories.Render) && render.getValue() && colorCategory.getValue() == color.StartLine));
    public Setting<Integer> startLineGreen = register(new Setting<>("Start Line Green", 0, 0, 255, v -> cCate(categories.Render) && render.getValue() && colorCategory.getValue() == color.StartLine));
    public Setting<Integer> startLineBlue = register(new Setting<>("Start Line Blue", 0, 0, 255, v -> cCate(categories.Render) && render.getValue() && colorCategory.getValue() == color.StartLine));
    public Setting<Integer> startLineAlpha = register(new Setting<>("Start Line Alpha", 200, 0, 255, v -> cCate(categories.Render) && render.getValue() && colorCategory.getValue() == color.StartLine));

    // End Line
    public Setting<Integer> endLineRed = register(new Setting<>("End Line Red", 47, 0, 255, v -> cCate(categories.Render) && render.getValue() && colorCategory.getValue() == color.EndLine));
    public Setting<Integer> endLineGreen = register(new Setting<>("End Line Green", 255, 0, 255, v -> cCate(categories.Render) && render.getValue() && colorCategory.getValue() == color.EndLine));
    public Setting<Integer> endLineBlue = register(new Setting<>("End Line Blue", 0, 0, 255, v -> cCate(categories.Render) && render.getValue() && colorCategory.getValue() == color.EndLine));
    public Setting<Integer> endLineAlpha = register(new Setting<>("End Line Alpha", 200, 0, 255, v -> cCate(categories.Render) && render.getValue() && colorCategory.getValue() == color.EndLine));

    // Start Fill
    public Setting<Integer> startFillRed = register(new Setting<>("Start Fill Red", 255, 0, 255, v -> cCate(categories.Render) && render.getValue() && colorCategory.getValue() == color.StartFill));
    public Setting<Integer> startFillGreen = register(new Setting<>("Start Fill Green", 0, 0, 255, v -> cCate(categories.Render) && render.getValue() && colorCategory.getValue() == color.StartFill));
    public Setting<Integer> startFillBlue = register(new Setting<>("Start Fill Blue", 0, 0, 255, v -> cCate(categories.Render) && render.getValue() && colorCategory.getValue() == color.StartFill));
    public Setting<Integer> startFillAlpha = register(new Setting<>("Start Fill Alpha", 120, 0, 255, v -> cCate(categories.Render) && render.getValue() && colorCategory.getValue() == color.StartFill));

    // End Fill
    public Setting<Integer> endFillRed = register(new Setting<>("End Fill Red", 47, 0, 255, v -> cCate(categories.Render) && render.getValue() && colorCategory.getValue() == color.EndFill));
    public Setting<Integer> endFillGreen = register(new Setting<>("End Fill Green", 255, 0, 255, v -> cCate(categories.Render) && render.getValue() && colorCategory.getValue() == color.EndFill));
    public Setting<Integer> endFillBlue = register(new Setting<>("End Fill Blue", 0, 0, 255, v -> cCate(categories.Render) && render.getValue() && colorCategory.getValue() == color.EndFill));
    public Setting<Integer> endFillAlpha = register(new Setting<>("End Fill Alpha", 120, 0, 255, v -> cCate(categories.Render) && render.getValue() && colorCategory.getValue() == color.EndFill));

    public SpeedMine() {
        super("SpeedMine", "", Category.MISC, true, false, false);
        instance = this;
    }

    private static SpeedMine instance;
    public static BlockPos minePosition;
    private Direction mineFacing;
    private int mineBreaks;
    public static float progress, prevProgress;
    public boolean worth = false;

    private final Timer attackTimer = new Timer();

    @Override
    public void onEnable() {
        reset();
    }

    @Override
    public void onDisable() {
        reset();
    }

    @Override
    public void onUpdate() {
        if (mc.player == null || mc.world == null || mc.interactionManager == null || mc.player.getAbilities().creativeMode) return;

        switch (mode.getValue()){
            case Packet -> {
                if (minePosition != null) {
                    if (mineBreaks >= breakAttempts.getValue() || mc.player.squaredDistanceTo(minePosition.toCenterPos()) > range.getPow2Value()) {
                        reset();
                        return;
                    }
                    if (progress == 0 && !mc.world.isAir(minePosition) && attackTimer.passedMs(800)) {
                        mc.interactionManager.attackBlock(minePosition, mineFacing);
                        mc.player.swingHand(Hand.MAIN_HAND);
                        attackTimer.reset();
                    }
                }

                if (minePosition != null && !mc.world.isAir(minePosition)) {
                    int invPickSlot = getTool(minePosition);
                    int hotBarPickSlot = InventoryUtil.getPickAxeHotbar().slot();
                    int prevSlot = -1;

                    if (invPickSlot == -1 && switchMode.getValue() == SwitchMode.Alternative) return;
                    if (hotBarPickSlot == -1 && switchMode.getValue() != SwitchMode.Alternative) return;

                    if (progress >= 1) {
                        if (switchMode.getValue() == SwitchMode.Alternative) {
                            mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, invPickSlot < 9 ? invPickSlot + 36 : invPickSlot, mc.player.getInventory().selectedSlot, SlotActionType.SWAP, mc.player);
                            closeScreen();
                        } else if (switchMode.getValue() == SwitchMode.Normal || switchMode.getValue() == SwitchMode.Silent) {
                            prevSlot = mc.player.getInventory().selectedSlot;
                            InventoryUtil.getPickAxeHotbar().switchTo();
                        }

                        if (stop.getValue())
                            mc.player.networkHandler.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, minePosition, mineFacing));
                        if (abort.getValue())
                            mc.player.networkHandler.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK, minePosition, mineFacing));
                        if (start.getValue())
                            mc.player.networkHandler.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, minePosition, mineFacing));
                        if (stop2.getValue())
                            mc.player.networkHandler.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, minePosition, mineFacing));

                        if (switchMode.getValue() == SwitchMode.Alternative) {
                            if (swapDelay.getValue() != 0) {
                                Ngm.asyncManager.run(() -> {
                                    mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, invPickSlot < 9 ? invPickSlot + 36 : invPickSlot, mc.player.getInventory().selectedSlot, SlotActionType.SWAP, mc.player);
                                    closeScreen();
                                }, swapDelay.getValue());
                            } else {
                                mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, invPickSlot < 9 ? invPickSlot + 36 : invPickSlot, mc.player.getInventory().selectedSlot, SlotActionType.SWAP, mc.player);
                                closeScreen();
                            }
                        } else if (switchMode.getValue() == SwitchMode.Silent) {
                            InventoryUtil.switchTo(prevSlot);
                        }

                        progress = 0;
                        mineBreaks++;
                    }
                    prevProgress = progress;
                    progress += getBlockStrength(mc.world.getBlockState(minePosition), minePosition);
                } else {
                    progress = 0;
                    prevProgress = 0;
                }

                if (rotate.getValue() && progress > 0.95 && minePosition != null && mc.player != null) {
                    float[] angle = PlayerManager.calcAngle(mc.player.getEyePos(), minePosition.toCenterPos());
                    RotationUtil rUtil = new RotationUtil();
                    rUtil.fixRotation = angle[0];
                }
            }
            case GrimInstant -> {
                if (minePosition != null) {
                    if (mc.player.squaredDistanceTo(minePosition.toCenterPos()) > range.getPow2Value()) {
                        reset();
                        return;
                    }
                }

                if (minePosition != null) {
                    if (mc.world.isAir(minePosition))
                        return;

                    int invPickSlot = getTool(minePosition);
                    int hotBarPickSlot = InventoryUtil.getPickAxeHotbar().slot();
                    int prevSlot = -1;

                    if (invPickSlot == -1 && switchMode.getValue() == SwitchMode.Alternative) return;
                    if (hotBarPickSlot == -1 && switchMode.getValue() != SwitchMode.Alternative) return;

                    if (progress >= 1) {
                        if (switchMode.getValue() == SwitchMode.Alternative) {
                            mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, invPickSlot < 9 ? invPickSlot + 36 : invPickSlot, mc.player.getInventory().selectedSlot, SlotActionType.SWAP, mc.player);
                            closeScreen();
                        } else if (switchMode.getValue() == SwitchMode.Normal || switchMode.getValue() == SwitchMode.Silent) {
                            prevSlot = mc.player.getInventory().selectedSlot;
                            InventoryUtil.getPickAxeHotbar().switchTo();
                        }

                        mc.player.networkHandler.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, minePosition, mineFacing));

                        if (switchMode.getValue() == SwitchMode.Alternative) {
                            if (swapDelay.getValue() != 0) {
                                Ngm.asyncManager.run(() -> {
                                    mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, invPickSlot < 9 ? invPickSlot + 36 : invPickSlot, mc.player.getInventory().selectedSlot, SlotActionType.SWAP, mc.player);
                                    closeScreen();
                                }, swapDelay.getValue());
                            } else {
                                mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, invPickSlot < 9 ? invPickSlot + 36 : invPickSlot, mc.player.getInventory().selectedSlot, SlotActionType.SWAP, mc.player);
                                closeScreen();
                            }
                        } else if (switchMode.getValue() == SwitchMode.Silent) {
                            InventoryUtil.switchTo(prevSlot);
                        }

                        progress = 0;
                        mineBreaks++;
                    }
                    prevProgress = progress;
                    progress += getBlockStrength(mc.world.getBlockState(minePosition), minePosition) * (mineBreaks >= 1 ? rebreakfactor.getValue() : 1);
                } else {
                    progress = 0;
                    prevProgress = 0;
                }
            }
        }
    }

    @Subscribe
    public void onAttackBlock(@NotNull AttackBlockEvent event) {
        if (mc.player != null
                && canBreak(event.getBlockPos())
                && !mc.player.getAbilities().creativeMode
                && (mode.getValue() == Mode.Packet || mode.getValue() == Mode.GrimInstant)
                && !event.getBlockPos().equals(minePosition)) {
            addBlockToMine(event.getBlockPos(), event.getEnumFacing(), true);
        }
    }

    @Subscribe
    private void onSync(SyncEvent event) {
        if (rotate.getValue() && progress > 0.95 && minePosition != null && mc.player != null) {
            float[] angle = PlayerManager.calcAngle(mc.player.getEyePos(), minePosition.toCenterPos());

            mc.player.setYaw(angle[0]);
            mc.player.setPitch(angle[1]);
        }
    }

    @Subscribe
    private void onPacketSend(PacketEvent.SendPost e) {
        if (e.getPacket() instanceof UpdateSelectedSlotC2SPacket && resetOnSwitch.getValue() && switchMode.getValue() != SwitchMode.Silent && mode.getValue() != Mode.GrimInstant) {
            addBlockToMine(minePosition, mineFacing, true);
        }
    }

    public void reset() {
        minePosition = null;
        mineFacing = null;
        progress = 0;
        mineBreaks = 0;
        prevProgress = 0;
    }

    private void closeScreen() {
        if (mc.player == null) return;

        mc.player.networkHandler.sendPacket(new CloseHandledScreenC2SPacket(mc.player.currentScreenHandler.syncId));
    }

    private boolean checkWorth() {
        return checkWorth(7.5f, minePosition);
    }

    public boolean checkWorth(float damage, BlockPos pos) {
        if (isDisabled()
                || pos == null
                || mc.world == null
                || progress < 0.95
                || mc.world.getBlockState(pos).getBlock() != Blocks.OBSIDIAN)
            return false;

        for (PlayerEntity player : Ngm.asyncManager.getAsyncPlayers()) {
            if (player == null
                    || player == mc.player
                    || Ngm.friendManager.isFriend(player))
                continue;

            BlockState currentState = mc.world.getBlockState(pos);
            mc.world.removeBlock(pos, false);
            float dmg = PlayerUtil.getExplosionDamage(pos.toCenterPos(), player);
            mc.world.setBlockState(pos, currentState);

            if (dmg > damage)
                return true;
        }

        return false;
    }

    private float getBlockStrength(@NotNull BlockState state, BlockPos position) {
        if (state == Blocks.AIR.getDefaultState()) {
            return 0.02f;
        }

        float hardness = state.getHardness(mc.world, position);

        if (hardness < 0)
            return 0;
        return getDigSpeed(state, position) / hardness / (canBreak(position) ? 30f : 100f);
    }

    private float getDestroySpeed(BlockPos position, BlockState state) {
        float destroySpeed = 1;
        int slot = getTool(position);

        if (mc.player == null)
            return 0;
        if (slot != -1 && mc.player.getInventory().getStack(slot) != null && !mc.player.getInventory().getStack(slot).isEmpty()) {
            destroySpeed *= mc.player.getInventory().getStack(slot).getMiningSpeedMultiplier(state);
        }

        return destroySpeed;
    }

    public float getDigSpeed(BlockState state, BlockPos position) {
        if (mc.player == null) return 0;
        float digSpeed = getDestroySpeed(position, state);

        if (digSpeed > 1) {
            ItemStack itemstack = mc.player.getInventory().getStack(getTool(position));
            int efficiencyModifier = EnchantmentHelper.getLevel(Enchantments.EFFICIENCY, itemstack);
            if (efficiencyModifier > 0 && !itemstack.isEmpty()) {
                digSpeed += (float) (StrictMath.pow(efficiencyModifier, 2) + 1);
            }
        }

        if (mc.player.hasStatusEffect(StatusEffects.HASTE))
            digSpeed *= 1 + (Objects.requireNonNull(mc.player.getStatusEffect(StatusEffects.HASTE)).getAmplifier() + 1) * 0.2F;


        if (mc.player.hasStatusEffect(StatusEffects.MINING_FATIGUE))
            digSpeed *= (float) Math.pow(0.3f, Objects.requireNonNull(mc.player.getStatusEffect(StatusEffects.MINING_FATIGUE)).getAmplifier() + 1);


        if (mc.player.isSubmergedInWater() && !EnchantmentHelper.hasAquaAffinity(mc.player)) digSpeed /= 5;
        if (!mc.player.isOnGround()) digSpeed /= 5;

        return digSpeed < 0 ? 0 : digSpeed * factor.getValue();
    }

    private int getTool(final BlockPos pos) {
        int index = -1;
        float currentFastest = 1.f;

        if (mc.world == null
                || mc.player == null
                || mc.world.getBlockState(pos).getBlock() instanceof AirBlock)
            return -1;

        for (int i = 9; i < 45; ++i) {
            final ItemStack stack = mc.player.getInventory().getStack(i >= 36 ? i - 36 : i);

            if (stack != ItemStack.EMPTY) {
                if (!(stack.getMaxDamage() - stack.getDamage() > 10))
                    continue;

                final float digSpeed = EnchantmentHelper.getLevel(Enchantments.EFFICIENCY, stack);
                final float destroySpeed = stack.getMiningSpeedMultiplier(mc.world.getBlockState(pos));

                if (digSpeed + destroySpeed > currentFastest) {
                    currentFastest = digSpeed + destroySpeed;
                    index = i;
                }
            }
        }

        return index >= 36 ? index - 36 : index;
    }

    private boolean canBreak(BlockPos pos) {
        if (mc.world == null)
            return false;

        final BlockState blockState = mc.world.getBlockState(pos);
        final Block block = blockState.getBlock();
        return block.getHardness() != -1;
    }

    private @NotNull Color getColor(@NotNull Color startColor, @NotNull Color endColor, float progress) {
        if (!smooth.getValue())
            return progress >= 0.95 ? endColor : startColor;

        final int rDiff = endColor.getRed() - startColor.getRed();
        final int gDiff = endColor.getGreen() - startColor.getGreen();
        final int bDiff = endColor.getBlue() - startColor.getBlue();
        final int aDiff = endColor.getAlpha() - startColor.getAlpha();

        return new Color(
                fixColorValue(startColor.getRed() + (int) (rDiff * progress)),
                fixColorValue(startColor.getGreen() + (int) (gDiff * progress)),
                fixColorValue(startColor.getBlue() + (int) (bDiff * progress)),
                fixColorValue(startColor.getAlpha() + (int) (aDiff * progress)));
    }

    private int fixColorValue(int colorVal) {
        return colorVal > 255 ? 255 : Math.max(colorVal, 0);
    }

    public boolean isWorth() {
        return worth;
    }

    public void addBlockToMine(BlockPos pos, @Nullable Direction facing, boolean allowReMine) {
        if (!allowReMine && (minePosition != null || progress != 0))
            return;
        if (mc.player == null)
            return;

        progress = 0;
        mineBreaks = 0;
        minePosition = pos;
        mineFacing = facing == null ? mc.player.getHorizontalFacing() : facing;

        if (pos != null && mineFacing != null) {
            mc.player.networkHandler.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, pos, mineFacing));
            mc.player.networkHandler.sendPacket(new PlayerActionC2SPacket(startMode.getValue() == StartMode.StartAbort ? PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK : PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, minePosition, mineFacing));
        }
    }

    public static SpeedMine getInstance() {
        return instance;
    }
}
