package yoscoins.init;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import yoscoins.YosCoins;
import yoscoins.block.entity.PiggyBankBlockEntity;

public class YosCoinsBlockEntities {
    public static BlockEntityType<PiggyBankBlockEntity> PIGGY_BANK;

    public static void onInitialize() {
        PIGGY_BANK = Registry.register(Registries.BLOCK_ENTITY_TYPE,
                new Identifier(YosCoins.MOD_ID, "piggy_bank"),
                FabricBlockEntityTypeBuilder.create(PiggyBankBlockEntity::new, YosCoinsBlocks.PIGGY_BANK).build(null));
    }
}