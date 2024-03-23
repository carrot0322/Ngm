package me.coolmint.ngm.mixin;

import me.coolmint.ngm.Ngm;
import net.minecraft.nbt.NbtTagSizeTracker;
import net.minecraft.network.PacketByteBuf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(PacketByteBuf.class)
public abstract class PacketByteBufMixin {
    @ModifyArg(method = "readNbt()Lnet/minecraft/nbt/NbtCompound;", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/PacketByteBuf;readNbt(Lnet/minecraft/nbt/NbtSizeTracker;)Lnet/minecraft/nbt/NbtElement;"))
    private NbtTagSizeTracker xlPackets(NbtTagSizeTracker sizeTracker) {
        return Ngm.moduleManager.isModuleEnabled("AntiPacketKick") ? NbtTagSizeTracker.ofUnlimitedBytes() : sizeTracker;
    }
}