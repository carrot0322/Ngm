package me.coolmint.ngm.manager;

import com.mojang.brigadier.CommandDispatcher;
import me.coolmint.ngm.features.command.Command;
import me.coolmint.ngm.features.command.impl.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientCommandSource;
import net.minecraft.command.CommandSource;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class CommandManager {
    private String prefix = ".";

    private final CommandDispatcher<CommandSource> dispatcher = new CommandDispatcher<>();
    private final CommandSource source = new ClientCommandSource(null, MinecraftClient.getInstance());
    private final List<Command> commands = new ArrayList<>();

    public CommandManager() {
        add(new BindCommand());
        add(new FriendCommand());
        add(new GamemodeCommand());
        add(new PrefixCommand());
        add(new ToggleCommand());
        add(new VClipCommand());
    }

    private void add(@NotNull Command command) {
        command.register(dispatcher);
        commands.add(command);
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public Command get(Class<? extends Command> commandClass) {
        for (Command command : commands)
            if (command.getClass().equals(commandClass)) return command;

        return null;
    }

    public List<Command> getCommands() {
        return commands;
    }

    public CommandSource getSource() {
        return source;
    }

    public CommandDispatcher<CommandSource> getDispatcher() {
        return dispatcher;
    }
}