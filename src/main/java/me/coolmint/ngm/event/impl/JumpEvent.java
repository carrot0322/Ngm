package me.coolmint.ngm.event.impl;

import me.coolmint.ngm.event.Event;

public class JumpEvent extends Event {
    private float motion;
    private float yaw;

    public JumpEvent(float motion, float yaw) {
        this.motion = motion;
        this.yaw = yaw;
    }

    public float getMotion() {
        return motion;
    }

    public void setMotion(float motion) {
        this.motion = motion;
    }

    public float getYaw() {
        return yaw;
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
    }
}
