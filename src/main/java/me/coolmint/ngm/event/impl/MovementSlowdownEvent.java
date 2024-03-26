package me.coolmint.ngm.event.impl;

import me.coolmint.ngm.event.Event;
import net.minecraft.client.input.Input;

public class MovementSlowdownEvent extends Event {
    public final Input input;

    public MovementSlowdownEvent(Input input) {
        this.input = input;
    }

    public Input getInput() {
        return input;
    }
}