package com.SmartEntityRender.debug;

import com.SmartEntityRender.culling.OcclusionCullingInstance;
import net.minecraft.util.Formatting;
import org.jspecify.annotations.NonNull;

import java.util.List;

/**
 * Debug entry for culled entities count
 * إدخال تصحيح لعدد الكيانات المحذوفة
 */
public class CulledEntitiesEntry {

    private final OcclusionCullingInstance cullingInstance;

    public CulledEntitiesEntry() {
        this.cullingInstance = OcclusionCullingInstance.getInstance();
    }

    /**
     * Add debug text to F3 screen
     * إضافة نص التصحيح إلى شاشة F3
     */
    public void addDebugText(@NonNull List<String> lines) {
        long checks = cullingInstance.getRenderChecksCount();
        long culled = cullingInstance.getRenderCulledCount();
        long rendered = cullingInstance.getRenderVisibleCount();

        int cacheSize = cullingInstance.getCacheSize();
        double cullPercentage = checks > 0 ? (culled * 100.0 / checks) : 0;

        lines.add(Formatting.GOLD + "SER" + Formatting.GRAY + ": "
                + Formatting.YELLOW + checks + Formatting.GRAY + " checks, "
                + Formatting.RED + culled + Formatting.GRAY + " culled (" + String.format("%.1f%%", cullPercentage)
                + "), "
                + Formatting.GREEN + rendered + Formatting.GRAY + " rendered, "
                + Formatting.AQUA + cacheSize + Formatting.GRAY + " cached");
    }

    /**
     * Get short debug text for compact display
     * الحصول على نص تصحيح مختصر للعرض المضغوط
     */
    public String getShortText() {
        long checks = cullingInstance.getRenderChecksCount();
        long culled = cullingInstance.getRenderCulledCount();
        double cullPercentage = checks > 0 ? (culled * 100.0 / checks) : 0;

        return String.format(Formatting.GOLD + "SER: " + Formatting.RED + "%d" + Formatting.GRAY + " culled (%.1f%%)",
                culled, cullPercentage);
    }
}
