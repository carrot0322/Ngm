package me.coolmint.ngm.features.modules.render;

import me.coolmint.ngm.features.modules.Module;
import net.minecraft.client.gui.screen.DeathScreen;
import net.minecraft.client.input.Input;
import net.minecraft.client.input.KeyboardInput;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.HungerManager;
import me.coolmint.ngm.features.settings.Setting;
import com.google.common.eventbus.Subscribe;
import me.coolmint.ngm.event.impl.OpenScreenEvent;
import me.coolmint.ngm.event.impl.EventMove;
import me.coolmint.ngm.event.impl.GameLeftEvent;

import java.util.Collection;
import java.util.Map;

public class FreeCam extends Module {

    public Setting<Float> Speed = register(new Setting<>("Speed", 1f, 0.0f, 10.0f));
    public Setting<Boolean> rotate = register(new Setting<>("Rotate", true));
    private Input freecamInput;
    private Entity playerEntity;
    private FreecamEntity freecamEntity;

    public FreeCam() {
        super("FreeCam", "", Module.Category.RENDER, true, false, false);
    }

    @Override
    public void onEnable() {
        if (mc.player == null || mc.world == null) {
            return;
        }
        freecamEntity = new FreecamEntity(mc.world);
        Input input = new KeyboardInput(mc.options);
        input.tick(false, 0.3f);
        freecamInput = input;
        playerEntity = mc.getCameraEntity();
        mc.setCameraEntity(freecamEntity);
    }

    @Override
    public void onDisable() {
        freecamEntity = null;
        mc.setCameraEntity(mc.player);
    }

    @Subscribe
    public void onDisconnect(GameLeftEvent event) {
        disable();
    }

    @Subscribe
    public void onScreenOpen(OpenScreenEvent event) {
        if (event.screen instanceof DeathScreen) {
            disable();
        }
    }

    @Subscribe
    public void onPlayerMove(EventMove event) {
        freecamInput.tick(false, 0.3f);
        freecamEntity.setHealth(mc.player.getHealth());
        freecamEntity.setAbsorptionAmount(mc.player.getAbsorptionAmount());
        freecamEntity.noClip = true;


        freecamEntity.resetPosition();
        freecamEntity.getInventory().clone(mc.player.getInventory());
        freecamEntity.hurtTime = mc.player.hurtTime;
        freecamEntity.maxHurtTime = mc.player.maxHurtTime;
    }

    public static class FreecamEntity extends ClientPlayerEntity {

        public FreecamEntity(ClientWorld world) {
            super(mc, world, mc.player.networkHandler, mc.player.getStatHandler(),
                    mc.player.getRecipeBook(), false, false);
        }

        @Override
        public boolean hasStatusEffect(StatusEffect statusEffect) {
            return mc.player.hasStatusEffect(statusEffect);
        }

        @Override
        public StatusEffectInstance getStatusEffect(StatusEffect statusEffect) {
            return mc.player.getStatusEffect(statusEffect);
        }

        @Override
        public Collection<StatusEffectInstance> getStatusEffects() {
            return mc.player.getStatusEffects();
        }

        @Override
        public Map<StatusEffect, StatusEffectInstance> getActiveStatusEffects() {
            return mc.player.getActiveStatusEffects();
        }

        @Override
        public float getAbsorptionAmount() {
            return mc.player.getAbsorptionAmount();
        }

        @Override
        public int getArmor() {
            return mc.player.getArmor();
        }

        @Override
        public HungerManager getHungerManager() {
            return mc.player.getHungerManager();
        }
    }
}