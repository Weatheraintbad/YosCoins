package yoscoins.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.fabricmc.loader.api.FabricLoader;
import yoscoins.client.YosCoinsClient;
import yoscoins.client.hud.YosCoinsHud;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public final class YosCoinsConfig {
    private static final File CONFIG_FILE = new File(
            FabricLoader.getInstance().getConfigDir().toFile(), "yoscoins-config.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public int hudOffsetX = 0;   // 相对锚点 X 偏移（像素）
    public int hudOffsetY = 0;   // 相对锚点 Y 偏移（像素）
    public boolean hudTopRight = true; // true=右上角 false=左上角
    public boolean hudEnabled = true;   //开关

    /* 统一入口：加载后自动刷新 HUD */
    public static YosCoinsConfig load() {
        YosCoinsConfig cfg = new YosCoinsConfig();
        if (CONFIG_FILE.exists()) {
            try (FileReader reader = new FileReader(CONFIG_FILE)) {
                JsonObject json = com.google.gson.JsonParser.parseReader(reader).getAsJsonObject();
                cfg.hudOffsetX  = json.get("hudOffsetX").getAsInt();
                cfg.hudOffsetY  = json.get("hudOffsetY").getAsInt();
                cfg.hudTopRight = json.get("hudTopRight").getAsBoolean();
            } catch (Exception e) {
                // 文件损坏 → 用默认
            }
        }
        cfg.save();          // 写回（含默认值）
        return cfg;
    }

    /** 写盘 + 立即刷新运行时实例 & HUD */
    public void save() {
        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            JsonObject json = new JsonObject();
            json.addProperty("hudOffsetX", hudOffsetX);
            json.addProperty("hudOffsetY", hudOffsetY);
            json.addProperty("hudTopRight", hudTopRight);
            GSON.toJson(json, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }

        /* 关键：把最新自己设回客户端，并通知 HUD 重新读字段 */
        YosCoinsClient.setConfig(this);   // 见下方
        YosCoinsHud.INSTANCE.reloadConfig();
    }
}