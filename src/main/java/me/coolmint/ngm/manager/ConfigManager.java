package me.coolmint.ngm.manager;

import com.google.gson.*;
import me.coolmint.ngm.Ngm;
import me.coolmint.ngm.features.Feature;
import me.coolmint.ngm.features.settings.Bind;
import me.coolmint.ngm.features.settings.EnumConverter;
import me.coolmint.ngm.features.settings.Setting;
import me.coolmint.ngm.util.traits.Jsonable;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

public class ConfigManager {
    private static final Path NGM_PATH = FabricLoader.getInstance().getGameDir().resolve("ngm");
    private static final Gson gson = new GsonBuilder()
            .setLenient()
            .setPrettyPrinting()
            .create();
    private final List<Jsonable> jsonables = List.of(Ngm.friendManager, Ngm.moduleManager);

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void setValueFromJson(Feature feature, Setting setting, JsonElement element) {
        String str;
        switch (setting.getType()) {
            case "Boolean" -> {
                setting.setValue(element.getAsBoolean());
            }
            case "Double" -> {
                setting.setValue(element.getAsDouble());
            }
            case "Float" -> {
                setting.setValue(element.getAsFloat());
            }
            case "Integer" -> {
                setting.setValue(element.getAsInt());
            }
            case "String" -> {
                str = element.getAsString();
                setting.setValue(str.replace("_", " "));
            }
            case "Bind" -> {
                setting.setValue(new Bind(element.getAsInt()));
            }
            case "Enum" -> {
                try {
                    EnumConverter converter = new EnumConverter(((Enum) setting.getValue()).getClass());
                    Enum value = converter.doBackward(element);
                    setting.setValue((value == null) ? setting.getDefaultValue() : value);
                } catch (Exception exception) {
                }
            }
            default -> {
                Ngm.LOGGER.error("Unknown Setting type for: " + feature.getName() + " : " + setting.getName());
            }
        }
    }

    public void load() {
        if (!NGM_PATH.toFile().exists()) NGM_PATH.toFile().mkdirs();

        // 버전 읽기
        try {
            String jsonString = Files.readString(NGM_PATH.resolve("version.json"));
            JsonObject jsonObject = JsonParser.parseString(jsonString).getAsJsonObject();

            if (!jsonObject.has("version")) return;

            String configVersion = jsonObject.get("version").getAsString();

            if (!Objects.equals(configVersion, Ngm.VERSION)){
                File file = new File(NGM_PATH.resolve("modules.json").toString());
                file.delete();
                File file1 = new File(NGM_PATH.resolve("version.json").toString());
                file1.delete();
                return;
            }

        } catch (Throwable ignored) {
        }

        for (Jsonable jsonable : jsonables) {
            try {
                String read = Files.readString(NGM_PATH.resolve(jsonable.getFileName()));
                jsonable.fromJson(JsonParser.parseString(read));
            } catch (Throwable ignored) {
            }
        }
    }

    public void save() {
        if (!NGM_PATH.toFile().exists()) NGM_PATH.toFile().mkdirs();
        // 켜두면 버그나는 모듈 끄기
        if(Ngm.moduleManager.isModuleEnabled("ClickGui")) Ngm.moduleManager.disableModule("ClickGui");
        if(Ngm.moduleManager.isModuleEnabled("MaceExploit")) Ngm.moduleManager.disableModule("MaceExploit");

        // 버전 쓰기
        try {
            JsonObject root = new JsonObject();
            root.addProperty("version", Ngm.VERSION);
            Files.writeString(NGM_PATH.resolve("version.json"), gson.toJson(root));
        } catch (Throwable e) {
            Ngm.LOGGER.error(e);
        }
        for (Jsonable jsonable : jsonables) {
            try {
                JsonElement json = jsonable.toJson();
                Files.writeString(NGM_PATH.resolve(jsonable.getFileName()), gson.toJson(json)); // 파일 쓰기
            } catch (Throwable e) {
                Ngm.LOGGER.error(e);
            }
        }
    }
}
