package yoscoins.networking;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.util.Identifier;

public class YosCoinsNetworking {
    public static final Identifier INVENTORY_CHANGED_PACKET_ID =
            new Identifier("yoscoins", "inventory_changed");

    public static void registerClientReceivers() {
        ClientPlayNetworking.registerGlobalReceiver(INVENTORY_CHANGED_PACKET_ID,
                (client, handler, buf, responseSender) -> {
                    client.execute(() -> {
                        yoscoins.client.hud.YosCoinsHud.markInventoryChanged();
                    });
                });
    }
}