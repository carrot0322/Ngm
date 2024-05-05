package me.coolmint.ngm.mixin;

import me.coolmint.ngm.event.impl.EntityMarginEvent;
import me.coolmint.ngm.event.impl.FixVelocityEvent;
import me.coolmint.ngm.event.impl.HitboxEvent;
import me.coolmint.ngm.event.impl.TeamColorEvent;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static me.coolmint.ngm.util.traits.Util.EVENT_BUS;
import static me.coolmint.ngm.util.traits.Util.mc;

@Mixin(Entity.class)
public abstract class MixinEntity {
    @Shadow
    public abstract World getEntityWorld();

    @Shadow public abstract int getId();

    @Inject(method = "getTeamColorValue", at = @At(value = "HEAD"), cancellable = true)
    private void hookGetTeamColorValue(CallbackInfoReturnable<Integer> cir) {
        TeamColorEvent teamColorEvent = new TeamColorEvent((Entity) (Object) this);
        EVENT_BUS.post(teamColorEvent);
        if (teamColorEvent.isCancelled()) {
            cir.setReturnValue(teamColorEvent.getColor());
            cir.cancel();
        }
    }

    @Inject(method = "updateVelocity", at = {@At("HEAD")}, cancellable = true)
    public void updateVelocityHook(float speed, Vec3d movementInput, CallbackInfo ci) {
        if ((Object) this == mc.player) {
            ci.cancel();
            FixVelocityEvent event = new FixVelocityEvent(movementInput, speed, mc.player.getYaw(), movementInputToVelocityC(movementInput, speed, mc.player.getYaw()));
            EVENT_BUS.post(event);
            mc.player.setVelocity(mc.player.getVelocity().add(event.getVelocity()));
        }
    }

    @Unique
    private static Vec3d movementInputToVelocityC(Vec3d movementInput, float speed, float yaw) {
        double d = movementInput.lengthSquared();
        if (d < 1.0E-7) {
            return Vec3d.ZERO;
        }
        Vec3d vec3d = (d > 1.0 ? movementInput.normalize() : movementInput).multiply(speed);
        float f = MathHelper.sin(yaw * ((float) Math.PI / 180));
        float g = MathHelper.cos(yaw * ((float) Math.PI / 180));
        return new Vec3d(vec3d.x * (double) g - vec3d.z * (double) f, vec3d.y, vec3d.z * (double) g + vec3d.x * (double) f);
    }

    @Inject(method={"getTargetingMargin"}, at={@At(value="HEAD")}, cancellable=true)
    void onGetTargetingMargin(CallbackInfoReturnable callbackInfoReturnable) {
        if (getEntityWorld() == null)
            return;

        Entity entity = getEntityWorld().getEntityById(getId());
        if (entity == null)
            return;

        EntityMarginEvent event = new EntityMarginEvent(entity, 0);
        EVENT_BUS.post(event);
        callbackInfoReturnable.setReturnValue(event.getMargin());
    }

    @Inject(method={"getBoundingBox"}, at={@At(value="RETURN")}, cancellable=true)
    void getBoundingBox(CallbackInfoReturnable callbackInfoReturnable) {
        if (getEntityWorld() == null)
            return;

        Entity entity = getEntityWorld().getEntityById(getId());
        if (entity == null)
            return;

        Box box = (Box)callbackInfoReturnable.getReturnValue();
        HitboxEvent event = new HitboxEvent(entity, box);
        EVENT_BUS.post(event);
        callbackInfoReturnable.setReturnValue(event.getBox());
    }
}
