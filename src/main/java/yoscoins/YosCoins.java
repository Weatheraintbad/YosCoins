package yoscoins;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yoscoins.init.ModScreens;
import yoscoins.init.YosCoinsBlockEntities;
import yoscoins.init.YosCoinsBlocks;
import yoscoins.init.YosCoinsItems;
import yoscoins.client.hud.YosCoinsHud;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;
import yoscoins.networking.YosCoinsNetworking;

public class YosCoins implements ModInitializer, ClientModInitializer {
    public static final String MOD_ID = "yoscoins";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    /* 服务端/公共初始化 */
    @Override
    public void onInitialize() {
        LOGGER.info("[{}] Common initialization started", MOD_ID);
        YosCoinsItems.onInitialize();   // 硬币 + 钱袋物品
        ModScreens.init();              // ScreenHandler
        YosCoinsBlocks.onInitialize();
        YosCoinsBlockEntities.onInitialize();

    }

    /* 客户端初始化 */
    @Override
    public void onInitializeClient() {
        LOGGER.info("[{}] Client initialization started", MOD_ID);
        YosCoinsHud.register();         // 仅客户端渲染
        YosCoinsNetworking.registerClientReceivers();
    }
}