package me.coolmint.ngm.features.modules.misc;

import me.coolmint.ngm.features.modules.Module;
import me.coolmint.ngm.features.settings.Setting;
import me.coolmint.ngm.util.models.Timer;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Random;

public class Spammer extends Module {
    public static ArrayList<String> SpamList = new ArrayList<>();
    public Setting<Boolean> bypass = register(new Setting<>("bypass", true));
    public Setting<Float> delay = register(new Setting<>("delay", 1.0f, 0.0f, 30.0f));
    private final Timer timer_delay = new Timer();

    public Spammer() {
        super("Spammer", "", Category.MISC, true, false, false);
    }

    public static void loadSpammer() {
        try {
            File file = new File("ngm/Spammer/spammer.txt");

            if (!file.exists()) file.createNewFile();
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
    }

    @Override
    public void onUpdate() {
        if (timer_delay.passedS(delay.getValue())) {
            if (SpamList.isEmpty()) {
                toggle();
                return;
            }
            String c = SpamList.get(new Random().nextInt(SpamList.size()));
            if (c.charAt(0) == '/') {
                c = c.replace("/", "");
                mc.player.networkHandler.sendCommand(c);
            } else mc.player.networkHandler.sendChatMessage(bypass.getValue() ? "/skill " + c : c);

            timer_delay.reset();
        }
    }
}