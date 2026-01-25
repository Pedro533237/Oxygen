package com.SmartEntityRender.debug;

import com.SmartEntityRender.culling.OcclusionCullingInstance;
import net.minecraft.util.Formatting;
import org.jspecify.annotations.NonNull;

import java.util.List;

/**
 * Debug entry for culled block entities count
 * إدخال تصحيح لعدد الكيانات الكتلية المحذوفة
 */
public class CulledBlockEntitiesEntry {
    
    private final OcclusionCullingInstance cullingInstance;
    
    public CulledBlockEntitiesEntry() {
        this.cullingInstance = OcclusionCullingInstance.getInstance();
    }
    
    /**
     * Add debug text to F3 screen
     * إضافة نص التصحيح إلى شاشة F3
     */
    public void addDebugText(@NonNull List<String> lines) {
        int culledBlockEntities = cullingInstance.getCulledBlockEntitiesCount();
        
        lines.add(Formatting.WHITE + "  Culled Block Entities: " + Formatting.AQUA + culledBlockEntities);
    }
    
    /**
     * Get short debug text
     */
    public String getShortText() {
        int culledBlockEntities = cullingInstance.getCulledBlockEntitiesCount();
        return String.format(Formatting.AQUA + "BE: %d culled", culledBlockEntities);
    }
}
