package me.coolmint.ngm.features.command.impl;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.coolmint.ngm.Ngm;
import me.coolmint.ngm.features.command.Command;
import me.coolmint.ngm.features.modules.client.ClickGuiModule;
import me.coolmint.ngm.util.client.ChatUtil;
import net.minecraft.command.CommandSource;
import net.minecraft.util.Formatting;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class PrefixCommand extends Command {
    public PrefixCommand() {
        super("prefix");
    }

    @Override
    public void executeBuild(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(arg("prefix", StringArgumentType.string()).executes(context -> {
            final String prefix = context.getArgument("prefix", String.class);

            ClickGuiModule.getInstance().prefix.setValue(String.valueOf(prefix.charAt(0)));
            Ngm.commandManager.setPrefix(prefix);
            ChatUtil.sendInfo("Prefix set to " + Formatting.DARK_GRAY + Ngm.commandManager.getPrefix());

            return SINGLE_SUCCESS;
        }));
    }
}
