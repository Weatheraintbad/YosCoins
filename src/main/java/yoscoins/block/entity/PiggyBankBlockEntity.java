package yoscoins.block.entity;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import yoscoins.init.YosCoinsBlockEntities;
import yoscoins.init.YosCoinsItems;
import yoscoins.block.PiggyBankBlock;
import net.minecraft.block.Block;

public class PiggyBankBlockEntity extends BlockEntity {
    private int copper = 0;
    private int silver = 0;
    private int gold = 0;

    public PiggyBankBlockEntity(BlockPos pos, net.minecraft.block.BlockState state) {
        super(YosCoinsBlockEntities.PIGGY_BANK, pos, state);
    }

    public void deposit(PiggyBankBlock.CoinType type, int amount) {
        switch (type) {
            case COPPER -> copper += amount;
            case SILVER -> silver += amount;
            case GOLD -> gold += amount;
        }
        markDirty();
    }

    public void dropAllCoins(World world, BlockPos pos) {
        dropCoins(world, pos, YosCoinsItems.COPPER_COIN, copper);
        dropCoins(world, pos, YosCoinsItems.SILVER_COIN, silver);
        dropCoins(world, pos, YosCoinsItems.GOLD_COIN, gold);
    }

    private void dropCoins(World world, BlockPos pos, net.minecraft.item.Item item, int count) {
        while (count > 0) {
            int stackSize = Math.min(64, count);
            Block.dropStack(world, pos, new ItemStack(item, stackSize));
            count -= stackSize;
        }
    }

    @Override
    public void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        nbt.putInt("Copper", copper);
        nbt.putInt("Silver", silver);
        nbt.putInt("Gold", gold);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        copper = nbt.getInt("Copper");
        silver = nbt.getInt("Silver");
        gold = nbt.getInt("Gold");
    }
}