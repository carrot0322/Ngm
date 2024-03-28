package me.coolmint.ngm.event.impl;

import me.coolmint.ngm.event.Event;

public class ReachEvent extends Event {
    private float reach;

    public float getReach() {
        return reach;
    }

    public void setReach(float reach) {
        this.reach = reach;
    }
}