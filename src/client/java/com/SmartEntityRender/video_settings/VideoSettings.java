package com.SmartEntityRender.video_settings;

import net.minecraft.text.Text;

public final class VideoSettings {
    private VideoSettings() {
    }

    // Sidebar (left)
    public static final int SIDEBAR_WIDTH = 140;
    public static final int SIDEBAR_PADDING = 0;

    // Middle panel (Display/Quality/Preferences)
    public static final int PANEL_WIDTH = 240;
    public static final int SIDEBAR_PANEL_GAP = 8;

    // Panel internals
    public static final int PANEL_INSET_X = 10;
    public static final int PANEL_TOP_Y = 56;
    public static final int CONTROL_H = 20;
    public static final int CONTROL_GAP = 8;

    public enum Section {
        DISPLAY,
        QUALITY_PERFORMANCE,
        PREFERENCES,
        CULLING,
        MEMORY_USAGE,
        ADDONS;

        public Text label() {
            return switch (this) {
                case DISPLAY -> Text.literal("Display");
                case QUALITY_PERFORMANCE -> Text.literal("Quality & Performance");
                case PREFERENCES -> Text.literal("Preferences");
                case CULLING -> Text.literal("Culling");
                case MEMORY_USAGE -> Text.literal("Memory Usage");
                case ADDONS -> Text.literal("Addons");
            };
        }
    }
}
