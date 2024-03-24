package me.coolmint.ngm.mixin;

import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.message.LastSeenMessagesCollector;
import net.minecraft.network.message.MessageChain;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.resource.featuretoggle.FeatureSet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ClientPlayNetworkHandler.class)
public interface IClientPlayNetworkHandlerAccessor {
    @Accessor("chunkLoadDistance")
    int getChunkLoadDistance();

    @Accessor("messagePacker")
    MessageChain.Packer getMessagePacker();

    @Accessor("lastSeenMessagesCollector")
    LastSeenMessagesCollector getLastSeenMessagesCollector();

    @Accessor("combinedDynamicRegistries")
    DynamicRegistryManager.Immutable getCombinedDynamicRegistries();

    @Accessor("enabledFeatures")
    FeatureSet getEnabledFeatures();
}