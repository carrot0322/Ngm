package me.coolmint.ngm.features.command.impl;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.coolmint.ngm.features.command.Command;
import net.minecraft.command.CommandSource;
import org.jetbrains.annotations.NotNull;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class MethodCommand extends Command {

    public MethodCommand() {
        super("method");
    }

    @Override
    public void executeBuild(@NotNull LiteralArgumentBuilder<CommandSource> builder) {
        builder.executes(context -> {
            sendMessage("method for reichC2.");
            sendMessage("tcp");
            sendMessage("bypasstcp");
            sendMessage("minecraft");
            return SINGLE_SUCCESS;
        });
    }
}