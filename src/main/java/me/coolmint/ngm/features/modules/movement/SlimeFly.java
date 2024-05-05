package me.coolmint.ngm.features.modules.movement;

import me.coolmint.ngm.features.modules.Module;
import net.minecraft.block.Blocks;

public class SlimeFly extends Module {
    public SlimeFly() {
        super("SlimeFly", "", Category.MOVEMENT, true, false, false);
    }

    @Override
    public void onUpdate() {
        if (mc.player == null)
            return;

        if(!mc.world.getBlockState(mc.player.getBlockPos().down()).getBlock().equals(Blocks.SLIME_BLOCK))
            return;

        if(!mc.options.jumpKey.isPressed() && !mc.player.isOnGround() && !mc.player.input.jumping && mc.player.getVelocity().y <= 0.0 && mc.player.fallDistance <= 1.0f)
            mc.player.setVelocity(mc.player.getVelocity().x, -4, mc.player.getVelocity().z);
    }
}
