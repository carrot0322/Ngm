package me.coolmint.ngm.features.modules.client;

import com.google.common.eventbus.Subscribe;
import io.netty.buffer.Unpooled;
import me.coolmint.ngm.event.impl.PacketEvent;
import me.coolmint.ngm.features.modules.Module;
import me.coolmint.ngm.features.settings.Setting;
import me.coolmint.ngm.util.client.ChatUtil;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.BrandCustomPayload;
import net.minecraft.network.packet.c2s.common.CustomPayloadC2SPacket;
import net.minecraft.network.packet.s2c.common.ResourcePackSendS2CPacket;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

public class ClientSpoof extends Module {
    private Setting<Boolean> spoofBrand = register(new Setting<>("SpoofBrand", false));
    private Setting<brandList> brand = register(new Setting<>("Brand", brandList.vanilla, v -> spoofBrand.getValue()));
    private Setting<Boolean> resourcePack = register(new Setting<>("ResourcePack Block", false));

    public enum brandList {
        vanilla,
        FeatherFabric,
        FeatherForge,
        Forge,
        Fabric
    }

    public ClientSpoof() {
        super("ClientSpoof", "", Category.CLIENT, true, false, false);
    }

    public String getBrand() {
        switch (brand.getValue()) {
            case vanilla -> {
                return "vanilla";
            }
            case FeatherFabric -> {
                return "Feather Fabric";
            }
            case FeatherForge -> {
                return "Feather Forge";
            }
            case Forge -> {
                return "Forge";
            }
            case Fabric -> {
                return "Fabric";
            }
        }
        return "vanilla";
    }

    @Subscribe
    private void onPacketRecieve(PacketEvent.ReceivePRE event) {
        if (resourcePack.getValue()) {
            if (!(event.packet instanceof ResourcePackSendS2CPacket packet)) return;
            event.cancel();
            MutableText msg = Text.literal("This server has ");
            msg.append(packet.required() ? "a required " : "an optional ");
            MutableText link = Text.literal("resource pack");
            link.setStyle(link.getStyle()
                    .withColor(Formatting.BLUE)
                    .withUnderline(true)
                    .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, packet.url()))
                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Click to download")))
            );
            msg.append(link);
            msg.append(".");
            ChatUtil.sendInfo("Resourpack Blocked :)");
        }
    }
}