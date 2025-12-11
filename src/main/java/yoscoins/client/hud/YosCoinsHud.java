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

    /* 缓存 */
    private int cachedX, cachedY;
    private final int[] counts = new int[3];
    private long lastSnapTick = -1;

    /* 对外接口：钱袋内容变化时调用 */
    public static void markInventoryChanged() {
        INSTANCE.lastSnapTick = -1;   // 强制下一帧重新统计
    }

    public static void register() {
        HudRenderCallback.EVENT.register(INSTANCE);
        reloadConfig();
    }

    public static void reloadConfig() {
        YosCoinsConfig cfg = YosCoinsClient.getConfig();
        if (cfg == null) return;
        INSTANCE.cachedX = Math.max(0, cfg.hudOffsetX);
        INSTANCE.cachedY = Math.max(0, cfg.hudOffsetY);
    }

    /* ---------- 实时统计 ---------- */
    private void snapshotCoins() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;
        long now = mc.player.age;
        if (lastSnapTick == now) return;        // 同帧复用
        lastSnapTick = now;

        int copper = 0, silver = 0, gold = 0;

        /* 散装钱币 */
        for (ItemStack s : mc.player.getInventory().main) {
            if (s.isEmpty()) continue;
            if (s.getItem() == YosCoinsItems.COPPER_COIN)      copper += s.getCount();
            else if (s.getItem() == YosCoinsItems.SILVER_COIN) silver += s.getCount();
            else if (s.getItem() == YosCoinsItems.GOLD_COIN)   gold   += s.getCount();
        }
        for (ItemStack s : mc.player.getInventory().offHand) {
            if (s.isEmpty()) continue;
            if (s.getItem() == YosCoinsItems.COPPER_COIN)      copper += s.getCount();
            else if (s.getItem() == YosCoinsItems.SILVER_COIN) silver += s.getCount();
            else if (s.getItem() == YosCoinsItems.GOLD_COIN)   gold   += s.getCount();
        }
        for (ItemStack s : mc.player.getInventory().armor) {
            if (s.isEmpty()) continue;
            if (s.getItem() == YosCoinsItems.COPPER_COIN)      copper += s.getCount();
            else if (s.getItem() == YosCoinsItems.SILVER_COIN) silver += s.getCount();
            else if (s.getItem() == YosCoinsItems.GOLD_COIN)   gold   += s.getCount();
        }

        /* 钱袋内部 */
        for (ItemStack s : mc.player.getInventory().main) {
            if (s.getItem() == YosCoinsItems.MONEY_POUCH) {
                SimpleInventory pouch = MoneyPouchItem.readInv(s);
                for (ItemStack c : pouch.stacks) {
                    if (c.isEmpty()) continue;
                    if (c.getItem() == YosCoinsItems.COPPER_COIN)      copper += c.getCount();
                    else if (c.getItem() == YosCoinsItems.SILVER_COIN) silver += c.getCount();
                    else if (c.getItem() == YosCoinsItems.GOLD_COIN)   gold   += c.getCount();
                }
            }
        }
        for (ItemStack s : mc.player.getInventory().offHand) {
            if (s.getItem() == YosCoinsItems.MONEY_POUCH) {
                SimpleInventory pouch = MoneyPouchItem.readInv(s);
                for (ItemStack c : pouch.stacks) {
                    if (c.isEmpty()) continue;
                    if (c.getItem() == YosCoinsItems.COPPER_COIN)      copper += c.getCount();
                    else if (c.getItem() == YosCoinsItems.SILVER_COIN) silver += c.getCount();
                    else if (c.getItem() == YosCoinsItems.GOLD_COIN)   gold   += c.getCount();
                }
            }
        }

        counts[0] = copper;
        counts[1] = silver;
        counts[2] = gold;
    }

    /* ---------- 渲染 ---------- */
    @Override
    public void onHudRender(DrawContext ctx, float tickDelta) {
        YosCoinsConfig cfg = YosCoinsClient.getConfig();
        if (cfg == null || !cfg.hudEnabled) return;

        snapshotCoins();

        int copper = counts[0], silver = counts[1], gold = counts[2];
        int x = cachedX;
        int y = cachedY;
        int gap = 11;

        drawIcon(ctx, GOLD_ICON,   gold,   0xFFD700, x, y);
        drawIcon(ctx, SILVER_ICON, silver, 0xC0C0C0, x, y + gap);
        drawIcon(ctx, COPPER_ICON, copper, 0xFFA500, x, y + gap * 2);
    }

    private void drawIcon(DrawContext ctx, Identifier texture,
                          int amount, int color, int x, int y) {
        ctx.drawTexture(texture, x, y, 0, 0, 9, 9, 9, 9);
        String txt   = String.valueOf(amount);
        int    txtW  = MinecraftClient.getInstance().textRenderer.getWidth(txt);
        float  scale = 0.75f;
        int    txtX  = (int)(x + 11 + (16 - txtW * scale) / 2f);
        int    txtY  = y + 2;
        ctx.getMatrices().push();
        ctx.getMatrices().scale(scale, scale, 1.0f);
        ctx.drawText(MinecraftClient.getInstance().textRenderer, txt,
                (int)(txtX / scale), (int)(txtY / scale),
                color, false);
        ctx.getMatrices().pop();
    }
}