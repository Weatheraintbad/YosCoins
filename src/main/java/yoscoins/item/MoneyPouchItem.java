package yoscoins.item;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import yoscoins.client.hud.YosCoinsHud;
import yoscoins.init.YosCoinsItems;

import java.util.List;

public class MoneyPouchItem extends Item {

    private static final String TAG_INV = "Inventory";

    // 添加一个静态变量来跟踪最后操作的时间
    private static long lastOperationTime = 0;

    public MoneyPouchItem(Settings settings) { super(settings); }

    // 实现钱袋右键收集、释放钱币
    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack pouch = player.getStackInHand(hand);

        // 记录操作时间
        lastOperationTime = world.getTime();

        if (world.isClient) {
            // 客户端直接标记变化，减少延迟
            YosCoinsHud.markInventoryChanged();
            return TypedActionResult.success(pouch);
        }

        SimpleInventory purse = readInv(pouch);
        PlayerInventory inv = player.getInventory();

        if (purse.isEmpty()) {
            // 收集钱币
            boolean collectedAny = false;
            for (int i = 0; i < inv.size(); i++) {
                ItemStack coin = inv.getStack(i);
                if (coin.getItem() == YosCoinsItems.COPPER_COIN ||
                        coin.getItem() == YosCoinsItems.SILVER_COIN ||
                        coin.getItem() == YosCoinsItems.GOLD_COIN) {
                    purse.addStack(coin.copy());
                    coin.setCount(0);
                    collectedAny = true;
                }
            }

            // 如果没有收集到任何钱币，尝试释放（这个条件应该不会触发，因为purse一开始是空的）
            if (!collectedAny && !purse.isEmpty()) {
                for (int i = 0; i < purse.size(); i++) {
                    ItemStack coin = purse.getStack(i);
                    if (!coin.isEmpty()) inv.offerOrDrop(coin.copy());
                }
                purse.clear();
                writeInv(pouch, purse);
                return TypedActionResult.success(pouch);
            }
        } else {
            // 释放钱币
            for (int i = 0; i < purse.size(); i++) {
                ItemStack coin = purse.getStack(i);
                if (!coin.isEmpty()) inv.offerOrDrop(coin.copy());
            }
            purse.clear();
        }

        // 写入钱袋（无论收集还是释放都需要更新钱袋内容）
        writeInv(pouch, purse);

        return TypedActionResult.success(pouch);
    }

    // 获取最后操作时间，用于HUD同步
    public static long getLastOperationTime() {
        return lastOperationTime;
    }

    // 钱袋动态标签，从上到下依次是金币、银币、铜币（以后加个图标）
    @Override
    public void appendTooltip(ItemStack stack, net.minecraft.world.World world, List<Text> tooltip, TooltipContext context) {
        SimpleInventory purse = readInv(stack);
        int c = 0, s = 0, g = 0;
        for (ItemStack coin : purse.stacks) {
            if (coin.getItem() == YosCoinsItems.GOLD_COIN)   g += coin.getCount();
            if (coin.getItem() == YosCoinsItems.SILVER_COIN) s += coin.getCount();
            if (coin.getItem() == YosCoinsItems.COPPER_COIN) c += coin.getCount();
        }

        // 空袋提示
        if (c == 0 && s == 0 && g == 0) {
            tooltip.add(Text.translatable("tooltip.yoscoins.money_pouch.empty"));
            return;
        }

        // 标签字体颜色
        if (g > 0) tooltip.add(Text.translatable("tooltip.yoscoins.money_pouch.gold",   g).styled(style -> style.withColor(0xFFD700)));
        if (s > 0) tooltip.add(Text.translatable("tooltip.yoscoins.money_pouch.silver", s).styled(style -> style.withColor(0xC0C0C0)));
        if (c > 0) tooltip.add(Text.translatable("tooltip.yoscoins.money_pouch.copper", c).styled(style -> style.withColor(0xFFA500)));
    }

    // 静态工具
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