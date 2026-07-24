package dev.pheological.hoplite_tweaks.config;

//? >=26 {
/*import net.minecraft.client.gui.GuiGraphicsExtractor;
*///?} else {
import net.minecraft.client.gui.GuiGraphics;
//?}
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

/**
 * Lightweight visual editor for positioning the cooldown overlay.
 */
public final class CooldownHudEditorScreen extends Screen {
    private static final int PANEL_EDGE = 0xFF4A607E;
    private static final int ROW = 0xDD111721;
    private static final int ROW_BORDER = 0xA037465C;
    private static final int ROW_SHADOW = 0x66000000;
    private static final int PROGRESS_TRACK = 0xB02B3546;
    private static final int MUTED = 0xFF9AA7B8;
    private static final int WHITE = 0xFFF4F7FB;
    private static final int RED = 0xFFFF6B7A;
    private static final int GREEN = 0xFF54D6A2;

    private final Screen parent;
    private boolean dragging;
    private double dragOffsetX;
    private double dragOffsetY;

    public CooldownHudEditorScreen(Screen parent) {
        super(Component.literal("Cooldown HUD Editor"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        addRenderableWidget(Button.builder(Component.literal("Done"), button -> onClose())
            .bounds(width - 106, 8, 98, 20)
            .build());
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() == 0) {
            int x = previewX();
            int y = previewY();
            if (event.x() >= x && event.x() <= x + previewWidth()
                && event.y() >= y && event.y() <= y + previewHeight()) {
                dragging = true;
                dragOffsetX = event.x() - x;
                dragOffsetY = event.y() - y;
                return true;
            }
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseDragged(
        MouseButtonEvent event,
        double dragX,
        double dragY
    ) {
        if (dragging && event.button() == 0) {
            updatePosition(event.x() - dragOffsetX, event.y() - dragOffsetY);
            return true;
        }
        return super.mouseDragged(event, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (event.button() == 0 && dragging) {
            dragging = false;
            HopliteTweaksConfig.save();
            return true;
        }
        return super.mouseReleased(event);
    }

    private void updatePosition(double requestedX, double requestedY) {
        int availableX = Math.max(1, width - previewWidth());
        int availableY = Math.max(1, height - previewHeight());
        int x = Math.clamp((int) Math.round(requestedX), 0, availableX);
        int y = Math.clamp((int) Math.round(requestedY), 0, availableY);
        HopliteTweaksConfig config = HopliteTweaksConfig.get();
        config.hudXPercent = Math.round(x * 100.0F / availableX);
        config.hudYPercent = Math.round(y * 100.0F / availableY);
    }

    private int previewX() {
        return Math.round(
            Math.max(0, width - previewWidth()) * HopliteTweaksConfig.get().hudXPercent / 100.0F
        );
    }

    private int previewY() {
        return Math.round(
            Math.max(0, height - previewHeight()) * HopliteTweaksConfig.get().hudYPercent / 100.0F
        );
    }

    private int previewWidth() {
        float scale = HopliteTweaksConfig.get().hudScalePercent / 100.0F;
        return Math.round((HopliteTweaksConfig.get().compactCooldowns ? 100 : 126) * scale);
    }

    private int previewHeight() {
        float scale = HopliteTweaksConfig.get().hudScalePercent / 100.0F;
        int rowHeight = Math.round((HopliteTweaksConfig.get().compactCooldowns ? 17 : 21) * scale);
        return rowHeight * 2 + Math.max(1, Math.round(2 * scale));
    }

    @Override
    public void onClose() {
        HopliteTweaksConfig.save();
        if (minecraft != null) {
            //? >=26.2 {
            /*minecraft.gui.setScreen(parent);
            *///?} else {
            minecraft.setScreen(parent);
            //?}
        }
    }

    //? >=26 {
    /*@Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractRenderState(graphics, mouseX, mouseY, delta);
        drawEditor(graphics);
    }
    *///?} else {
    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        super.render(graphics, mouseX, mouseY, delta);
        drawEditor(graphics);
    }
    //?}

    private void drawEditor(Object graphics) {
        centeredText(graphics, title, width / 2, 12, WHITE);
        centeredText(
            graphics,
            Component.literal("Drag the cooldown preview. Its position saves automatically."),
            width / 2,
            26,
            MUTED
        );

        int x = previewX();
        int y = previewY();
        int width = previewWidth();
        int height = previewHeight();
        float scale = HopliteTweaksConfig.get().hudScalePercent / 100.0F;
        int rowHeight = Math.round((HopliteTweaksConfig.get().compactCooldowns ? 17 : 21) * scale);
        int gap = Math.max(1, Math.round(2 * scale));

        outline(graphics, x, y, width, height, dragging ? 0xFF74B9FF : PANEL_EDGE);
        previewRow(graphics, x, y, width, rowHeight, "Ender Pearl", "14s", RED, 0.75F);
        previewRow(graphics, x, y + rowHeight + gap, width, rowHeight, "Mace", "2.1", GREEN, 0.18F);
    }

    private void previewRow(
        Object graphics,
        int x,
        int y,
        int width,
        int rowHeight,
        String name,
        String time,
        int color,
        float bar
    ) {
        roundedFill(graphics, x + 1, y + 2, width, rowHeight, ROW_SHADOW);
        roundedFill(graphics, x, y, width, rowHeight, ROW_BORDER);
        roundedFill(graphics, x + 1, y + 1, width - 2, rowHeight - 2, ROW);
        fill(graphics, x + 4, y + 1, x + width - 4, y + 2, 0x20FFFFFF);
        int textY = y + Math.max(3, Math.round((rowHeight - 9) / 2.0F));
        text(graphics, Component.literal(name), x + 6, textY, WHITE);
        int badgeWidth = font.width(time) + 8;
        int badgeX = x + width - badgeWidth - 3;
        roundedFill(graphics, badgeX, y + 3, badgeWidth, rowHeight - 7,
            withAlpha(color, 0x2E));
        text(graphics, Component.literal(time), badgeX + 4, textY, color);
        int trackWidth = width - 10;
        fill(graphics, x + 5, y + rowHeight - 3, x + width - 5, y + rowHeight - 1,
            PROGRESS_TRACK);
        fill(graphics, x + 5, y + rowHeight - 3,
            x + 5 + Math.round(trackWidth * bar), y + rowHeight - 1, color);
    }

    private static int withAlpha(int color, int alpha) {
        return alpha << 24 | color & 0x00FFFFFF;
    }

    private static void outline(Object graphics, int x, int y, int width, int height, int color) {
        fill(graphics, x, y, x + width, y + 1, color);
        fill(graphics, x, y + height - 1, x + width, y + height, color);
        fill(graphics, x, y, x + 1, y + height, color);
        fill(graphics, x + width - 1, y, x + width, y + height, color);
    }

    private static void fill(Object graphics, int left, int top, int right, int bottom, int color) {
        //? >=26 {
        /*((GuiGraphicsExtractor) graphics).fill(left, top, right, bottom, color);
        *///?} else {
        ((GuiGraphics) graphics).fill(left, top, right, bottom, color);
        //?}
    }

    private static void roundedFill(
        Object graphics,
        int x,
        int y,
        int width,
        int height,
        int color
    ) {
        if (width <= 2 || height <= 2) {
            fill(graphics, x, y, x + width, y + height, color);
            return;
        }
        fill(graphics, x + 1, y, x + width - 1, y + height, color);
        fill(graphics, x, y + 1, x + width, y + height - 1, color);
    }

    private void text(Object graphics, Component value, int x, int y, int color) {
        //? >=26 {
        /*((GuiGraphicsExtractor) graphics).text(font, value, x, y, color, false);
        *///?} else {
        ((GuiGraphics) graphics).drawString(font, value, x, y, color, false);
        //?}
    }

    private void centeredText(Object graphics, Component value, int x, int y, int color) {
        //? >=26 {
        /*((GuiGraphicsExtractor) graphics).centeredText(font, value, x, y, color);
        *///?} else {
        ((GuiGraphics) graphics).drawCenteredString(font, value, x, y, color);
        //?}
    }
}
