package me.coolmint.ngm.event.impl;

import me.coolmint.ngm.event.Event;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;

public class VelocityMultiplierEvent extends Event {
    private final BlockState state;

    public VelocityMultiplierEvent(BlockState state) {
        this.state = state;
    }

    public Block getBlock() {
        return state.getBlock();
    }
}