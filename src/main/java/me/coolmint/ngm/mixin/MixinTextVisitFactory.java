package me.coolmint.ngm.mixin;

import me.coolmint.ngm.features.modules.misc.KoreanDetector;
import net.minecraft.text.TextVisitFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import static me.coolmint.ngm.util.traits.Util.mc;

@Mixin(TextVisitFactory.class)
public abstract class MixinTextVisitFactory {
    @ModifyArg(at = @At(value = "INVOKE",
            target = "Lnet/minecraft/text/TextVisitFactory;visitFormatted(Ljava/lang/String;ILnet/minecraft/text/Style;Lnet/minecraft/text/Style;Lnet/minecraft/text/CharacterVisitor;)Z",
            ordinal = 0),
            method = {
                    "visitFormatted(Ljava/lang/String;ILnet/minecraft/text/Style;Lnet/minecraft/text/CharacterVisitor;)Z"},
            index = 0)
    private static String adjustText(String text) {
        KoreanDetector koreanDetector = new KoreanDetector();
        if (mc.getWindow() != null)
            return koreanDetector.replaceName(text);
        else return text;
    }
}