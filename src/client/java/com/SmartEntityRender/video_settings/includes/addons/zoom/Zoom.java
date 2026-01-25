package com.SmartEntityRender.video_settings.includes.addons.zoom;

import com.SmartEntityRender.config.Config;
import com.SmartEntityRender.video_settings.VideoSettings;
import com.SmartEntityRender.video_settings.includes.Display;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.option.KeyBinding.Category;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public final class Zoom {
    private Zoom() {
    }

    private static KeyBinding zoomKey;
    private static boolean isZooming = false;
    private static float currentZoomDivisor = 1.0f;
    private static float targetZoomDivisor = 1.0f;
    private static float lastZoomDivisor = 1.0f;

    private static ClickableWidget initialZoomWidget;
    private static ClickableWidget smoothZoomWidget;

    public static void register() {
        zoomKey = new KeyBinding(
                "key.smartentityrender.zoom",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_C,
                Category.MISC);
        KeyBindingHelper.registerKeyBinding(zoomKey);

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) {
                return;
            }

            if (zoomKey.isPressed()) {
                if (!isZooming()) {
                    startZoom();
                }
            } else if (isZooming()) {
                stopZoom();
            }

            tick();
        });
    }

    public static boolean isZooming() {
        return isZooming && Config.getInstance().isCameraZoomEnabled();
    }

    public static float getZoomDivisor(float tickDelta) {
        if (!Config.getInstance().isSmoothZoomEnabled()) {
            return currentZoomDivisor;
        }
        return lastZoomDivisor + (currentZoomDivisor - lastZoomDivisor) * tickDelta;
    }

    private static void startZoom() {
        if (!Config.getInstance().isCameraZoomEnabled()) {
            return;
        }

        isZooming = true;

        int initialZoomPercent = Config.getInstance().getInitialZoomLevel();
        targetZoomDivisor = initialZoomPercent / 100.0f;

        if (!Config.getInstance().isSmoothZoomEnabled()) {
            currentZoomDivisor = targetZoomDivisor;
        }
    }

    private static void stopZoom() {
        isZooming = false;
        targetZoomDivisor = 1.0f;

        if (!Config.getInstance().isSmoothZoomEnabled()) {
            currentZoomDivisor = 1.0f;
        }
    }

    private static void tick() {
        if (Config.getInstance().isSmoothZoomEnabled()) {
            float lerpSpeed = 0.7f;
            currentZoomDivisor += (targetZoomDivisor - currentZoomDivisor) * lerpSpeed;

            if (Math.abs(currentZoomDivisor - targetZoomDivisor) < 0.001f) {
                currentZoomDivisor = targetZoomDivisor;
            }
        }

        lastZoomDivisor = currentZoomDivisor;
    }

    public static final class Panel implements Drawable {
        private final int x;
        private final int y;
        private final int width;
        private final int height;
        private final TextRenderer textRenderer;
        private final Supplier<VideoSettings.Section> selectedSection;

        public Panel(
                int x,
                int y,
                int width,
                int height,
                TextRenderer textRenderer,
                Supplier<VideoSettings.Section> selectedSection) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.textRenderer = textRenderer;
            this.selectedSection = selectedSection;
        }

        @Override
        public void render(DrawContext context, int mouseX, int mouseY, float delta) {
            if (selectedSection.get() != VideoSettings.Section.ADDONS) {
                return;
            }

            int windowH = MinecraftClient.getInstance().getWindow().getScaledHeight();
            int panelH = Math.max(this.height, Math.max(0, windowH - this.y));

            int top = 0x880D1117;
            int bottom = 0x88090B10;
            context.fillGradient(x, y, x + width, y + panelH, top, bottom);

            int border = 0xAA24262C;
            context.fill(x, y, x + width, y + 1, border);
            context.fill(x, y + panelH - 1, x + width, y + panelH, border);
            context.fill(x, y, x + 1, y + panelH, border);
            context.fill(x + width - 1, y, x + width, y + panelH, border);

            int titleX = x + 10;
            int titleY = y + 8;
            context.drawText(textRenderer, Text.literal("Addons"), titleX, titleY, 0xFFEDEDED, false);
            context.drawText(textRenderer, Text.literal("Camera & Special Features"), titleX, titleY + 12, 0xFF9AA0A6,
                    false);
            context.drawText(textRenderer, selectedSection.get().label(), titleX, titleY + 26, 0xFF6B7280, false);
        }
    }

    public static void addControls(
            int x,
            int currentY,
            int width,
            List<ClickableWidget> widgets,
            Function<ClickableWidget, ClickableWidget> register) {

        var zoomToggle = new Display.ToggleButton(
                x,
                currentY,
                width,
                VideoSettings.CONTROL_H,
                Text.literal("Enable Camera Zoom"),
                () -> Config.getInstance().isCameraZoomEnabled(),
                value -> Config.getInstance().setCameraZoomEnabled(value));
        zoomToggle.setTooltip(Tooltip.of(Text.literal(
                "Enable or disable camera zoom functionality\n• On: Zoom with keybind\n• Off: Normal camera")));
        widgets.add(register.apply(zoomToggle));
        currentY += VideoSettings.CONTROL_H + VideoSettings.CONTROL_GAP;

        // Only show zoom options if camera zoom is enabled
        if (Config.getInstance().isCameraZoomEnabled()) {
            var initialZoomSlider = new InitialZoomLevelSlider(
                    x,
                    currentY,
                    width,
                    VideoSettings.CONTROL_H);
            initialZoomSlider.setTooltip(Tooltip.of(Text.literal(
                    "Initial zoom level when activating zoom\n• 100%: No zoom\n• 500%: Maximum zoom")));
            initialZoomWidget = register.apply(initialZoomSlider);
            widgets.add(initialZoomWidget);
            currentY += VideoSettings.CONTROL_H + VideoSettings.CONTROL_GAP;

            var smoothZoomToggle = new Display.ToggleButton(
                    x,
                    currentY,
                    width,
                    VideoSettings.CONTROL_H,
                    Text.literal("Smooth Zoom Animation"),
                    () -> Config.getInstance().isSmoothZoomEnabled(),
                    value -> Config.getInstance().setSmoothZoomEnabled(value));
            smoothZoomToggle.setTooltip(Tooltip.of(Text.literal(
                    "Enable smooth zoom transitions\n• On: Animated zoom\n• Off: Instant zoom")));
            smoothZoomWidget = register.apply(smoothZoomToggle);
            widgets.add(smoothZoomWidget);
        }
    }

    public static boolean isZoomOptionWidget(ClickableWidget widget) {
        return widget == initialZoomWidget || widget == smoothZoomWidget;
    }

    public static final class InitialZoomLevelSlider extends SliderWidget {
        private static final int MIN_ZOOM = 100;
        private static final int MAX_ZOOM = 500;

        public InitialZoomLevelSlider(int x, int y, int width, int height) {
            super(x, y, width, height, Text.empty(), toSliderValue());
            updateMessage();
        }

        private static double toSliderValue() {
            int current = Config.getInstance().getInitialZoomLevel();
            int clamped = Math.max(MIN_ZOOM, Math.min(MAX_ZOOM, current));
            return (clamped - MIN_ZOOM) / (double) (MAX_ZOOM - MIN_ZOOM);
        }

        private int getSelectedZoom() {
            int range = MAX_ZOOM - MIN_ZOOM;
            int zoom = MIN_ZOOM + (int) Math.round(this.value * range);
            return Math.max(MIN_ZOOM, Math.min(MAX_ZOOM, zoom));
        }

        @Override
        public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
            int x0 = getX();
            int y0 = getY();
            int x1 = x0 + width;
            int y1 = y0 + height;

            int bg = 0x8011131A;
            int bgHover = 0x99181A22;
            context.fill(x0, y0, x1, y1, this.hovered ? bgHover : bg);

            int border = this.hovered ? 0x9960A5FA : 0xAA2A2D36;
            context.fill(x0, y0, x1, y0 + 1, border);
            context.fill(x0, y1 - 1, x1, y1, border);
            context.fill(x0, y0, x0 + 1, y1, border);
            context.fill(x1 - 1, y0, x1, y1, border);

            int knobW = 6;
            int trackX0 = x0 + 4;
            int trackX1 = x1 - 4;
            int trackW = Math.max(1, trackX1 - trackX0);
            int knobX = trackX0 + (int) Math.round(this.value * (trackW - knobW));
            int knobY0 = y0 + 3;
            int knobY1 = y1 - 3;
            context.fill(knobX, knobY0, knobX + knobW, knobY1, 0xFF60A5FA);

            updateMessage();
            TextRenderer tr = MinecraftClient.getInstance().textRenderer;
            int textY = y0 + (height - 8) / 2;
            context.drawTextWithShadow(tr, getMessage(), x0 + 8, textY, 0xFFE5E7EB);
        }

        @Override
        protected void updateMessage() {
            int zoom = getSelectedZoom();
            setMessage(Text.literal("Initial Zoom: " + zoom + "%"));
        }

        @Override
        protected void applyValue() {
            Config.getInstance().setInitialZoomLevel(getSelectedZoom());
        }

        @Override
        public void appendClickableNarrations(NarrationMessageBuilder builder) {
        }
    }
}
