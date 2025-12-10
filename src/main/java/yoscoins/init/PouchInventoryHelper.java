package yoscoins.init;

import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;

public final class PouchInventoryHelper {

    private static final String TAG = "Inventory";

    /* 读 NBT -> 库存 */
    public static SimpleInventory read(ItemStack stack) {
        SimpleInventory inv = new SimpleInventory(9);
        NbtCompound nbt = stack.getOrCreateNbt();
        if (nbt.contains(TAG, NbtList.LIST_TYPE)) {
            NbtList list = nbt.getList(TAG, NbtCompound.COMPOUND_TYPE);
            for (int i = 0; i < list.size(); i++) {
                ItemStack coin = ItemStack.fromNbt(list.getCompound(i));
                inv.setStack(i, coin);
            }
        }
        return inv;
    }

    /* 写回 NBT */
    public static void write(ItemStack stack, SimpleInventory inv) {
        NbtList list = new NbtList();
        for (int i = 0; i < inv.size(); i++) {
            ItemStack coin = inv.getStack(i);
            if (!coin.isEmpty()) list.add(coin.writeNbt(new NbtCompound()));
        }
        stack.getOrCreateNbt().put(TAG, list);
    }
}