package com.SmartEntityRender.culling;

import com.SmartEntityRender.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Cull Task - مهمة الإخفاء
 * تعمل على خيط منفصل لحساب الرؤية بشكل غير متزامن
 */
public class CullTask implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger("SmartEntityRender.CullTask");
    
    private final OcclusionCullingInstance cullingInstance;
    private final Config config;
    
    public CullTask(OcclusionCullingInstance cullingInstance) {
        this.cullingInstance = cullingInstance;
        this.config = Config.getInstance();
    }
    
    @Override
    public void run() {
        LOGGER.info("Cull Task started on thread: {}", Thread.currentThread().getName());
        
        while (cullingInstance.isRunning()) {
            try {
                // تحديث الذاكرة المؤقتة للرؤية
                cullingInstance.updateVisibilityCache();
                
                // الانتظار حسب sleepDelay
                Thread.sleep(config.getSleepDelay());
                
            } catch (InterruptedException e) {
                LOGGER.info("Cull Task interrupted");
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                LOGGER.error("Error in Cull Task", e);
                // الاستمرار في العمل رغم الأخطاء
            }
        }
        
        LOGGER.info("Cull Task stopped");
    }
}
