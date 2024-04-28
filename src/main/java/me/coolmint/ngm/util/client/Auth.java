package me.coolmint.ngm.util.client;

import me.coolmint.ngm.Ngm;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Base64;

import static me.coolmint.ngm.util.traits.Util.mc;

public class Auth {

    public static boolean auth() {
        String hwid = getHwid();
        String username = mc.getSession().getUsername(); // Assuming mc is a valid Minecraft session

        try {
            String encodedHwid = URLEncoder.encode(hwid, "UTF-8");
            String encodedUsername = URLEncoder.encode(username, "UTF-8");

            URL url = new URL("http://121.254.171.162:20831/auth/" + encodedHwid + "/" + encodedUsername);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                    return reader.readLine().equalsIgnoreCase("True");
                }
            } else {
                Ngm.LOGGER.error("[Ngm] Auth server is offline");
                System.exit(523);
            }
        } catch (IOException e) {
            Ngm.LOGGER.error("[Ngm] Auth server is offline");
            System.exit(523);
        }

        return false;
    }

    public static String getHwid() {
        String hwid = System.getenv("PROCESSOR_IDENTIFIER")
                + System.getProperty("user.name")
                + System.getProperty("user.home")
                + System.getProperty("os.name");
        String encodedHWID = encodeHWID(hwid);
        return encodedHWID;
    }

    public static String encodeHWID(String hwid) {
        String realHWID = Arrays.toString(hwid.getBytes());
        return "NGM-" + System.getenv("PROCESSOR_IDENTIFIER").length() * 9 + System.getenv("PROCESSOR_IDENTIFIER").length() + hwid.length() * 7 + realHWID.length() * 12 + Character.getName(realHWID.length()).length() + Base64.getEncoder().encodeToString(realHWID.getBytes()).length();
    }
}