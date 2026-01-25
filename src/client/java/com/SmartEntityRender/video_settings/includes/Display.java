package com.SmartEntityRender.video_settings.includes;

import com.SmartEntityRender.config.Config;
import com.SmartEntityRender.video_settings.VideoSettings;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.util.Monitor;
import net.minecraft.client.util.VideoMode;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

public final class Display {
    private Display() {
    }

    public static int getGuiScaleFixed(GameOptions options) {
        int raw = (Integer) options.getGuiScale().getValue();
        if (raw < 1) {
            return 1;
        }
        if (raw > 4) {
            return 4;
        }
        return raw;
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
            if (selectedSection.get() != VideoSettings.Section.DISPLAY) {
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
            context.drawText(textRenderer, Text.literal("Video Settings"), titleX, titleY, 0xFFEDEDED, false);
            context.drawText(textRenderer, Text.literal("Sections"), titleX, titleY + 12, 0xFF9AA0A6, false);
            context.drawText(textRenderer, selectedSection.get().label(), titleX, titleY + 26, 0xFF6B7280, false);
        }
    }

    public static final class ScrollController {
        private final List<ClickableWidget> widgets;
        private final List<Integer> baseY = new ArrayList<>();
        private int scroll;
        private int scrollMax;
        private int panelY;
        private int panelH;

        public ScrollController(List<ClickableWidget> widgets) {
            this.widgets = widgets;
        }

        public void setPanelBounds(int panelY, int panelH) {
            this.panelY = panelY;
            this.panelH = panelH;
        }

        public void init() {
            baseY.clear();
            for (ClickableWidget w : widgets) {
                baseY.add(w.getY());
            }

            int contentBottom = panelY + VideoSettings.PANEL_TOP_Y;
            for (ClickableWidget w : widgets) {
                int bottom = w.getY() + w.getHeight();
                if (bottom > contentBottom) {
                    contentBottom = bottom;
                }
            }

            int viewBottom = panelY + panelH - 12;
            int contentHeight = contentBottom - (panelY + VideoSettings.PANEL_TOP_Y);
            int viewHeight = viewBottom - (panelY + VideoSettings.PANEL_TOP_Y);
            scrollMax = Math.max(0, contentHeight - viewHeight);
            scroll = Math.min(scroll, scrollMax);

            apply();
        }

        public void resetScroll() {
            scroll = 0;
            apply();
        }

        public void apply() {
            int count = Math.min(widgets.size(), baseY.size());
            for (int i = 0; i < count; i++) {
                ClickableWidget w = widgets.get(i);
                int base = baseY.get(i);
                w.setY(base - scroll);
            }
        }

        public boolean isWidgetVisible(ClickableWidget w) {
            int viewTop = panelY + VideoSettings.PANEL_TOP_Y;
            int viewBottom = panelY + panelH - 12;
            int wTop = w.getY();
            int wBottom = w.getY() + w.getHeight();
            return wBottom > viewTop && wTop < viewBottom;
        }

        public boolean handleScroll(double mouseX, double mouseY, double verticalAmount, int panelX, int panelW) {
            if (scrollMax <= 0) {
                return false;
            }
            boolean insidePanel = mouseX >= panelX
                    && mouseX <= panelX + panelW
                    && mouseY >= panelY
                    && mouseY <= panelY + panelH;
            if (!insidePanel) {
                return false;
            }

            int step = 12;
            int next = scroll - (int) Math.round(verticalAmount * step);
            if (next < 0) {
                next = 0;
            } else if (next > scrollMax) {
                next = scrollMax;
            }
            if (next != scroll) {
                scroll = next;
                apply();
            }
            return true;
        }

        public int getScroll() {
            return scroll;
        }

        public void setScroll(int value) {
            this.scroll = Math.max(0, Math.min(scrollMax, value));
            apply();
        }

        public int getScrollMax() {
            return scrollMax;
        }

        public int getPanelY() {
            return panelY;
        }

        public int getPanelH() {
            return panelH;
        }
    }

    private static List<VideoMode> getMonitorModes(Monitor monitor) {
        List<VideoMode> out = new ArrayList<>();
        if (monitor == null) {
            return out;
        }

        int count = monitor.getVideoModeCount();
        List<VideoMode> modes = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            modes.add(monitor.getVideoMode(i));
        }

        Set<String> seen = new HashSet<>();
        for (VideoMode m : modes) {
            String key = m.asString();
            if (seen.add(key)) {
                out.add(m);
            }
        }

