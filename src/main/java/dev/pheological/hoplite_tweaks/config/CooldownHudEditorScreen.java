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
    private static final int PANEL = 0xE610141D;
    private static final int PANEL_EDGE = 0xFF4A607E;
    private static final int ROW = 0xE819202C;
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
        return Math.round((HopliteTweaksConfig.get().compactCooldowns ? 112 : 148) * scale);
    }

    private int previewHeight() {
        float scale = HopliteTweaksConfig.get().hudScalePercent / 100.0F;
        int rowHeight = Math.round((HopliteTweaksConfig.get().compactCooldowns ? 23 : 30) * scale);
        return Math.round(26 * scale) + rowHeight * 2;
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
        int header = Math.round(21 * scale);
        int rowHeight = Math.round((HopliteTweaksConfig.get().compactCooldowns ? 23 : 30) * scale);

        fill(graphics, x, y, x + width, y + height, PANEL);
        outline(graphics, x, y, width, height, dragging ? 0xFF74B9FF : PANEL_EDGE);
        text(graphics, Component.literal("COOLDOWNS"), x + 8, y + 7, MUTED);
        previewRow(graphics, x, y + header, width, rowHeight, "Ender Pearl", "14s", RED, 0.75F);
        previewRow(graphics, x, y + header + rowHeight, width, rowHeight, "Mace", "2.1", GREEN, 0.18F);
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
        int innerX = x + 7;
        int innerWidth = width - 14;
        fill(graphics, innerX, y, innerX + innerWidth, y + rowHeight - 3, ROW);
        fill(graphics, innerX, y, innerX + 3, y + rowHeight - 3, color);
        text(graphics, Component.literal(name), innerX + 7, y + 4, WHITE);
        text(graphics, Component.literal(time), innerX + innerWidth - 28, y + 4, color);
        int barY = y + rowHeight - 7;
        fill(graphics, innerX + 7, barY, innerX + innerWidth - 5, barY + 2, 0xFF303B4D);
        fill(graphics, innerX + 7, barY, innerX + 7 + Math.round((innerWidth - 12) * bar), barY + 2, color);
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
