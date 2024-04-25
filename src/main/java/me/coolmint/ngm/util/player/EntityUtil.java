package me.coolmint.ngm.util.player;

import net.minecraft.entity.MovementType;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.Vec3d;

import static me.coolmint.ngm.util.traits.Util.mc;

public class EntityUtil {
    public static void motion(double x, double y, double z) {
        double dist = 5.0;

        for(int i = 0; i < 20 && dist > 1.0; ++i) {
            double dx = x - mc.player.getX();
            double dy = y - mc.player.getY();
            double dz = z - mc.player.getZ();
            double hdist = Math.sqrt(dx * dx + dz * dz);
            double rx = Math.atan2(dx, dz);
            double ry = Math.atan2(dy, hdist);
            dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
            double o = dist > 1.0 ? 1.0 : dist;
            Vec3d vec = new Vec3d(Math.sin(rx) * Math.cos(ry) * o, o * Math.sin(ry * 1.0), Math.cos(rx) * Math.cos(ry) * o);
            mc.player.move(MovementType.SELF, vec);
            mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY(), mc.player.getZ(), true));
        }
    }
}
