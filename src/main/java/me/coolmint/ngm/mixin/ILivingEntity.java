package me.coolmint.ngm.mixin;

import me.coolmint.ngm.event.impl.JumpEvent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Accessor;

import static me.coolmint.ngm.util.traits.Util.EVENT_BUS;

@Mixin(LivingEntity.class)
public interface ILivingEntity {
    @Accessor("lastAttackedTicks")
    int getLastAttackedTicks();

    @Accessor("jumpingCooldown")
    int getLastJumpCooldown();

    @Accessor("jumpingCooldown")
    void setLastJumpCooldown(int val);

}