package me.coolmint.ngm.features.command.impl;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.coolmint.ngm.Ngm;
import me.coolmint.ngm.features.command.Command;
import me.coolmint.ngm.features.command.args.ModuleArgumentType;
import me.coolmint.ngm.features.modules.Module;
import net.minecraft.command.CommandSource;
import org.jetbrains.annotations.NotNull;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class ToggleCommand extends Command {
    public ToggleCommand() {
        super("toggle");
    }

    @Override
    public void executeBuild(@NotNull LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(arg("module", ModuleArgumentType.create()).executes(context -> {
            final Module module = context.getArgument("module", Module.class);

            Ngm.moduleManager.toggleModule(module.getName());

            return SINGLE_SUCCESS;
        }));
    }
}