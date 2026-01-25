package com.SmartEntityRender.mixin.client.video_settings;

import com.SmartEntityRender.config.Config;
import com.SmartEntityRender.video_settings.VideoSettings;
import com.SmartEntityRender.video_settings.includes.Culling;
import com.SmartEntityRender.video_settings.includes.Display;
import com.SmartEntityRender.video_settings.includes.MemoryUsage;
import com.SmartEntityRender.video_settings.includes.Preferences;
import com.SmartEntityRender.video_settings.includes.QualityPerformance;
import com.SmartEntityRender.video_settings.includes.addons.zoom.Zoom;
import com.SmartEntityRender.video_settings.sidebar.VideoSettingsSidebar;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.GameOptionsScreen;
import net.minecraft.client.gui.screen.option.VideoOptionsScreen;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.OptionListWidget;
import net.minecraft.client.option.GameOptions;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(GameOptionsScreen.class)
public abstract class VideoSettingsSidebarMixin extends Screen {
    @Shadow
    @Final
    protected Screen parent;

    @Shadow
    protected GameOptions gameOptions;

    @Shadow
    protected OptionListWidget body;

    @Unique
    private VideoSettings.Section ser$selectedSection = VideoSettings.Section.DISPLAY;

    @Unique
    private static VideoSettings.Section ser$lastSelectedSection = VideoSettings.Section.DISPLAY;

    @Unique
    private ClickableWidget ser$displayButton;

    @Unique
    private ClickableWidget ser$qualityButton;

    @Unique
    private ClickableWidget ser$preferencesButton;

    @Unique
    private ClickableWidget ser$cullingButton;

    @Unique
    private ClickableWidget ser$memoryButton;

    @Unique
    private ClickableWidget ser$addonsButton;

    @Unique
    private int ser$sidebarY;

    @Unique
    private int ser$sidebarH;

    @Unique
    private final List<ClickableWidget> ser$displayWidgets = new ArrayList<>();

    @Unique
    private final List<ClickableWidget> ser$qualityWidgets = new ArrayList<>();

    @Unique
    private final List<ClickableWidget> ser$preferencesWidgets = new ArrayList<>();

    @Unique
    private final List<ClickableWidget> ser$cullingWidgets = new ArrayList<>();

    @Unique
    private final List<ClickableWidget> ser$memoryWidgets = new ArrayList<>();

    @Unique
    private final List<ClickableWidget> ser$addonsWidgets = new ArrayList<>();

    @Unique
    private Display.ScrollController ser$displayScrollController;

    @Unique
    private QualityPerformance.ScrollController ser$qualityScrollController;

    @Unique
    private Display.ScrollController ser$cullingScrollController;

    @Unique
    private Display.ScrollController ser$memoryScrollController;

    @Unique
    private int ser$displayPanelX;

    @Unique
    private int ser$displayPanelY;

    @Unique
    private int ser$displayPanelW;

    @Unique
    private int ser$displayPanelH;

    @Unique
    private int ser$lastPanelH = -1;

    @Unique
    private boolean ser$bodyLayoutCaptured;

    @Unique
    private int ser$bodyOriginalX;

    @Unique
    private int ser$bodyOriginalWidth;

    @Unique
    private boolean ser$pendingResize;

    @Unique
    private int ser$lastScaledWidth;

    @Unique
    private int ser$lastScaledHeight;

    @Unique
    private static final String SER$WARNING_LINE_1 = "Early build: may cause issues.";
    private static final String SER$WARNING_LINE_2 = "May conflict with mods or crash.";
    private static final String SER$WARNING_LINE_3 = "Join our Discord for support.";

    @Unique
    private int ser$pendingResizeTicks;

    @Unique
    private boolean ser$isDraggingScrollbar;

    @Unique
    private int ser$dragStartY;

    @Unique
    private int ser$dragStartScroll;

    protected VideoSettingsSidebarMixin(Text title) {
        super(title);
    }

