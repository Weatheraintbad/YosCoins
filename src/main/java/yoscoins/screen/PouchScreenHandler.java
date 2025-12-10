package yoscoins.screen;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import yoscoins.item.MoneyPouchItem;

public class PouchScreenHandler extends GenericContainerScreenHandler {

    private final ItemStack pouch;   // 本地副本

    public PouchScreenHandler(int syncId, PlayerInventory playerInventory, PacketByteBuf buf) {
        super(ScreenHandlerType.GENERIC_9X1, syncId, playerInventory,
                new SimpleInventory(9), 1);
        this.pouch = buf.readItemStack();
        SimpleInventory inv = MoneyPouchItem.readInv(pouch);
        for (int i = 0; i < 9; i++) this.getSlot(i).setStack(inv.getStack(i));
    }

    @Override
    public void onClosed(PlayerEntity player) {
        super.onClosed(player);
        SimpleInventory inv = new SimpleInventory(9);
        for (int i = 0; i < 9; i++) inv.setStack(i, getSlot(i).getStack());
        MoneyPouchItem.writeInv(pouch, inv);
    }
}