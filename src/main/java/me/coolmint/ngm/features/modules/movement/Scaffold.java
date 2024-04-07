package me.coolmint.ngm.features.modules.movement;

import com.google.common.eventbus.Subscribe;
import me.coolmint.ngm.event.impl.EventSync;
import me.coolmint.ngm.event.impl.MovementSlowdownEvent;
import me.coolmint.ngm.event.impl.PacketEvent;
import me.coolmint.ngm.features.modules.Module;
import me.coolmint.ngm.features.settings.Setting;
import me.coolmint.ngm.util.models.Timer;
import me.coolmint.ngm.util.player.BlockInteractionHelper;
import me.coolmint.ngm.util.player.PlayerUtility;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import org.joml.Vector3f;

public class Scaffold extends Module {
    public Setting<Modes> Mode = register(new Setting<>("Mode", Modes.Normal));
    public Setting<Boolean> StopMotion = register(new Setting<>("StopMotion", true));
    public Setting<Float> Delay = register(new Setting<>("Delay", 0.1f, 0.0f, 1.0f));

    public Scaffold() {
        super("Scaffold", "", Category.MOVEMENT, true, false, false);
    }

    private final Timer _timer = new Timer();
    private final Timer _towerPauseTimer = new Timer();
    private final Timer _towerTimer = new Timer();

    @Subscribe
    public void onMotionUpdate(EventSync event){
        if (event.isCancelled())
            return;

        if (!_timer.passedMs((long) (Delay.getValue() * 1000)))
            return;

        // verify we have a block in our hand
        ItemStack stack = mc.player.getMainHandStack();

        int prevSlot = -1;

        if (!verifyStack(stack))
        {
            for (int i = 0; i < 9; ++i)
            {
                stack = mc.player.getInventory().getStack(i);

                if (verifyStack(stack))
                {
                    prevSlot = mc.player.getInventory().selectedSlot;
                    mc.player.getInventory().selectedSlot = i;
                    mc.interactionManager.tick();
                    break;
                }
            }
        }

        if (!verifyStack(stack))
            return;

        _timer.reset();

        BlockPos toPlaceAt = null;

        BlockPos feetBlock = PlayerUtility.GetLocalPlayerPosFloored().down();

        boolean placeAtFeet = isValidPlaceBlockState(feetBlock);

        // verify we are on tower mode, feet block is valid to be placed at, and
        if (Mode.getValue() == Modes.Tower && placeAtFeet && mc.player.input.jumping && _towerTimer.passedMs(250))
        {
            // todo: this can be moved to only do it on an SPacketPlayerPosLook?
            if (_towerPauseTimer.passedMs(1500))
            {
                _towerPauseTimer.reset();
                mc.player.setVelocity(mc.player.getVelocity().x, -0.28f, mc.player.getVelocity().z);
            }
            else
            {
                final float towerMotion = 0.41999998688f;

                mc.player.setVelocity(0, towerMotion, 0);

            }
        }

        if (placeAtFeet)
            toPlaceAt = feetBlock;
        else // find a supporting position for feet block
        {
            BlockInteractionHelper.ValidResult result = BlockInteractionHelper.valid(feetBlock);

            // find a supporting block
            if (result != BlockInteractionHelper.ValidResult.Ok && result != BlockInteractionHelper.ValidResult.AlreadyBlockThere)
            {
                BlockPos[] array = { feetBlock.north(), feetBlock.south(), feetBlock.east(), feetBlock.west() };

                BlockPos toSelect = null;
                double lastDistance = 420.0;

                for (BlockPos pos : array)
                {
                    if (!isValidPlaceBlockState(pos))
                        continue;

                    double dist = pos.getSquaredDistance((int)mc.player.getX(), (int)mc.player.getY(), (int)mc.player.getZ());
                    if (lastDistance > dist)
                    {
                        lastDistance = dist;
                        toSelect = pos;
                    }
                }

                // if we found a position, that's our selection
                if (toSelect != null)
                    toPlaceAt = toSelect;
            }

        }

        if (toPlaceAt != null) {
            // PositionRotation
            // CPacketPlayerTryUseItemOnBlock
            // CPacketAnimation

            final Vec3d eyesPos = new Vec3d(mc.player.getX(), mc.player.getY() + mc.player.getEyeY(), mc.player.getZ());

            for (final Direction side : Direction.values()) {
                final BlockPos neighbor = toPlaceAt.offset(side);
                final Direction side2 = side.getOpposite();

                if (mc.world.getBlockState(neighbor).isSolidBlock(mc.player.getWorld(), neighbor)) {
                    final Vec3d hitVec = new Vec3d(neighbor).add(0.5, 0.5, 0.5).add(new Vec3d(side2.getDirectionVec()).scale(0.5));
                    if (eyesPos.distanceTo(hitVec) <= 5.0f) {
                        float[] rotations = BlockInteractionHelper.getFacingRotations(toPlaceAt.getX(), toPlaceAt.getY(), toPlaceAt.getZ(), side);

                        event.cancel();
                        PlayerUtility.PacketFacePitchAndYaw(rotations[1], rotations[0]);
                        break;
                    }
                }
            }
        }
        else
            _towerPauseTimer.reset();

        // set back our previous slot
        if (prevSlot != -1)
        {
            mc.player.getInventory().selectedSlot = prevSlot;
            mc.interactionManager.tick();
        }
    }


    @Subscribe
    public void onPacket(PacketEvent event){
        if (event.getPacket() instanceof PlayerPositionLookS2CPacket){
            // reset this if we flagged the anticheat
            _towerTimer.reset();
        }
    }

    @Subscribe
    public void onMove(EventSync event){
        if (!StopMotion.getValue())
            return;

        double x = event.X;
        double y = event.Y;
        double z = event.Z;

        if (mc.player.isOnGround() && !mc.player.noClip) {
            double increment;
            for (increment = 0.05D; x != 0.0D && isOffsetBBEmpty(x, -1.0f, 0.0D); ) {
                if (x < increment && x >= -increment) {
                    x = 0.0D;
                } else if (x > 0.0D) {
                    x -= increment;
                } else {
                    x += increment;
                }
            }
            while (z != 0.0D && isOffsetBBEmpty(0.0D, -1.0f, z)) {
                if (z < increment && z >= -increment) {
                    z = 0.0D;
                } else if (z > 0.0D) {
                    z -= increment;
                } else {
                    z += increment;
                }
            }
            while (x != 0.0D && z != 0.0D && isOffsetBBEmpty(x, -1.0f, z)) {
                if (x < increment && x >= -increment) {
                    x = 0.0D;
                } else if (x > 0.0D) {
                    x -= increment;
                } else {
                    x += increment;
                }
                if (z < increment && z >= -increment) {
                    z = 0.0D;
                } else if (z > 0.0D) {
                    z -= increment;
                } else {
                    z += increment;
                }
            }
        }

        event.X = x;
        event.Y = y;
        event.Z = z;
        event.cancel();
    }

    private boolean isOffsetBBEmpty(double x, double y, double z) {
        return mc.world.getCollisions(mc.player, mc.player.getBoundingBox().offset(x, y, z)) == null;
    }

    private boolean isValidPlaceBlockState(BlockPos pos) {
        BlockInteractionHelper.ValidResult result = BlockInteractionHelper.valid(pos);

        if (result == BlockInteractionHelper.ValidResult.AlreadyBlockThere)
            return mc.world.getBlockState(pos).isReplaceable();

        return result == BlockInteractionHelper.ValidResult.Ok;
    }

    private boolean verifyStack(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof BlockItem;
    }

    public enum Modes {
        Tower,
        Normal,
    }
}
