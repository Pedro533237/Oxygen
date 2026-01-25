package com.SmartEntityRender.debug;

import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Main debug renderer that manages all debug entries
 * عارض التصحيح الرئيسي الذي يدير جميع إدخالات التصحيح
 */
public class DebugRenderer {

    private static final Logger LOGGER = LoggerFactory.getLogger("SmartEntityRender/Debug");
    private static DebugRenderer instance;

    private final CulledEntitiesEntry culledEntitiesEntry;
    private final CullTimingEntry cullTimingEntry;

    private DebugRenderer() {
        this.culledEntitiesEntry = new CulledEntitiesEntry();
        this.cullTimingEntry = new CullTimingEntry();

        LOGGER.info("Debug Renderer initialized");
    }

    public static DebugRenderer getInstance() {
        if (instance == null) {
            instance = new DebugRenderer();
        }
        return instance;
    }

    /**
     * Add all debug information to F3 screen
     * إضافة جميع معلومات التصحيح إلى شاشة F3
     */
    public void addDebugText(@NonNull List<String> lines) {
        // Keep the F3 output minimal: only the most important stats.
        culledEntitiesEntry.addDebugText(lines);
        cullTimingEntry.addDebugText(lines);
    }

    /**
     * Get compact debug text for right side of F3 screen
     * الحصول على نص تصحيح مضغوط للجانب الأيمن من شاشة F3
     */
    public String getCompactText() {
        return culledEntitiesEntry.getShortText();
    }
}
