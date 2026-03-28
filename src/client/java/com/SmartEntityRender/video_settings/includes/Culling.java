package com.SmartEntityRender.video_settings.includes;

import com.SmartEntityRender.config.Config;
import com.SmartEntityRender.video_settings.VideoSettings;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public final class Culling {
    private Culling() {
    }

    private static Text leavesCullingModeLabel(int mode) {
        return switch (mode) {
            case 1 -> Text.literal("Depth");
            case 2 -> Text.literal("Vertical");
            case 3 -> Text.literal("Perimeter");
            default -> Text.literal("Off");
        };
    }

    private static Text leavesCullingAmountLabel(int amount) {
        return Text.literal("Depth " + amount);
    }

    public static int addControls(
            Function<ClickableWidget, ClickableWidget> register,
            List<ClickableWidget> widgets,
            int x,
            int y,
            int width,
            Runnable onSave) {
        int currentY = y;

        var cloudCullingToggle = new Display.ToggleButton(
                x,
                currentY,
                width,
                VideoSettings.CONTROL_H,
                Text.literal("Cloud Culling"),
                () -> Config.getInstance().isCloudCulling(),
                value -> {
                    Config.getInstance().setCloudCulling(value);
                    MinecraftClient client = MinecraftClient.getInstance();
                    if (client != null && client.worldRenderer != null && client.world != null) {
                        client.worldRenderer.reload();
                    }
                });
        cloudCullingToggle.setTooltip(Tooltip.of(Text.literal(
                "Enable face culling for clouds\n• ON: Skip hidden cloud faces\n• OFF: Vanilla cloud rendering")));
        widgets.add(register.apply(cloudCullingToggle));
        currentY += VideoSettings.CONTROL_H + VideoSettings.CONTROL_GAP;

        var signTextCullingToggle = new Display.ToggleButton(
                x,
                currentY,
                width,
                VideoSettings.CONTROL_H,
                Text.literal("Sign Text Culling"),
                () -> Config.getInstance().isSignTextCulling(),
                value -> Config.getInstance().setSignTextCulling(value));
        signTextCullingToggle.setTooltip(Tooltip.of(Text.literal(
                "Skip rendering sign text when not visible\n• ON: Hide back-facing text\n• OFF: Render all sign text")));
        widgets.add(register.apply(signTextCullingToggle));
        currentY += VideoSettings.CONTROL_H + VideoSettings.CONTROL_GAP;

        var weatherFrustumCullingToggle = new Display.ToggleButton(
                x,
                currentY,
                width,
                VideoSettings.CONTROL_H,
                Text.literal("Rain/Snow Frustum Culling"),
                () -> Config.getInstance().isWeatherFrustumCulling(),
                value -> Config.getInstance().setWeatherFrustumCulling(value));
        weatherFrustumCullingToggle.setTooltip(Tooltip.of(Text.literal(
                "Cull rain/snow outside the view frustum\n• ON: Skip off-screen precipitation\n• OFF: Render full weather")));
        widgets.add(register.apply(weatherFrustumCullingToggle));
        currentY += VideoSettings.CONTROL_H + VideoSettings.CONTROL_GAP;

        var beaconBeamFrustumCullingToggle = new Display.ToggleButton(
                x,
                currentY,
                width,
                VideoSettings.CONTROL_H,
                Text.literal("Beacon Beam Frustum Culling"),
                () -> Config.getInstance().isBeaconBeamFrustumCulling(),
                value -> Config.getInstance().setBeaconBeamFrustumCulling(value));
        beaconBeamFrustumCullingToggle.setTooltip(Tooltip.of(Text.literal(
                "Cull beacon beams outside the view frustum\n• ON: Skip off-screen beams\n• OFF: Render full beams")));
        widgets.add(register.apply(beaconBeamFrustumCullingToggle));
        currentY += VideoSettings.CONTROL_H + VideoSettings.CONTROL_GAP;

        var blockStateAggressiveCullingToggle = new Display.ToggleButton(
                x,
                currentY,
                width,
                VideoSettings.CONTROL_H,
                Text.literal("BlockState Culling (Aggressive)"),
                () -> Config.getInstance().isBlockStateCullingAggressive(),
                value -> Config.getInstance().setBlockStateCullingAggressive(value));
        blockStateAggressiveCullingToggle.setTooltip(Tooltip.of(Text.literal(
                "Aggressively cull block faces based on state\n• ON: Reduce overdraw strongly\n• OFF: Use vanilla face culling")));
        widgets.add(register.apply(blockStateAggressiveCullingToggle));
        currentY += VideoSettings.CONTROL_H + VideoSettings.CONTROL_GAP;

        var chunkRenderFaceCacheToggle = new Display.ToggleButton(
                x,
                currentY,
                width,
                VideoSettings.CONTROL_H,
                Text.literal("Chunk Face Cache (Sodium-like)"),
                () -> Config.getInstance().isChunkRenderFaceCache(),
                value -> Config.getInstance().setChunkRenderFaceCache(value));
        chunkRenderFaceCacheToggle.setTooltip(Tooltip.of(Text.literal(
                "Cache repeated block face visibility checks during chunk rebuilds\n• ON: Lower CPU cost in chunk rendering\n• OFF: Vanilla per-face checks")));
        widgets.add(register.apply(chunkRenderFaceCacheToggle));
        currentY += VideoSettings.CONTROL_H + VideoSettings.CONTROL_GAP;

        var moddedBlockStateCullingToggle = new Display.ToggleButton(
                x,
                currentY,
                width,
                VideoSettings.CONTROL_H,
                Text.literal("Modded BlockState Culling"),
                () -> Config.getInstance().isModdedBlockStateCulling(),
                value -> Config.getInstance().setModdedBlockStateCulling(value));
        moddedBlockStateCullingToggle.setTooltip(Tooltip.of(Text.literal(
                "Apply blockstate culling to modded blocks by default\n• ON: Cull modded blocks\n• OFF: Only aggressive toggle affects all")));
        widgets.add(register.apply(moddedBlockStateCullingToggle));
        currentY += VideoSettings.CONTROL_H + VideoSettings.CONTROL_GAP;

        var dontCullListButton = new Display.KeyValueButton(
                x,
                currentY,
                width,
                VideoSettings.CONTROL_H,
                Text.literal("Dont Cull List"),
                () -> {
                    int size = Config.getInstance().getBlockStateDontCullList().size();
                    return Text.literal(size + " blocks");
                },
                () -> {
                });
        dontCullListButton.setTooltip(Tooltip.of(Text.literal(
                "Blocks excluded from blockstate culling (Read-only)\n§7Edit manually in config file:\n§7config/smartentityrender.properties\n§7Example: blockStateDontCullList=minecraft:glass,minecraft:leaves")));
        dontCullListButton.active = false;
        widgets.add(register.apply(dontCullListButton));
        currentY += VideoSettings.CONTROL_H + VideoSettings.CONTROL_GAP;

        var itemFrameCustomToggle = new Display.ToggleButton(
                x,
                currentY,
                width,
                VideoSettings.CONTROL_H,
                Text.literal("Custom Item Frame Renderer"),
                () -> Config.getInstance().isItemFrameCustomRenderer(),
                value -> Config.getInstance().setItemFrameCustomRenderer(value));
        itemFrameCustomToggle.setTooltip(Tooltip.of(Text.literal(
                "Use optimized renderer for item frames\n• ON: Enable item frame optimizations\n• OFF: Vanilla renderer")));
        widgets.add(register.apply(itemFrameCustomToggle));
        currentY += VideoSettings.CONTROL_H + VideoSettings.CONTROL_GAP;

        var itemFrameMapCullingToggle = new Display.ToggleButton(
                x,
                currentY,
                width,
                VideoSettings.CONTROL_H,
                Text.literal("Item Frame Map Culling"),
                () -> Config.getInstance().isItemFrameMapCulling(),
                value -> Config.getInstance().setItemFrameMapCulling(value));
        itemFrameMapCullingToggle.setTooltip(Tooltip.of(Text.literal(
                "Cull map rendering when not visible\n• ON: Skip hidden maps\n• OFF: Always render maps")));
        widgets.add(register.apply(itemFrameMapCullingToggle));
        currentY += VideoSettings.CONTROL_H + VideoSettings.CONTROL_GAP;

        var itemFrameLodToggle = new Display.ToggleButton(
                x,
                currentY,
                width,
                VideoSettings.CONTROL_H,
                Text.literal("Item Frame LOD + Range"),
                () -> Config.getInstance().isItemFrameLodEnabled(),
                value -> Config.getInstance().setItemFrameLodEnabled(value));
        itemFrameLodToggle.setTooltip(Tooltip.of(Text.literal(
                "Reduce detail after a distance and cull at max range\n• ON: Distance-based LOD\n• OFF: Full detail always")));
        widgets.add(register.apply(itemFrameLodToggle));
        currentY += VideoSettings.CONTROL_H + VideoSettings.CONTROL_GAP;

        int[] lodSteps = new int[] { 8, 12, 16, 24, 32, 48, 64, 96, 128 };
        var itemFrameLodDistanceButton = new Display.KeyValueButton(
                x,
                currentY,
                width,
                VideoSettings.CONTROL_H,
                Text.literal("Item Frame LOD Distance"),
                () -> Text.literal(Config.getInstance().getItemFrameLodDistance() + " blocks"),
                () -> {
                    int current = Config.getInstance().getItemFrameLodDistance();
                    int next = lodSteps[0];
                    for (int i = 0; i < lodSteps.length; i++) {
                        if (lodSteps[i] == current) {
                            next = lodSteps[(i + 1) % lodSteps.length];
                            break;
                        }
                    }
                    Config.getInstance().setItemFrameLodDistance(next);
                });
        itemFrameLodDistanceButton.setTooltip(Tooltip.of(Text.literal(
                "Distance where item frame LOD starts")));
        widgets.add(register.apply(itemFrameLodDistanceButton));
        currentY += VideoSettings.CONTROL_H + VideoSettings.CONTROL_GAP;

        int[] rangeSteps = new int[] { 16, 24, 32, 48, 64, 96, 128, 192, 256 };
        var itemFrameMaxDistanceButton = new Display.KeyValueButton(
                x,
                currentY,
                width,
                VideoSettings.CONTROL_H,
                Text.literal("Item Frame Max Distance"),
                () -> Text.literal(Config.getInstance().getItemFrameMaxDistance() + " blocks"),
                () -> {
                    int current = Config.getInstance().getItemFrameMaxDistance();
                    int next = rangeSteps[0];
                    for (int i = 0; i < rangeSteps.length; i++) {
                        if (rangeSteps[i] == current) {
                            next = rangeSteps[(i + 1) % rangeSteps.length];
                            break;
                        }
                    }
                    Config.getInstance().setItemFrameMaxDistance(next);
                });
        itemFrameMaxDistanceButton.setTooltip(Tooltip.of(Text.literal(
                "Maximum distance to render item frames")));
        widgets.add(register.apply(itemFrameMaxDistanceButton));
        currentY += VideoSettings.CONTROL_H + VideoSettings.CONTROL_GAP;

        var itemFrameThreeFaceToggle = new Display.ToggleButton(
                x,
                currentY,
                width,
                VideoSettings.CONTROL_H,
                Text.literal("3-Face Item Frame Culling"),
                () -> Config.getInstance().isItemFrameThreeFaceCulling(),
                value -> Config.getInstance().setItemFrameThreeFaceCulling(value));
        itemFrameThreeFaceToggle.setTooltip(Tooltip.of(Text.literal(
                "Cull back + side faces at distance\n• ON: Front-facing only when far\n• OFF: Render all faces")));
        widgets.add(register.apply(itemFrameThreeFaceToggle));
        currentY += VideoSettings.CONTROL_H + VideoSettings.CONTROL_GAP;

        var itemFrameThreeFaceDistanceButton = new Display.KeyValueButton(
                x,
                currentY,
                width,
                VideoSettings.CONTROL_H,
                Text.literal("3-Face Culling Distance"),
                () -> Text.literal(Config.getInstance().getItemFrameThreeFaceDistance() + " blocks"),
                () -> {
                    int current = Config.getInstance().getItemFrameThreeFaceDistance();
                    int next = lodSteps[0];
                    for (int i = 0; i < lodSteps.length; i++) {
                        if (lodSteps[i] == current) {
                            next = lodSteps[(i + 1) % lodSteps.length];
                            break;
                        }
                    }
                    Config.getInstance().setItemFrameThreeFaceDistance(next);
                });
        itemFrameThreeFaceDistanceButton.setTooltip(Tooltip.of(Text.literal(
                "Distance where 3-face culling starts")));
        widgets.add(register.apply(itemFrameThreeFaceDistanceButton));
        currentY += VideoSettings.CONTROL_H + VideoSettings.CONTROL_GAP;

        var paintingCullingToggle = new Display.ToggleButton(
                x,
                currentY,
                width,
                VideoSettings.CONTROL_H,
                Text.literal("Painting Culling"),
                () -> Config.getInstance().isPaintingCulling(),
                value -> Config.getInstance().setPaintingCulling(value));
        paintingCullingToggle.setTooltip(Tooltip.of(Text.literal(
                "Cull back/inside faces of paintings\n• ON: Hide rear faces\n• OFF: Render both sides")));
        widgets.add(register.apply(paintingCullingToggle));
        currentY += VideoSettings.CONTROL_H + VideoSettings.CONTROL_GAP;

        var leavesCullingModeButton = new Display.KeyValueButton(
                x,
                currentY,
                width,
                VideoSettings.CONTROL_H,
                Text.literal("Leaves Culling Mode"),
                () -> leavesCullingModeLabel(Config.getInstance().getLeavesCullingMode()),
                () -> {
                    int next = (Config.getInstance().getLeavesCullingMode() + 1) % 4;
                    Config.getInstance().setLeavesCullingMode(next);
                });
        leavesCullingModeButton.setTooltip(Tooltip.of(Text.literal(
                "Leaves culling modes\n• Off/Depth/Vertical/Perimeter")));
        widgets.add(register.apply(leavesCullingModeButton));
        currentY += VideoSettings.CONTROL_H + VideoSettings.CONTROL_GAP;

        var leavesCullingAmountButton = new Display.KeyValueButton(
                x,
                currentY,
                width,
                VideoSettings.CONTROL_H,
                Text.literal("Leaves Culling Amount"),
                () -> leavesCullingAmountLabel(Config.getInstance().getLeavesCullingAmount()),
                () -> {
                    int next = Config.getInstance().getLeavesCullingAmount() + 1;
                    if (next > 4) {
                        next = 1;
                    }
                    Config.getInstance().setLeavesCullingAmount(next);
                });
        leavesCullingAmountButton.setTooltip(Tooltip.of(Text.literal(
                "Depth amount for leaves culling\n• Lower: Faster")));
        widgets.add(register.apply(leavesCullingAmountButton));
        currentY += VideoSettings.CONTROL_H + VideoSettings.CONTROL_GAP;

        var includeMangroveRootsToggle = new Display.ToggleButton(
                x,
                currentY,
                width,
                VideoSettings.CONTROL_H,
                Text.literal("Include Mangrove Roots"),
                () -> Config.getInstance().isIncludeMangroveRoots(),
                value -> Config.getInstance().setIncludeMangroveRoots(value));
        includeMangroveRootsToggle.setTooltip(Tooltip.of(Text.literal(
                "Apply leaves culling to mangrove roots (Depth mode only)")));
        widgets.add(register.apply(includeMangroveRootsToggle));
        currentY += VideoSettings.CONTROL_H + VideoSettings.CONTROL_GAP;

        var endGatewayCullingToggle = new Display.ToggleButton(
                x,
                currentY,
                width,
                VideoSettings.CONTROL_H,
                Text.literal("End Gateway Culling"),
                () -> Config.getInstance().isEndGatewayCulling(),
                value -> Config.getInstance().setEndGatewayCulling(value));
        endGatewayCullingToggle.setTooltip(Tooltip.of(Text.literal(
                "Allow surrounding blocks to cull End Gateway faces")));
        widgets.add(register.apply(endGatewayCullingToggle));
        currentY += VideoSettings.CONTROL_H + VideoSettings.CONTROL_GAP;

        var powderSnowCullingToggle = new Display.ToggleButton(
                x,
                currentY,
                width,
                VideoSettings.CONTROL_H,
                Text.literal("Powder Snow Culling"),
                () -> Config.getInstance().isPowderSnowCulling(),
                value -> Config.getInstance().setPowderSnowCulling(value));
        powderSnowCullingToggle.setTooltip(Tooltip.of(Text.literal(
                "Fix culling for powder snow against solid faces")));
        widgets.add(register.apply(powderSnowCullingToggle));
        currentY += VideoSettings.CONTROL_H + VideoSettings.CONTROL_GAP;

        var entityModelCullingToggle = new Display.ToggleButton(
                x,
                currentY,
                width,
                VideoSettings.CONTROL_H,
                Text.literal("Entity Model Culling"),
                () -> Config.getInstance().isEntityModelCulling(),
                value -> Config.getInstance().setEntityModelCulling(value));
        entityModelCullingToggle.setTooltip(Tooltip.of(Text.literal(
                "Entity model culling (no renderer hook yet)")));
        widgets.add(register.apply(entityModelCullingToggle));
        currentY += VideoSettings.CONTROL_H + VideoSettings.CONTROL_GAP;

        var endGatewayBeamToggle = new Display.ToggleButton(
                x,
                currentY,
                width,
                VideoSettings.CONTROL_H,
                Text.literal("End Gateway Beam Culling"),
                () -> Config.getInstance().isEndGatewayBeamCulling(),
                value -> Config.getInstance().setEndGatewayBeamCulling(value));
        endGatewayBeamToggle.setTooltip(Tooltip.of(Text.literal(
                "Frustum culling for End Gateway beams\n• ON: Skip off-screen beams\n• OFF: Render all beams")));
        widgets.add(register.apply(endGatewayBeamToggle));
        currentY += VideoSettings.CONTROL_H + VideoSettings.CONTROL_GAP;

        var advancedBlockCullingToggle = new Display.ToggleButton(
                x,
                currentY,
                width,
                VideoSettings.CONTROL_H,
                Text.literal("Advanced Block Culling"),
                () -> Config.getInstance().isAdvancedBlockCulling(),
                value -> Config.getInstance().setAdvancedBlockCulling(value));
        advancedBlockCullingToggle.setTooltip(Tooltip.of(Text.literal(
                "Cull faces for doors, stairs, rails, fences, glass panes, shulker boxes\n• ON: Aggressive culling for special blocks\n• OFF: Standard culling only")));
        widgets.add(register.apply(advancedBlockCullingToggle));
        currentY += VideoSettings.CONTROL_H + VideoSettings.CONTROL_GAP;

        var modelCullshapeToggle = new Display.ToggleButton(
                x,
                currentY,
                width,
                VideoSettings.CONTROL_H,
                Text.literal("Model Cullshape Cache"),
                () -> Config.getInstance().isModelCullshapeOptimization(),
                value -> Config.getInstance().setModelCullshapeOptimization(value));
        modelCullshapeToggle.setTooltip(Tooltip.of(Text.literal(
                "Cache cullshape calculations to reduce CPU usage\n• ON: Cache results (faster)\n• OFF: Recalculate every frame")));
        widgets.add(register.apply(modelCullshapeToggle));
        currentY += VideoSettings.CONTROL_H + VideoSettings.CONTROL_GAP;

        var resetCacheButton = new Display.KeyValueButton(
                x,
                currentY,
                width,
                VideoSettings.CONTROL_H,
                Text.literal("Reset Culling Cache"),
                () -> Text.literal("Click to Clear"),
                () -> {
                    try {
                        com.SmartEntityRender.culling.OcclusionCullingInstance.getInstance().clearCaches();
                        com.SmartEntityRender.memory.CullshapeCache.clearCache();
                        MinecraftClient client = MinecraftClient.getInstance();
                        if (client != null && client.worldRenderer != null && client.world != null) {
                            client.worldRenderer.reload();
                        }
                    } catch (Exception e) {
                    }
                });
        resetCacheButton.setTooltip(Tooltip.of(Text.literal(
                "Clear all culling and model caches\n• Useful after changing settings\n• Forces cache rebuild")));
        widgets.add(register.apply(resetCacheButton));
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
            if (selectedSection.get() != VideoSettings.Section.CULLING) {
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
}
