package me.coolmint.ngm.features.modules.misc;

import me.coolmint.ngm.features.modules.Module;
import me.coolmint.ngm.features.settings.Setting;
import me.coolmint.ngm.util.models.Timer;
import me.coolmint.ngm.util.player.BlockUtil;
import me.coolmint.ngm.util.player.RotationUtil;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Nuker extends Module {
    public Setting<Float> range = register(new Setting<>("Range", 3.40f, 1.00f, 6.00f));
    public Setting<Integer> delay = register(new Setting<>("Delay", 5000, 0, 10000));
    public Setting<Mode> mode = new Setting<>("mode", Mode.SPEED); // register 뺴놓음

    public Nuker() {
        super("Nuker", "", Category.MISC, true, false, false);
    }

    Timer timer = new Timer();

    @Override
    public void onEnable(){
        timer.reset();
    }

    @Override
    public void onUpdate() {
        // get valid blocks
        Iterable<BlockPos> validBlocks = getValidBlocks(range.getValue(), pos -> mode.getValue().validator.test(this, pos));

        // break all blocks
        switch (mode.getValue()){
            case NORMAL -> {

            }
            case SPEED -> breakBlocksWithPacketSpam(validBlocks);
        }

    }

    private ArrayList<BlockPos> getValidBlocks(double range, Predicate<BlockPos> validator) {
        Vec3d eyesVec = RotationUtil.getEyesPos().subtract(0.5, 0.5, 0.5);
        double rangeSq = Math.pow(range + 0.5, 2);
        int rangeI = (int)Math.ceil(range);

        BlockPos center = BlockPos.ofFloored(RotationUtil.getEyesPos());
        BlockPos min = center.add(-rangeI, -rangeI, -rangeI);
        BlockPos max = center.add(rangeI, rangeI, rangeI);

        return BlockUtil.getAllInBox(min, max).stream()
                .filter(pos -> !mc.world.getBlockState(pos).isAir())
                .filter(pos -> eyesVec.squaredDistanceTo(Vec3d.of(pos)) <= rangeSq)
                .filter(BlockUtil::canBeClicked).filter(validator)
                .sorted(Comparator.comparingDouble(
                        pos -> eyesVec.squaredDistanceTo(Vec3d.of(pos))))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public void breakBlocksWithPacketSpam(Iterable<BlockPos> blocks) {
        Vec3d eyesPos = RotationUtil.getEyesPos();

        for(BlockPos pos : blocks)
        {
            Vec3d posVec = Vec3d.ofCenter(pos);
            double distanceSqPosVec = eyesPos.squaredDistanceTo(posVec);

            for(Direction side : Direction.values())
            {
                Vec3d hitVec = posVec.add(Vec3d.of(side.getVector()).multiply(0.5));

                // check if side is facing towards player and check air
                if(eyesPos.squaredDistanceTo(hitVec) >= distanceSqPosVec || mc.world.getBlockState(pos).isAir())
                    continue;

                if(!timer.passedNS(delay.getValue()))
                    continue;

                timer.reset();

                // break block
                mc.player.networkHandler.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, pos, side));
                mc.player.networkHandler.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, pos, side));

                break;
            }
        }
    }

    private enum Mode {
        NORMAL((n, pos) -> true),
        SPEED((n, pos) -> true);

        private final BiPredicate<Nuker, BlockPos> validator;

        private Mode(BiPredicate<Nuker, BlockPos> validator) {
            this.validator = validator;
        }
    }
}
