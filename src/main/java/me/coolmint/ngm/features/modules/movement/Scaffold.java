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
    public Setting<Boolean> tower = register(new Setting<>("Tower", true));
    public Setting<Float> towerSpeed = register(new Setting<>("TowerSpeed", 0.5f, 0.0f, 1.0f, v -> tower.getValue()));
    public Setting<Boolean> whileMoving = register(new Setting<>("TowerWhileMoving", true, v -> tower.getValue()));
    public Setting<Boolean> autoSwitch = register(new Setting<>("AutoSwitch", true));
    public Setting<Boolean> rotate = register(new Setting<>("Rotate", true));

    public Scaffold() {
        super("Scaffold", "", Category.MOVEMENT, true, false, false);
    }

    private final BlockPos.Mutable bp = new BlockPos.Mutable();
    private int placeRange = 4;

    @Override
    public void onTick() {
        Vec3d vec = mc.player.getPos().add(mc.player.getVelocity()).add(0, -0.75, 0);
        Vec3d pos = mc.player.getPos();
        bp.set(pos.x, vec.y, pos.z);
        if (mc.options.sneakKey.isPressed() && !mc.options.jumpKey.isPressed() && mc.player.getY() + vec.y > -1) {
            bp.setY(bp.getY() - 1);
        }
        if (bp.getY() >= mc.player.getBlockPos().getY()) {
            bp.setY(mc.player.getBlockPos().getY() - 1);
        }
        BlockPos targetBlock = bp.toImmutable();

        if ((BlockUtils.getPlaceSide(bp) == null)) {
            pos = pos.add(0, -0.98f, 0);
            pos.add(mc.player.getVelocity());

            List<BlockPos> blockPosArray = new ArrayList<>();
            for (int x = (int) (mc.player.getX() - placeRange); x < mc.player.getX() + placeRange; x++) {
                for (int z = (int) (mc.player.getZ() - placeRange); z < mc.player.getZ() + placeRange; z++) {
                    for (int y = (int) Math.max(mc.world.getBottomY(), mc.player.getY() - placeRange); y < Math.min(mc.world.getTopY(), mc.player.getY() + placeRange); y++) {
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

        place(bp);

        FindItemResult result = InvUtils.findInHotbar(itemStack -> validItem(itemStack, bp));
        if (tower.getValue() && mc.options.jumpKey.isPressed() && !mc.options.sneakKey.isPressed() && result.found() && (autoSwitch.getValue() || result.getHand() != null)) {
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
        return scaffolding() && tower.getValue() && mc.options.jumpKey.isPressed() && !mc.options.sneakKey.isPressed() &&
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

        if (BlockUtils.place(bp, item, rotate.getValue(), 50, true, true)) {
            return true;
        }
        return false;
    }
}
