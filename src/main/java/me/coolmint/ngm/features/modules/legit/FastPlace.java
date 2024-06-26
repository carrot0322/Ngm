package me.coolmint.ngm.features.modules.legit;

import me.coolmint.ngm.features.modules.Module;
import me.coolmint.ngm.mixin.IMinecraftClientAccessor;
import net.minecraft.client.MinecraftClient;

public class FastPlace extends Module {
    public FastPlace() {
        super("FastPlace", "", Category.LEGIT, true, false, false);
    }

    @Override
    public void onTick() {
        ((IMinecraftClientAccessor) MinecraftClient.getInstance()).setItemUseCooldown(0);
        super.onTick();
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }

}