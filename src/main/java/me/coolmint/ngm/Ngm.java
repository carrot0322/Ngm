package me.coolmint.ngm;

import com.google.common.eventbus.EventBus;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Ngm implements ModInitializer, ClientModInitializer {
    public static final String NAME = "Ngm";
    public static final String VERSION = "1.0.0-dev";

    public static float TIMER = 1f;

    public static final Logger LOGGER = LogManager.getLogger("Ngm");
    public static me.coolmint.ngm.manager.ServerManager serverManager;
    public static me.coolmint.ngm.manager.ColorManager colorManager;
    public static me.coolmint.ngm.manager.RotationManager rotationManager;
    public static me.coolmint.ngm.manager.PositionManager positionManager;
    public static me.coolmint.ngm.manager.HoleManager holeManager;
    public static me.coolmint.ngm.manager.EventManager eventManager;
    public static me.coolmint.ngm.manager.SpeedManager speedManager;
    public static me.coolmint.ngm.manager.CommandManager commandManager;
    public static me.coolmint.ngm.manager.FriendManager friendManager;
    public static me.coolmint.ngm.manager.ModuleManager moduleManager;
    public static me.coolmint.ngm.manager.ConfigManager configManager;
    public static me.coolmint.ngm.manager.PlayerManager playerManager;
    public static me.coolmint.ngm.manager.NetworkManager networkManager;
    public static me.coolmint.ngm.manager.TickManager tickManager;


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
        holeManager = new me.coolmint.ngm.manager.HoleManager();
        playerManager = new me.coolmint.ngm.manager.PlayerManager();
        networkManager = new me.coolmint.ngm.manager.NetworkManager();
        tickManager = new me.coolmint.ngm.manager.TickManager();
    }


    @Override public void onInitializeClient() {
        LOGGER.info("[{}] Initializing Client", NAME);
        eventManager.init();
        moduleManager.init();

        configManager = new me.coolmint.ngm.manager.ConfigManager();
        configManager.load();
        colorManager.init();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> configManager.save()));
    }
}
