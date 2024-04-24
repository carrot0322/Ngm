package me.coolmint.ngm.event.impl;

import me.coolmint.ngm.event.Event;

public class SyncEvent extends Event {
    public SyncEvent(float yaw, float pitch) {
        this.yaw = yaw;
        this.pitch = pitch;
    }

    float yaw;
    float pitch;

    public float getYaw() {
        return yaw;
    }

    public float getPitch() {
        return pitch;
    }
}