package me.coolmint.ngm.event.impl;

import me.coolmint.ngm.event.Event;

public class EventPlayerJump extends Event {
    private boolean pre;

    public EventPlayerJump(boolean pre) {
        this.pre = pre;
    }

    public boolean isPre() {
        return pre;
    }
}