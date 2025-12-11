package yoscoins.client.screen;

import yoscoins.client.YosCoinsClient;
import yoscoins.client.hud.YosCoinsHud;
import yoscoins.config.YosCoinsConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

public class HudConfigScreen extends Screen {

    private static final Text TITLE    = Text.translatable("screen.yoscoins.hud_config");
    private static final Text OFFSET_X = Text.translatable("screen.yoscoins.offset_x");
    private static final Text OFFSET_Y = Text.translatable("screen.yoscoins.offset_y");

    private TextFieldWidget offsetXField;
    private TextFieldWidget offsetYField;
    private YosCoinsConfig liveCfg;

    /* 尺寸与 1.1 倍间距 */
    private static final int FIELD_W = 200;
    private static final int FIELD_H = 20;
    private static final int BUTTON_W = 200;
    private static final int BUTTON_H = 20;
    private static final float GAP_MUL = 1.1f;
    private static final int LABEL_H = 12;                 // 文字高度
    private static final int LABEL_GAP = (int)(8 * GAP_MUL);
    private static final int CTRL_GAP  = (int)(18 * GAP_MUL);

    public HudConfigScreen() {
        super(TITLE);
    }

    @Override
    protected void init() {
        liveCfg = YosCoinsClient.getConfig();

        int centerX = width / 2;
        int y = 60;

        /* ---- X 偏移 ---- */
        y += LABEL_H;
        offsetXField = new TextFieldWidget(textRenderer, centerX - FIELD_W / 2, y, FIELD_W, FIELD_H, OFFSET_X);
        offsetXField.setText(String.valueOf(liveCfg.hudOffsetX));
        offsetXField.setChangedListener(str -> applyPreview());
        addDrawableChild(offsetXField);

        y += FIELD_H + LABEL_GAP;

        /* ---- Y 偏移 ---- */
        y += LABEL_H;
        offsetYField = new TextFieldWidget(textRenderer, centerX - FIELD_W / 2, y, FIELD_W, FIELD_H, OFFSET_Y);
        offsetYField.setText(String.valueOf(liveCfg.hudOffsetY));
        offsetYField.setChangedListener(str -> applyPreview());
        addDrawableChild(offsetYField);

        y += FIELD_H + CTRL_GAP;

        /* ---- 显示 / 隐藏 ---- */
        ButtonWidget enabledBtn = ButtonWidget.builder(
                liveCfg.hudEnabled ? Text.literal("已显示") : Text.literal("已隐藏"),
                b -> {
                    liveCfg.hudEnabled = !liveCfg.hudEnabled;
                    b.setMessage(liveCfg.hudEnabled ? Text.literal("已显示") : Text.literal("已隐藏"));
                    YosCoinsHud.reloadConfig();
                }).dimensions(centerX - BUTTON_W / 2, y, BUTTON_W, BUTTON_H).build();
        addDrawableChild(enabledBtn);

        y += BUTTON_H + CTRL_GAP;

        /* ---- 保存 ---- */
        addDrawableChild(ButtonWidget.builder(
                Text.translatable("screen.yoscoins.save"),
                b -> {
                    try {
                        liveCfg.hudOffsetX = Integer.parseInt(offsetXField.getText());
                        liveCfg.hudOffsetY = Integer.parseInt(offsetYField.getText());
                        liveCfg.save();
                        close();
                    } catch (NumberFormatException ignored) {}
                }).dimensions(centerX - BUTTON_W / 2, y, BUTTON_W, BUTTON_H).build());
    }

    private void applyPreview() {
        try {
            liveCfg.hudOffsetX = Integer.parseInt(offsetXField.getText());
            liveCfg.hudOffsetY = Integer.parseInt(offsetYField.getText());
            YosCoinsHud.reloadConfig();
        } catch (NumberFormatException ignored) {}
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        renderBackground(ctx);
        ctx.drawCenteredTextWithShadow(textRenderer, title, width / 2, 20, 0xFFFFFF);

        /* 手动绘制透明标签 */
        int centerX = width / 2;
        int y = 60;
        ctx.drawCenteredTextWithShadow(textRenderer, OFFSET_X, centerX, y, 0xFFFFFF);
        y += LABEL_H + FIELD_H + LABEL_GAP;
        ctx.drawCenteredTextWithShadow(textRenderer, OFFSET_Y, centerX, y, 0xFFFFFF);

        super.render(ctx, mouseX, mouseY, delta);
    }
}