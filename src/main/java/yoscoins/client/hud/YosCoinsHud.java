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

    /* ----------- 实时统计 + 同帧复用 ----------- */
    private final int[] counts = new int[3];
    private long lastSnapTick = -1;          // 版本号：客户端 tick

    // 添加：用于同步和避免重复计算的锁/标志
    private boolean isSnapshotInProgress = false;
    private long lastInventoryChangeTick = -1;
    private boolean shouldUseCachedForThisFrame = false;

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

    // 添加：当检测到库存变化时调用（需要在其他地方调用，比如钱袋收入钱币时）
    public static void markInventoryChanged() {
        if (MinecraftClient.getInstance().player != null) {
            INSTANCE.lastInventoryChangeTick = MinecraftClient.getInstance().player.age;
            // 标记接下来几帧使用缓存值，避免瞬时的重复计算
            INSTANCE.shouldUseCachedForThisFrame = true;
        }
    }

    /* ---------- 强制快照：深拷贝钱袋，隔离并发修改 ---------- */
    private static SimpleInventory snapshotPouch(ItemStack pouchStack) {
        SimpleInventory raw = MoneyPouchItem.readInv(pouchStack);
        SimpleInventory snap = new SimpleInventory(raw.size());
        for (int i = 0; i < raw.size(); i++)
            snap.setStack(i, raw.getStack(i).copy());
        return snap;
    }

    /** 实时统计：同帧只算一次 */
    private void snapshotCoins() {
        // 防止重入
        if (isSnapshotInProgress) {
            return;
        }

        MinecraftClient mc = MinecraftClient.getInstance();
        long now = mc.player.age;

        // 同一帧已采样，直接复用
        if (lastSnapTick == now) return;

        // 如果最近有库存变化，并且在同一tick内，使用更稳定的逻辑
        if (now == lastInventoryChangeTick) {
            // 在这种情况下，我们稍微延迟一帧更新，避免重复计算
            shouldUseCachedForThisFrame = true;
        }

        // 如果标记为使用缓存，并且有缓存值，使用缓存
        if (shouldUseCachedForThisFrame && lastSnapTick >= 0) {
            // 只使用缓存的这一帧，下一帧恢复正常
            if (now > lastInventoryChangeTick + 1) {
                shouldUseCachedForThisFrame = false;
            }
            return;
        }

        isSnapshotInProgress = true;
        try {
            lastSnapTick = now;

            int copper = 0, silver = 0, gold = 0;

            // 使用优化的统计方法，避免重复遍历
            // 先统计所有散装钱币
            for (ItemStack s : mc.player.getInventory().main) {
                if (s.isEmpty()) continue;
                if (s.getItem() == YosCoinsItems.COPPER_COIN)          copper += s.getCount();
                else if (s.getItem() == YosCoinsItems.SILVER_COIN)     silver += s.getCount();
                else if (s.getItem() == YosCoinsItems.GOLD_COIN)       gold   += s.getCount();
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

            // 现在统计所有钱袋中的钱币
            // 注意：我们只在散装钱币统计完成后才统计钱袋，避免重复
            for (ItemStack s : mc.player.getInventory().main) {
                if (s.isEmpty()) continue;
                if (s.getItem() == YosCoinsItems.MONEY_POUCH) {
                    SimpleInventory pouch = snapshotPouch(s);
                    for (ItemStack c : pouch.stacks) {
                        if (c.isEmpty()) continue;
                        if (c.getItem() == YosCoinsItems.COPPER_COIN)      copper += c.getCount();
                        else if (c.getItem() == YosCoinsItems.SILVER_COIN) silver += c.getCount();
                        else if (c.getItem() == YosCoinsItems.GOLD_COIN)   gold   += c.getCount();
                    }
                }
            }

            // 检查副手和盔甲槽是否有钱袋
            for (ItemStack s : mc.player.getInventory().offHand) {
                if (!s.isEmpty() && s.getItem() == YosCoinsItems.MONEY_POUCH) {
                    SimpleInventory pouch = snapshotPouch(s);
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

        } finally {
            isSnapshotInProgress = false;
        }
    }

    @Override
    public void onHudRender(DrawContext context, float tickDelta) {
        if (!YosCoinsClient.getConfig().hudEnabled) return;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        snapshotCoins();

        int copper = counts[0], silver = counts[1], gold = counts[2];

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
        int    txtY  = y + 2;
        ctx.getMatrices().push();
        ctx.getMatrices().scale(scale, scale, 1.0f);
        ctx.drawText(mc.textRenderer, txt,
                (int)(txtX / scale), (int)(txtY / scale),
                color, false);
        ctx.getMatrices().pop();
    }
}