    @Unique
    private static List<String> ser$wrapText(TextRenderer tr, String text, int maxWidth) {
        List<String> lines = new ArrayList<>();
        if (text == null || text.isEmpty()) {
            return lines;
        }
        String remaining = text;
        while (!remaining.isEmpty()) {
            String line = tr.trimToWidth(remaining, maxWidth);
            if (line.isEmpty()) {
                break;
            }
            lines.add(line);
            if (line.length() >= remaining.length()) {
                break;
            }
            remaining = remaining.substring(line.length()).stripLeading();
        }
        return lines;
    }

    @Unique
    private void ser$drawScrollbar(DrawContext context, int panelX, int panelY, int panelW, int panelH,
            int scroll, int scrollMax) {
        if (scrollMax <= 0) {
            return;
        }

        int viewTop = panelY + VideoSettings.PANEL_TOP_Y;
        int viewBottom = panelY + panelH - 12;
        int trackH = viewBottom - viewTop;
        if (trackH <= 0) {
            return;
        }

        int trackW = 4;
        int trackX = panelX + panelW - 6;
        int trackY = viewTop;
        context.fill(trackX, trackY, trackX + trackW, trackY + trackH, 0x55343A45);

        float ratio = trackH / (float) (trackH + scrollMax);
        int thumbH = Math.max(12, Math.round(trackH * ratio));
        int thumbY = trackY + Math.round((trackH - thumbH) * (scroll / (float) scrollMax));
        context.fill(trackX, thumbY, trackX + trackW, thumbY + thumbH, 0xAA6B7280);
    }

    @Unique
    private boolean ser$isMouseOverScrollbar(double mouseX, double mouseY) {
        int panelX = ser$displayPanelX;
        int panelY = ser$displayPanelY;
        int panelW = ser$displayPanelW;
        int panelH = ser$displayPanelH;

        Object controller = null;
        if (ser$selectedSection == VideoSettings.Section.DISPLAY) {
            controller = ser$displayScrollController;
        } else if (ser$selectedSection == VideoSettings.Section.QUALITY_PERFORMANCE) {
            controller = ser$qualityScrollController;
        } else if (ser$selectedSection == VideoSettings.Section.CULLING) {
            controller = ser$cullingScrollController;
        } else if (ser$selectedSection == VideoSettings.Section.MEMORY_USAGE) {
            controller = ser$memoryScrollController;
        }

        if (controller == null) {
            return false;
        }

        int scrollMax = ser$getScrollMax(controller);
        if (scrollMax <= 0) {
            return false;
        }

        int viewTop = panelY + VideoSettings.PANEL_TOP_Y;
        int viewBottom = panelY + panelH - 12;
        int trackH = viewBottom - viewTop;
        if (trackH <= 0) {
            return false;
        }

        int trackW = 4;
        int trackX = panelX + panelW - 6;
        int trackY = viewTop;

        int scroll = ser$getScroll(controller);
        float ratio = trackH / (float) (trackH + scrollMax);
        int thumbH = Math.max(12, Math.round(trackH * ratio));
        int thumbY = trackY + Math.round((trackH - thumbH) * (scroll / (float) scrollMax));

        return mouseX >= trackX && mouseX <= trackX + trackW && mouseY >= thumbY && mouseY <= thumbY + thumbH;
    }

    @Unique
    private int ser$getScroll(Object controller) {
        if (controller instanceof Display.ScrollController) {
            return ((Display.ScrollController) controller).getScroll();
        } else if (controller instanceof QualityPerformance.ScrollController) {
            return ((QualityPerformance.ScrollController) controller).getScroll();
        }
        return 0;
    }

    @Unique
    private int ser$getScrollMax(Object controller) {
        if (controller instanceof Display.ScrollController) {
            return ((Display.ScrollController) controller).getScrollMax();
        } else if (controller instanceof QualityPerformance.ScrollController) {
            return ((QualityPerformance.ScrollController) controller).getScrollMax();
        }
        return 0;
    }

