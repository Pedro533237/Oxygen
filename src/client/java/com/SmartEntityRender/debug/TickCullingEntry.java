package com.SmartEntityRender.debug;

import com.SmartEntityRender.config.Config;
import com.SmartEntityRender.culling.OcclusionCullingInstance;
import net.minecraft.util.Formatting;
import org.jspecify.annotations.NonNull;

import java.util.List;

/**
 * Debug entry for tick culling information
 * إدخال تصحيح لمعلومات حذف التحديثات
 */
public class TickCullingEntry {
    
    private final OcclusionCullingInstance cullingInstance;
    private final Config config;
    
    public TickCullingEntry() {
        this.cullingInstance = OcclusionCullingInstance.getInstance();
        this.config = Config.getInstance();
    }
    
    /**
     * Add debug text to F3 screen
     * إضافة نص التصحيح إلى شاشة F3
     */
    public void addDebugText(@NonNull List<String> lines) {
        boolean tickCullingEnabled = config.isTickCulling();
        int tickCulledCount = cullingInstance.getTickCulledEntitiesCount();
        
        String status = tickCullingEnabled ? Formatting.GREEN + "ON" : Formatting.RED + "OFF";
        
        lines.add(Formatting.WHITE + "  Tick Culling: " + status);
        if (tickCullingEnabled) {
            lines.add(Formatting.WHITE + "  Tick Culled Entities: " + Formatting.YELLOW + tickCulledCount);
        }
    }
    
    /**
     * Get short debug text
     */
    public String getShortText() {
        boolean tickCullingEnabled = config.isTickCulling();
        int tickCulledCount = cullingInstance.getTickCulledEntitiesCount();
        
        if (tickCullingEnabled) {
            return String.format(Formatting.YELLOW + "Ticks: %d culled", tickCulledCount);
        } else {
            return Formatting.GRAY + "Tick Culling: OFF";
        }
    }
}
