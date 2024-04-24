package me.coolmint.ngm.event.impl;

import me.coolmint.ngm.event.Event;

public class Render3DEvent extends Event {
    private final float delta;

    public Render3DEvent(float delta) {
        this.delta = delta;
    }

    public float getDelta() {
        return delta;
    }
}
