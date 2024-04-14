package me.coolmint.ngm.features.command.impl;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.coolmint.ngm.features.command.Command;
import me.coolmint.ngm.features.modules.Module;
import net.minecraft.command.CommandSource;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class TcpCommand extends Command {

    public TcpCommand() {
        super("tcp", "bypasstcp", "minecraft");
    }

    @Override
    public void executeBuild(@NotNull LiteralArgumentBuilder<CommandSource> builder) {
        builder.executes(context -> {
            new Thread(() -> {
                try {
                    TimeUnit.SECONDS.sleep(3);
                    sendMessage("attack sent.");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();

            return SINGLE_SUCCESS;
        });
    }
}
