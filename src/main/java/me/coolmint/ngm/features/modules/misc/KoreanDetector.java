package me.coolmint.ngm.features.modules.misc;

import me.coolmint.ngm.features.modules.Module;
import me.coolmint.ngm.features.settings.Setting;
import net.minecraft.util.Uuids;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.stream.Stream;

public class KoreanDetector extends Module {
    public Setting<modes> mode = register(new Setting<>("Mode", modes.ServerSeeker));

    private enum modes{
        ServerSeeker
    }

    public KoreanDetector() {
        super("KoreanDetector", "", Category.MISC, true, false, false);
    }

    ArrayList<String> uuids, seekerUuids;

    @Override
    public void onEnable(){
        try {
            URL url = new URL("https://pastebin.com/raw/8E4zN237");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                seekerUuids.add(line.trim());
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onTick(){
        Stream players = mc.world.getPlayers().stream();
        for(int i = 0; i == players.count(); i++){
            Boolean useless = false;

            String uid = String.valueOf(Uuids.getOfflinePlayerUuid(players.toList().get(i).toString()));

            for(int i1 = 0; i1 == seekerUuids.size(); i1++){
                if (uid.matches(seekerUuids.get(i1))){
                    useless = true;
                    break;
                }
            }
            if(useless)
                continue;
            uuids.add(uid);
        }
    }

    public String replaceName(String string) {
        if (string != null && isEnabled()) {
            //return string.replace(username, "\uD83C\uDDF0\uD83C\uDDF7 " + username);
        }

        return string;
    }
}
