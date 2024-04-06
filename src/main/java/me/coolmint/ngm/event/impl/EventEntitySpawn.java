package me.coolmint.ngm.event.impl;

import net.minecraft.entity.Entity;
import me.coolmint.ngm.event.Event;

public class EventEntitySpawn extends Event {
    private final Entity entity;
    public EventEntitySpawn(Entity entity) {
        this.entity = entity;
    }

    public Entity getEntity() {
        return this.entity;
    }
}