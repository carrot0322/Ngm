package me.coolmint.ngm.event.impl;

import me.coolmint.ngm.event.Event;
import net.minecraft.entity.Entity;

public class AttackEvent extends Event {
    private Entity entity;
    boolean pre;

    public AttackEvent(Entity entity, boolean pre){
        this.entity = entity;
        this.pre = pre;
    }

    public Entity getEntity(){
        return  entity;
    }

    public boolean isPre(){
        return pre;
    }
}