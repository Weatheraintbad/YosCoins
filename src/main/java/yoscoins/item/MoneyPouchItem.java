package yoscoins.item;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import yoscoins.init.YosCoinsItems;

import java.util.List;

public class MoneyPouchItem extends Item {

    private static final String TAG_INV = "Inventory";

    public MoneyPouchItem(Settings settings) { super(settings); }

    /* 右键吸/倒币（已存在，保留） */
    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack pouch = player.getStackInHand(hand);
        if (world.isClient) return TypedActionResult.success(pouch);

        SimpleInventory purse = readInv(pouch);
        PlayerInventory inv = player.getInventory();

        if (purse.isEmpty()) {
            /* 吸币 */
            for (int i = 0; i < inv.size(); i++) {
                ItemStack coin = inv.getStack(i);
                if (coin.getItem() == YosCoinsItems.COPPER_COIN ||
                        coin.getItem() == YosCoinsItems.SILVER_COIN ||
                        coin.getItem() == YosCoinsItems.GOLD_COIN) {
                    purse.addStack(coin.copy());
                    coin.setCount(0);
                }
            }
        } else {
            /* 倒币 */
            for (int i = 0; i < purse.size(); i++) {
                ItemStack coin = purse.getStack(i);
                if (!coin.isEmpty()) inv.offerOrDrop(coin.copy());
            }
            purse.clear();
        }
        writeInv(pouch, purse);
        return TypedActionResult.success(pouch);
    }

    /* ===== 动态 Tooltip ===== */
    @Override
    public void appendTooltip(ItemStack stack, net.minecraft.world.World world, List<Text> tooltip, TooltipContext context) {
        SimpleInventory purse = readInv(stack);
        int c = 0, s = 0, g = 0;
        for (ItemStack coin : purse.stacks) {
            if (coin.getItem() == YosCoinsItems.COPPER_COIN)   c += coin.getCount();
            if (coin.getItem() == YosCoinsItems.SILVER_COIN) s += coin.getCount();
            if (coin.getItem() == YosCoinsItems.GOLD_COIN)   g += coin.getCount();
        }

        /* 空袋提示 */
        if (c == 0 && s == 0 && g == 0) {
            tooltip.add(Text.translatable("tooltip.yoscoins.money_pouch.empty"));
            return;
        }

        /* 彩色余额 */
        if (c > 0) tooltip.add(Text.translatable("tooltip.yoscoins.money_pouch.copper", c).styled(style -> style.withColor(0xFFA500)));
        if (s > 0) tooltip.add(Text.translatable("tooltip.yoscoins.money_pouch.silver", s).styled(style -> style.withColor(0xC0C0C0)));
        if (g > 0) tooltip.add(Text.translatable("tooltip.yoscoins.money_pouch.gold",   g).styled(style -> style.withColor(0xFFD700)));
    }

    /* 静态工具（已存在，保留） */
    public static SimpleInventory readInv(ItemStack stack) {
        SimpleInventory inv = new SimpleInventory(9);
        NbtCompound nbt = stack.getOrCreateNbt();
        if (nbt.contains(TAG_INV, NbtList.LIST_TYPE)) {
            NbtList list = nbt.getList(TAG_INV, NbtCompound.COMPOUND_TYPE);
            for (int i = 0; i < list.size(); i++) {
                ItemStack coin = ItemStack.fromNbt(list.getCompound(i));
                inv.setStack(i, coin);
            }
        }
        return inv;
    }

    public static void writeInv(ItemStack stack, SimpleInventory inv) {
        NbtList list = new NbtList();
        for (int i = 0; i < inv.size(); i++) {
            ItemStack coin = inv.getStack(i);
            if (!coin.isEmpty()) list.add(coin.writeNbt(new NbtCompound()));
        }
        stack.getOrCreateNbt().put(TAG_INV, list);
    }
}