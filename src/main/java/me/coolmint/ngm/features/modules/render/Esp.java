package me.coolmint.ngm.features.modules.render;

import com.google.common.eventbus.Subscribe;
import me.coolmint.ngm.Ngm;
import me.coolmint.ngm.event.impl.EntityOutlineEvent;
import me.coolmint.ngm.event.impl.TeamColorEvent;
import me.coolmint.ngm.features.modules.Module;
import me.coolmint.ngm.features.settings.Setting;
import me.coolmint.ngm.util.entity.EntityUtil;
import net.minecraft.block.entity.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;

import java.awt.*;

public class Esp extends Module {
    Setting<ESPMode> mode = register(new Setting<>("Mode", ESPMode.GLOW));
    Setting<Float> width = register(new Setting<>("Linewidth", 0.1f, 1.25f, 5.0f));
    Setting<Boolean> players = register(new Setting<>("Players", true));
    Setting<Color> playersColor = register(new Setting<>("PlayersColor", new Color(200, 60, 60, 255), v -> players.getValue()));
    Setting<Boolean> monsters = register(new Setting<>("Monsters", true));
    Setting<Color> monstersColor = register(new Setting<>("MonstersColor", new Color(200, 60, 60, 255), v -> monsters.getValue()));
    Setting<Boolean> animals = register(new Setting<>("Animals", true));
    Setting<Color> animalsColor = register(new Setting<>("AnimalsColor", new Color(0, 200, 0, 255), v -> animals.getValue()));
    Setting<Boolean> vehicles = register(new Setting<>("Vehicles", false));
    Setting<Color> vehiclesColor = register(new Setting<>("VehiclesColor", new Color(200, 100, 0, 255), v -> vehicles.getValue()));
    Setting<Boolean> items = register(new Setting<>("Items", false));
    Setting<Color> itemsColor = register(new Setting<>("ItemsColor", new Color(200, 100, 0, 255), v -> items.getValue()));
    Setting<Boolean> crystals = register(new Setting<>("EndCrystals", false));
    Setting<Color> crystalsColor = register(new Setting<>("EndCrystalsColor", new Color(200, 100, 200, 255), v -> crystals.getValue()));
    Setting<Boolean> chests = register(new Setting<>("Chests", true));
    Setting<Color> chestsColor = register(new Setting<>("ChestsColor", new Color(200, 200, 101, 255), v -> chests.getValue()));
    Setting<Boolean> echests = register(new Setting<>("EnderChests", true));
    Setting<Color> echestsColor = register(new Setting<>("EnderChestsColor", new Color(155, 0, 200, 255), v -> echests.getValue()));
    Setting<Boolean> shulkers = register(new Setting<>("Shulkers", true));
    Setting<Color> shulkersColor = register(new Setting<>("ShulkersColor", new Color(200, 0, 106, 255), v -> shulkers.getValue()));
    Setting<Boolean> hoppers = register(new Setting<>("Hoppers", false));
    Setting<Color> hoppersColor = register(new Setting<>("HoppersColor", new Color(100, 100, 100, 255), v -> hoppers.getValue()));
    Setting<Boolean> furnaces = register(new Setting<>("Furnaces", false));
    Setting<Color> furnacesColor = register(new Setting<>("FurnacesColor", new Color(100, 100, 100, 255), v -> furnaces.getValue()));


    public Esp() {
        super("Esp", "", Category.RENDER, true, false, false);
    }

    @Subscribe
    public void onEntityOutline(EntityOutlineEvent event) {
        if (mode.getValue() == ESPMode.GLOW && checkESP(event.getEntity())) {
            event.cancel();
        }
    }

    @Subscribe
    public void onTeamColor(TeamColorEvent event) {
        if (mode.getValue() == ESPMode.GLOW && checkESP(event.getEntity())) {
            event.cancel();
            event.setColor(Ngm.colorManager.getColor().getRGB());
        }
    }

    public Color getStorageESPColor(BlockEntity tileEntity) {
        if (tileEntity instanceof ChestBlockEntity) {
            return chestsColor.getValue();
        }
        if (tileEntity instanceof EnderChestBlockEntity) {
            return echestsColor.getValue();
        }
        if (tileEntity instanceof ShulkerBoxBlockEntity) {
            return shulkersColor.getValue();
        }
        if (tileEntity instanceof HopperBlockEntity) {
            return hoppersColor.getValue();
        }
        if (tileEntity instanceof FurnaceBlockEntity) {
            return furnacesColor.getValue();
        }
        return null;
    }

    public Color getESPColor(Entity entity) {
        if (entity instanceof PlayerEntity) {
            return playersColor.getValue();
        }
        if (EntityUtil.isMonster(entity)) {
            return monstersColor.getValue();
        }
        if (EntityUtil.isNeutral(entity) || EntityUtil.isPassive(entity)) {
            return animalsColor.getValue();
        }
        if (EntityUtil.isVehicle(entity)) {
            return vehiclesColor.getValue();
        }
        if (entity instanceof EndCrystalEntity) {
            return crystalsColor.getValue();
        }
        if (entity instanceof ItemEntity) {
            return itemsColor.getValue();
        }
        return null;
    }

    public boolean checkESP(Entity entity) {
        return entity != mc.player && entity instanceof PlayerEntity && players.getValue()
                || EntityUtil.isMonster(entity) && monsters.getValue()
                || (EntityUtil.isNeutral(entity)
                || EntityUtil.isPassive(entity)) && animals.getValue()
                || EntityUtil.isVehicle(entity) && vehicles.getValue()
                || entity instanceof EndCrystalEntity && crystals.getValue()
                || entity instanceof ItemEntity && items.getValue();
    }

    public enum ESPMode {
        // OUTLINE,
        GLOW
    }
}
