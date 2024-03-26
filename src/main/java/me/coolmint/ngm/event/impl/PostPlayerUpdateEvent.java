package me.coolmint.ngm.event.impl;

import me.coolmint.ngm.event.Event;

public class PostPlayerUpdateEvent extends Event {
    private int iterations;

    public int getIterations() {
        return iterations;
    }

    public void setIterations(int in) {
        iterations = in;
    }
}