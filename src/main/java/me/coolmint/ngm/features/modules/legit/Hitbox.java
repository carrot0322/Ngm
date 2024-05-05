package me.coolmint.ngm.features.modules.legit;

import com.google.common.eventbus.Subscribe;
import me.coolmint.ngm.event.impl.EntityMarginEvent;
import me.coolmint.ngm.event.impl.HitboxEvent;
import me.coolmint.ngm.event.impl.RenderHitboxEvent;
import me.coolmint.ngm.features.modules.Module;
import me.coolmint.ngm.features.settings.Setting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;

public class Hitbox extends Module {
    public Setting<Float> expand = register(new Setting<>("Expand", 0.5f, 0f, 20.0f));
    public Setting<Boolean> everything = register(new Setting<>("everything", false));
    public Setting<Boolean> player = register(new Setting<>("player", true, v -> !everything.getValue()));
    public Setting<Boolean> self = register(new Setting<>("Self", false));
    public Setting<Boolean> crystal = register(new Setting<>("crystal", true, v -> !everything.getValue()));

    public Hitbox() {
        super("HitBox", "", Category.LEGIT, true, false, false);
    }

    public Entity entity;

    @Subscribe
    public void onEntityMargin(EntityMarginEvent event) {
        if (event.equals(mc.player))
            return;

        if(everything.getValue()){
            event.setMargin(event.getMargin() + expand.getValue() / 10);
            event.cancel();
        }

        if (event.getEntity() instanceof PlayerEntity && !player.getValue() || event.getEntity() instanceof EndCrystalEntity && !crystal.getValue())
            return;

        event.setMargin(event.getMargin() + expand.getValue() / 10);
        event.cancel();
    }

    @Subscribe
    public void onHitbox(HitboxEvent event) {
        if(!self.getValue() && event.getEntity().equals(mc.player))
            return;

        if(everything.getValue()){
            event.setBox(event.getBox().expand(expand.getValue() / 10));
            event.cancel();
        }

        if (event.getEntity() instanceof PlayerEntity && !player.getValue() || event.getEntity() instanceof EndCrystalEntity && !crystal.getValue())
            return;

        event.setBox(event.getBox().expand(expand.getValue() / 10));
        event.cancel();
    }

    @Subscribe
    public void onRenderHitbox(RenderHitboxEvent event) {
        entity = event.getEntity();
    }
}
