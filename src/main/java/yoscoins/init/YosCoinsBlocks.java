package yoscoins.init;

import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.MapColor;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import yoscoins.YosCoins;
import yoscoins.block.PiggyBankBlock;

public class YosCoinsBlocks {
    /* 铁镐级挖掘 + 普通石头放置音效 */
    public static final Block PIGGY_BANK = new PiggyBankBlock(
            FabricBlockSettings.copyOf(Blocks.STONE)          // 用石头基础
                    .mapColor(MapColor.STONE_GRAY)
                    .strength(3.0F, 6.0F)                     // 硬度/爆炸抗性
                    .sounds(BlockSoundGroup.STONE)            // 放置声音 = 普通石头
    );

    public static void onInitialize() {
        Registry.register(Registries.BLOCK, new Identifier(YosCoins.MOD_ID, "piggy_bank"), PIGGY_BANK);
        Registry.register(Registries.ITEM, new Identifier(YosCoins.MOD_ID, "piggy_bank"),
                new BlockItem(PIGGY_BANK, new Item.Settings()));
    }
}