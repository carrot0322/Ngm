package me.coolmint.ngm.util.player;

import me.coolmint.ngm.mixin.IClientWorldMixin;
import net.minecraft.client.network.PendingUpdateManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.Vec3d;

import static me.coolmint.ngm.util.traits.Util.mc;

public class PlayerUtil {
    private static final double diagonal = 1 / Math.sqrt(2);
    private static final Vec3d horizontalVelocity = new Vec3d(0, 0, 0);

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
