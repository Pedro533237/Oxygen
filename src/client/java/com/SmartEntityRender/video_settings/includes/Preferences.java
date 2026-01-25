package com.SmartEntityRender.video_settings.includes;

import com.SmartEntityRender.video_settings.VideoSettings;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.option.GameOptions;
import net.minecraft.text.Text;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public final class Preferences {
    private Preferences() {
    }

    public static int addControls(
            Function<ClickableWidget, ClickableWidget> register,
            List<ClickableWidget> widgets,
            GameOptions options,
            int x,
            int y,
            int width,
            Runnable onSave) {
        int currentY = y;

        var vignetteToggle = new Display.ToggleButton(
                x,
                currentY,
                width,
                VideoSettings.CONTROL_H,
                Text.literal("Vignette"),
                () -> (Boolean) options.getVignette().getValue(),
                value -> {
                    options.getVignette().setValue(value);
                    options.write();
                });
        vignetteToggle.setTooltip(Tooltip.of(Text.literal(
                "Darken the corners of the screen\n• ON: Classic vignette\n• OFF: No dark corners")));
        widgets.add(register.apply(vignetteToggle));
        currentY += VideoSettings.CONTROL_H + VideoSettings.CONTROL_GAP;

        var autosaveIndicatorToggle = new Display.ToggleButton(
                x,
                currentY,
                width,
                VideoSettings.CONTROL_H,
                Text.literal("Autosave Indicator"),
                () -> (Boolean) options.getShowAutosaveIndicator().getValue(),
                value -> {
                    options.getShowAutosaveIndicator().setValue(value);
                    options.write();
                });
        autosaveIndicatorToggle.setTooltip(Tooltip.of(Text.literal(
                "Show the autosave icon\n• ON: Show indicator\n• OFF: Hide indicator")));
        widgets.add(register.apply(autosaveIndicatorToggle));
        currentY += VideoSettings.CONTROL_H + VideoSettings.CONTROL_GAP;

        var chunkFadeSlider = new Display.ChunkFadeSlider(
                x,
                currentY,
                width,
                VideoSettings.CONTROL_H,
                options);
        chunkFadeSlider.setTooltip(Tooltip.of(Text.literal(
                "Chunk fade-in time\n• Higher: Smoother loading\n• Lower: Faster pop-in")));
        widgets.add(register.apply(chunkFadeSlider));
        currentY += VideoSettings.CONTROL_H + VideoSettings.CONTROL_GAP;

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
            if (selectedSection.get() != VideoSettings.Section.PREFERENCES) {
                return;
            }

            int windowH = net.minecraft.client.MinecraftClient.getInstance().getWindow().getScaledHeight();
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
}
