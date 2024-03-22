package me.coolmint.ngm.event.impl;

import me.coolmint.ngm.event.Event;
import me.coolmint.ngm.event.Stage;

public class UpdateWalkingPlayerEvent extends Event {
    private final Stage stage;

    public UpdateWalkingPlayerEvent(Stage stage) {
        this.stage = stage;
    }

    public Stage getStage() {
        return stage;
    }
}
