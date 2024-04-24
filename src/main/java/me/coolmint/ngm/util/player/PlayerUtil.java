package me.coolmint.ngm.util.player;

import me.coolmint.ngm.mixin.IClientWorldMixin;
import net.minecraft.client.network.PendingUpdateManager;
import net.minecraft.client.world.ClientWorld;

public class PlayerUtil {
    public static int getWorldActionId(ClientWorld world) {
        PendingUpdateManager pum = getUpdateManager(world);
        int p = pum.getSequence();
        pum.close();
        return p;
    }

    private static PendingUpdateManager getUpdateManager(ClientWorld world) {
        return ((IClientWorldMixin) world).acquirePendingUpdateManager();
    }
}
