package me.coolmint.ngm.features.command.impl;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.coolmint.ngm.Ngm;
import me.coolmint.ngm.features.command.Command;
import me.coolmint.ngm.features.command.args.FriendArgumentType;
import me.coolmint.ngm.features.command.args.PlayerListEntryArgumentType;
import me.coolmint.ngm.util.ChatUtil;
import net.minecraft.command.CommandSource;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class FriendCommand extends Command {
    public FriendCommand(){super("friend");}

    @Override
    public void executeBuild(@NotNull LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(literal("add").then(arg("player", PlayerListEntryArgumentType.create()).executes(context -> {
            GameProfile profile = PlayerListEntryArgumentType.get(context).getProfile();

            if (!Ngm.friendManager.isFriend(profile.getName())) {
                Ngm.friendManager.addFriend(profile.getName());
                ChatUtil.sendInfo("Added %s %s %s to friends.".formatted(Formatting.GREEN, profile.getName(), Formatting.RESET));
            }
            else ChatUtil.sendInfo("Already friends with that player.");

            return SINGLE_SUCCESS;
        })));

        builder.then(literal("remove").then(arg("player", FriendArgumentType.create()).executes(context -> {
            String nickname = context.getArgument("player", String.class);

            Ngm.friendManager.removeFriend(nickname);
            ChatUtil.sendInfo(nickname + " has been unfriended");
            return SINGLE_SUCCESS;
        })));

        builder.executes(context -> {
            if (Ngm.friendManager.getFriends().isEmpty()) {
                ChatUtil.sendInfo("Friend list empty D:");
            } else {
                StringBuilder f = new StringBuilder("Friends: ");
                for (String friend : Ngm.friendManager.getFriends()) {
                    try {
                        f.append(friend).append(", ");
                    } catch (Exception ignored) {
                    }
                }
                ChatUtil.sendInfo(f.toString());
            }
            return SINGLE_SUCCESS;
        });
    }
}
