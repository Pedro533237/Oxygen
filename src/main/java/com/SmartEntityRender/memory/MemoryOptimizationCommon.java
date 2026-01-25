package com.SmartEntityRender.memory;

import com.SmartEntityRender.config.Config;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import net.minecraft.state.property.Property;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.shape.VoxelShape;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;

public final class MemoryOptimizationCommon {
    private static final Map<Reference2ObjectArrayMap<Property<?>, Comparable<?>>, Reference2ObjectArrayMap<Property<?>, Comparable<?>>> PROPERTY_MAP_CACHE = Collections
            .synchronizedMap(new WeakHashMap<>());
    private static final Map<Map<?, ?>, Map<?, ?>> WITH_MAP_CACHE = Collections.synchronizedMap(new WeakHashMap<>());
    private static final Map<VoxelShape, VoxelShape> SHAPE_CACHE = Collections.synchronizedMap(new WeakHashMap<>());
    private static final Map<CrashKey, CrashException> LOCK_CRASH_CACHE = Collections
            .synchronizedMap(new WeakHashMap<>());

    private MemoryOptimizationCommon() {
    }

    public static boolean isSystemEnabled() {
        return Config.getInstance().isMemorySystemEnabled();
    }

    public static Reference2ObjectArrayMap<Property<?>, Comparable<?>> internPropertyMap(
            Reference2ObjectArrayMap<Property<?>, Comparable<?>> map) {
        if (!isSystemEnabled() || !Config.getInstance().isMemoryCompactBlockStateEnabled() || map == null) {
            return map;
        }
        synchronized (PROPERTY_MAP_CACHE) {
            Reference2ObjectArrayMap<Property<?>, Comparable<?>> cached = PROPERTY_MAP_CACHE.get(map);
            if (cached != null) {
                return cached;
            }
            PROPERTY_MAP_CACHE.put(map, map);
            return map;
        }
    }

    @SuppressWarnings("unchecked")
    public static <S> Map<Property<?>, S[]> internWithMap(Map<Property<?>, S[]> map) {
        if (!isSystemEnabled() || !Config.getInstance().isMemoryFastMapEnabled() || map == null) {
            return map;
        }
        synchronized (WITH_MAP_CACHE) {
            Map<Property<?>, S[]> cached = (Map<Property<?>, S[]>) WITH_MAP_CACHE.get(map);
            if (cached != null) {
                return cached;
            }
            WITH_MAP_CACHE.put(map, map);
            return map;
        }
    }

    public static VoxelShape internVoxelShape(VoxelShape shape) {
        if (!isSystemEnabled() || !Config.getInstance().isMemoryShapeCacheDedupEnabled() || shape == null) {
            return shape;
        }
        synchronized (SHAPE_CACHE) {
            VoxelShape cached = SHAPE_CACHE.get(shape);
            if (cached != null) {
                return cached;
            }
            SHAPE_CACHE.put(shape, shape);
            return shape;
        }
    }

    public static CrashException internLockCrash(String name, Thread thread, CrashException crash) {
        if (!isSystemEnabled() || !Config.getInstance().isMemoryThreadingDetectorOptimizedEnabled() || crash == null) {
            return crash;
        }
        CrashKey key = new CrashKey(name, thread);
        synchronized (LOCK_CRASH_CACHE) {
            CrashException cached = LOCK_CRASH_CACHE.get(key);
            if (cached != null) {
                return cached;
            }
            LOCK_CRASH_CACHE.put(key, crash);
            return crash;
        }
    }

    private static final class CrashKey {
        private final String name;
        private final long threadId;
        private final String threadName;

        private CrashKey(String name, Thread thread) {
            this.name = name != null ? name : "";
            if (thread != null) {
                this.threadId = thread.threadId();
                this.threadName = thread.getName();
            } else {
                this.threadId = -1L;
                this.threadName = "";
            }
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof CrashKey that)) {
                return false;
            }
            return threadId == that.threadId
                    && Objects.equals(name, that.name)
                    && Objects.equals(threadName, that.threadName);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, threadId, threadName);
        }
    }
}
