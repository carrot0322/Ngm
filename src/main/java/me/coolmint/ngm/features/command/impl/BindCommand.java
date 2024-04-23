package me.coolmint.ngm.features.command.impl;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.coolmint.ngm.Ngm;
import me.coolmint.ngm.features.command.Command;
import me.coolmint.ngm.features.command.args.ModuleArgumentType;
import me.coolmint.ngm.features.modules.Module;
import me.coolmint.ngm.util.ChatUtil;
import net.minecraft.client.util.InputUtil;
import net.minecraft.command.CommandSource;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static me.coolmint.ngm.util.client.KeyboardUtil.getShortKeyName;

public class BindCommand extends Command {
    public BindCommand() {
        super("bind");
    }

    @Override
    public void executeBuild(@NotNull LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(arg("module", ModuleArgumentType.create())
                .then(arg("key", StringArgumentType.word()).executes(context -> {
                    final Module module = context.getArgument("module", Module.class);
                    final String stringKey = context.getArgument("key", String.class);

                    if (stringKey == null) {
                        ChatUtil.sendInfo(module.getName() + " is bound to " + Formatting.GRAY + module.getBind());
                        return SINGLE_SUCCESS;
                    }

                    int key;
                    if (stringKey.equalsIgnoreCase("none") || stringKey.equalsIgnoreCase("null")) {
                        key = -1;
                    } else {
                        try {
                            key = InputUtil.fromTranslationKey("key.keyboard." + stringKey.toLowerCase()).getCode();
                        } catch (NumberFormatException e) {
                            ChatUtil.sendError("There is no such button");
                            return SINGLE_SUCCESS;
                        }
                    }


                    if (key == 0) {
                        ChatUtil.sendError("Unknown key '" + stringKey + "'!");
                        return SINGLE_SUCCESS;
                    }
                    module.setBind(key);

                    ChatUtil.sendInfo("Bind for " + Formatting.GREEN + module.getName() + Formatting.WHITE + " set to " + Formatting.GRAY + stringKey.toUpperCase());

                    return SINGLE_SUCCESS;
                }))
        );

        builder.then(literal("list").executes(context -> {
            StringBuilder binds = new StringBuilder("Binds: ");
            for (Module feature : Ngm.moduleManager.modules) {
                if (!Objects.equals(feature.getBind().toString(), "None")) {
                    binds.append("\n- " + feature.getName() + " -> " + getShortKeyName(feature));
                }
            }
            ChatUtil.sendInfo(binds.toString());
            return SINGLE_SUCCESS;
        }));

        builder.then(literal("reset").executes(context -> {
            for (Module mod : Ngm.moduleManager.modules) mod.setBind(-1);
            ChatUtil.sendInfo("Done!");
            return SINGLE_SUCCESS;
        }));
    }
}