        out.sort(Comparator
                .comparingInt(VideoMode::getWidth)
                .thenComparingInt(VideoMode::getHeight)
                .thenComparingInt(VideoMode::getRefreshRate));
        return out;
    }

    private static int getFullscreenResolutionIndex(GameOptions options, Monitor monitor) {
        if (monitor == null) {
            return 0;
        }
        String raw = options.fullscreenResolution;
        if (raw == null || raw.isEmpty()) {
            return 0;
        }
        List<VideoMode> modes = getMonitorModes(monitor);
        for (int i = 0; i < modes.size(); i++) {
            if (raw.equals(modes.get(i).asString())) {
                return i + 1;
            }
        }
        return 0;
    }

    private static void setFullscreenResolutionIndex(GameOptions options, int index) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null) {
            return;
        }
        Monitor monitor = client.getWindow().getMonitor();
        if (monitor == null) {
            return;
        }
        List<VideoMode> modes = getMonitorModes(monitor);
        Optional<VideoMode> mode = Optional.empty();
        if (index > 0 && index - 1 < modes.size()) {
            mode = Optional.of(modes.get(index - 1));
        }
        options.fullscreenResolution = mode.map(VideoMode::asString).orElse("");
        client.getWindow().setFullscreenVideoMode(mode);
        client.getWindow().applyFullscreenVideoMode();
        options.write();
        client.onResolutionChanged();
    }

    private static int getFullscreenResolutionMaxIndex(GameOptions options) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null) {
            return 0;
        }
        Monitor monitor = client.getWindow().getMonitor();
        if (monitor == null) {
            return 0;
        }
        return getMonitorModes(monitor).size();
    }

    public static int addControls(
            Function<ClickableWidget, ClickableWidget> register,
            List<ClickableWidget> widgets,
            GameOptions options,
            int x,
            int y,
            int width,
            Runnable onSave,
            Runnable onFullscreenToggled,
            Supplier<Text> guiScaleText,
            Runnable onGuiScalePressed) {
        int currentY = y;

        // 1) Resolution (drag to change)
        var resolutionSlider = new Display.FullscreenResolutionSlider(
                x,
                currentY,
                width,
                VideoSettings.CONTROL_H,
                options);
        resolutionSlider.setTooltip(net.minecraft.client.gui.tooltip.Tooltip
                .of(Text.literal("Drag to change resolution\n• Desktop: Use monitor default")));
        widgets.add(register.apply(resolutionSlider));
        currentY += VideoSettings.CONTROL_H + VideoSettings.CONTROL_GAP;

        // 2) FPS limit: 30..240 + Unlimited
        var fpsSlider = new Display.FpsLimitSlider(
                x,
                currentY,
                width,
                VideoSettings.CONTROL_H,
                options);
        fpsSlider.setTooltip(net.minecraft.client.gui.tooltip.Tooltip
                .of(Text.literal(
                        "Limit maximum frames per second\n• Higher: Smoother gameplay\n• Lower: Better performance")));
        widgets.add(register.apply(fpsSlider));
        currentY += VideoSettings.CONTROL_H + VideoSettings.CONTROL_GAP;

        // 3) Render Distance: 2..32 chunks
        var renderDistanceSlider = new Display.RenderDistanceSlider(
                x,
                currentY,
                width,
                VideoSettings.CONTROL_H,
                options);
        renderDistanceSlider.setTooltip(net.minecraft.client.gui.tooltip.Tooltip
                .of(Text.literal("How far you can see terrain\n• Higher: See further\n• Lower: Better FPS")));
        widgets.add(register.apply(renderDistanceSlider));
        currentY += VideoSettings.CONTROL_H + VideoSettings.CONTROL_GAP;

        // 4) Brightness: 0% (Dark) to 500% (Super Bright)
        var brightnessSlider = new Display.BrightnessSlider(
                x,
                currentY,
                width,
                VideoSettings.CONTROL_H,
                options);
        brightnessSlider.setTooltip(net.minecraft.client.gui.tooltip.Tooltip
                .of(Text.literal("Adjust screen brightness\n• 0%: Dark\n• 50%: Normal\n• 100%: Bright")));
        widgets.add(register.apply(brightnessSlider));
        currentY += VideoSettings.CONTROL_H + VideoSettings.CONTROL_GAP;

        // 4.5) Cloud Distance: 2..128 chunks
        var cloudDistanceSlider = new Display.CloudDistanceSlider(
                x,
                currentY,
                width,
                VideoSettings.CONTROL_H,
                options);
        cloudDistanceSlider.setTooltip(net.minecraft.client.gui.tooltip.Tooltip
                .of(Text.literal(
                        "Cloud rendering distance\n• Higher: See clouds further\n• Lower: Better performance")));
        widgets.add(register.apply(cloudDistanceSlider));
        currentY += VideoSettings.CONTROL_H + VideoSettings.CONTROL_GAP;

        // 5) VSync on/off
        var vsyncButton = new Display.ToggleButton(
                x,
                currentY,
                width,
                VideoSettings.CONTROL_H,
                Text.literal("VSync"),
                () -> (Boolean) options.getEnableVsync().getValue(),
                value -> options.getEnableVsync().setValue(value));
        vsyncButton.setTooltip(net.minecraft.client.gui.tooltip.Tooltip
                .of(Text.literal(
                        "Synchronize FPS with monitor\n• Reduces screen tearing\n• May lower FPS")));
        widgets.add(register.apply(vsyncButton));
        currentY += VideoSettings.CONTROL_H + VideoSettings.CONTROL_GAP;

        // 6) Fullscreen on/off
        var fullscreenButton = new Display.ToggleButton(
                x,
                currentY,
                width,
                VideoSettings.CONTROL_H,
                Text.literal("Fullscreen"),
                () -> (Boolean) options.getFullscreen().getValue(),
                value -> {
                    options.getFullscreen().setValue(value);
                    options.write();
                    MinecraftClient client = MinecraftClient.getInstance();
                    if (client != null && client.getWindow() != null) {
                        client.getWindow().toggleFullscreen();
                    }
                    if (onFullscreenToggled != null) {
                        onFullscreenToggled.run();
                    }
                });
        fullscreenButton.setTooltip(net.minecraft.client.gui.tooltip.Tooltip
                .of(Text.literal(
                        "Toggle fullscreen mode\n• ON: Exclusive fullscreen\n• OFF: Windowed mode\nPress F11 to toggle quickly")));
        widgets.add(register.apply(fullscreenButton));
        currentY += VideoSettings.CONTROL_H + VideoSettings.CONTROL_GAP;

        // 6.5) View Bobbing
        var viewBobbingButton = new Display.ToggleButton(
                x,
                currentY,
                width,
                VideoSettings.CONTROL_H,
                Text.literal("View Bobbing"),
                () -> (Boolean) options.getBobView().getValue(),
                value -> {
                    options.getBobView().setValue(value);
                    options.write();
                });
        viewBobbingButton.setTooltip(net.minecraft.client.gui.tooltip.Tooltip
                .of(Text.literal("Camera shake when moving\n• OFF: Stable camera\n• ON: Natural movement")));
        widgets.add(register.apply(viewBobbingButton));
        currentY += VideoSettings.CONTROL_H + VideoSettings.CONTROL_GAP;

        // 6.7) Show FPS
        var showFPSButton = new Display.ToggleButton(
                x,
                currentY,
                width,
                VideoSettings.CONTROL_H,
                Text.literal("Show FPS"),
                () -> Config.getInstance().isShowFPS(),
                value -> Config.getInstance().setShowFPS(value));
        showFPSButton.setTooltip(net.minecraft.client.gui.tooltip.Tooltip
                .of(Text.literal("Display FPS counter in corner\n• Shows current frames per second")));
        widgets.add(register.apply(showFPSButton));
        currentY += VideoSettings.CONTROL_H + VideoSettings.CONTROL_GAP;

        // 7) GUI Scale
        var guiScaleButton = new Display.KeyValueButton(
                x,
                currentY,
                width,
                VideoSettings.CONTROL_H,
                Text.literal("GUI Scale"),
                guiScaleText,
                onGuiScalePressed);
        guiScaleButton.setTooltip(net.minecraft.client.gui.tooltip.Tooltip
                .of(Text.literal("Change interface size\n• 1: Large\n• 2: Medium\n• 3: Small")));
        widgets.add(register.apply(guiScaleButton));
        currentY += VideoSettings.CONTROL_H + VideoSettings.CONTROL_GAP;

        // 8) Clouds - Fast/Fancy/Off
        var cloudsButton = new Display.KeyValueButton(
                x,
                currentY,
                width,
                VideoSettings.CONTROL_H,
                Text.literal("Clouds"),
                () -> {
                    var mode = options.getCloudRenderMode().getValue();
                    String modeName = mode.name();
                    return Text.literal(modeName.substring(0, 1).toUpperCase() + modeName.substring(1).toLowerCase());
                },
                () -> {
                    var current = options.getCloudRenderMode().getValue();
                    var values = net.minecraft.client.option.CloudRenderMode.values();
                    int nextIndex = (current.ordinal() + 1) % values.length;
                    options.getCloudRenderMode().setValue(values[nextIndex]);
                    options.write();
                });
        cloudsButton.setTooltip(net.minecraft.client.gui.tooltip.Tooltip
                .of(Text.literal(
                        "Cloud rendering quality\n• Fancy: Best quality\n• Fast: Better FPS\n• Off: No clouds")));
        widgets.add(register.apply(cloudsButton));
        currentY += VideoSettings.CONTROL_H + VideoSettings.CONTROL_GAP;

        // 8.5) Attack Indicator - Crosshair/Hotbar/Off
        var attackIndicatorButton = new Display.KeyValueButton(
                x,
                currentY,
                width,
                VideoSettings.CONTROL_H,
                Text.literal("Attack Indicator"),
                () -> {
                    var mode = options.getAttackIndicator().getValue();
                    String modeName = mode.name();
                    return Text.literal(modeName.substring(0, 1).toUpperCase() + modeName.substring(1).toLowerCase());
                },
                () -> {
                    var current = options.getAttackIndicator().getValue();
                    var values = net.minecraft.client.option.AttackIndicator.values();
                    int nextIndex = (current.ordinal() + 1) % values.length;
                    options.getAttackIndicator().setValue(values[nextIndex]);
                    options.write();
                });
        attackIndicatorButton.setTooltip(net.minecraft.client.gui.tooltip.Tooltip
                .of(Text.literal(
                        "Attack cooldown indicator\n• Crosshair: Center\n• Hotbar: Bottom\n• Off: Hidden")));
        widgets.add(register.apply(attackIndicatorButton));
        currentY += VideoSettings.CONTROL_H + VideoSettings.CONTROL_GAP;

        // 9) Save Button
        if (onSave != null) {
            var saveButton = new Display.SaveButton(
                    x,
                    currentY,
                    width,
                    VideoSettings.CONTROL_H,
                    onSave);
            widgets.add(register.apply(saveButton));
            currentY += VideoSettings.CONTROL_H + VideoSettings.CONTROL_GAP;
        }

        return currentY;
    }

    public static final class FullscreenResolutionSlider extends SliderWidget {
        private final GameOptions options;
        private Integer pendingIndex;
        private boolean pendingChange;

        public FullscreenResolutionSlider(int x, int y, int width, int height, GameOptions options) {
            super(x, y, width, height, Text.empty(), toSliderValue(options));
            this.options = options;
            updateMessage();
        }

        private static double toSliderValue(GameOptions options) {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client == null) {
                return 0.0D;
            }
            Monitor monitor = client.getWindow().getMonitor();
            if (monitor == null) {
                return 0.0D;
            }
            int index = getFullscreenResolutionIndex(options, monitor);
            int max = getMonitorModes(monitor).size();
            if (max <= 0) {
                return 0.0D;
            }
            return index / (double) max;
        }

        private int getSelectedIndex() {
            int max = getFullscreenResolutionMaxIndex(options);
            if (max <= 0) {
                return 0;
            }
            int index = (int) Math.round(this.value * max);
            return Math.max(0, Math.min(max, index));
        }

        @Override
        public boolean mouseReleased(net.minecraft.client.gui.Click click) {
            boolean handled = super.mouseReleased(click);
            if (pendingChange) {
                setFullscreenResolutionIndex(options, pendingIndex != null ? pendingIndex : getSelectedIndex());
                pendingChange = false;
                pendingIndex = null;
            }
            return handled;
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
            setMessage(Text.literal("Resolution: " + getPreviewResolutionLabel()));
        }

        private String getPreviewResolutionLabel() {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client == null) {
                return "Unavailable";
            }
            Monitor monitor = client.getWindow().getMonitor();
            if (monitor == null) {
                return "Unavailable";
            }
            int index = getSelectedIndex();
            if (index <= 0) {
                return "Desktop";
            }
            List<VideoMode> modes = getMonitorModes(monitor);
            if (index - 1 >= modes.size()) {
                return "Desktop";
            }
            return formatModeText(modes.get(index - 1));
        }

        @Override
        protected void applyValue() {
            pendingIndex = getSelectedIndex();
            pendingChange = true;
        }
    }

    public static final class FpsLimitSlider extends SliderWidget {
        private static final int MIN_FPS = 30;
        private static final int MAX_FPS = 240;
        private static final int UNLIMITED_FPS = 260;
        private static final int STEPS = (MAX_FPS - MIN_FPS) + 1;

        private final GameOptions options;

        public FpsLimitSlider(int x, int y, int width, int height, GameOptions options) {
            super(x, y, width, height, Text.empty(), toSliderValue(options));
            this.options = options;
            updateMessage();
        }

        private static double toSliderValue(GameOptions options) {
            int fps = (Integer) options.getMaxFps().getValue();
            int index;
            if (fps >= UNLIMITED_FPS) {
                index = STEPS;
            } else {
                int clamped = Math.max(MIN_FPS, Math.min(MAX_FPS, fps));
                index = clamped - MIN_FPS;
            }
            return index / (double) STEPS;
        }

        private int getSelectedIndex() {
            int index = (int) Math.round(this.value * STEPS);
            return Math.max(0, Math.min(STEPS, index));
        }

        private int getSelectedFps() {
            int index = getSelectedIndex();
            if (index >= STEPS) {
                return UNLIMITED_FPS;
            }
            return MIN_FPS + index;
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
            int fps = getSelectedFps();
            if (fps >= UNLIMITED_FPS) {
                setMessage(Text.literal("Max FPS: Unlimited"));
            } else {
                setMessage(Text.literal("Max FPS: " + fps));
            }
        }

        @Override
        protected void applyValue() {
            options.getMaxFps().setValue(getSelectedFps());
            MinecraftClient.getInstance().options.write();
        }
    }

    /**
     * Render Distance Slider - نسخة طبق الأصل من FPS Slider
     */
    public static final class RenderDistanceSlider extends SliderWidget {
        private static final int MIN_CHUNKS = 2;
        private static final int MAX_CHUNKS = 32;

        private final GameOptions options;

        public RenderDistanceSlider(int x, int y, int width, int height, GameOptions options) {
            super(x, y, width, height, Text.empty(), toSliderValue(options));
            this.options = options;
            updateMessage();
        }

        private static double toSliderValue(GameOptions options) {
            int current = (Integer) options.getViewDistance().getValue();
            int clamped = Math.max(MIN_CHUNKS, Math.min(MAX_CHUNKS, current));
            return (clamped - MIN_CHUNKS) / (double) (MAX_CHUNKS - MIN_CHUNKS);
        }

        private int getSelectedChunks() {
            int range = MAX_CHUNKS - MIN_CHUNKS;
            int chunks = MIN_CHUNKS + (int) Math.round(this.value * range);
            return Math.max(MIN_CHUNKS, Math.min(MAX_CHUNKS, chunks));
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
            int chunks = getSelectedChunks();
            setMessage(Text.literal("Render Distance: " + chunks + " chunks"));
        }

        @Override
        protected void applyValue() {
            options.getViewDistance().setValue(getSelectedChunks());
            MinecraftClient.getInstance().options.write();
        }
    }

    /**
     * Brightness Slider - 0% (Dark) to 100% (Bright)
     */
    public static final class BrightnessSlider extends SliderWidget {
        private static final int MIN = 0; // 0% - Dark
        private static final int MAX = 100; // 100% - Bright

        private final GameOptions options;

        public BrightnessSlider(int x, int y, int width, int height, GameOptions options) {
            super(x, y, width, height, Text.empty(), toSliderValue(options));
            this.options = options;
            updateMessage();
        }

        private static double toSliderValue(GameOptions options) {
            double gamma = (Double) options.getGamma().getValue();
            // Gamma: 0.0 -> 1.0 maps to percentage: 0 -> 100
            int percentage = (int) Math.round(gamma * 100);
            int clamped = Math.max(MIN, Math.min(MAX, percentage));
            return (clamped - MIN) / (double) (MAX - MIN);
        }

        private int getSelectedPercentage() {
            int percentage = MIN + (int) Math.round(this.value * (MAX - MIN));
            return Math.max(MIN, Math.min(MAX, percentage));
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
            int percentage = getSelectedPercentage();
            String label;
            if (percentage == 0) {
                label = "Brightness: Dark";
            } else if (percentage == 50) {
                label = "Brightness: Normal";
            } else if (percentage == 100) {
                label = "Brightness: Bright";
            } else {
                label = "Brightness: " + percentage + "%";
            }
            setMessage(Text.literal(label));
        }

        @Override
        protected void applyValue() {
            int percentage = getSelectedPercentage();
            double gamma = percentage / 100.0;
            options.getGamma().setValue(gamma);
            MinecraftClient.getInstance().options.write();
        }
    }

    /**
     * Cloud Distance Slider - 2 to 128 chunks (مسافة السحب)
     */
    public static final class CloudDistanceSlider extends SliderWidget {
        private static final int MIN_DISTANCE = 2;
        private static final int MAX_DISTANCE = 128;

        private final GameOptions options;

        public CloudDistanceSlider(int x, int y, int width, int height, GameOptions options) {
            super(x, y, width, height, Text.empty(), toSliderValue(options));
            this.options = options;
            updateMessage();
        }

        private static double toSliderValue(GameOptions options) {
            int current = (Integer) options.getCloudRenderDistance().getValue();
            int clamped = Math.max(MIN_DISTANCE, Math.min(MAX_DISTANCE, current));
            return (clamped - MIN_DISTANCE) / (double) (MAX_DISTANCE - MIN_DISTANCE);
        }

        private int getSelectedDistance() {
            int range = MAX_DISTANCE - MIN_DISTANCE;
            int distance = MIN_DISTANCE + (int) Math.round(this.value * range);
            return Math.max(MIN_DISTANCE, Math.min(MAX_DISTANCE, distance));
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
            int distance = getSelectedDistance();
            setMessage(Text.literal("Cloud Distance: " + distance + " chunks"));
        }

        @Override
        protected void applyValue() {
            int distance = getSelectedDistance();
            options.getCloudRenderDistance().setValue(distance);
            MinecraftClient.getInstance().options.write();

            // Rebuild clouds
            var client = MinecraftClient.getInstance();
            if (client.worldRenderer != null) {
                client.worldRenderer.reload();
            }
        }
    }

    /**
     * Entity Distance Scaling - 50% to 500%
     */
    public static final class EntityDistanceSlider extends SliderWidget {
        private static final int MIN_PERCENT = 50;
        private static final int MAX_PERCENT = 500;

        private final GameOptions options;

        public EntityDistanceSlider(int x, int y, int width, int height, GameOptions options) {
            super(x, y, width, height, Text.empty(), toSliderValue(options));
            this.options = options;
            updateMessage();
        }

        private static double toSliderValue(GameOptions options) {
            double scaling = (Double) options.getEntityDistanceScaling().getValue();
            int percent = (int) Math.round(scaling * 100.0);
            int clamped = Math.max(MIN_PERCENT, Math.min(MAX_PERCENT, percent));
            return (clamped - MIN_PERCENT) / (double) (MAX_PERCENT - MIN_PERCENT);
        }

        private int getSelectedPercent() {
            int percent = MIN_PERCENT + (int) Math.round(this.value * (MAX_PERCENT - MIN_PERCENT));
            return Math.max(MIN_PERCENT, Math.min(MAX_PERCENT, percent));
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
            int percent = getSelectedPercent();
            setMessage(Text.literal("Entity Distance: " + percent + "%"));
        }

        @Override
        protected void applyValue() {
            int percent = getSelectedPercent();
            double scaling = percent / 100.0;
            options.getEntityDistanceScaling().setValue(scaling);
            MinecraftClient.getInstance().options.write();
        }
    }

    /**
     * Weather Effects Radius - 3 to 10
     */
    public static final class WeatherRadiusSlider extends SliderWidget {
        private static final int MIN_RADIUS = 3;
        private static final int MAX_RADIUS = 10;

        public WeatherRadiusSlider(int x, int y, int width, int height) {
            super(x, y, width, height, Text.empty(), toSliderValue());
            updateMessage();
        }

        private static GameOptions options() {
            MinecraftClient client = MinecraftClient.getInstance();
            return client != null ? client.options : null;
        }

        private static double toSliderValue() {
            GameOptions options = options();
            int current = options != null ? (Integer) options.getWeatherRadius().getValue() : MAX_RADIUS;
            int clamped = Math.max(MIN_RADIUS, Math.min(MAX_RADIUS, current));
            return (clamped - MIN_RADIUS) / (double) (MAX_RADIUS - MIN_RADIUS);
        }

        private int getSelectedRadius() {
            int radius = MIN_RADIUS + (int) Math.round(this.value * (MAX_RADIUS - MIN_RADIUS));
            return Math.max(MIN_RADIUS, Math.min(MAX_RADIUS, radius));
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
            int radius = getSelectedRadius();
            setMessage(Text.literal("Weather Radius: " + radius));
        }

        @Override
        protected void applyValue() {
            int radius = getSelectedRadius();
            GameOptions options = options();
            if (options != null) {
                options.getWeatherRadius().setValue(radius);
                options.write();
            }
        }
    }

    /**
     * Biome Blend Radius - 0 to 7
     */
    public static final class BiomeBlendSlider extends SliderWidget {
        private static final int MIN_RADIUS = 0;
        private static final int MAX_RADIUS = 7;

        public BiomeBlendSlider(int x, int y, int width, int height) {
            super(x, y, width, height, Text.empty(), toSliderValue());
            updateMessage();
        }

        private static GameOptions options() {
            MinecraftClient client = MinecraftClient.getInstance();
            return client != null ? client.options : null;
        }

        private static double toSliderValue() {
            GameOptions options = options();
            int current = options != null ? (Integer) options.getBiomeBlendRadius().getValue() : 2;
            int clamped = Math.max(MIN_RADIUS, Math.min(MAX_RADIUS, current));
            return (clamped - MIN_RADIUS) / (double) (MAX_RADIUS - MIN_RADIUS);
        }

        private int getSelectedRadius() {
            int radius = MIN_RADIUS + (int) Math.round(this.value * (MAX_RADIUS - MIN_RADIUS));
            return Math.max(MIN_RADIUS, Math.min(MAX_RADIUS, radius));
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
            int radius = getSelectedRadius();
            setMessage(Text.literal("Biome Blend: " + radius));
        }

        @Override
        protected void applyValue() {
            int radius = getSelectedRadius();
            GameOptions options = options();
            if (options != null) {
                options.getBiomeBlendRadius().setValue(radius);
                options.write();
            }
        }
    }

    /**
     * Mipmap Levels - 0 to 4
     */
    public static final class MipmapLevelsSlider extends SliderWidget {
        private static final int MIN_LEVEL = 0;
        private static final int MAX_LEVEL = 4;

        public MipmapLevelsSlider(int x, int y, int width, int height) {
            super(x, y, width, height, Text.empty(), toSliderValue());
            updateMessage();
        }

        private static GameOptions options() {
            MinecraftClient client = MinecraftClient.getInstance();
            return client != null ? client.options : null;
        }

        private static double toSliderValue() {
            GameOptions options = options();
            int current = options != null ? (Integer) options.getMipmapLevels().getValue() : MAX_LEVEL;
            int clamped = Math.max(MIN_LEVEL, Math.min(MAX_LEVEL, current));
            return (clamped - MIN_LEVEL) / (double) (MAX_LEVEL - MIN_LEVEL);
        }

        private int getSelectedLevel() {
            int level = MIN_LEVEL + (int) Math.round(this.value * (MAX_LEVEL - MIN_LEVEL));
            return Math.max(MIN_LEVEL, Math.min(MAX_LEVEL, level));
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
            int level = getSelectedLevel();
            setMessage(Text.literal("Mipmap Levels: " + level));
        }

        @Override
        protected void applyValue() {
            int level = getSelectedLevel();
            GameOptions options = options();
            if (options != null) {
                options.getMipmapLevels().setValue(level);
                options.write();
            }
        }
    }

    /**
     * Chunk Fade - 0 to 2000 ms
     */
    public static final class ChunkFadeSlider extends SliderWidget {
        private static final int MIN_MS = 0;
        private static final int MAX_MS = 2000;
        private static final int STEP_MS = 50;

        private final GameOptions options;

        public ChunkFadeSlider(int x, int y, int width, int height, GameOptions options) {
            super(x, y, width, height, Text.empty(), toSliderValue(options));
            this.options = options;
            updateMessage();
        }

        private static double toSliderValue(GameOptions options) {
            double seconds = (Double) options.getChunkFade().getValue();
            int ms = (int) Math.round(seconds * 1000.0D);
            int clamped = Math.max(MIN_MS, Math.min(MAX_MS, ms));
            return (clamped - MIN_MS) / (double) (MAX_MS - MIN_MS);
        }

        private int getSelectedMs() {
            int raw = MIN_MS + (int) Math.round(this.value * (MAX_MS - MIN_MS));
            int snapped = Math.round(raw / (float) STEP_MS) * STEP_MS;
            return Math.max(MIN_MS, Math.min(MAX_MS, snapped));
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
            int ms = getSelectedMs();
            setMessage(Text.literal("Chunk Fade: " + ms + " ms"));
        }

        @Override
        protected void applyValue() {
            int ms = getSelectedMs();
            options.getChunkFade().setValue(ms / 1000.0D);
            options.write();
        }
    }

    public static final class FPSOverlay {
        private static long lastUpdateTime = 0;
        private static int cachedFPS = 0;
        private static final long UPDATE_INTERVAL = 500; // Update every 500ms

        public static void render(DrawContext context) {
            if (!Config.getInstance().isShowFPS()) {
                return;
            }

            MinecraftClient client = MinecraftClient.getInstance();
            if (client == null) {
                return;
            }

            // Update FPS cache every 500ms
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastUpdateTime >= UPDATE_INTERVAL) {
                cachedFPS = client.getCurrentFps();
                lastUpdateTime = currentTime;
            }

            // Render FPS in top-right corner
            TextRenderer textRenderer = client.textRenderer;
            String fpsText = cachedFPS + " FPS";

            int screenWidth = client.getWindow().getScaledWidth();
            int textWidth = textRenderer.getWidth(fpsText);
            int x = screenWidth - textWidth - 4;
            int y = 4;

            // Background
            context.fill(x - 2, y - 1, x + textWidth + 2, y + 9, 0x80000000);

            // FPS text with color based on performance
            int color;
            if (cachedFPS >= 60) {
                color = 0xFF00FF00; // Green for 60+ FPS
            } else if (cachedFPS >= 30) {
                color = 0xFFFFFF00; // Yellow for 30-59 FPS
            } else {
                color = 0xFFFF0000; // Red for below 30 FPS
            }

            context.drawTextWithShadow(textRenderer, fpsText, x, y, color);
        }
    }

    /**
     * Toggle button with label on left and circular ON/OFF indicator on right.
     */
    public static final class ToggleButton extends ClickableWidget {
        private final Text label;
        private final Supplier<Boolean> getValue;
        private final java.util.function.Consumer<Boolean> setValue;

        public ToggleButton(int x, int y, int width, int height, Text label,
                Supplier<Boolean> getValue, java.util.function.Consumer<Boolean> setValue) {
            super(x, y, width, height, Text.empty());
            this.label = label;
            this.getValue = getValue;
            this.setValue = setValue;
        }

        @Override
        protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
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

            if (this.hovered) {
                context.fill(x0, y0, x0 + 3, y1, 0x9960A5FA);
            }

            TextRenderer tr = MinecraftClient.getInstance().textRenderer;
            int labelColor = this.active ? 0xFF9AA0A6 : 0xFF6B7280;
            int textY = y0 + (height - 8) / 2;
            int labelX = x0 + 10;
            context.drawTextWithShadow(tr, label, labelX, textY, labelColor);

            // Simple checkbox on the right
            boolean isOn = getValue.get();
            int boxSize = 14;
            int boxX = x1 - boxSize - 10;
            int boxY = y0 + (height - boxSize) / 2;

            int borderColor = this.hovered ? 0xFF9AA0A6 : 0xFF6B7280;
            int borderWidth = 2;

            // Draw border (always visible)
            context.fill(boxX, boxY, boxX + boxSize, boxY + borderWidth, borderColor); // Top
            context.fill(boxX, boxY + boxSize - borderWidth, boxX + boxSize, boxY + boxSize, borderColor); // Bottom
            context.fill(boxX, boxY, boxX + borderWidth, boxY + boxSize, borderColor); // Left
            context.fill(boxX + boxSize - borderWidth, boxY, boxX + boxSize, boxY + boxSize, borderColor); // Right

            if (isOn) {
                // Fill inner area only (not covering the border)
                context.fill(boxX + borderWidth, boxY + borderWidth,
                        boxX + boxSize - borderWidth, boxY + boxSize - borderWidth, 0xFF60A5FA);
            }
        }

        @Override
        public void onClick(net.minecraft.client.gui.Click click, boolean ctrlDown) {
            if (!this.active || !this.visible) {
                return;
            }
            boolean current = getValue.get();
            setValue.accept(!current);
            MinecraftClient.getInstance().options.write();
            super.onClick(click, ctrlDown);
        }

        @Override
        protected void appendClickableNarrations(NarrationMessageBuilder builder) {
            builder.put(net.minecraft.client.gui.screen.narration.NarrationPart.TITLE,
                    Text.literal(label.getString() + ": " + (getValue.get() ? "ON" : "OFF")));
        }
    }

    /**
     * A sidebar-styled single-row button: left label + right value.
     */
    public static final class KeyValueButton extends ClickableWidget {
        private final Text label;
        private final Supplier<Text> value;
        private final Runnable onPress;

        public KeyValueButton(int x, int y, int width, int height, Text label, Supplier<Text> value, Runnable onPress) {
            super(x, y, width, height, Text.empty());
            this.label = label;
            this.value = value;
            this.onPress = onPress;
        }

        @Override
        protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
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

            if (this.hovered) {
                context.fill(x0, y0, x0 + 3, y1, 0x9960A5FA);
            }

            TextRenderer tr = MinecraftClient.getInstance().textRenderer;
            int labelColor = this.active ? 0xFF9AA0A6 : 0xFF6B7280;
            int valueColor = this.active ? 0xFFF3F4F6 : 0xFF6B7280;

            int textY = y0 + (height - 8) / 2;
            int labelX = x0 + 10;
            context.drawTextWithShadow(tr, label, labelX, textY, labelColor);

            Text valueText = value.get();
            int valueWidth = tr.getWidth(valueText);
            int valueX = x1 - 12 - valueWidth;
            if (valueX < labelX + tr.getWidth(label) + 8) {
                // If it doesn't fit, fall back to drawing only the value.
                valueX = labelX;
            }
            context.drawTextWithShadow(tr, valueText, valueX, textY, valueColor);

            // Small arrow indicator (dropdown feel)
            context.drawTextWithShadow(tr, Text.literal("▾"), x1 - 10, textY, labelColor);
        }

        @Override
        public void onClick(net.minecraft.client.gui.Click click, boolean ctrlDown) {
            if (!this.active || !this.visible) {
                return;
            }
            onPress.run();
            super.onClick(click, ctrlDown);
        }

        @Override
        protected void appendClickableNarrations(NarrationMessageBuilder builder) {
            builder.put(net.minecraft.client.gui.screen.narration.NarrationPart.TITLE, label);
        }
    }

    /**
     * Save Button - زر حفظ بنفس تصميم الأزرار الأخرى
     */
    public static final class SaveButton extends ClickableWidget {
        private final Runnable onPress;

        public SaveButton(int x, int y, int width, int height, Runnable onPress) {
            super(x, y, width, height, Text.literal("Save"));
            this.onPress = onPress;
        }

        @Override
        protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
            int x0 = getX();
            int y0 = getY();
            int x1 = x0 + width;
            int y1 = y0 + height;

            // خلفية مميزة للزر
            int bg = 0xCC1E3A5F;
            int bgHover = 0xFF2563EB;
            context.fill(x0, y0, x1, y1, this.hovered ? bgHover : bg);

            // حدود
            int border = this.hovered ? 0xFF60A5FA : 0xFF3B82F6;
            context.fill(x0, y0, x1, y0 + 1, border);
            context.fill(x0, y1 - 1, x1, y1, border);
            context.fill(x0, y0, x0 + 1, y1, border);
            context.fill(x1 - 1, y0, x1, y1, border);

            if (this.hovered) {
                context.fill(x0, y0, x0 + 3, y1, 0xFF60A5FA);
            }

            // نص الزر في المنتصف
            TextRenderer tr = MinecraftClient.getInstance().textRenderer;
            int textColor = 0xFFFFFFFF;
            int textWidth = tr.getWidth(getMessage());
            int textX = x0 + (width - textWidth) / 2;
            int textY = y0 + (height - 8) / 2;
            context.drawTextWithShadow(tr, getMessage(), textX, textY, textColor);
        }

        @Override
        public void onClick(net.minecraft.client.gui.Click click, boolean ctrlDown) {
            if (!this.active || !this.visible) {
                return;
            }
            onPress.run();
            super.onClick(click, ctrlDown);
        }

        @Override
        protected void appendClickableNarrations(NarrationMessageBuilder builder) {
            builder.put(net.minecraft.client.gui.screen.narration.NarrationPart.TITLE, getMessage());
        }
    }

    public static String formatModeText(VideoMode mode) {
        return mode.getWidth() + "x" + mode.getHeight() + "  " + mode.getRefreshRate() + "Hz";
    }
}
