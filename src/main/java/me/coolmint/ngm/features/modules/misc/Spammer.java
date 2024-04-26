package me.coolmint.ngm.features.modules.misc;

import com.google.common.eventbus.Subscribe;
import me.coolmint.ngm.event.impl.GameLeftEvent;
import me.coolmint.ngm.features.modules.Module;
import me.coolmint.ngm.features.settings.Setting;
import me.coolmint.ngm.util.ChatUtil;
import me.coolmint.ngm.util.models.Timer;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Random;

public class Spammer extends Module {
    public Setting<Boolean> bypass = register(new Setting<>("Bypass", true));
    public Setting<Float> delay = register(new Setting<>("Delay", 1.0f, 0.0f, 30.0f));
    private final Setting<Boolean> autoDisable = register(new Setting<>("Auto Disable", true));

    public Spammer() {
        super("Spammer", "", Category.MISC, true, false, false);
    }

    public static ArrayList<String> SpamList = new ArrayList<>();
    private final Timer timer_delay = new Timer();

    public static void loadSpammer() {
        File path = new File("ngm/spammer/");
        File file = new File("ngm/spammer/spammer.txt");

        try {
            if(!path.exists())
                path.mkdirs();
            if (!file.exists())
                file.createNewFile();

            new Thread(() -> {
                try {
                    FileInputStream fis = new FileInputStream(file);
                    InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8);
                    BufferedReader reader = new BufferedReader(isr);
                    ArrayList<String> lines = new ArrayList<>();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        lines.add(line);
                    }

                    boolean newline = false;

                    for (String l : lines) {
                        if (l.equals("")) {
                            newline = true;
                            break;
                        }
                    }

                    SpamList.clear();
                    ArrayList<String> spamList = new ArrayList<>();

                    if (newline) {
                        StringBuilder spamChunk = new StringBuilder();

                        for (String l : lines) {
                            if (l.equals("")) {
                                if (!spamChunk.isEmpty()) {
                                    spamList.add(spamChunk.toString());
                                    spamChunk = new StringBuilder();
                                }
                            } else {
                                spamChunk.append(l).append(" ");
                            }
                        }
                        spamList.add(spamChunk.toString());
                    } else spamList.addAll(lines);
                    SpamList = spamList;
                } catch (Exception ignored) {
                }
            }).start();
        } catch (IOException ignored) {
        }
    }

    @Override
    public void onEnable() {
        loadSpammer();
        if (SpamList.isEmpty()) {
            ChatUtil.sendError("SpamList is Empty - add list at ./minecraft/ngm/spammer/spammer.txt");
            disable();
        }
    }

    @Override
    public void onUpdate() {
        if (timer_delay.passedS(delay.getValue())) {
            String string = SpamList.get(new Random().nextInt(SpamList.size()));
            if (string.charAt(0) == '/') {
                string = string.replace("/", "");
                mc.player.networkHandler.sendCommand(string);
            } else
                mc.player.networkHandler.sendChatMessage(bypass.getValue() ? "/skill " + string : string);

            timer_delay.reset();
        }
    }

    @Subscribe
    private void onGameLeft(GameLeftEvent event) {
        if (autoDisable.getValue()) toggle();
    }
}
