package me.coolmint.ngm.util.player;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;

import static java.lang.Double.isNaN;
import static me.coolmint.ngm.util.traits.Util.mc;

public class BlockInteractionHelper {
    public static boolean checkForNeighbours(BlockPos blockPos)
    {
        // check if we don't have a block adjacent to blockpos
        if (!hasNeighbour(blockPos))
        {
            // find air adjacent to blockpos that does have a block adjacent to it, let's fill this first as to form a bridge between the player and the original blockpos. necessary if the player is
            // going diagonal.
            for (Direction side : Direction.values())
            {
                BlockPos neighbour = blockPos.offset(side);
                if (hasNeighbour(neighbour))
                {
                    return true;
                }

                if (side == Direction.UP && mc.world.getBlockState(blockPos).getBlock() == Blocks.WATER)
                {
                    if (mc.world.getBlockState(blockPos.up()).getBlock() == Blocks.AIR /*&& ModuleManager.Get().GetMod(LiquidInteractModule.class).isEnabled()*/)
                        return true;
                }
            }
            return false;
        }
        return true;
    }

    public static boolean hasNeighbour(BlockPos blockPos)
    {
        for (Direction side : Direction.values())
        {
            BlockPos neighbour = blockPos.offset(side);
            if (!mc.world.getBlockState(neighbour).isReplaceable())
            {
                return true;
            }
        }
        return false;
    }

    public enum ValidResult
    {
        NoEntityCollision,
        AlreadyBlockThere,
        NoNeighbors,
        Ok,
    }

    public static ValidResult valid(BlockPos pos)
    {
        // There are no entities to block placement,
        if (!mc.world.canCollide(mc.player, new Box(pos)))
            return ValidResult.NoEntityCollision;

        /*
        if (mc.world.getBlockState(pos.down()).getBlock() == Blocks.WATER)
            if (ModuleManager.Get().GetMod(LiquidInteractModule.class).isEnabled())
                return ValidResult.Ok;
         */

        if (!BlockInteractionHelper.checkForNeighbours(pos))
            return ValidResult.NoNeighbors;

        BlockState l_State = mc.world.getBlockState(pos);

        if (l_State.getBlock() == Blocks.AIR)
        {
            final BlockPos[] l_Blocks =
                    { pos.north(), pos.south(), pos.east(), pos.west(), pos.up(), pos.down() };

            for (BlockPos l_Pos : l_Blocks)
            {
                BlockState l_State2 = mc.world.getBlockState(l_Pos);

                if (l_State2.getBlock() == Blocks.AIR)
                    continue;

                for (final Direction side : Direction.values())
                {
                    final BlockPos neighbor = pos.offset(side);

                    boolean l_IsWater = mc.world.getBlockState(neighbor).getBlock() == Blocks.WATER;
                    BlockState block = mc.world.getBlockState(neighbor);

                    if (block.isSolidBlock(mc.player.getWorld(), neighbor) /*|| (l_IsWater && ModuleManager.Get().GetMod(LiquidInteractModule.class).isEnabled())*/)
                    {
                        return ValidResult.Ok;
                    }
                }
            }

            return ValidResult.NoNeighbors;
        }

        return ValidResult.AlreadyBlockThere;
    }

    public static float[] getFacingRotations(int x, int y, int z, Direction facing) {
        return getFacingRotations(x, y, z, facing, 1);
    }

    public static float[] getFacingRotations(int x, int y, int z, Direction facing, double width) {
        return getRotationsForPosition(x + 0.5 + facing.getVector().getX() * width / 2.0, y + 0.5 + facing.getVector().getY() * width / 2.0, z + 0.5 + facing.getVector().getZ() * width / 2.0);
    }

    public static float[] getRotationsForPosition(double x, double y, double z) {
        return getRotationsForPosition(x, y, z, mc.player.getX(), mc.player.getY() + mc.player.getEyeY(), mc.player.getZ());
    }

    public static float[] getRotationsForPosition(double x, double y, double z, double sourceX, double sourceY, double sourceZ) {
        double deltaX = x - sourceX;
        double deltaY = y - sourceY;
        double deltaZ = z - sourceZ;

        double yawToEntity;

        if (deltaZ < 0 && deltaX < 0) { // quadrant 3
            yawToEntity = 90D + Math.toDegrees(Math.atan(deltaZ / deltaX)); // 90
            // degrees
            // forward
        } else if (deltaZ < 0 && deltaX > 0) { // quadrant 4
            yawToEntity = -90D + Math.toDegrees(Math.atan(deltaZ / deltaX)); // 90
            // degrees
            // back
        } else { // quadrants one or two
            yawToEntity = Math.toDegrees(-Math.atan(deltaX / deltaZ));
        }

        double distanceXZ = Math.sqrt(deltaX * deltaX + deltaZ
                * deltaZ);

        double pitchToEntity = -Math.toDegrees(Math.atan(deltaY / distanceXZ));

        yawToEntity = wrapAngleTo180((float) yawToEntity);
        pitchToEntity = wrapAngleTo180((float) pitchToEntity);

        yawToEntity = isNaN(yawToEntity) ? 0 : yawToEntity;
        pitchToEntity = isNaN(pitchToEntity) ? 0 : pitchToEntity;

        return new float[]{(float) yawToEntity, (float) pitchToEntity};
    }

    public static float wrapAngleTo180(float angle) {
        angle %= 360.0F;

        while (angle >= 180.0F) {
            angle -= 360.0F;
        }
        while (angle < -180.0F) {
            angle += 360.0F;
        }

        return angle;
    }
}
