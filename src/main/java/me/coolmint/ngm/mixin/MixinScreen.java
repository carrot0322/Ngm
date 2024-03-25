package me.coolmint.ngm.mixin;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.coolmint.ngm.Ngm;
import me.coolmint.ngm.manager.CommandManager;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Style;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;

@Mixin(Screen.class)
public abstract class MixinScreen {
    @Inject(method = "handleTextClick", at = @At(value = "INVOKE", target = "Lorg/slf4j/Logger;error(Ljava/lang/String;Ljava/lang/Object;)V", ordinal = 1, remap = false), cancellable = true)
    private void onRunCommand(Style style, CallbackInfoReturnable<Boolean> cir) {
        if (Objects.requireNonNull(style.getClickEvent()).getValue().startsWith(Ngm.commandManager.getPrefix()))
            try {
                CommandManager manager = Ngm.commandManager;
                manager.getDispatcher().execute(style.getClickEvent().getValue().substring(Ngm.commandManager.getPrefix().length()), manager.getSource());
                cir.setReturnValue(true);
            } catch (CommandSyntaxException ignored) {
            }
    }
}