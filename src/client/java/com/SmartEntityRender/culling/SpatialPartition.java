package com.SmartEntityRender.culling;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.ChunkPos;
import org.jspecify.annotations.NonNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Spatial Partitioning system using chunks
 * نظام التقسيم المكاني باستخدام الـ Chunks
 */
public class SpatialPartition {
    
    // خريطة الكيانات حسب الـ Chunk
    private final Map<ChunkPos, Set<Integer>> chunkEntities;
    
    // خريطة الكيانات إلى مواقع الـ Chunk
    private final Map<Integer, ChunkPos> entityChunks;
    
    public SpatialPartition() {
        this.chunkEntities = new ConcurrentHashMap<>();
        this.entityChunks = new ConcurrentHashMap<>();
    }
    
    /**
     * Add entity to spatial partition
     * إضافة كيان إلى التقسيم المكاني
     */
    public void addEntity(@NonNull Entity entity) {
        ChunkPos chunkPos = entity.getChunkPos();
        int entityId = entity.getId();
        
        // إزالة من الموقع القديم إذا كان موجوداً
        removeEntity(entityId);
        
        // إضافة إلى الموقع الجديد
        chunkEntities.computeIfAbsent(chunkPos, k -> ConcurrentHashMap.newKeySet())
                     .add(entityId);
        entityChunks.put(entityId, chunkPos);
    }
    
    /**
     * Remove entity from spatial partition
     * إزالة كيان من التقسيم المكاني
     */
    public void removeEntity(int entityId) {
        ChunkPos oldChunk = entityChunks.remove(entityId);
        if (oldChunk != null) {
            Set<Integer> entities = chunkEntities.get(oldChunk);
            if (entities != null) {
                entities.remove(entityId);
                // إزالة الـ Chunk إذا كان فارغاً
                if (entities.isEmpty()) {
                    chunkEntities.remove(oldChunk);
                }
            }
        }
    }
    
    /**
     * Update entity position in spatial partition
     * تحديث موقع الكيان في التقسيم المكاني
     */
    public void updateEntity(@NonNull Entity entity) {
        int entityId = entity.getId();
        ChunkPos newChunk = entity.getChunkPos();
        ChunkPos oldChunk = entityChunks.get(entityId);
        
        // إذا تغير الـ Chunk
        if (oldChunk == null || !oldChunk.equals(newChunk)) {
            addEntity(entity);
        }
    }
    
    /**
     * Get entities in chunk
     * الحصول على الكيانات في Chunk
     */
    public Set<Integer> getEntitiesInChunk(@NonNull ChunkPos chunkPos) {
        Set<Integer> entities = chunkEntities.get(chunkPos);
        return entities != null ? new HashSet<>(entities) : Collections.emptySet();
    }
    
    /**
     * Get entities in radius (multiple chunks)
     * الحصول على الكيانات في نطاق (عدة Chunks)
     */
    public Set<Integer> getEntitiesInRadius(@NonNull ChunkPos center, int chunkRadius) {
        Set<Integer> result = new HashSet<>();
        
        int centerX = center.x;
        int centerZ = center.z;
        
        for (int dx = -chunkRadius; dx <= chunkRadius; dx++) {
            for (int dz = -chunkRadius; dz <= chunkRadius; dz++) {
                ChunkPos pos = new ChunkPos(centerX + dx, centerZ + dz);
                Set<Integer> entities = chunkEntities.get(pos);
                if (entities != null) {
                    result.addAll(entities);
                }
            }
        }
        
        return result;
    }
    
    /**
     * Get entity count in chunk
     * الحصول على عدد الكيانات في Chunk
     */
    public int getEntityCountInChunk(@NonNull ChunkPos chunkPos) {
        Set<Integer> entities = chunkEntities.get(chunkPos);
        return entities != null ? entities.size() : 0;
    }
    
    /**
     * Clear all data
     * مسح جميع البيانات
     */
    public void clear() {
        chunkEntities.clear();
        entityChunks.clear();
    }
    
    /**
     * Get total entity count
     * الحصول على العدد الكلي للكيانات
     */
    public int getTotalEntityCount() {
        return entityChunks.size();
    }
    
    /**
     * Get chunk count
     * الحصول على عدد الـ Chunks
     */
    public int getChunkCount() {
        return chunkEntities.size();
    }
    
    /**
     * Get all loaded chunks
     * الحصول على جميع الـ Chunks المحملة
     */
    public Set<ChunkPos> getLoadedChunks() {
        return new HashSet<>(chunkEntities.keySet());
    }
}
