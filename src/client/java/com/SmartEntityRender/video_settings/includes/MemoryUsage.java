package com.SmartEntityRender.video_settings.includes;

import com.SmartEntityRender.config.Config;
import com.SmartEntityRender.video_settings.VideoSettings;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public final class MemoryUsage {
        private MemoryUsage() {
        }

        public static int addControls(
                        Function<ClickableWidget, ClickableWidget> register,
                        List<ClickableWidget> widgets,
                        int x,
                        int y,
                        int width,
                        Runnable onSave) {
                int currentY = y;

                var systemToggle = new Display.ToggleButton(
                                x,
                                currentY,
                                width,
                                VideoSettings.CONTROL_H,
                                Text.literal("Memory Optimization System"),
                                () -> Config.getInstance().isMemorySystemEnabled(),
                                value -> {
                                        Config.getInstance().setMemorySystemEnabled(value);
                                });
                systemToggle.setTooltip(Tooltip.of(Text.literal(
                                "Master toggle for memory optimizations\n• ON: Enable memory features\n• OFF: Disable all sub-options & use vanilla\n§cTurning OFF disables all features below")));
                widgets.add(register.apply(systemToggle));
                currentY += VideoSettings.CONTROL_H + VideoSettings.CONTROL_GAP;

                var fastMapToggle = new Display.ToggleButton(
                                x,
                                currentY,
                                width,
                                VideoSettings.CONTROL_H,
                                Text.literal("FastMap Tables"),
                                () -> Config.getInstance().isMemoryFastMapEnabled(),
                                value -> Config.getInstance().setMemoryFastMapEnabled(value));
                fastMapToggle.setTooltip(Tooltip.of(Text.literal(
                                "Replace heavy state tables with compact FastMap\n• Lower memory usage\n§7Requires Master toggle ON")));
                widgets.add(register.apply(fastMapToggle));
                currentY += VideoSettings.CONTROL_H + VideoSettings.CONTROL_GAP;

                var compactBlockStateToggle = new Display.ToggleButton(
                                x,
                                currentY,
                                width,
                                VideoSettings.CONTROL_H,
                                Text.literal("Compact BlockState"),
                                () -> Config.getInstance().isMemoryCompactBlockStateEnabled(),
                                value -> Config.getInstance().setMemoryCompactBlockStateEnabled(value));
                compactBlockStateToggle.setTooltip(Tooltip.of(Text.literal(
                                "Reduce duplicate BlockState property storage\n• Lighter custom representation\n§7Requires Master toggle ON")));
                widgets.add(register.apply(compactBlockStateToggle));
                currentY += VideoSettings.CONTROL_H + VideoSettings.CONTROL_GAP;

                var multipartConditionToggle = new Display.ToggleButton(
                                x,
                                currentY,
                                width,
                                VideoSettings.CONTROL_H,
                                Text.literal("Multipart Condition Cache"),
                                () -> Config.getInstance().isMemoryMultipartConditionCacheEnabled(),
                                value -> Config.getInstance().setMemoryMultipartConditionCacheEnabled(value));
                multipartConditionToggle.setTooltip(Tooltip.of(Text.literal(
                                "Cache multipart conditions instead of recreating\n• Fewer allocations\n§7Requires Master toggle ON")));
                widgets.add(register.apply(multipartConditionToggle));
                currentY += VideoSettings.CONTROL_H + VideoSettings.CONTROL_GAP;

                var mrlInternToggle = new Display.ToggleButton(
                                x,
                                currentY,
                                width,
                                VideoSettings.CONTROL_H,
                                Text.literal("ModelResourceLocation Reuse"),
                                () -> Config.getInstance().isMemoryModelResourceLocationInterningEnabled(),
                                value -> Config.getInstance().setMemoryModelResourceLocationInterningEnabled(value));
                mrlInternToggle.setTooltip(Tooltip.of(Text.literal(
                                "Reuse identical ModelResourceLocation strings\n• Lower string churn\n§7Requires Master toggle ON")));
                widgets.add(register.apply(mrlInternToggle));
                currentY += VideoSettings.CONTROL_H + VideoSettings.CONTROL_GAP;

                var shapeCacheToggle = new Display.ToggleButton(
                                x,
                                currentY,
                                width,
                                VideoSettings.CONTROL_H,
                                Text.literal("Shared Block Shapes"),
                                () -> Config.getInstance().isMemoryShapeCacheDedupEnabled(),
                                value -> Config.getInstance().setMemoryShapeCacheDedupEnabled(value));
                shapeCacheToggle.setTooltip(Tooltip.of(Text.literal(
                                "Deduplicate collision/render shapes\n• Reuse identical shapes\n§7Requires Master toggle ON")));
                widgets.add(register.apply(shapeCacheToggle));
                currentY += VideoSettings.CONTROL_H + VideoSettings.CONTROL_GAP;

                var quadDataToggle = new Display.ToggleButton(
                                x,
                                currentY,
                                width,
                                VideoSettings.CONTROL_H,
                                Text.literal("Shared Quad Data"),
                                () -> Config.getInstance().isMemoryQuadDataDedupEnabled(),
                                value -> Config.getInstance().setMemoryQuadDataDedupEnabled(value));
                quadDataToggle.setTooltip(Tooltip.of(Text.literal(
                                "Share identical model quad vertex data\n• Smaller mesh memory\n§7Requires Master toggle ON")));
                widgets.add(register.apply(quadDataToggle));
                currentY += VideoSettings.CONTROL_H + VideoSettings.CONTROL_GAP;

                var threadingToggle = new Display.ToggleButton(
                                x,
                                currentY,
                                width,
                                VideoSettings.CONTROL_H,
                                Text.literal("Threading Detector Optimization"),
                                () -> Config.getInstance().isMemoryThreadingDetectorOptimizedEnabled(),
                                value -> Config.getInstance().setMemoryThreadingDetectorOptimizedEnabled(value));
                threadingToggle.setTooltip(Tooltip.of(Text.literal(
                                "Reduce ThreadingDetector allocations\n• Lower object count\n§7Requires Master toggle ON")));
                widgets.add(register.apply(threadingToggle));
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
                        if (selectedSection.get() != VideoSettings.Section.MEMORY_USAGE) {
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
                        context.drawText(textRenderer, Text.literal("Video Settings"), titleX, titleY, 0xFFEDEDED,
                                        false);
                        context.drawText(textRenderer, Text.literal("Sections"), titleX, titleY + 12, 0xFF9AA0A6,
                                        false);
                        context.drawText(textRenderer, selectedSection.get().label(), titleX, titleY + 26, 0xFF6B7280,
                                        false);
                }
        }
}
