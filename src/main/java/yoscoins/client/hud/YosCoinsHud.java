package yoscoins.client.hud;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import yoscoins.client.YosCoinsClient;
import yoscoins.config.YosCoinsConfig;
import yoscoins.init.YosCoinsItems;
import yoscoins.item.MoneyPouchItem;

public final class YosCoinsHud implements HudRenderCallback {

    private static final Identifier COPPER_ICON = new Identifier("yoscoins", "textures/gui/copper_icon.png");
    private static final Identifier SILVER_ICON = new Identifier("yoscoins", "textures/gui/silver_icon.png");
    private static final Identifier GOLD_ICON   = new Identifier("yoscoins", "textures/gui/gold_icon.png");

    public static final YosCoinsHud INSTANCE = new YosCoinsHud();

    private int cachedX, cachedY;
    private boolean cachedTopRight;

    public static void register() {
        HudRenderCallback.EVENT.register(INSTANCE);
        INSTANCE.reloadConfig();
    }

    public void reloadConfig() {
        YosCoinsConfig cfg = YosCoinsClient.getConfig();
        if (cfg == null) return;
        cachedTopRight = cfg.hudTopRight;
        cachedX = Math.max(0, cfg.hudOffsetX);
        cachedY = Math.max(0, cfg.hudOffsetY);
    }

    @Override
    public void onHudRender(DrawContext context, float tickDelta) {
        if (!YosCoinsClient.getConfig().hudEnabled) return;

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        int windowWidth  = mc.getWindow().getWidth();
        int windowHeight = mc.getWindow().getHeight();
        double guiScale  = mc.getWindow().getScaleFactor();

        int boxW = (int)(45 * guiScale);
        int boxH = (int)((11 * 2 + 9) * guiScale);
        int offX = (int)(cachedX * guiScale);
        int offY = (int)(cachedY * guiScale);

        int x = cachedTopRight ? windowWidth - boxW - offX : offX;
        int y = offY;

        x = Math.max(2, Math.min(x, windowWidth - boxW - 2));
        y = Math.max(2, Math.min(y, windowHeight - boxH - 2));

        int copper = 0, silver = 0, gold = 0;
        for (ItemStack stack : mc.player.getInventory().main) {
            if (stack.isEmpty()) continue;
            if (stack.getItem() == YosCoinsItems.COPPER_COIN) copper += stack.getCount();
            if (stack.getItem() == YosCoinsItems.SILVER_COIN) silver += stack.getCount();
            if (stack.getItem() == YosCoinsItems.GOLD_COIN)   gold   += stack.getCount();
            if (stack.getItem() == YosCoinsItems.MONEY_POUCH) {
                SimpleInventory pouch = MoneyPouchItem.readInv(stack);
                for (ItemStack coin : pouch.stacks) {
                    if (coin.getItem() == YosCoinsItems.COPPER_COIN) copper += coin.getCount();
                    if (coin.getItem() == YosCoinsItems.SILVER_COIN) silver += coin.getCount();
                    if (coin.getItem() == YosCoinsItems.GOLD_COIN)   gold   += coin.getCount();
                }
            }
        }
        for (ItemStack stack : mc.player.getInventory().offHand) {
            if (stack.isEmpty()) continue;
            if (stack.getItem() == YosCoinsItems.COPPER_COIN) copper += stack.getCount();
            if (stack.getItem() == YosCoinsItems.SILVER_COIN) silver += stack.getCount();
            if (stack.getItem() == YosCoinsItems.GOLD_COIN)   gold   += stack.getCount();
        }
        for (ItemStack stack : mc.player.getInventory().armor) {
            if (stack.isEmpty()) continue;
            if (stack.getItem() == YosCoinsItems.COPPER_COIN) copper += stack.getCount();
            if (stack.getItem() == YosCoinsItems.SILVER_COIN) silver += stack.getCount();
            if (stack.getItem() == YosCoinsItems.GOLD_COIN)   gold   += stack.getCount();
        }

        int gap = 11;
        drawIcon(context, mc, GOLD_ICON,   gold,   0xFFD700, x, y);
        drawIcon(context, mc, SILVER_ICON, silver, 0xC0C0C0, x, y + gap);
        drawIcon(context, mc, COPPER_ICON, copper, 0xFFA500, x, y + gap * 2);
    }

    private void drawIcon(DrawContext ctx, MinecraftClient mc, Identifier texture,
                          int amount, int color, int x, int y) {
        ctx.drawTexture(texture, x, y, 0, 0, 9, 9, 9, 9);

        String txt   = String.valueOf(amount);
        int    txtW  = mc.textRenderer.getWidth(txt);
        float  scale = 0.75f;
        int    txtX  = (int)(x + 11 + (16 - txtW * scale) / 2f);
        int    txtY  = y + 2;                      // 往下 2 像素，与 9×9 图标中心对齐

        ctx.getMatrices().push();
        ctx.getMatrices().scale(scale, scale, 1.0f);
        ctx.drawText(mc.textRenderer, txt,
                (int)(txtX / scale), (int)(txtY / scale),
                color, false);
        ctx.getMatrices().pop();
    }
}