package me.coolmint.ngm.auth;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.Base64;

import static me.coolmint.ngm.util.traits.Util.mc;

public class Init {

    public static boolean auth() {
        String hwid = getHwid();
        try {
            URL url = new URL("http://121.254.171.162:20831/auth/" + hwid + "/" + mc.getSession().getUsername());
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setDoOutput(true);

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String response = reader.readLine();

            if (response.equalsIgnoreCase("True")) {
                return true;
            } else {
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
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
