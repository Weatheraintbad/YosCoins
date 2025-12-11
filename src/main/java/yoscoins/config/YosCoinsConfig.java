package yoscoins.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.fabricmc.loader.api.FabricLoader;
import yoscoins.client.YosCoinsClient;
import yoscoins.client.hud.YosCoinsHud;

import java.io.*;

public final class YosCoinsConfig {
    private static final File CONFIG_FILE = new File(
            FabricLoader.getInstance().getConfigDir().toFile(), "yoscoins-config.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public int  hudOffsetX = 0;   // 相对左上角 X 偏移（像素）
    public int  hudOffsetY = 0;   // 相对左上角 Y 偏移（像素）
    public boolean hudEnabled = true;   // 开关

    /* 统一入口：加载后自动刷新 HUD */
    public static YosCoinsConfig load() {
        YosCoinsConfig cfg = new YosCoinsConfig();
        if (CONFIG_FILE.exists()) {
            try (FileReader reader = new FileReader(CONFIG_FILE)) {
                JsonObject json = com.google.gson.JsonParser.parseReader(reader).getAsJsonObject();
                if (json.has("hudOffsetX")) cfg.hudOffsetX = json.get("hudOffsetX").getAsInt();
                if (json.has("hudOffsetY")) cfg.hudOffsetY = json.get("hudOffsetY").getAsInt();
                // 旧存档里可能还有 hudTopRight，直接忽略即可
            } catch (Exception e) {
                // 文件损坏就用默认值
            }
        }
        cfg.save();
        return cfg;
    }

    public void save() {
        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            JsonObject json = new JsonObject();
            json.addProperty("hudOffsetX", hudOffsetX);
            json.addProperty("hudOffsetY", hudOffsetY);
            json.addProperty("hudEnabled", hudEnabled);
            GSON.toJson(json, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }

        YosCoinsClient.setConfig(this);
        YosCoinsHud.INSTANCE.reloadConfig();
    }
}