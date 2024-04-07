package me.coolmint.ngm.features.modules.movement;


import com.google.common.collect.Streams;
import me.coolmint.ngm.features.modules.Module;
import me.coolmint.ngm.features.settings.Setting;
import me.coolmint.ngm.util.player.FindItemResult;
import me.coolmint.ngm.util.player.InvUtils;
import me.coolmint.ngm.util.player.PlayerUtils;
import me.coolmint.ngm.util.world.BlockUtils;
import net.minecraft.block.Block;
import net.minecraft.block.FallingBlock;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Scaffold extends Module {
    public Setting<Boolean> fastTower = register(new Setting<>("FastTower", true));
    public Setting<Double> towerSpeed = register(new Setting<>("Delay", 0.5d, 0.0d, 1.0d, v -> fastTower.getValue()));
    public Setting<Boolean> whileMoving = register(new Setting<>("WhileMoving", true, v -> fastTower.getValue()));
    public Setting<Boolean> swing = register(new Setting<>("Swing", true));
    public Setting<Boolean> autoSwitch = register(new Setting<>("AutoSwitch", true));
    public Setting<Boolean> rotate = register(new Setting<>("Rotate", true));
    public Setting<Boolean> airPlace = register(new Setting<>("AirPlace", false));
    public Setting<Double> aheadDistance = register(new Setting<>("AheadDistance", 0.0d, 0.0d, 1.0d, v -> !airPlace.getValue()));
    public Setting<Double> placeRange = register(new Setting<>("Range", 4.0d, 0.0d, 8.0d, v -> !airPlace.getValue()));
    public Setting<Double> radius = register(new Setting<>("Radius", 0.0d, 0.0d, 6.0d, v -> airPlace.getValue()));
    public Setting<Double> blocksPerTick = register(new Setting<>("BlocksPerTick", 3.0d, 1.0d, 20.0d, v -> airPlace.getValue()));

    public Scaffold() {
        super("Scaffold", "", Category.MOVEMENT, true, false, false);
    }

    private final BlockPos.Mutable bp = new BlockPos.Mutable();

    @Override
    public void onTick() {
        Vec3d vec = mc.player.getPos().add(mc.player.getVelocity()).add(0, -0.75, 0);
        if (airPlace.getValue()) {
            bp.set(vec.getX(), vec.getY(), vec.getZ());
        } else {
            Vec3d pos = mc.player.getPos();
            if (aheadDistance.getValue() != 0 && !towering() && !mc.world.getBlockState(mc.player.getBlockPos().down()).getCollisionShape(mc.world, mc.player.getBlockPos()).isEmpty()) {
                Vec3d dir = Vec3d.fromPolar(0, mc.player.getYaw()).multiply(aheadDistance.getValue(), 0, aheadDistance.getValue());
                if (mc.options.forwardKey.isPressed()) pos = pos.add(dir.x, 0, dir.z);
                if (mc.options.backKey.isPressed()) pos = pos.add(-dir.x, 0, -dir.z);
                if (mc.options.leftKey.isPressed()) pos = pos.add(dir.z, 0, -dir.x);
                if (mc.options.rightKey.isPressed()) pos = pos.add(-dir.z, 0, dir.x);
            }
            bp.set(pos.x, vec.y, pos.z);
        }
        if (mc.options.sneakKey.isPressed() && !mc.options.jumpKey.isPressed() && mc.player.getY() + vec.y > -1) {
            bp.setY(bp.getY() - 1);
        }
        if (bp.getY() >= mc.player.getBlockPos().getY()) {
            bp.setY(mc.player.getBlockPos().getY() - 1);
        }
        BlockPos targetBlock = bp.toImmutable();

        if (!airPlace.getValue() && (BlockUtils.getPlaceSide(bp) == null)) {
            Vec3d pos = mc.player.getPos();
            pos = pos.add(0, -0.98f, 0);
            pos.add(mc.player.getVelocity());

            List<BlockPos> blockPosArray = new ArrayList<>();
            for (int x = (int) (mc.player.getX() - placeRange.getValue()); x < mc.player.getX() + placeRange.getValue(); x++) {
                for (int z = (int) (mc.player.getZ() - placeRange.getValue()); z < mc.player.getZ() + placeRange.getValue(); z++) {
                    for (int y = (int) Math.max(mc.world.getBottomY(), mc.player.getY() - placeRange.getValue()); y < Math.min(mc.world.getTopY(), mc.player.getY() + placeRange.getValue()); y++) {
                        bp.set(x, y, z);
                        if (BlockUtils.getPlaceSide(bp) == null) continue;
                        if (!BlockUtils.canPlace(bp)) continue;
                        if (mc.player.getEyePos().squaredDistanceTo(Vec3d.ofCenter(bp.offset(BlockUtils.getClosestPlaceSide(bp)))) > 36) continue;
                        blockPosArray.add(new BlockPos(bp));
                    }
                }
            }
            if (blockPosArray.isEmpty()) return;

            blockPosArray.sort(Comparator.comparingDouble((blockPos) -> blockPos.getSquaredDistance(targetBlock)));

            bp.set(blockPosArray.get(0));
        }

        if (airPlace.getValue()) {
            List<BlockPos> blocks = new ArrayList<>();
            for (int x = (int) (bp.getX() - radius.getValue()); x <= bp.getX() + radius.getValue(); x++) {
                for (int z = (int) (bp.getZ() - radius.getValue()); z <= bp.getZ() + radius.getValue(); z++) {
                    BlockPos blockPos = BlockPos.ofFloored(x, bp.getY(), z);
                    if (mc.player.getPos().distanceTo(Vec3d.ofCenter(blockPos)) <= radius.getValue() || (x == bp.getX() && z == bp.getZ())) {
                        blocks.add(blockPos);
                    }
                }
            }

            if (!blocks.isEmpty()) {
                blocks.sort(Comparator.comparingDouble(PlayerUtils::squaredDistanceTo));
                int counter = 0;
                for (BlockPos block : blocks) {
                    if (place(block)) {
                        counter++;
                    }

                    if (counter >= blocksPerTick.getValue()) {
                        break;
                    }
                }
            }
        } else {
            place(bp);
        }

        FindItemResult result = InvUtils.findInHotbar(itemStack -> validItem(itemStack, bp));
        if (fastTower.getValue() && mc.options.jumpKey.isPressed() && !mc.options.sneakKey.isPressed() && result.found() && (autoSwitch.getValue() || result.getHand() != null)) {
            Vec3d velocity = mc.player.getVelocity();
            Box playerBox = mc.player.getBoundingBox();
            if (Streams.stream(mc.world.getBlockCollisions(mc.player, playerBox.offset(0, 1, 0))).toList().isEmpty()) {
                // If there is no block above the player: move the player up, so he can place another block
                if (whileMoving.getValue() || !PlayerUtils.isMoving()) {
                    velocity = new Vec3d(velocity.x, towerSpeed.getValue(), velocity.z);
                }
                mc.player.setVelocity(velocity);
            } else {
                // If there is a block above the player: move the player down, so he's on top of the placed block
                mc.player.setVelocity(velocity.x, Math.ceil(mc.player.getY()) - mc.player.getY(), velocity.z);
                mc.player.setOnGround(true);
            }
        }
    }

    public boolean scaffolding() {
        return isEnabled();
    }

    public boolean towering() {
        FindItemResult result = InvUtils.findInHotbar(itemStack -> validItem(itemStack, bp));
        return scaffolding() && fastTower.getValue() && mc.options.jumpKey.isPressed() && !mc.options.sneakKey.isPressed() &&
                (whileMoving.getValue() || !PlayerUtils.isMoving()) && result.found() && (autoSwitch.getValue() || result.getHand() != null);
    }

    private boolean validItem(ItemStack itemStack, BlockPos pos) {
        if (!(itemStack.getItem() instanceof BlockItem)) return false;

        Block block = ((BlockItem) itemStack.getItem()).getBlock();

        if (!Block.isShapeFullCube(block.getDefaultState().getCollisionShape(mc.world, pos))) return false;
        return !(block instanceof FallingBlock) || !FallingBlock.canFallThrough(mc.world.getBlockState(pos));
    }

    private boolean place(BlockPos bp) {
        FindItemResult item = InvUtils.findInHotbar(itemStack -> validItem(itemStack, bp));
        if (!item.found()) return false;

        if (item.getHand() == null && !autoSwitch.getValue()) return false;

        if (BlockUtils.place(bp, item, rotate.getValue(), 50, swing.getValue(), true)) {
            // Render block if was placed
            /*
            if (render.getValue())
                RenderUtils.renderTickingBlock(bp.toImmutable(), sideColor.getValue(), lineColor.getValue(), shapeMode.getValue(), 0, 8, true, false);

             */
            return true;
        }
        return false;
    }
}
