package me.coolmint.ngm.event.impl;

import me.coolmint.ngm.event.Event;
import net.minecraft.util.math.Vec3d;

public class MoveEvent extends Event {
    public double x, y, z;
    public Vec3d movement;

    public MoveEvent(double x, double y, double z, Vec3d movement) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.movement = movement;
    }

    public void setY(double y) {
        this.y = y;
    }

    public void setZ(double z) {
        this.z = z;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getX() {
        return this.x;
    }

    public double getY() {
        return this.y;
    }

    public double getZ() {
        return this.z;
    }
}