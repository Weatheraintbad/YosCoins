package yoscoins.client.screen;


import yoscoins.client.YosCoinsClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import yoscoins.client.hud.YosCoinsHud;
import yoscoins.config.YosCoinsConfig;


public class HudConfigScreen extends Screen {

    private static final Text TITLE       = Text.translatable("screen.yoscoins.hud_config");
    private static final Text OFFSET_X    = Text.translatable("screen.yoscoins.offset_x");
    private static final Text OFFSET_Y    = Text.translatable("screen.yoscoins.offset_y");
    private static final Text TOP_RIGHT   = Text.translatable("screen.yoscoins.top_right");

    private TextFieldWidget offsetXField;
    private TextFieldWidget offsetYField;
    private ButtonWidget    topRightBtn;

    public HudConfigScreen() {
        super(TITLE);
    }

    @Override
    protected void init() {
        // 加载最新配置
        YosCoinsConfig cfg = YosCoinsClient.getConfig();

        offsetXField = new TextFieldWidget(textRenderer, width / 2 - 100, 60, 200, 20, OFFSET_X);
        offsetXField.setText(String.valueOf(cfg.hudOffsetX));
        addDrawableChild(offsetXField);

        offsetYField = new TextFieldWidget(textRenderer, width / 2 - 100, 90, 200, 20, OFFSET_Y);
        offsetYField.setText(String.valueOf(cfg.hudOffsetY));
        addDrawableChild(offsetYField);

        topRightBtn = ButtonWidget.builder(
                cfg.hudTopRight ? TOP_RIGHT : Text.translatable("screen.yoscoins.top_left"),
                b -> {
                    cfg.hudTopRight = !cfg.hudTopRight;
                    b.setMessage(cfg.hudTopRight ? TOP_RIGHT : Text.translatable("screen.yoscoins.top_left"));
                }).dimensions(width / 2 - 100, 120, 200, 20).build();
        addDrawableChild(topRightBtn);

        ButtonWidget enabledBtn = ButtonWidget.builder(
                cfg.hudEnabled ? Text.literal("显示") : Text.literal("隐藏"),
                b -> {
                    cfg.hudEnabled = !cfg.hudEnabled;
                    b.setMessage(cfg.hudEnabled ? Text.literal("显示") : Text.literal("隐藏"));
                }).dimensions(width / 2 - 100, 150, 200, 20).build();
        addDrawableChild(enabledBtn);

        // 保存并立即刷新
        addDrawableChild(ButtonWidget.builder(
                Text.translatable("screen.yoscoins.save"),
                b -> {
                    try {
                        cfg.hudOffsetX  = Integer.parseInt(offsetXField.getText());
                        cfg.hudOffsetY  = Integer.parseInt(offsetYField.getText());
                        cfg.hudTopRight = topRightBtn.getMessage().equals(TOP_RIGHT);
                        cfg.hudEnabled  = enabledBtn.getMessage().equals(Text.literal("显示"));

                        cfg.save();                          // 写盘
                        YosCoinsHud.INSTANCE.reloadConfig(); // 立即通知 HUD
                        close();
                    } catch (NumberFormatException ignore) {}
                }).dimensions(width / 2 - 100, height - 30, 200, 20).build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, width, height, 0x4D000000);
        context.drawCenteredTextWithShadow(textRenderer, title, width / 2, 20, 0xFFFFFF);
        context.drawCenteredTextWithShadow(textRenderer, OFFSET_X, width / 2 - 100, 50, 0xFFFFFF);
        context.drawCenteredTextWithShadow(textRenderer, OFFSET_Y, width / 2 - 100, 80, 0xFFFFFF);
        super.render(context, mouseX, mouseY, delta);
    }
}