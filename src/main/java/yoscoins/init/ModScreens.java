package yoscoins.init;

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import yoscoins.YosCoins;
import yoscoins.screen.PouchScreenHandler;

public class ModScreens {
    public static final ExtendedScreenHandlerType<PouchScreenHandler> POUCH =
            Registry.register(Registries.SCREEN_HANDLER,
                    new Identifier(YosCoins.MOD_ID, "pouch"),
                    new ExtendedScreenHandlerType<>(PouchScreenHandler::new));

    public static void init() { /* 空着即可 */ }
}