    @Unique
    private void ser$setScroll(Object controller, int value) {
        if (controller instanceof Display.ScrollController) {
            ((Display.ScrollController) controller).setScroll(value);
        } else if (controller instanceof QualityPerformance.ScrollController) {
            ((QualityPerformance.ScrollController) controller).setScroll(value);
        }
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        ser$lastScaledWidth = width;
        ser$lastScaledHeight = height;
        this.init(width, height);
    }

    @Override
    public void tick() {
        super.tick();
        ser$syncFullscreenOption();
        if (!ser$pendingResize) {
            ser$updateSectionVisibility();
            return;
        }
        MinecraftClient mc = MinecraftClient.getInstance();
        int scaledW = mc.getWindow().getScaledWidth();
        int scaledH = mc.getWindow().getScaledHeight();
        if (scaledW != ser$lastScaledWidth || scaledH != ser$lastScaledHeight) {
            ser$pendingResize = false;
            ser$pendingResizeTicks = 0;
            this.resize(scaledW, scaledH);
            return;
        }

        if (ser$pendingResizeTicks > 0) {
            ser$pendingResizeTicks--;
            if (ser$pendingResizeTicks == 0) {
                ser$pendingResize = false;
                this.resize(scaledW, scaledH);
            }
        }

        ser$updateSectionVisibility();
    }

    @Unique
    private void ser$setSection(VideoSettings.Section section) {
        ser$selectedSection = section;
        ser$lastSelectedSection = section;
        ser$updateSectionVisibility();
    }

