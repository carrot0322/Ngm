package me.coolmint.ngm.manager;

import com.google.common.collect.Lists;
import com.google.common.eventbus.Subscribe;
import me.coolmint.ngm.Ngm;
import me.coolmint.ngm.event.impl.SyncEvent;
import me.coolmint.ngm.event.impl.TickEvent;
import me.coolmint.ngm.features.Feature;
import me.coolmint.ngm.features.gui.ClickGui;
import me.coolmint.ngm.features.modules.Module;
import me.coolmint.ngm.util.client.ChatUtil;
import me.coolmint.ngm.util.models.Timer;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.Entity;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import static me.coolmint.ngm.util.traits.Util.mc;

public class AsyncManager {
    private ClientService clientService = new ClientService();
    public static ExecutorService executor = Executors.newCachedThreadPool();

    private static final Timer updateTimer = new Timer();

    private volatile Iterable<Entity> threadSafeEntityList = Collections.emptyList();
    private volatile List<AbstractClientPlayerEntity> threadSafePlayersList = Collections.emptyList();
    public final AtomicBoolean ticking = new AtomicBoolean(false);

    public static void sleep(int delay) {
        try {
            Thread.sleep(delay);
        } catch (Exception ignored) {
        }
    }

    @Subscribe
    public void onPostTick(TickEvent.Post e) {
        if (mc.world == null) return;

        threadSafeEntityList = Lists.newArrayList(mc.world.getEntities());
        threadSafePlayersList = Lists.newArrayList(mc.world.getPlayers());
        ticking.set(false);
    }

    public Iterable<Entity> getAsyncEntities() {
        return threadSafeEntityList;
    }

    public List<AbstractClientPlayerEntity> getAsyncPlayers() {
        return threadSafePlayersList;
    }

    public AsyncManager() {
        clientService.setName("Ngm-AsyncProcessor");
        clientService.setDaemon(true);
        clientService.start();
    }

    @Subscribe
    public void onSync(SyncEvent e) {
        if (!clientService.isAlive()) {
            clientService = new ClientService();
            clientService.setName("Ngm-AsyncProcessor");
            clientService.setDaemon(true);
            clientService.start();
        }
    }

    public static class ClientService extends Thread {
        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                if(updateTimer.passedMs(16)) {
                    updateTimer.reset();
                }

                try {
                    if (!Feature.fullNullCheck()) {
                        Thread.sleep(100);
                    } else Thread.yield();
                } catch (Exception exception) {
                    exception.printStackTrace();
                    ChatUtil.sendInfo(exception.getMessage());
                }
            }
        }
    }

    @Subscribe
    public void onTick(TickEvent e) {
        ticking.set(true);
    }

    public void run(Runnable runnable, long delay) {
        executor.execute(() -> {
            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            runnable.run();
        });
    }

    public void run(Runnable r) {
        executor.execute(r);
    }
}
