package me.coolmint.ngm.event.impl;

import me.coolmint.ngm.event.Event;
import net.minecraft.entity.MovementType;

public class EventSync extends Event {
    public EventSync(float yaw, float pitch, double x1, double y1, double z1) {
        this.yaw = yaw;
        this.pitch = pitch;
        X = x1;
        Y = y1;
        Z = z1;
    }

    float yaw;
    float pitch;

    public float getYaw() {
        return yaw;
    }

    public float getPitch() {
        return pitch;
    }

    public double X;
    public double Y;
    public double Z;
}