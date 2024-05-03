package me.coolmint.ngm.util.player;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static me.coolmint.ngm.util.traits.Util.mc;

public class BlockUtil {

    private static List<Block> blacklist = Arrays.asList(Blocks.AIR, Blocks.WATER, Blocks.LAVA);

    public static BlockData getBlockData(BlockPos input) {
        List<BlockPos> positions = Arrays.asList(
                input.add(0, -1, 0),
                input.add(-1, 0, 0),
                input.add(1, 0, 0),
                input.add(0, 0, -1),
                input.add(0, 0, 1),
                input.add(-1, 0, 1),
                input.add(1, 0, -1),
                input.add(-1, 0, -1),
                input.add(1, 0, 1)
        );
        for (BlockPos pos : positions) {
            if (!blacklist.contains(mc.world.getBlockState(pos).getBlock())) {
                Direction facing = getFacing(input, pos);
                return new BlockData(pos, facing);
            }
        }
        return null;
    }

    private static Direction getFacing(BlockPos input, BlockPos neighbor) {
        if (input.getX() < neighbor.getX()) {
            return Direction.WEST;
        } else if (input.getX() > neighbor.getX()) {
            return Direction.EAST;
        } else if (input.getZ() < neighbor.getZ()) {
            return Direction.NORTH;
        } else if (input.getZ() > neighbor.getZ()) {
            return Direction.SOUTH;
        } else {
            return Direction.UP;
        }
    }

    public static class BlockData {
        public BlockPos position;
        public Direction direction;

        public BlockData(BlockPos position, Direction direction) {
            this.position = position;
            this.direction = direction;
        }
    }

    public static BlockState getState(BlockPos pos) {
        return mc.world.getBlockState(pos);
    }

    private static VoxelShape getOutlineShape(BlockPos pos) {
        return getState(pos).getOutlineShape(mc.world, pos);
    }

    public static boolean canBeClicked(BlockPos pos) {
        return getOutlineShape(pos) != VoxelShapes.empty();
    }

    public static ArrayList<BlockPos> getAllInBox(BlockPos from, BlockPos to) {
        ArrayList<BlockPos> blocks = new ArrayList<>();

        BlockPos min = new BlockPos(Math.min(from.getX(), to.getX()),
                Math.min(from.getY(), to.getY()), Math.min(from.getZ(), to.getZ()));
        BlockPos max = new BlockPos(Math.max(from.getX(), to.getX()),
                Math.max(from.getY(), to.getY()), Math.max(from.getZ(), to.getZ()));

        for(int x = min.getX(); x <= max.getX(); x++)
            for(int y = min.getY(); y <= max.getY(); y++)
                for(int z = min.getZ(); z <= max.getZ(); z++)
                    blocks.add(new BlockPos(x, y, z));

        return blocks;
    }

    public static ArrayList<BlockPos> getAllInBox(BlockPos center, int range) {
        return getAllInBox(center.add(-range, -range, -range),
                center.add(range, range, range));
    }
}