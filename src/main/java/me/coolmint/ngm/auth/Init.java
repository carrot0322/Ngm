package me.coolmint.ngm.auth;

import net.minecraft.client.MinecraftClient;

import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.Base64;

public class Init {

    public static boolean auth() {
        String hwid = getHwid();
        try {
            URL url = new URL("https://grandlineclient.s3.ap-northeast-2.amazonaws.com/ngm/HWID.txt");
            URLConnection conn = url.openConnection();
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains(hwid)) {
                    return true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void sendFailed() throws IOException {
        Webhook webhook = new Webhook("https://discord.com/api/webhooks/1223945919033249812/Ergny2Y2B9FPMtyJzAHpwUXZvv6-hbRypc5XTlF8d0NbkihRSFsQ1z5hzwUkSnZg19SF");
        Webhook.EmbedObject embed = new Webhook.EmbedObject();
        embed.setThumbnail("https://cdn.discordapp.com/attachments/1223945894790172693/1224233204781678602/Screenshot_20240401_144325_CapCut.png?ex=661cbf05&is=660a4a05&hm=cf0c9f905ea5818535372baef3b412bc8e00b11dd78775572057481b2188f538&");
        embed.setTitle("Auth failed - " + MinecraftClient.getInstance().getSession().getUsername());
        embed.setDescription(getHwid());
        embed.setColor(Color.red);
        webhook.addEmbed(embed);

        if (!auth()) webhook.execute();
    }

    public static void sendWebhook() throws IOException {
        Webhook webhook = new Webhook("https://discord.com/api/webhooks/1223945919033249812/Ergny2Y2B9FPMtyJzAHpwUXZvv6-hbRypc5XTlF8d0NbkihRSFsQ1z5hzwUkSnZg19SF");
        Webhook.EmbedObject embed = new Webhook.EmbedObject();
        embed.setThumbnail("https://cdn.discordapp.com/attachments/1223945894790172693/1224233204781678602/Screenshot_20240401_144325_CapCut.png?ex=661cbf05&is=660a4a05&hm=cf0c9f905ea5818535372baef3b412bc8e00b11dd78775572057481b2188f538&");
        embed.setTitle("New login - " + MinecraftClient.getInstance().getSession().getUsername());
        embed.setDescription(getHwid());
        embed.setColor(Color.GREEN);
        webhook.addEmbed(embed);

        if (auth()) webhook.execute();
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
