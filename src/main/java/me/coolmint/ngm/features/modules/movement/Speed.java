package me.coolmint.ngm.features.modules.movement;

import me.coolmint.ngm.features.modules.Module;
import com.google.common.eventbus.Subscribe;
import net.minecraft.entity.effect.StatusEffects;
import me.coolmint.ngm.Ngm;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import me.coolmint.ngm.event.impl.EventMove;
import me.coolmint.ngm.event.impl.EventSync;
import me.coolmint.ngm.features.settings.Setting;
import me.coolmint.ngm.util.MovementUtility;

public class Speed extends Module {

    public Speed() {
        super("Speed", "", Module.Category.MOVEMENT, true, false, false);
    }

    private final Setting<Mode> mode = register(new Setting<>("Mode", Mode.NCP));
    public Setting<Boolean> useTimer = register(new Setting<>("Use Timer", false));
    public Setting<Boolean> pauseInLiquids = register(new Setting<>("PauseInLiquids", false));
    public double baseSpeed;
    private int stage, ticks;
    private float prevForward = 0;
    private me.coolmint.ngm.util.models.Timer startDelay = new me.coolmint.ngm.util.models.Timer();

    public enum Mode {
        StrictStrafe, MatrixJB, NCP
    }

    @Override
    public void onDisable() {
        Ngm.TICK_TIMER = 1f;
    }

    @Override
    public void onEnable() {
        stage = 1;
        ticks = 0;
        baseSpeed = 0.2873D;
        startDelay.reset();
    }

    @Subscribe
    public void onSync(EventSync e) {
        if(mc.player.isInFluid() && pauseInLiquids.getValue()){return;}
        if (mode.getValue() == Mode.MatrixJB) {
            boolean closeToGround = false;

            for (VoxelShape a : mc.world.getBlockCollisions(mc.player, mc.player.getBoundingBox().expand(0.5, 0.0, 0.5).offset(0.0, -1.0, 0.0)))
                if (a != VoxelShapes.empty()) {
                    closeToGround = true;
                    break;
                }

            if (MovementUtility.isMoving() && closeToGround && mc.player.fallDistance <= 0) {
                Ngm.TICK_TIMER = 1f;
                mc.player.setOnGround(true);
                mc.player.jump();
            } else if (mc.player.fallDistance > 0 && useTimer.getValue()) {
                Ngm.TICK_TIMER = 1.088f;
                mc.player.addVelocity(0f, -0.003f, 0f);
            }
        }
    }

    @Subscribe
    public void onMove(EventMove event) {
        if (mc.player.isInFluid() && pauseInLiquids.getValue()){return;}
        if (mode.getValue() != Mode.NCP && mode.getValue() != Mode.StrictStrafe) return;
        if (mc.player.getAbilities().flying) return;
        if (mc.player.isFallFlying()) return;
        if (mc.player.getHungerManager().getFoodLevel() <= 6) return;
        if (event.isCancelled()) return;
        event.cancel();

        if (MovementUtility.isMoving()) {
            Ngm.TICK_TIMER = useTimer.getValue() ? 1.088f : 1f;
            float currentSpeed = mode.getValue() == Mode.NCP && mc.player.input.movementForward <= 0 && prevForward > 0 ? Ngm.playerManager.currentPlayerSpeed * 0.66f : Ngm.playerManager.currentPlayerSpeed;
            if (stage == 1 && mc.player.isOnGround()) {
                mc.player.setVelocity(mc.player.getVelocity().x, MovementUtility.getJumpSpeed(), mc.player.getVelocity().z);
                event.setY(MovementUtility.getJumpSpeed());
                baseSpeed *= 2.149;
                stage = 2;
            } else if (stage == 2) {
                baseSpeed = currentSpeed - (0.66 * (currentSpeed - MovementUtility.getBaseMoveSpeed()));
                stage = 3;
            } else {
                if ((mc.world.getBlockCollisions(mc.player, mc.player.getBoundingBox().offset(0.0, mc.player.getVelocity().getY(), 0.0)).iterator().hasNext() || mc.player.verticalCollision))
                    stage = 1;
                baseSpeed = currentSpeed - currentSpeed / 159.0D;
            }

            baseSpeed = Math.max(baseSpeed, MovementUtility.getBaseMoveSpeed());

            double ncpSpeed = mode.getValue() == Mode.StrictStrafe || mc.player.input.movementForward < 1 ? 0.465 : 0.576;
            double ncpBypassSpeed = mode.getValue() == Mode.StrictStrafe || mc.player.input.movementForward < 1 ? 0.44 : 0.57;

            if (mc.player.hasStatusEffect(StatusEffects.SPEED)) {
                double amplifier = mc.player.getStatusEffect(StatusEffects.SPEED).getAmplifier();
                ncpSpeed *= 1 + (0.2 * (amplifier + 1));
                ncpBypassSpeed *= 1 + (0.2 * (amplifier + 1));
            }

            if (mc.player.hasStatusEffect(StatusEffects.SLOWNESS)) {
                double amplifier = mc.player.getStatusEffect(StatusEffects.SLOWNESS).getAmplifier();
                ncpSpeed /= 1 + (0.2 * (amplifier + 1));
                ncpBypassSpeed /= 1 + (0.2 * (amplifier + 1));
            }

            baseSpeed = Math.min(baseSpeed, ticks > 25 ? ncpSpeed : ncpBypassSpeed);

            if (ticks++ > 50)
                ticks = 0;

            MovementUtility.modifyEventSpeed(event, baseSpeed);
            prevForward = mc.player.input.movementForward;
        } else {
            Ngm.TICK_TIMER = 1f;
            event.setX(0);
            event.setZ(0);
        }
    }
}