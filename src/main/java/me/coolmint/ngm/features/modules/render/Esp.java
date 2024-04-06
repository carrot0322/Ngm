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
    Setting<ESPMode> modeConfig = register(new Setting<>("Mode", ESPMode.GLOW));
    Setting<Float> widthConfig = register(new Setting<>("Linewidth", 0.1f, 1.25f, 5.0f));
    Setting<Boolean> playersConfig = register(new Setting<>("Players", true));
    Setting<Color> playersColorConfig = register(new Setting<>("PlayersColor", new Color(200, 60, 60, 255), v -> playersConfig.getValue()));
    Setting<Boolean> monstersConfig = register(new Setting<>("Monsters", true));
    Setting<Color> monstersColorConfig = register(new Setting<>("MonstersColor", new Color(200, 60, 60, 255), v -> monstersConfig.getValue()));
    Setting<Boolean> animalsConfig = register(new Setting<>("Animals", true));
    Setting<Color> animalsColorConfig = register(new Setting<>("AnimalsColor", new Color(0, 200, 0, 255), v -> animalsConfig.getValue()));
    Setting<Boolean> vehiclesConfig = register(new Setting<>("Vehicles", false));
    Setting<Color> vehiclesColorConfig = register(new Setting<>("VehiclesColor", new Color(200, 100, 0, 255), v -> vehiclesConfig.getValue()));
    Setting<Boolean> itemsConfig = register(new Setting<>("Items", false));
    Setting<Color> itemsColorConfig = register(new Setting<>("ItemsColor", new Color(200, 100, 0, 255), v -> itemsConfig.getValue()));
    Setting<Boolean> crystalsConfig = register(new Setting<>("EndCrystals", false));
    Setting<Color> crystalsColorConfig = register(new Setting<>("EndCrystalsColor", new Color(200, 100, 200, 255), v -> crystalsConfig.getValue()));
    Setting<Boolean> chestsConfig = register(new Setting<>("Chests", true));
    Setting<Color> chestsColorConfig = register(new Setting<>("ChestsColor", new Color(200, 200, 101, 255), v -> chestsConfig.getValue()));
    Setting<Boolean> echestsConfig = register(new Setting<>("EnderChests", true));
    Setting<Color> echestsColorConfig = register(new Setting<>("EnderChestsColor", new Color(155, 0, 200, 255), v -> echestsConfig.getValue()));
    Setting<Boolean> shulkersConfig = register(new Setting<>("Shulkers", true));
    Setting<Color> shulkersColorConfig = register(new Setting<>("ShulkersColor", new Color(200, 0, 106, 255), v -> shulkersConfig.getValue()));
    Setting<Boolean> hoppersConfig = register(new Setting<>("Hoppers", false));
    Setting<Color> hoppersColorConfig = register(new Setting<>("HoppersColor", new Color(100, 100, 100, 255), v -> hoppersConfig.getValue()));
    Setting<Boolean> furnacesConfig = register(new Setting<>("Furnaces", false));
    Setting<Color> furnacesColorConfig = register(new Setting<>("FurnacesColor", new Color(100, 100, 100, 255), v -> furnacesConfig.getValue()));


    public Esp() {
        super("Esp", "", Category.RENDER, true, false, false);
    }

    @Subscribe
    public void onEntityOutline(EntityOutlineEvent event) {
        if (modeConfig.getValue() == ESPMode.GLOW && checkESP(event.getEntity())) {
            event.cancel();
        }
    }

    @Subscribe
    public void onTeamColor(TeamColorEvent event) {
        if (modeConfig.getValue() == ESPMode.GLOW && checkESP(event.getEntity())) {
            event.cancel();
            event.setColor(Ngm.colorManager.getColor().getRGB());
        }
    }

    public Color getStorageESPColor(BlockEntity tileEntity) {
        if (tileEntity instanceof ChestBlockEntity) {
            return chestsColorConfig.getValue();
        }
        if (tileEntity instanceof EnderChestBlockEntity) {
            return echestsColorConfig.getValue();
        }
        if (tileEntity instanceof ShulkerBoxBlockEntity) {
            return shulkersColorConfig.getValue();
        }
        if (tileEntity instanceof HopperBlockEntity) {
            return hoppersColorConfig.getValue();
        }
        if (tileEntity instanceof FurnaceBlockEntity) {
            return furnacesColorConfig.getValue();
        }
        return null;
    }

    public Color getESPColor(Entity entity) {
        if (entity instanceof PlayerEntity) {
            return playersColorConfig.getValue();
        }
        if (EntityUtil.isMonster(entity)) {
            return monstersColorConfig.getValue();
        }
        if (EntityUtil.isNeutral(entity) || EntityUtil.isPassive(entity)) {
            return animalsColorConfig.getValue();
        }
        if (EntityUtil.isVehicle(entity)) {
            return vehiclesColorConfig.getValue();
        }
        if (entity instanceof EndCrystalEntity) {
            return crystalsColorConfig.getValue();
        }
        if (entity instanceof ItemEntity) {
            return itemsColorConfig.getValue();
        }
        return null;
    }

    public boolean checkESP(Entity entity) {
        return entity != mc.player && entity instanceof PlayerEntity && playersConfig.getValue()
                || EntityUtil.isMonster(entity) && monstersConfig.getValue()
                || (EntityUtil.isNeutral(entity)
                || EntityUtil.isPassive(entity)) && animalsConfig.getValue()
                || EntityUtil.isVehicle(entity) && vehiclesConfig.getValue()
                || entity instanceof EndCrystalEntity && crystalsConfig.getValue()
                || entity instanceof ItemEntity && itemsConfig.getValue();
    }

    public enum ESPMode {
        // OUTLINE,
        GLOW
    }
}