    @Unique
    private void ser$syncFullscreenOption() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null) {
            return;
        }
        boolean windowFullscreen = mc.getWindow().isFullscreen();
        boolean optionFullscreen = (Boolean) this.gameOptions.getFullscreen().getValue();
        if (windowFullscreen != optionFullscreen) {
            this.gameOptions.getFullscreen().setValue(windowFullscreen);
        }
    }

    @Unique
    private void ser$syncPanelHeight() {
        int windowH = MinecraftClient.getInstance().getWindow().getScaledHeight();
        int panelH = Math.max(ser$displayPanelH, Math.max(0, windowH - ser$displayPanelY));
        boolean grew = ser$lastPanelH > -1 && panelH > ser$lastPanelH;
        if (panelH == ser$lastPanelH) {
            return;
        }
        ser$lastPanelH = panelH;

        ser$sidebarH = Math.max(0, windowH - ser$sidebarY);
        if (ser$addonsButton != null) {
            ser$addonsButton.setY(ser$sidebarY + ser$sidebarH - 50);
        }
        if (ser$displayScrollController != null) {
            ser$displayScrollController.setPanelBounds(ser$displayPanelY, panelH);
            if (grew) {
                ser$displayScrollController.resetScroll();
            }
            ser$displayScrollController.init();
        }
        if (ser$qualityScrollController != null) {
            ser$qualityScrollController.setPanelBounds(ser$displayPanelY, panelH);
            if (grew) {
                ser$qualityScrollController.resetScroll();
            }
            ser$qualityScrollController.init();
        }
        if (ser$cullingScrollController != null) {
            ser$cullingScrollController.setPanelBounds(ser$displayPanelY, panelH);
            if (grew) {
                ser$cullingScrollController.resetScroll();
            }
            ser$cullingScrollController.init();
        }
        if (ser$memoryScrollController != null) {
            ser$memoryScrollController.setPanelBounds(ser$displayPanelY, panelH);
            if (grew) {
                ser$memoryScrollController.resetScroll();
            }
            ser$memoryScrollController.init();
        }
    }

    @Unique
    private void ser$updateSectionVisibility() {
        ser$syncPanelHeight();

        boolean display = ser$selectedSection == VideoSettings.Section.DISPLAY;
        for (ClickableWidget w : ser$displayWidgets) {
            boolean inView = display
                    && ser$displayScrollController != null
                    && ser$displayScrollController.isWidgetVisible(w);
            w.visible = inView;
            w.active = inView;
        }

        boolean quality = ser$selectedSection == VideoSettings.Section.QUALITY_PERFORMANCE;
        for (ClickableWidget w : ser$qualityWidgets) {
            boolean inView = quality
                    && ser$qualityScrollController != null
                    && ser$qualityScrollController.isWidgetVisible(w);
            w.visible = inView;
            w.active = inView;
        }

        boolean preferences = ser$selectedSection == VideoSettings.Section.PREFERENCES;
        for (ClickableWidget w : ser$preferencesWidgets) {
            w.visible = preferences;
            w.active = preferences;
        }

        boolean culling = ser$selectedSection == VideoSettings.Section.CULLING;
        for (ClickableWidget w : ser$cullingWidgets) {
            boolean inView = culling
                    && ser$cullingScrollController != null
                    && ser$cullingScrollController.isWidgetVisible(w);
            w.visible = inView;
            w.active = inView;
        }

        boolean memory = ser$selectedSection == VideoSettings.Section.MEMORY_USAGE;
        for (ClickableWidget w : ser$memoryWidgets) {
            boolean inView = memory
                    && ser$memoryScrollController != null
                    && ser$memoryScrollController.isWidgetVisible(w);
            w.visible = inView;
            w.active = inView;
        }

        boolean addons = ser$selectedSection == VideoSettings.Section.ADDONS;
        boolean zoomEnabled = Config.getInstance().isCameraZoomEnabled();
        for (ClickableWidget w : ser$addonsWidgets) {
            boolean visible = addons;
            if (addons && !zoomEnabled && Zoom.isZoomOptionWidget(w)) {
                visible = false;
            }
            w.visible = visible;
            w.active = visible;
        }

        // Always reserve space for the sidebar + panel so they never overlap the
        // vanilla options list (regardless of the selected section).
        if (ser$bodyLayoutCaptured) {
            // Disable the vanilla Video Options list completely.
            // The mod provides its own sidebar + panels UI, and keeping the vanilla
            // list can cause overlapping/scissor issues and confusing duplicated controls.
            this.body.visible = false;
            this.body.active = false;

            // Important: Some versions/widgets can still consume mouse events even
            // when not visible. Move it off-screen and shrink it so it can't be hit.
            this.body.setX(this.width + 10_000);
            this.body.setWidth(0);
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (ser$selectedSection == VideoSettings.Section.DISPLAY && ser$displayScrollController != null) {
            if (ser$displayScrollController.handleScroll(mouseX, mouseY, verticalAmount, ser$displayPanelX,
                    ser$displayPanelW)) {
                ser$updateSectionVisibility();
                return true;
            }
        }
        if (ser$selectedSection == VideoSettings.Section.QUALITY_PERFORMANCE && ser$qualityScrollController != null) {
            if (ser$qualityScrollController.handleScroll(mouseX, mouseY, verticalAmount, ser$displayPanelX,
                    ser$displayPanelW)) {
                ser$updateSectionVisibility();
                return true;
            }
        }
        if (ser$selectedSection == VideoSettings.Section.CULLING && ser$cullingScrollController != null) {
            if (ser$cullingScrollController.handleScroll(mouseX, mouseY, verticalAmount, ser$displayPanelX,
                    ser$displayPanelW)) {
                ser$updateSectionVisibility();
                return true;
            }
        }
        if (ser$selectedSection == VideoSettings.Section.MEMORY_USAGE && ser$memoryScrollController != null) {
            if (ser$memoryScrollController.handleScroll(mouseX, mouseY, verticalAmount, ser$displayPanelX,
                    ser$displayPanelW)) {
                ser$updateSectionVisibility();
                return true;
            }
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Unique
    private static int ser$clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    @Inject(method = "init", at = @At("RETURN"))
    private void ser$removeDoneButton(CallbackInfo ci) {
        if (!((Object) this instanceof VideoOptionsScreen)) {
            return;
        }

        // إزالة جميع عناصر Video Settings الأصلية من Minecraft نهائياً
        this.clearChildren();
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void ser$addVideoSettingsSidebar(CallbackInfo ci) {
        if (!((Object) this instanceof VideoOptionsScreen)) {
            return;
        }

        ser$displayWidgets.clear();
        ser$qualityWidgets.clear();
        ser$preferencesWidgets.clear();
        ser$cullingWidgets.clear();
        ser$memoryWidgets.clear();
        ser$displayScrollController = new Display.ScrollController(ser$displayWidgets);
        ser$qualityScrollController = new QualityPerformance.ScrollController(ser$qualityWidgets);
        ser$cullingScrollController = new Display.ScrollController(ser$cullingWidgets);
        ser$memoryScrollController = new Display.ScrollController(ser$memoryWidgets);

        // إخفاء وإزالة قائمة الخيارات الأصلية تماماً
        this.body.visible = false;
        this.body.active = false;
        this.body.setX(-10000); // نقلها خارج الشاشة
        this.body.setWidth(0);
        this.body.setHeight(0);
        try {
            this.children().remove(this.body);
        } catch (Throwable ignored) {
        }

        // Restore last tab selection (especially important when we reopen the screen
        // after changing GUI scale).
        ser$selectedSection = ser$lastSelectedSection;

        if (!ser$bodyLayoutCaptured) {
            ser$bodyOriginalX = this.body.getX();
            ser$bodyOriginalWidth = this.body.getWidth();
            ser$bodyLayoutCaptured = true;
        }

        int screenW = this.width;
        int screenH = this.height;

        int sidebarW = ser$clamp((int) (screenW * 0.22f), 120, 180);
        int panelGap = VideoSettings.SIDEBAR_PANEL_GAP;
        int minPanelW = 220;
        int targetPanelW = (int) (screenW * 0.36f);
        int panelW = ser$clamp(targetPanelW,
                minPanelW,
                Math.max(minPanelW, screenW - sidebarW - panelGap - 10));
        int sidebarX = VideoSettings.SIDEBAR_PADDING;
        int sidebarY = 0;
        int sidebarH = screenH;
        ser$sidebarY = sidebarY;
        ser$sidebarH = sidebarH;

        this.addDrawable(new VideoSettingsSidebar.Panel(
                sidebarX,
                sidebarY,
                sidebarW,
                sidebarH,
                this.textRenderer));

        int buttonX = sidebarX + 8;
        int buttonW = sidebarW - 16;
        int buttonY = sidebarY + 32;
        int buttonH = 22;
        int gap = 8;

        ser$displayButton = this.addDrawableChild(new VideoSettingsSidebar.TabButton(
                buttonX,
                buttonY,
                buttonW,
                buttonH,
                Text.literal("Display"),
                VideoSettings.Section.DISPLAY,
                () -> ser$selectedSection,
                this::ser$setSection));

        ser$qualityButton = this.addDrawableChild(new VideoSettingsSidebar.TabButton(
                buttonX,
                buttonY + (buttonH + gap),
                buttonW,
                buttonH,
                Text.literal("Quality & Performance"),
                VideoSettings.Section.QUALITY_PERFORMANCE,
                () -> ser$selectedSection,
                this::ser$setSection));

        ser$preferencesButton = this.addDrawableChild(new VideoSettingsSidebar.TabButton(
                buttonX,
                buttonY + 2 * (buttonH + gap),
                buttonW,
                buttonH,
                Text.literal("Preferences"),
                VideoSettings.Section.PREFERENCES,
                () -> ser$selectedSection,
                this::ser$setSection));

        ser$cullingButton = this.addDrawableChild(new VideoSettingsSidebar.TabButton(
                buttonX,
                buttonY + 3 * (buttonH + gap),
                buttonW,
                buttonH,
                Text.literal("Culling"),
                VideoSettings.Section.CULLING,
                () -> ser$selectedSection,
                this::ser$setSection));

        ser$memoryButton = this.addDrawableChild(new VideoSettingsSidebar.TabButton(
                buttonX,
                buttonY + 4 * (buttonH + gap),
                buttonW,
                buttonH,
                Text.literal("Memory Usage"),
                VideoSettings.Section.MEMORY_USAGE,
                () -> ser$selectedSection,
                this::ser$setSection));

        // Add special Addons button with unique design
        int addonsButtonY = sidebarY + sidebarH - 50; // Position near bottom
        ser$addonsButton = this.addDrawableChild(new VideoSettingsSidebar.AddonsButton(
                buttonX,
                addonsButtonY,
                buttonW,
                28, // Slightly taller than regular buttons
                () -> ser$setSection(VideoSettings.Section.ADDONS)));

        // Section panels: adjacent to the sidebar (not on the far right)
        int panelX = sidebarX + sidebarW + panelGap;
        int panelY = 0;
        int panelH = screenH;
        ser$displayPanelX = panelX;
        ser$displayPanelY = panelY;
        ser$displayPanelW = panelW;
        ser$displayPanelH = panelH;
        ser$displayScrollController.setPanelBounds(panelY, panelH);
        ser$qualityScrollController.setPanelBounds(panelY, panelH);
        ser$cullingScrollController.setPanelBounds(panelY, panelH);
        ser$memoryScrollController.setPanelBounds(panelY, panelH);

        this.addDrawable(new Display.Panel(
                panelX,
                panelY,
                panelW,
                panelH,
                this.textRenderer,
                () -> ser$selectedSection));

        this.addDrawable(new QualityPerformance.Panel(
                panelX,
                panelY,
                panelW,
                panelH,
                this.textRenderer,
                () -> ser$selectedSection));

        this.addDrawable(new Preferences.Panel(
                panelX,
                panelY,
                panelW,
                panelH,
                this.textRenderer,
                () -> ser$selectedSection));

        this.addDrawable(new Culling.Panel(
                panelX,
                panelY,
                panelW,
                panelH,
                this.textRenderer,
                () -> ser$selectedSection));

        this.addDrawable(new MemoryUsage.Panel(
                panelX,
                panelY,
                panelW,
                panelH,
                this.textRenderer,
                () -> ser$selectedSection));

        this.addDrawable(new Zoom.Panel(
                panelX,
                panelY,
                panelW,
                panelH,
                this.textRenderer,
                () -> ser$selectedSection));

        this.addDrawable(new Drawable() {
            @Override
            public void render(DrawContext context, int mouseX, int mouseY, float delta) {
                TextRenderer tr = MinecraftClient.getInstance().textRenderer;
                int screenW = width;
                int screenH = height;
                int padding = 12;
                int maxWidth = Math.min(320, Math.max(180, screenW / 3));
                int lineHeight = 10;
                String[] lines = new String[] {
                        SER$WARNING_LINE_1,
                        SER$WARNING_LINE_2,
                        SER$WARNING_LINE_3
                };

                int blockX = Math.max(padding, screenW - padding - maxWidth - 8);
                int y = screenH - padding - (lines.length * lineHeight);
                for (String rawLine : lines) {
                    String line = tr.trimToWidth(rawLine, maxWidth);
                    context.drawText(tr, Text.literal(line), blockX, y, 0xFFB0B7C3, false);
                    y += lineHeight;
                }
            }
        });

        this.addDrawable(new Drawable() {
            @Override
            public void render(DrawContext context, int mouseX, int mouseY, float delta) {
                if (ser$selectedSection == VideoSettings.Section.DISPLAY && ser$displayScrollController != null) {
                    ser$drawScrollbar(context, panelX, panelY, panelW, panelH,
                            ser$displayScrollController.getScroll(),
                            ser$displayScrollController.getScrollMax());
                    return;
                }
                if (ser$selectedSection == VideoSettings.Section.QUALITY_PERFORMANCE
                        && ser$qualityScrollController != null) {
                    ser$drawScrollbar(context, panelX, panelY, panelW, panelH,
                            ser$qualityScrollController.getScroll(),
                            ser$qualityScrollController.getScrollMax());
                    return;
                }
                if (ser$selectedSection == VideoSettings.Section.CULLING && ser$cullingScrollController != null) {
                    ser$drawScrollbar(context, panelX, panelY, panelW, panelH,
                            ser$cullingScrollController.getScroll(),
                            ser$cullingScrollController.getScrollMax());
                    return;
                }
                if (ser$selectedSection == VideoSettings.Section.MEMORY_USAGE && ser$memoryScrollController != null) {
                    ser$drawScrollbar(context, panelX, panelY, panelW, panelH,
                            ser$memoryScrollController.getScroll(),
                            ser$memoryScrollController.getScrollMax());
                }
            }
        });

        int controlX = panelX + VideoSettings.PANEL_INSET_X;
        int controlW = panelW - (VideoSettings.PANEL_INSET_X * 2);
        int y = panelY + VideoSettings.PANEL_TOP_Y;

        y = Display.addControls(
                this::addDrawableChild,
                this.ser$displayWidgets,
                this.gameOptions,
                controlX,
                y,
                controlW,
                () -> {
                    this.gameOptions.write();
                    MinecraftClient.getInstance().setScreen(this.parent);
                },
                () -> {
                    MinecraftClient mc = MinecraftClient.getInstance();
                    mc.getWindow().toggleFullscreen();
                    mc.onResolutionChanged();
                    ser$lastScaledWidth = mc.getWindow().getScaledWidth();
                    ser$lastScaledHeight = mc.getWindow().getScaledHeight();
                    ser$pendingResize = true;
                    ser$pendingResizeTicks = 3;
                },
                () -> Text.literal(String.valueOf(Display.getGuiScaleFixed(this.gameOptions))),
                () -> {
                    int current = Display.getGuiScaleFixed(this.gameOptions);
                    int next = (current >= 3) ? 1 : current + 1;
                    this.gameOptions.getGuiScale().setValue(next);
                    this.gameOptions.write();

                    MinecraftClient mc = MinecraftClient.getInstance();
                    mc.onResolutionChanged();
                    mc.execute(() -> mc.setScreen(new VideoOptionsScreen(this.parent, mc, this.gameOptions)));
                });

        ser$displayScrollController.init();

        // Quality & Performance controls
        int qualityX = panelX + VideoSettings.PANEL_INSET_X;
        int qualityW = panelW - (VideoSettings.PANEL_INSET_X * 2);
        int qy = panelY + VideoSettings.PANEL_TOP_Y;
        QualityPerformance.addControls(
                this::addDrawableChild,
                this.ser$qualityWidgets,
                this.gameOptions,
                qualityX,
                qy,
                qualityW,
                () -> {
                    this.gameOptions.write();
                    MinecraftClient.getInstance().setScreen(this.parent);
                });
        ser$qualityScrollController.init();

        // Preferences controls
        int preferencesX = panelX + VideoSettings.PANEL_INSET_X;
        int preferencesW = panelW - (VideoSettings.PANEL_INSET_X * 2);
        int py = panelY + VideoSettings.PANEL_TOP_Y;
        Preferences.addControls(
                this::addDrawableChild,
                this.ser$preferencesWidgets,
                this.gameOptions,
                preferencesX,
                py,
                preferencesW,
                () -> {
                    this.gameOptions.write();
                    MinecraftClient.getInstance().setScreen(this.parent);
                });

        // Culling controls
        int cullingX = panelX + VideoSettings.PANEL_INSET_X;
        int cullingW = panelW - (VideoSettings.PANEL_INSET_X * 2);
        int cy = panelY + VideoSettings.PANEL_TOP_Y;
        Culling.addControls(
                this::addDrawableChild,
                this.ser$cullingWidgets,
                cullingX,
                cy,
                cullingW,
                () -> {
                    this.gameOptions.write();
                    MinecraftClient.getInstance().setScreen(this.parent);
                });
        ser$cullingScrollController.init();

        // Memory Usage controls
        int memoryX = panelX + VideoSettings.PANEL_INSET_X;
        int memoryW = panelW - (VideoSettings.PANEL_INSET_X * 2);
        int my = panelY + VideoSettings.PANEL_TOP_Y;
        MemoryUsage.addControls(
                this::addDrawableChild,
                this.ser$memoryWidgets,
                memoryX,
                my,
                memoryW,
                () -> {
                    this.gameOptions.write();
                    MinecraftClient.getInstance().setScreen(this.parent);
                });
        ser$memoryScrollController.init();

        // Addons controls
        int addonsX = panelX + VideoSettings.PANEL_INSET_X;
        int addonsW = panelW - (VideoSettings.PANEL_INSET_X * 2);
        int ay = panelY + VideoSettings.PANEL_TOP_Y;
        Zoom.addControls(
                addonsX,
                ay,
                addonsW,
                this.ser$addonsWidgets,
                this::addDrawableChild);

        ser$updateSectionVisibility();
    }

    @Inject(method = "removed", at = @At("TAIL"))
    private void ser$persistOptionsOnClose(CallbackInfo ci) {
        this.gameOptions.write();
        MinecraftClient client = MinecraftClient.getInstance();
        if (client != null) {
            client.options.write();
        }
    }

    @Override
    public boolean mouseClicked(net.minecraft.client.gui.Click click, boolean bl) {
        double mouseX = click.x();
        double mouseY = click.y();
        int button = click.button();

        if (button == 0 && ser$isMouseOverScrollbar(mouseX, mouseY)) {
            ser$isDraggingScrollbar = true;
            ser$dragStartY = (int) mouseY;

            Object controller = null;
            if (ser$selectedSection == VideoSettings.Section.DISPLAY) {
                controller = ser$displayScrollController;
            } else if (ser$selectedSection == VideoSettings.Section.QUALITY_PERFORMANCE) {
                controller = ser$qualityScrollController;
            } else if (ser$selectedSection == VideoSettings.Section.CULLING) {
                controller = ser$cullingScrollController;
            } else if (ser$selectedSection == VideoSettings.Section.MEMORY_USAGE) {
                controller = ser$memoryScrollController;
            }

            if (controller != null) {
                ser$dragStartScroll = ser$getScroll(controller);
            }
            return true;
        }
        return super.mouseClicked(click, bl);
    }

    @Override
    public boolean mouseDragged(net.minecraft.client.gui.Click click, double deltaX, double deltaY) {
        if (ser$isDraggingScrollbar) {
            double mouseY = click.y();

            Object controller = null;
            if (ser$selectedSection == VideoSettings.Section.DISPLAY) {
                controller = ser$displayScrollController;
            } else if (ser$selectedSection == VideoSettings.Section.QUALITY_PERFORMANCE) {
                controller = ser$qualityScrollController;
            } else if (ser$selectedSection == VideoSettings.Section.CULLING) {
                controller = ser$cullingScrollController;
            } else if (ser$selectedSection == VideoSettings.Section.MEMORY_USAGE) {
                controller = ser$memoryScrollController;
            }

            if (controller != null) {
                int panelY = ser$displayPanelY;
                int panelH = ser$displayPanelH;
                int viewTop = panelY + VideoSettings.PANEL_TOP_Y;
                int viewBottom = panelY + panelH - 12;
                int trackH = viewBottom - viewTop;

                int scrollMax = ser$getScrollMax(controller);
                if (scrollMax > 0 && trackH > 0) {
                    int mouseDelta = (int) mouseY - ser$dragStartY;
                    float ratio = trackH / (float) (trackH + scrollMax);
                    int thumbH = Math.max(12, Math.round(trackH * ratio));
                    int scrollableTrackH = trackH - thumbH;

                    int scrollDelta = 0;
                    if (scrollableTrackH > 0) {
                        scrollDelta = Math.round((mouseDelta / (float) scrollableTrackH) * scrollMax);
                    }

                    int newScroll = Math.max(0, Math.min(scrollMax, ser$dragStartScroll + scrollDelta));
                    ser$setScroll(controller, newScroll);
                    ser$updateSectionVisibility();
                }
            }
            return true;
        }
        return super.mouseDragged(click, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(net.minecraft.client.gui.Click click) {
        if (ser$isDraggingScrollbar) {
            ser$isDraggingScrollbar = false;
            return true;
        }
        return super.mouseReleased(click);
    }

}
