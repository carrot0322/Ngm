package me.coolmint.toolbox;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.coolmint.ngm.Ngm;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public class AuthServer extends JPanel implements ActionListener {
    public static final Path NGM_PATH = Path.of(System.getProperty("user.dir") + File.separator + "ngm");
    public static final Gson gson = new GsonBuilder()
            .setLenient()
            .setPrettyPrinting()
            .create();

    JButton change = new JButton("Change Server");
    JLabel label = new JLabel("Server: " + getServer());

    public AuthServer(){
        change.addActionListener(this);

        add(label);
        add(change);
    }

    private String getServer(){
        try {
            String jsonString = Files.readString(NGM_PATH.resolve("server.json"));
            JsonObject jsonObject = JsonParser.parseString(jsonString).getAsJsonObject();

            if (!jsonObject.has("server")) return "main";

            String server = jsonObject.get("server").getAsString();

            if (Objects.equals(server, "main")){
                return "main";
            }
            if (Objects.equals(server, "sub")){
                return "sub";
            }
        } catch (Throwable ignored) {
        }

        return "error / turn off anti virus";
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == change) {
            if (!NGM_PATH.toFile().exists()) {
                try {
                    Files.createDirectories(NGM_PATH); // 디렉토리 생성
                } catch (IOException ex) {
                    System.err.println("Failed to create directory: " + ex.getMessage());
                    return;
                }
            }

            try {
                String currentServer = getServer();
                String newServer = currentServer.equals("main") ? "sub" : "main";

                JsonObject root = new JsonObject();
                root.addProperty("server", newServer);

                Path filePath = NGM_PATH.resolve("server.json");
                Files.writeString(filePath, gson.toJson(root));

                // 레이블 업데이트
                label.setText("Server: " + newServer);
            } catch (IOException err) {
                System.err.println("Failed to write JSON to file: " + err.getMessage());
                err.printStackTrace();
            }
        }
    }
}
