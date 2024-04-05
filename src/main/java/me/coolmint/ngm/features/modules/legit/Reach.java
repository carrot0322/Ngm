package me.coolmint.ngm.features.modules.legit;

import com.google.common.eventbus.Subscribe;
import me.coolmint.ngm.event.impl.ReachEvent;
import me.coolmint.ngm.features.modules.Module;
import me.coolmint.ngm.features.settings.Setting;

public class Reach extends Module {
    public Setting<Float> reach = register(new Setting<>("ReachAdd", 0.00f, 0.00f, 3.00f));

    public Reach() {
        super("Reach", "", Category.LEGIT, true, false, false);
    }

    @Subscribe
    public void onReach(ReachEvent event) {
        event.cancel();
        event.setReach(reach.getValue());
    }
}
