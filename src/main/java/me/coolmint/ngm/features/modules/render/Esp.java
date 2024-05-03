package me.coolmint.ngm.features.modules.render;

import com.google.common.eventbus.Subscribe;
import me.coolmint.ngm.event.impl.EntityOutlineEvent;
import me.coolmint.ngm.event.impl.TeamColorEvent;
import me.coolmint.ngm.features.modules.Module;
import me.coolmint.ngm.features.settings.Setting;
import me.coolmint.ngm.util.player.EntityUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;

import java.awt.*;

public class Esp extends Module {
    enum EspSetting {
        Render,
        Entity,
        Color
    }

    enum Modes{
        Glow
    }

    enum EntityList{
        Player,
        Monster,
        Animal,
        Vehicle,
        Item,
        Crystal
    }

    public Setting<EspSetting> category = register(new Setting<>("Category", EspSetting.Render));

    // Render
    public Setting<Modes> mode = register(new Setting<>("Mode", Modes.Glow, v -> category.getValue() == EspSetting.Render));

    // Entity
    public Setting<Boolean> player = register(new Setting<>("Players", true, v -> category.getValue() == EspSetting.Entity));
    public Setting<Boolean> monster = register(new Setting<>("Monsters", true, v -> category.getValue() == EspSetting.Entity));
    public Setting<Boolean> animal = register(new Setting<>("Animals", false, v -> category.getValue() == EspSetting.Entity));
    public Setting<Boolean> vehicle = register(new Setting<>("Vehicles", false, v -> category.getValue() == EspSetting.Entity));
    public Setting<Boolean> item = register(new Setting<>("Items", false, v -> category.getValue() == EspSetting.Entity));
    public Setting<Boolean> crystal = register(new Setting<>("Crystals", false, v -> category.getValue() == EspSetting.Entity));

    // Color
    public Setting<EntityList> colorEntity = register(new Setting<>("Entity", EntityList.Player));

    //Player
    public Setting<Integer> playerRed = register(new Setting<>("Red", 255, 0, 255, v -> category.getValue() == EspSetting.Color && colorEntity.getValue() == EntityList.Player));
    public Setting<Integer> playerGreen = register(new Setting<>("Green", 87, 0, 255, v -> category.getValue() == EspSetting.Color && colorEntity.getValue() == EntityList.Player));
    public Setting<Integer> playerBlue = register(new Setting<>("Blue", 51, 0, 255, v -> category.getValue() == EspSetting.Color && colorEntity.getValue() == EntityList.Player));

    //Monster
    public Setting<Integer> monsterRed = register(new Setting<>("Red", 255, 0, 255, v -> category.getValue() == EspSetting.Color && colorEntity.getValue() == EntityList.Monster));
    public Setting<Integer> monsterGreen = register(new Setting<>("Green", 100, 0, 255, v -> category.getValue() == EspSetting.Color && colorEntity.getValue() == EntityList.Monster));
    public Setting<Integer> monsterBlue = register(new Setting<>("Blue", 0, 0, 255, v -> category.getValue() == EspSetting.Color && colorEntity.getValue() == EntityList.Monster));

    //Animal
    public Setting<Integer> animalRed = register(new Setting<>("Red", 100, 0, 255, v -> category.getValue() == EspSetting.Color && colorEntity.getValue() == EntityList.Animal));
    public Setting<Integer> animalGreen = register(new Setting<>("Green", 255, 0, 255, v -> category.getValue() == EspSetting.Color && colorEntity.getValue() == EntityList.Animal));
    public Setting<Integer> animalBlue = register(new Setting<>("Blue", 80, 0, 255, v -> category.getValue() == EspSetting.Color && colorEntity.getValue() == EntityList.Animal));

    //Vehicle
    public Setting<Integer> vehicleRed = register(new Setting<>("Red", 80, 0, 255, v -> category.getValue() == EspSetting.Color && colorEntity.getValue() == EntityList.Vehicle));
    public Setting<Integer> vehicleGreen = register(new Setting<>("Green", 255, 0, 255, v -> category.getValue() == EspSetting.Color && colorEntity.getValue() == EntityList.Vehicle));
    public Setting<Integer> vehicleBlue = register(new Setting<>("Blue", 255, 0, 255, v -> category.getValue() == EspSetting.Color && colorEntity.getValue() == EntityList.Vehicle));

    //Item
    public Setting<Integer> itemRed = register(new Setting<>("Red", 255, 0, 255, v -> category.getValue() == EspSetting.Color && colorEntity.getValue() == EntityList.Item));
    public Setting<Integer> itemGreen = register(new Setting<>("Green", 255, 0, 255, v -> category.getValue() == EspSetting.Color && colorEntity.getValue() == EntityList.Item));
    public Setting<Integer> itemBlue = register(new Setting<>("Blue", 50, 0, 255, v -> category.getValue() == EspSetting.Color && colorEntity.getValue() == EntityList.Item));

    //Crystal
    public Setting<Integer> crystalRed = register(new Setting<>("Red", 255, 0, 255, v -> category.getValue() == EspSetting.Color && colorEntity.getValue() == EntityList.Crystal));
    public Setting<Integer> crystalGreen = register(new Setting<>("Green", 50, 0, 255, v -> category.getValue() == EspSetting.Color && colorEntity.getValue() == EntityList.Crystal));
    public Setting<Integer> crystalBlue = register(new Setting<>("Blue", 245, 0, 255, v -> category.getValue() == EspSetting.Color && colorEntity.getValue() == EntityList.Crystal));

    public Esp() {
        super("Esp", "", Category.RENDER, true, false, false);
    }

    @Subscribe
    public void onEntityOutline(EntityOutlineEvent event) {
        if (mode.getValue() == Modes.Glow && checkESP(event.getEntity())) {
            event.cancel();
        }
    }

    @Subscribe
    public void onTeamColor(TeamColorEvent event) {
        if (mode.getValue() == Modes.Glow    && checkESP(event.getEntity())) {
            event.cancel();
            event.setColor(getESPColor(event.getEntity()).getRGB());
        }
    }

    public Color getESPColor(Entity entity) {
        if (entity instanceof PlayerEntity) {
            return new Color(playerRed.getValue(), playerGreen.getValue(), playerBlue.getValue());
        }
        if (EntityUtil.isMonster(entity)) {
            return new Color(monsterRed.getValue(), monsterGreen.getValue(), monsterBlue.getValue());
        }
        if (EntityUtil.isNeutral(entity) || EntityUtil.isPassive(entity)) {
            return new Color(animalRed.getValue(), animalGreen.getValue(), animalBlue.getValue());
        }
        if (EntityUtil.isVehicle(entity)) {
            return new Color(vehicleRed.getValue(), vehicleGreen.getValue(), vehicleBlue.getValue());
        }
        if (entity instanceof EndCrystalEntity) {
            return new Color(crystalRed.getValue(), crystalGreen.getValue(), crystalBlue.getValue());
        }
        if (entity instanceof ItemEntity) {
            return new Color(itemRed.getValue(), itemGreen.getValue(), itemBlue.getValue());
        }
        return null;
    }

    public boolean checkESP(Entity entity) {
        return entity != mc.player && entity instanceof PlayerEntity && player.getValue()
                || EntityUtil.isMonster(entity) && monster.getValue()
                || (EntityUtil.isNeutral(entity)
                || EntityUtil.isPassive(entity)) && animal.getValue()
                || EntityUtil.isVehicle(entity) && vehicle.getValue()
                || entity instanceof EndCrystalEntity && crystal.getValue()
                || entity instanceof ItemEntity && item.getValue();
    }
}
