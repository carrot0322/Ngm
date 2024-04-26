package me.coolmint.ngm.features.command.impl;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.coolmint.ngm.features.command.Command;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.GameModeArgumentType;
import net.minecraft.world.GameMode;
import org.jetbrains.annotations.NotNull;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class GamemodeCommand extends Command {
    public GamemodeCommand() {
        super("gamemode", "gm");
    }

    @Override
    public void executeBuild(@NotNull LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(arg("gamemode", GameModeArgumentType.gameMode()).executes(context -> {
            final GameMode gamemode = context.getArgument("gamemode", GameMode.class);
            mc.interactionManager.setGameMode(gamemode);
            return SINGLE_SUCCESS;
        }));
    }
}
