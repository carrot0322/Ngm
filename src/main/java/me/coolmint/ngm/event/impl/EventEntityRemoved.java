package me.coolmint.ngm.event.impl;

import net.minecraft.entity.Entity;
import me.coolmint.ngm.event.Event;

public class EventEntityRemoved extends Event {
    public Entity entity;

    public EventEntityRemoved(Entity entity) {
        this.entity = entity;
    }
}