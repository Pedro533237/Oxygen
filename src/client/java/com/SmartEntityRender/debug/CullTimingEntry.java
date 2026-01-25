package com.SmartEntityRender.debug;

import com.SmartEntityRender.culling.OcclusionCullingInstance;
import net.minecraft.util.Formatting;
import org.jspecify.annotations.NonNull;

import java.util.List;

/**
 * Debug entry for culling timing information
 * إدخال تصحيح لمعلومات توقيت الحذف
 */
public class CullTimingEntry {

    private final OcclusionCullingInstance cullingInstance;

    public CullTimingEntry() {
        this.cullingInstance = OcclusionCullingInstance.getInstance();
    }

    /**
     * Add debug text to F3 screen
     * إضافة نص التصحيح إلى شاشة F3
     */
    public void addDebugText(@NonNull List<String> lines) {
        long avgCullTime = cullingInstance.getAverageCullTime();

        // تحويل من نانوثانية إلى ميكروثانية
        double avgCullMicros = avgCullTime / 1000.0;

        lines.add(Formatting.LIGHT_PURPLE + "SER Avg Cull: " + String.format("%.2f", avgCullMicros) + " μs");
    }

    /**
     * Get short debug text
     */
    public String getShortText() {
        long avgCullTime = cullingInstance.getAverageCullTime();
        double avgCullMicros = avgCullTime / 1000.0;

        return String.format(Formatting.LIGHT_PURPLE + "Cull: %.2f μs", avgCullMicros);
    }
}
