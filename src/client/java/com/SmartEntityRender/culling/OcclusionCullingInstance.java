package com.SmartEntityRender.culling;

import com.SmartEntityRender.config.Config;
import com.SmartEntityRender.config.WhitelistManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Occlusion Culling Instance - مثيل إخفاء الكيانات
 * يستخدم path-tracing غير متزامن على CPU threads منفصلة
 */
public class OcclusionCullingInstance {
    private static final Logger LOGGER = LoggerFactory.getLogger("SmartEntityRender.Culling");
    private static OcclusionCullingInstance INSTANCE;

    private final MinecraftClient client;
    private final Config config;
    private final WhitelistManager whitelist;
    private final SpatialPartition spatialPartition;
    private final DensityBasedCulling densityCulling;

    // نظام المهام متعددة الخيوط
    private final Thread cullThread;
    private final AtomicBoolean running;
    private final CullTask cullTask;

    // نتائج الرؤية المخبأة
    private final Map<Integer, Boolean> visibilityCache;
    private final Map<Integer, Long> lastUpdateTime;

    // إحصائيات للـ Debug
    private long renderChecksCount = 0;
    private long renderCulledCount = 0;
    private long renderVisibleCount = 0;
    private int culledBlockEntitiesCount = 0;
    private int tickCulledEntitiesCount = 0;
    private long lastCullTime = 0;
    private long totalCullTime = 0;
    private int cullOperations = 0;

    private OcclusionCullingInstance() {
        @org.jspecify.annotations.Nullable
        MinecraftClient clientInstance = MinecraftClient.getInstance();
        if (clientInstance == null) {
            throw new IllegalStateException("MinecraftClient instance is null");
        }
        @SuppressWarnings("nullness")
        @NonNull
        MinecraftClient nonNullClient = clientInstance;

        this.client = nonNullClient;
        this.config = Config.getInstance();
        this.whitelist = WhitelistManager.getInstance();
        this.spatialPartition = new SpatialPartition();
        this.densityCulling = new DensityBasedCulling();

        this.visibilityCache = new ConcurrentHashMap<>();
        this.lastUpdateTime = new ConcurrentHashMap<>();
        this.running = new AtomicBoolean(true);

        // تهيئة نظام المهام
        this.cullTask = new CullTask(this);
        this.cullThread = new Thread(cullTask, "SmartEntityRender-CullThread");
        this.cullThread.setDaemon(true);
        this.cullThread.setUncaughtExceptionHandler((thread, throwable) -> {
            LOGGER.error("Error in cull thread", throwable);
        });

        this.cullThread.start();
        LOGGER.info("Occlusion Culling System initialized with async path-tracing");
    }

