package me.coolmint.ngm.event.impl;

import net.minecraft.entity.Entity;
import me.coolmint.ngm.event.Event;

public class EventAttack extends Event {
    private Entity entity;

    public EventAttack(Entity entity){
        this.entity = entity;
    }

    public Entity getEntity(){
        return  entity;
    }
}