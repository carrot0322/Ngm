package me.coolmint.ngm.features.command.args;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import me.coolmint.ngm.Ngm;
import me.coolmint.ngm.features.modules.Module;
import net.minecraft.command.CommandSource;
import net.minecraft.text.Text;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class FriendArgumentType implements ArgumentType<String> {
    private static final List<String> EXAMPLES = Ngm.friendManager.getFriends().stream().limit(5).toList();

    public static FriendArgumentType create() {
        return new FriendArgumentType();
    }

    @Override
    public String parse(StringReader reader) throws CommandSyntaxException {
        String friend = reader.readString();
        if (!Ngm.friendManager.isFriend(friend)) throw new DynamicCommandExceptionType(
                name -> Text.literal("Friend with name " + name.toString() + " does not exists(")
        ).create(friend);

        return friend;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return CommandSource.suggestMatching(Ngm.friendManager.getFriends(), builder);
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}
