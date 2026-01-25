package com.SmartEntityRender.memory;

import com.SmartEntityRender.config.Config;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;

import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Cullshape Cache System - نظام تخزين أشكال القص
 * يخزن نتائج getCullingFace() لتجنب إعادة الحساب
 */
public final class CullshapeCache {
    private static final Map<BlockState, Map<Direction, VoxelShape>> CACHE = new WeakHashMap<>();

    private static final Object LOCK = new Object();

    private CullshapeCache() {
    }

    /**
     * Get cached cullshape or compute and cache it
     * الحصول على cullshape من الذاكرة أو حسابه وتخزينه
     */
    public static VoxelShape getCachedCullshape(BlockState state, Direction direction) {
        if (!Config.getInstance().isModelCullshapeOptimization() || state == null || direction == null) {
            return state != null ? state.getCullingFace(direction) : null;
        }

        synchronized (LOCK) {
            Map<Direction, VoxelShape> directionMap = CACHE.get(state);
            if (directionMap != null) {
                VoxelShape cached = directionMap.get(direction);
                if (cached != null) {
                    return cached;
                }
            }
        }

        // حساب القيمة
        VoxelShape result = state.getCullingFace(direction);

        // تخزين في الذاكرة
        if (result != null) {
            synchronized (LOCK) {
                Map<Direction, VoxelShape> directionMap = CACHE.computeIfAbsent(
                        state, k -> new ConcurrentHashMap<>());
                directionMap.putIfAbsent(direction, result);
            }
        }

        return result;
    }

    /**
     * Clear the cache
     * مسح الذاكرة المؤقتة
     */
    public static void clearCache() {
        synchronized (LOCK) {
            CACHE.clear();
        }
    }

    /**
     * Get cache size (for debugging)
     * الحصول على حجم الذاكرة المؤقتة
     */
    public static int getCacheSize() {
        synchronized (LOCK) {
            return CACHE.size();
        }
    }
}
