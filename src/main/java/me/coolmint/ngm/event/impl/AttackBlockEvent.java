package me.coolmint.ngm.event.impl;

import me.coolmint.ngm.event.Event;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class AttackBlockEvent extends Event {
    private BlockPos blockPos;
    private Direction enumFacing;

    public AttackBlockEvent(BlockPos blockPos, Direction enumFacing) {
        this.blockPos = blockPos;
        this.enumFacing = enumFacing;
    }

    public BlockPos getBlockPos() {
        return this.blockPos;
    }

    public void setBlockPos(BlockPos blockPos) {
        this.blockPos = blockPos;
    }

    public Direction getEnumFacing() {
        return this.enumFacing;
    }

    public void setEnumFacing(Direction enumFacing) {
        this.enumFacing = enumFacing;
    }
}