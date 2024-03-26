package me.coolmint.ngm.event.impl;

import me.coolmint.ngm.event.Event;

public class TickCounterEvent extends Event {
    private float ticks;

    public float getTicks() {
        return ticks;
    }

    public void setTicks(float ticks) {
        this.ticks = ticks;
    }
}