    public static OcclusionCullingInstance getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new OcclusionCullingInstance();
        }
        return INSTANCE;
    }

    /**
     * Check if entity should be rendered
     * فحص ما إذا كان يجب رندرة الكيان
     * 
     * @param entity الكيان المراد فحصه
     * @return true إذا كان يجب رندرة الكيان
     */
    public boolean shouldRenderEntity(Entity entity) {
        if (entity == null) {
            return true;
        }

        long startTime = System.nanoTime();
        renderChecksCount++;

        // فحص الإعدادات
        if (config.isSkipEntityCulling()) {
            renderVisibleCount++;
            return true;
        }

        // فحص القائمة البيضاء
        String entityTypeId = EntityType.getId(entity.getType()).toString();
        if (whitelist.isEntityWhitelisted(entityTypeId)) {
            renderVisibleCount++;
            return true;
        }

        // فحص الكثافة أولاً - إذا كانت المنطقة كثيفة جداً
        Entity camera = client.getCameraEntity();
        Vec3d cameraPos = camera != null ? new Vec3d(camera.getX(), camera.getY(), camera.getZ())
                : new Vec3d(entity.getX(), entity.getY(), entity.getZ());
        if (!densityCulling.shouldRenderInDenseArea(entity, cameraPos)) {
            renderCulledCount++;
            return false; // مخفي بسبب الكثافة
        }

        // فحص الذاكرة المؤقتة
        Integer entityId = entity.getId();
        Boolean cached = visibilityCache.get(entityId);
        Long lastUpdate = lastUpdateTime.get(entityId);

        // إذا كانت النتيجة محفوظة وحديثة
        long currentTime = System.currentTimeMillis();
        long visibleCacheMs = Math.max(25L, config.getSleepDelay() * 50L);
        long hiddenCacheMs = 25L; // keep hidden results extremely short to avoid pop-in delay
        if (cached != null && lastUpdate != null) {
            long age = currentTime - lastUpdate;
            long ttl = Boolean.TRUE.equals(cached) ? visibleCacheMs : hiddenCacheMs;
            if (age < ttl) {
                if (cached) {
                    renderVisibleCount++;
                } else {
                    renderCulledCount++;
                }

                long endTime = System.nanoTime();
                lastCullTime = endTime - startTime;
                totalCullTime += lastCullTime;
                cullOperations++;

                return cached;
            }
        }

        // حساب الرؤية (سيتم تحديثه بواسطة CullTask)
        boolean visible = calculateVisibility(entity);
        visibilityCache.put(entityId, visible);
        lastUpdateTime.put(entityId, currentTime);

        if (visible) {
            renderVisibleCount++;
        } else {
            renderCulledCount++;
        }

        long endTime = System.nanoTime();
        lastCullTime = endTime - startTime;
        totalCullTime += lastCullTime;
        cullOperations++;

        return visible;
    }

    /**
     * Calculate visibility using path-tracing and ray casting
     * حساب الرؤية باستخدام path-tracing وتتبع الأشعة
     */
    private boolean calculateVisibility(@NonNull Entity entity) {
        net.minecraft.entity.Entity cameraEntity = client.getCameraEntity();
        World world = client.world;

        if (cameraEntity == null || world == null) {
            return true;
        }

        // حساب المسافة
        Vec3d cameraPos = cameraEntity.getCameraPosVec(1.0f);
        Vec3d entityPos = new Vec3d(entity.getX(), entity.getY(), entity.getZ());
        double distance = cameraPos.squaredDistanceTo(entityPos);
        int tracingDistance = config.getTracingDistance();

        // فحص المسافة
        if (!RayCaster.isInRenderDistance(entity, cameraPos, tracingDistance)) {
            return false; // بعيد جداً
        }

        // الكيانات القريبة جداً دائماً مرئية
        if (distance < 16.0) {
            return true;
        }

        // استخدام Ray Casting للتحقق من الانسداد
        if (config.isOcclusionCulling()) {
            // استخدام multi-point ray casting للدقة
            int samples = config.isStrictFrustumCulling() ? 5 : 3;
            return RayCaster.isEntityVisibleMultiPoint(entity, cameraPos, world, samples);
        }

        // fallback: فحص frustum فقط
        return isInFrustum(entity);
    }

    /**
     * Simple frustum check
     * فحص بسيط للـ frustum
     */
    private boolean isInFrustum(@NonNull Entity entity) {
        return true; // مؤقتاً
    }

    /**
     * Update visibility cache (called by CullTask)
     * تحديث ذاكرة الرؤية (يُستدعى بواسطة CullTask)
     */
    public void updateVisibilityCache() {
        if (client.world == null) {
            return;
        }

        // تحديث Spatial Partition والحصول على قائمة الكيانات
        java.util.List<Entity> entities = new java.util.ArrayList<>();
        for (Entity entity : client.world.getEntities()) {
            if (entity != null) {
                spatialPartition.updateEntity(entity);
                entities.add(entity);
            }
        }

        // تحديث خريطة الكثافة كل 20 إطار
        long frameCount = System.currentTimeMillis() / 50; // تقريبي للفريمات
        densityCulling.updateDensityMap(entities, frameCount);

        // تنظيف الكيانات القديمة
        long currentTime = System.currentTimeMillis();
        lastUpdateTime.entrySet().removeIf(entry -> (currentTime - entry.getValue()) > 5000);
        visibilityCache.keySet().retainAll(lastUpdateTime.keySet());
    }

    /**
     * Check if should cull entity ticks
     * فحص ما إذا كان يجب إيقاف ticks الكيان
     */
    public boolean shouldCullTicks(@NonNull Entity entity) {
        if (!config.isTickCulling()) {
            return false;
        }

        // فحص القائمة البيضاء
        String entityTypeId = EntityType.getId(entity.getType()).toString();
        if (whitelist.isTickCullingWhitelisted(entityTypeId)) {
            return false;
        }

        // إيقاف ticks للكيانات البعيدة وغير المرئية
        boolean shouldCull = !shouldRenderEntity(entity);
        if (shouldCull) {
            tickCulledEntitiesCount++;
        }

        return shouldCull;
    }

    /**
     * Shutdown the culling system
     * إيقاف نظام الإخفاء
     */
    public void shutdown() {
        running.set(false);
        if (cullThread != null && cullThread.isAlive()) {
            cullThread.interrupt();
        }
        LOGGER.info("Occlusion Culling System shut down");
    }

    /**
     * Clear all caches
     * مسح جميع الذاكرة المؤقتة
     */
    public void clearCaches() {
        visibilityCache.clear();
        lastUpdateTime.clear();
        resetStatistics();
        LOGGER.info("Visibility caches cleared");
    }

    /**
     * Reset statistics
     * إعادة تعيين الإحصائيات
     */
    public void resetStatistics() {
        renderChecksCount = 0;
        renderCulledCount = 0;
        renderVisibleCount = 0;
        culledBlockEntitiesCount = 0;
        tickCulledEntitiesCount = 0;
        cullOperations = 0;
        totalCullTime = 0;
    }

    // Getters for statistics
    public long getRenderChecksCount() {
        return renderChecksCount;
    }

    public long getRenderCulledCount() {
        return renderCulledCount;
    }

    public long getRenderVisibleCount() {
        return renderVisibleCount;
    }

    public int getCulledBlockEntitiesCount() {
        return culledBlockEntitiesCount;
    }

    public int getTickCulledEntitiesCount() {
        return tickCulledEntitiesCount;
    }

    public long getLastCullTime() {
        return lastCullTime;
    }

    public long getAverageCullTime() {
        return cullOperations > 0 ? totalCullTime / cullOperations : 0;
    }

    public void incrementCulledBlockEntities() {
        culledBlockEntitiesCount++;
    }

    // Getters
    public boolean isRunning() {
        return running.get();
    }

    public int getCacheSize() {
        return visibilityCache.size();
    }

    public int getCachedVisibleCount() {
        int count = 0;
        for (Boolean value : visibilityCache.values()) {
            if (Boolean.TRUE.equals(value)) {
                count++;
            }
        }
        return count;
    }

    public int getCachedHiddenCount() {
        int count = 0;
        for (Boolean value : visibilityCache.values()) {
            if (Boolean.FALSE.equals(value)) {
                count++;
            }
        }
        return count;
    }

    public MinecraftClient getClient() {
        return client;
    }
}
