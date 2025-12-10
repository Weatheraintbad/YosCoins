package yoscoins.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import yoscoins.client.hud.YosCoinsHud;
import yoscoins.config.YosCoinsConfig;

@Environment(EnvType.CLIENT)
public class YosCoinsClient implements ClientModInitializer {

    private static YosCoinsConfig config;   // 单例

    /* 首次访问时延迟加载，后续一直复用同一实例 */
    public static YosCoinsConfig getConfig() {
        if (config == null) config = YosCoinsConfig.load();
        return config;
    }

    /* 供 YosCoinsConfig.save() 调用，保证内存实时更新 */
    public static void setConfig(YosCoinsConfig newCfg) {
        config = newCfg;
    }

    @Override
    public void onInitializeClient() {
        YosCoinsHud.register();   // 注册 HUD 渲染事件
    }
}