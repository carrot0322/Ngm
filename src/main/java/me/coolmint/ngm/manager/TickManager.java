package me.coolmint.ngm.manager;

import com.google.common.collect.Lists;
import com.google.common.eventbus.Subscribe;
import me.coolmint.ngm.event.impl.PacketEvent;
import me.coolmint.ngm.event.impl.TickCounterEvent;
import me.coolmint.ngm.util.EvictingQueue;
import net.minecraft.network.packet.s2c.play.WorldTimeUpdateS2CPacket;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Queue;

import static me.coolmint.ngm.util.traits.Util.EVENT_BUS;
import static me.coolmint.ngm.util.traits.Util.mc;

public class TickManager {
    private final ArrayDeque<Float> ticks = new EvictingQueue<>(20);
    private long time;
    private float clientTick = 1.0f;

    public TickManager() {
        EVENT_BUS.register(this);
    }

    @Subscribe
    public void onPacketInbound(PacketEvent.Receive event) {
        if (mc.player == null || mc.world == null) {
            return;
        }
        // ticks/actual
        if (event.getPacket() instanceof WorldTimeUpdateS2CPacket) {
            float last = 20000.0f / (System.currentTimeMillis() - time);
            ticks.addFirst(last);
            time = System.currentTimeMillis();
        }
    }

    public void setClientTick(float ticks) {
        clientTick = ticks;
    }

    @Subscribe
    public void onTickCounter(TickCounterEvent event) {
        if (clientTick != 1.0f) {
            event.cancel();
            event.setTicks(clientTick);
        }
    }

    public Queue<Float> getTicks() {
        return ticks;
    }

    public float getTpsAverage() {
        float avg = 0.0f;
        // fix ConcurrentModificationException
        ArrayList<Float> ticksCopy = Lists.newArrayList(ticks);
        if (!ticksCopy.isEmpty()) {
            for (float t : ticksCopy) {
                avg += t;
            }
            avg /= ticksCopy.size();
        }
        return Math.min(100.0f, avg); // Server may compensate
    }

    public float getTpsCurrent() {
        try {
            if (!ticks.isEmpty()) {
                return ticks.getFirst();
            }
        } catch (NoSuchElementException ignored) {

        }
        return 20.0f;
    }

    public float getTpsMin() {
        float min = 20.0f;
        for (float t : ticks) {
            if (t < min) {
                min = t;
            }
        }
        return min;
    }

    public float getTickSync(TickSync tps) {
        return switch (tps) {
            case AVERAGE -> getTpsAverage();
            case CURRENT -> getTpsCurrent();
            case MINIMAL -> getTpsMin();
            case NONE -> 20.0f;
        };
    }

    public enum TickSync {
        CURRENT,
        AVERAGE,
        MINIMAL,
        NONE
    }
}
