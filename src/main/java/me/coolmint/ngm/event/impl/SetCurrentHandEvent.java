package me.coolmint.ngm.event.impl;

import me.coolmint.ngm.event.Event;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;

import static me.coolmint.ngm.util.traits.Util.mc;

public class SetCurrentHandEvent extends Event {
    //
    private final Hand hand;

    public SetCurrentHandEvent(Hand hand) {
        this.hand = hand;
    }

    public Hand getHand() {
        return hand;
    }

    public ItemStack getStackInHand() {
        return mc.player.getStackInHand(hand);
    }
}