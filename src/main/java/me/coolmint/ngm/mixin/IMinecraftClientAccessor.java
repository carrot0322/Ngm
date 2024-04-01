package me.coolmint.ngm.mixin;

import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MinecraftClient.class)
public interface IMinecraftClientAccessor {

    @Mutable
    @Accessor("itemUseCooldown")
    public void setItemUseCooldown(int itemUseCooldown);
}