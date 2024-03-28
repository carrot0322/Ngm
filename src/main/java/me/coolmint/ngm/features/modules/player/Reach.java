package me.coolmint.ngm.features.modules.player;

import com.google.common.eventbus.Subscribe;
import me.coolmint.ngm.event.impl.ReachEvent;
import me.coolmint.ngm.features.modules.Module;
import me.coolmint.ngm.features.settings.Setting;

public class Reach extends Module {
    public Setting<Float> reach = register(new Setting<>("ReachAdd", 0f, 0f, 3f));

    public Reach() {
        super("Reach", "", Category.PLAYER, true, false, false);
    }

    @Subscribe
    public void onReach(ReachEvent event) {
        event.cancel();
        event.setReach(reach.getValue());
    }
}
