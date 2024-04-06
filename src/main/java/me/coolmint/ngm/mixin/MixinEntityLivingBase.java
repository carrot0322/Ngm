package me.coolmint.ngm.mixin;

import me.coolmint.ngm.event.impl.JumpEvent;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.potion.Potion;
import net.minecraft.potion.Potions;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import net.minecraft.entity.LivingEntity;

import java.util.Map;

import static me.coolmint.ngm.util.traits.Util.EVENT_BUS;
import static me.coolmint.ngm.util.traits.Util.mc;

@Mixin(LivingEntity.class)
public abstract class MixinEntityLivingBase {
    @Shadow protected abstract float getJumpVelocity();

    @Shadow public abstract float getYaw(float tickDelta);

    @Shadow public abstract Map<StatusEffect, StatusEffectInstance> getActiveStatusEffects();

    /**
     * @author c_arrot_
     * @reason Scaffold
     */
    @Overwrite
    protected void jump() {
        float jumpMotion = this.getJumpVelocity();
        JumpEvent jumpEvent = new JumpEvent(jumpMotion, this.getYaw(1f));
        EVENT_BUS.post(jumpEvent);

        if (jumpEvent.isCancelled())
            return;

        mc.player.setVelocity(mc.player.getVelocity().x, jumpEvent.getMotion(), mc.player.getVelocity().z);

        /* ㅈㅅ 모르겠음 점프포션 쳐먹지마
        if (this.isPotionActive(Potion.JUMP)) {
            mc.player.setVelocity(mc.player.getVelocity().x, mc.player.getVelocity().y + (this.getActiveStatusEffects() + 1) * 0.1f, mc.player.getVelocity().z);
            this.motionY += (this.getActivePotionEffect(Potion.JUMP).getAmplifier() + 1) * 0.1F;
        }
         */

        if (mc.player.isSprinting()) {
            float f = jumpEvent.getYaw() * 0.017453292F;
            mc.player.setVelocity(mc.player.getVelocity().x - MathHelper.sin(f) * 0.2f, mc.player.getVelocity().y, mc.player.getVelocity().z + MathHelper.cos(f) * 0.2f);
        }

        mc.player.velocityDirty = true;
    }
}