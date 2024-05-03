package me.coolmint.ngm;

import me.coolmint.ngm.util.client.Auth;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.metadata.ModMetadata;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Ngm implements ModInitializer, ClientModInitializer {
    public static final ModMetadata MOD_META = FabricLoader.getInstance().getModContainer("ngm").orElseThrow().getMetadata();
    public static final String NAME = MOD_META.getName();
    public static final String VERSION = MOD_META.getVersion().getFriendlyString();;

    public static float TICK_TIMER = 1f;

    public static final Logger LOGGER = LogManager.getLogger("Ngm");
    public static me.coolmint.ngm.manager.ServerManager serverManager;
    public static me.coolmint.ngm.manager.ColorManager colorManager;
    public static me.coolmint.ngm.manager.RotationManager rotationManager;
    public static me.coolmint.ngm.manager.PositionManager positionManager;
    public static me.coolmint.ngm.manager.EventManager eventManager;
    public static me.coolmint.ngm.manager.SpeedManager speedManager;
    public static me.coolmint.ngm.manager.CommandManager commandManager;
    public static me.coolmint.ngm.manager.FriendManager friendManager;
    public static me.coolmint.ngm.manager.ModuleManager moduleManager;
    public static me.coolmint.ngm.manager.ConfigManager configManager;
    public static me.coolmint.ngm.manager.PlayerManager playerManager;

    @Override public void onInitialize() {
        eventManager = new me.coolmint.ngm.manager.EventManager();
        serverManager = new me.coolmint.ngm.manager.ServerManager();
        rotationManager = new me.coolmint.ngm.manager.RotationManager();
        positionManager = new me.coolmint.ngm.manager.PositionManager();
        friendManager = new me.coolmint.ngm.manager.FriendManager();
        colorManager = new me.coolmint.ngm.manager.ColorManager();
        moduleManager = new me.coolmint.ngm.manager.ModuleManager();
        commandManager = new me.coolmint.ngm.manager.CommandManager();
        speedManager = new me.coolmint.ngm.manager.SpeedManager();
        playerManager = new me.coolmint.ngm.manager.PlayerManager();
    }


    @Override public void onInitializeClient() {
        if (!Auth.auth()) {
            LOGGER.warn("[{}] Invalid Hwid", Ngm.NAME);
            System.exit(523);
        }

        eventManager.init();
        moduleManager.init();

        configManager = new me.coolmint.ngm.manager.ConfigManager();
        configManager.load();
        colorManager.init();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> configManager.save()));
    }
}
