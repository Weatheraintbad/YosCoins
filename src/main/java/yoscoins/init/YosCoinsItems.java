package yoscoins.init;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import yoscoins.YosCoins;
import yoscoins.item.MoneyPouchItem;

public class YosCoinsItems {

    public static final Item COPPER_COIN = new Item(new Item.Settings());
    public static final Item SILVER_COIN = new Item(new Item.Settings());
    public static final Item GOLD_COIN   = new Item(new Item.Settings());
    public static final Item MONEY_POUCH = new MoneyPouchItem(new Item.Settings().maxCount(1));

    public static final RegistryKey<net.minecraft.item.ItemGroup> COIN_GROUP_KEY =
            RegistryKey.of(Registries.ITEM_GROUP.getKey(), new Identifier(YosCoins.MOD_ID, "coin"));

    public static void onInitialize() {
        Registry.register(Registries.ITEM, id("copper_coin"), COPPER_COIN);
        Registry.register(Registries.ITEM, id("silver_coin"), SILVER_COIN);
        Registry.register(Registries.ITEM, id("gold_coin"),   GOLD_COIN);
        Registry.register(Registries.ITEM, id("money_pouch"), MONEY_POUCH);

        // 注册创造分组（新加入存钱罐）
        Registry.register(Registries.ITEM_GROUP, COIN_GROUP_KEY,
                FabricItemGroup.builder()
                        .icon(() -> new ItemStack(MONEY_POUCH))
                        .displayName(Text.translatable("itemGroup.yoscoins.coin"))
                        .entries((ctx, entries) -> {
                            entries.add(COPPER_COIN);
                            entries.add(SILVER_COIN);
                            entries.add(GOLD_COIN);
                            entries.add(MONEY_POUCH);
                            entries.add(YosCoinsBlocks.PIGGY_BANK);   // <-- 新增
                        })
                        .build());
    }

    private static Identifier id(String path) {
        return new Identifier(YosCoins.MOD_ID, path);
    }
}