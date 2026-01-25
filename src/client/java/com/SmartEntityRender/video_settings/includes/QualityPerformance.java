package com.SmartEntityRender.video_settings.includes;

import com.SmartEntityRender.config.Config;
import com.SmartEntityRender.video_settings.VideoSettings;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.TextureFilteringMode;
import net.minecraft.text.Text;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public final class QualityPerformance {
        private QualityPerformance() {
        }

        public static final class ScrollController {
                private final List<ClickableWidget> widgets;
                private final List<Integer> baseY = new java.util.ArrayList<>();
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

                public boolean handleScroll(double mouseX, double mouseY, double verticalAmount, int panelX,
                                int panelW) {
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

        public static int addControls(
                        Function<ClickableWidget, ClickableWidget> register,
                        List<ClickableWidget> widgets,
                        GameOptions options,
                        int x,
                        int y,
                        int width,
                        Runnable onSave) {
                int currentY = y;

                var particlesButton = new Display.KeyValueButton(
                                x,
                                currentY,
                                width,
                                VideoSettings.CONTROL_H,
                                Text.literal("Particles"),
                                () -> {
                                        var mode = options.getParticles().getValue();
                                        boolean on = mode != net.minecraft.particle.ParticlesMode.MINIMAL;
                                        return Text.literal(on ? "On" : "Off");
                                },
                                () -> {
                                        var mode = options.getParticles().getValue();
                                        if (mode == net.minecraft.particle.ParticlesMode.MINIMAL) {
                                                options.getParticles()
                                                                .setValue(net.minecraft.particle.ParticlesMode.ALL);
                                        } else {
                                                options.getParticles()
                                                                .setValue(net.minecraft.particle.ParticlesMode.MINIMAL);
                                        }
                                        options.write();
                                });
                particlesButton.setTooltip(Tooltip.of(Text.literal(
                                "Particle effects toggle\n• On: Full particles\n• Off: Minimal particles")));
                widgets.add(register.apply(particlesButton));
                currentY += VideoSettings.CONTROL_H + VideoSettings.CONTROL_GAP;

                var xpMergeToggle = new Display.ToggleButton(
                                x,
                                currentY,
                                width,
                                VideoSettings.CONTROL_H,
                                Text.literal("Merge XP Orbs"),
                                () -> Config.getInstance().isMergeXpOrbsEnabled(),
                                value -> Config.getInstance().setMergeXpOrbsEnabled(value));
                xpMergeToggle.setTooltip(Tooltip.of(Text.literal(
                                "Combine nearby XP orbs into one\n• On: Fewer entities, better FPS\n• Off: Normal orbs")));
                widgets.add(register.apply(xpMergeToggle));
                currentY += VideoSettings.CONTROL_H + VideoSettings.CONTROL_GAP;

                var smoothLightingToggle = new Display.ToggleButton(
                                x,
                                currentY,
                                width,
                                VideoSettings.CONTROL_H,
                                Text.literal("Smooth Lighting"),
                                () -> (Boolean) options.getAo().getValue(),
                                value -> {
                                        options.getAo().setValue(value);
                                        options.write();
                                });
                smoothLightingToggle.setTooltip(Tooltip.of(Text.literal(
                                "Ambient Occlusion lighting\n• ON: Smoother shading\n• OFF: Flat lighting")));
                widgets.add(register.apply(smoothLightingToggle));
                currentY += VideoSettings.CONTROL_H + VideoSettings.CONTROL_GAP;

                var biomeBlendSlider = new Display.BiomeBlendSlider(
                                x,
                                currentY,
                                width,
                                VideoSettings.CONTROL_H);
                biomeBlendSlider.setTooltip(Tooltip.of(Text.literal(
                                "Biome blend radius\n• Higher: Smoother transitions\n• Lower: Sharper edges")));
                widgets.add(register.apply(biomeBlendSlider));
                currentY += VideoSettings.CONTROL_H + VideoSettings.CONTROL_GAP;

                var weatherRadiusSlider = new Display.WeatherRadiusSlider(
                                x,
                                currentY,
                                width,
                                VideoSettings.CONTROL_H);
                weatherRadiusSlider.setTooltip(Tooltip.of(Text.literal(
                                "Weather effects radius\n• Higher: Wider rain/snow\n• Lower: Better performance")));
                widgets.add(register.apply(weatherRadiusSlider));
                currentY += VideoSettings.CONTROL_H + VideoSettings.CONTROL_GAP;

                var mipmapLevelsSlider = new Display.MipmapLevelsSlider(
                                x,
                                currentY,
                                width,
                                VideoSettings.CONTROL_H);
                mipmapLevelsSlider.setTooltip(Tooltip.of(Text.literal(
                                "Mipmap levels control\n• Higher: Better texture quality\n• Lower: Better performance")));
                widgets.add(register.apply(mipmapLevelsSlider));
                currentY += VideoSettings.CONTROL_H + VideoSettings.CONTROL_GAP;

                var textureFilteringButton = new Display.KeyValueButton(
                                x,
                                currentY,
                                width,
                                VideoSettings.CONTROL_H,
                                Text.literal("Texture Filtering"),
                                () -> textureFilteringLabel(
                                                (TextureFilteringMode) options.getTextureFiltering().getValue()),
                                () -> {
                                        TextureFilteringMode current = (TextureFilteringMode) options
                                                        .getTextureFiltering().getValue();
                                        options.getTextureFiltering().setValue(nextTextureFiltering(current));
                                        options.write();
                                });
                textureFilteringButton.setTooltip(Tooltip.of(Text.literal(
                                "Texture filtering mode\n• None: Sharp pixels\n• RGSS: Smoother edges\n• Anisotropic: Best quality")));
                widgets.add(register.apply(textureFilteringButton));
                currentY += VideoSettings.CONTROL_H + VideoSettings.CONTROL_GAP;

                var leavesQualityButton = new Display.KeyValueButton(
                                x,
                                currentY,
                                width,
                                VideoSettings.CONTROL_H,
                                Text.literal("Leaves Quality"),
                                () -> {
                                        boolean cutout = (Boolean) options.getCutoutLeaves().getValue();
                                        return Text.literal(cutout ? "Fast" : "Fancy");
                                },
                                () -> {
                                        boolean cutout = (Boolean) options.getCutoutLeaves().getValue();
                                        options.getCutoutLeaves().setValue(!cutout);
                                        options.write();
                                });
                leavesQualityButton.setTooltip(Tooltip.of(Text.literal(
                                "Tree leaves rendering\n• Fast: Better FPS\n• Fancy: Better look")));
                widgets.add(register.apply(leavesQualityButton));
                currentY += VideoSettings.CONTROL_H + VideoSettings.CONTROL_GAP;

                var mobsShadowToggle = new Display.ToggleButton(
                                x,
                                currentY,
                                width,
                                VideoSettings.CONTROL_H,
                                Text.literal("Entity Shadow: Mobs"),
                                () -> Config.getInstance().isEntityShadowMobsEnabled(),
                                value -> {
                                        Config.getInstance().setEntityShadowMobsEnabled(value);
                                        boolean any = value || Config.getInstance().isEntityShadowDropsEnabled();
                                        options.getEntityShadows().setValue(any);
                                        options.write();
                                });
                mobsShadowToggle.setTooltip(Tooltip.of(Text.literal(
                                "Shadows for mobs\n• On: Show shadows\n• Off: Hide shadows")));
                widgets.add(register.apply(mobsShadowToggle));
                currentY += VideoSettings.CONTROL_H + VideoSettings.CONTROL_GAP;

                var dropsShadowToggle = new Display.ToggleButton(
                                x,
                                currentY,
                                width,
                                VideoSettings.CONTROL_H,
                                Text.literal("Entity Shadow: Drops"),
                                () -> Config.getInstance().isEntityShadowDropsEnabled(),
                                value -> {
                                        Config.getInstance().setEntityShadowDropsEnabled(value);
                                        boolean any = value || Config.getInstance().isEntityShadowMobsEnabled();
                                        options.getEntityShadows().setValue(any);
                                        options.write();
                                });
                dropsShadowToggle.setTooltip(Tooltip.of(Text.literal(
                                "Shadows for dropped items\n• On: Show shadows\n• Off: Hide shadows")));
                widgets.add(register.apply(dropsShadowToggle));
                currentY += VideoSettings.CONTROL_H + VideoSettings.CONTROL_GAP;

                var ladder3dToggle = new Display.ToggleButton(
                                x,
                                currentY,
                                width,
                                VideoSettings.CONTROL_H,
                                Text.literal("3D Ladder Model"),
                                () -> Config.getInstance().isLadder3dModelEnabled(),
                                value -> {
                                        Config.getInstance().setLadder3dModelEnabled(value);
                                        // Reload resources to apply model changes
                                        net.minecraft.client.MinecraftClient client = net.minecraft.client.MinecraftClient
                                                        .getInstance();
                                        if (client != null) {
                                                client.execute(() -> {
                                                        client.reloadResources();
                                                });
                                        }
                                });
                ladder3dToggle.setTooltip(Tooltip.of(Text.literal(
                                "Convert ladders to 3D models\n• On: Volumetric 3D ladders\n• Off: Flat texture\n§7Reloads resources when changed")));
                widgets.add(register.apply(ladder3dToggle));
                currentY += VideoSettings.CONTROL_H + VideoSettings.CONTROL_GAP;

                var entityDistanceSlider = new Display.EntityDistanceSlider(
                                x,
                                currentY,
                                width,
                                VideoSettings.CONTROL_H,
                                options);
                entityDistanceSlider.setTooltip(Tooltip.of(Text.literal(
                                "Entity render distance scaling\n• Higher: Further entities\n• Lower: Better performance")));
                widgets.add(register.apply(entityDistanceSlider));
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

        private static Text textureFilteringLabel(TextureFilteringMode mode) {
                return switch (mode) {
                        case NONE -> Text.literal("None");
                        case RGSS -> Text.literal("RGSS");
                        case ANISOTROPIC -> Text.literal("Anisotropic");
                };
        }

        private static TextureFilteringMode nextTextureFiltering(TextureFilteringMode mode) {
                return switch (mode) {
                        case NONE -> TextureFilteringMode.RGSS;
                        case RGSS -> TextureFilteringMode.ANISOTROPIC;
                        case ANISOTROPIC -> TextureFilteringMode.NONE;
                };
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
                        if (selectedSection.get() != VideoSettings.Section.QUALITY_PERFORMANCE) {
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
