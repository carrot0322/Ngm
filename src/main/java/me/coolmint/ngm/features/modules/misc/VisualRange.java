package me.coolmint.ngm.features.modules.misc;

import com.google.common.eventbus.Subscribe;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Formatting;
import me.coolmint.ngm.Ngm;
import me.coolmint.ngm.event.impl.EventEntityRemoved;
import me.coolmint.ngm.event.impl.EventEntitySpawn;
import me.coolmint.ngm.features.modules.Module;
import me.coolmint.ngm.features.settings.Setting;

import java.util.ArrayList;

import static me.coolmint.ngm.features.command.Command.sendMessage;

public class VisualRange extends Module {
    private static final ArrayList<String> entities = new ArrayList<>();
    private final Setting<Boolean> leave = register(new Setting<>("Leave", true));
    private final Setting<Boolean> enter = register(new Setting<>("Enter", true));
    private final Setting<Boolean> friends = register(new Setting<>("Friends", true));
    private final Setting<Boolean> soundpl = register(new Setting<>("Sound", true));
    private final Setting<Mode> mode = register(new Setting<>("Mode", Mode.Chat));

    public VisualRange() {
        super("VisualRange", "", Category.MISC, true, false, false);
    }
    @Subscribe
    public void onEntityAdded(EventEntitySpawn event) {
        if (!isValid(event.getEntity())) return;

        if (!entities.contains(event.getEntity().getName().getString()))
            entities.add(event.getEntity().getName().getString());
        else return;

        if (enter.getValue()) notify(event.getEntity(), true);
    }

    @Subscribe
    public void onEntityRemoved(EventEntityRemoved event) {
        if (!isValid(event.entity)) return;

        if (entities.contains(event.entity.getName().getString()))
            entities.remove(event.entity.getName().getString());
        else return;

        if (leave.getValue()) notify(event.entity, false);
    }

    public void notify(Entity entity, boolean enter) {
        String message = "";
        if (Ngm.friendManager.isFriend(entity.getName().getString()))
            message = Formatting.AQUA + entity.getName().getString();
        else message = Formatting.GRAY + entity.getName().getString();

        if (enter) message += Formatting.GREEN + " was found!";
        else message += Formatting.RED + " left to X:" + (int)entity.getX() + " Z:" + (int) entity.getZ();

        if (mode.getValue() == Mode.Chat)
            sendMessage(message);

        if (soundpl.getValue()) {
            try {
                if (enter)
                    mc.world.playSound(mc.player, mc.player.getBlockPos(), SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.BLOCKS, 1f, 1f);
                else
                    mc.world.playSound(mc.player, mc.player.getBlockPos(), SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.BLOCKS, 1f, 1f);
            } catch (Exception ignored) {
            }
        }
    }

    public boolean isValid(Entity entity) {
        if (!(entity instanceof PlayerEntity)) return false;
        return entity != mc.player && (!Ngm.friendManager.isFriend(entity.getName().getString()) || friends.getValue());
    }

    public enum Mode {
        Chat
    }
}