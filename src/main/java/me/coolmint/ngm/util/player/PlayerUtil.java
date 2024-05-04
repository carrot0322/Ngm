package me.coolmint.ngm.util.player;

import me.coolmint.ngm.mixin.IClientWorldMixin;
import me.coolmint.ngm.mixin.IExplosion;
import net.minecraft.client.network.PendingUpdateManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.DamageUtil;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Difficulty;
import net.minecraft.world.explosion.Explosion;

import static me.coolmint.ngm.util.traits.Util.mc;
import static net.minecraft.world.explosion.Explosion.getExposure;

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

    public static boolean terrainIgnore = false;
    public static BlockPos anchorIgnore = null;
    public static BlockPos setToAir = null;

    public static Explosion explosion;

    public static float getExplosionDamage(Vec3d explosionPos, PlayerEntity target) {
        if (mc.world.getDifficulty() == Difficulty.PEACEFUL || target == null)
            return 0f;

        if (explosion == null)
            explosion = new Explosion(mc.world, mc.player, 1f, 33f, 7f, 6f, false, Explosion.DestructionType.DESTROY);

        ((IExplosion) explosion).setX(explosionPos.x);
        ((IExplosion) explosion).setY(explosionPos.y);
        ((IExplosion) explosion).setZ(explosionPos.z);

        if (((IExplosion) explosion).getWorld() != mc.world)
            ((IExplosion) explosion).setWorld(mc.world);

        if (!new Box(MathHelper.floor(explosionPos.x - 11), MathHelper.floor(explosionPos.y - 11), MathHelper.floor(explosionPos.z - 11), MathHelper.floor(explosionPos.x + 13), MathHelper.floor(explosionPos.y + 13), MathHelper.floor(explosionPos.z + 13)).intersects(target.getBoundingBox()))
            return 0f;

        if (!target.isImmuneToExplosion(explosion) && !target.isInvulnerable()) {
            double distExposure = (float) target.squaredDistanceTo(explosionPos) / 144;
            if (distExposure <= 1.0) {
                terrainIgnore = true;
                double exposure = getExposure(explosionPos, target);
                terrainIgnore = false;
                double finalExposure = (1.0 - distExposure) * exposure;

                float toDamage = (float) Math.floor((finalExposure * finalExposure + finalExposure) / 2. * 7. * 12. + 1.);

                if (mc.world.getDifficulty() == Difficulty.EASY) toDamage = Math.min(toDamage / 2f + 1f, toDamage);
                else if (mc.world.getDifficulty() == Difficulty.HARD) toDamage = toDamage * 3f / 2f;

                toDamage = DamageUtil.getDamageLeft(toDamage, ((IExplosion) explosion).getDamageSource(), target.getArmor(), (float) target.getAttributeInstance(EntityAttributes.GENERIC_ARMOR_TOUGHNESS).getValue());

                if (target.hasStatusEffect(StatusEffects.RESISTANCE)) {
                    int resistance = 25 - (target.getStatusEffect(StatusEffects.RESISTANCE).getAmplifier() + 1) * 5;
                    float resistance_1 = toDamage * resistance;
                    toDamage = Math.max(resistance_1 / 25f, 0f);
                }

                if (toDamage <= 0f) toDamage = 0f;
                else {
                    int protAmount = EnchantmentHelper.getProtectionAmount(target.getArmorItems(), mc.world.getDamageSources().explosion(explosion));
                    if (protAmount > 0)
                        toDamage = DamageUtil.getInflictedDamage(toDamage, protAmount);
                }
                return toDamage;
            }
        }
        return 0f;
    }
}
