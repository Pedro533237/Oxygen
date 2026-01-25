package com.SmartEntityRender.config;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import org.jspecify.annotations.NonNull;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Whitelist Manager - مدير القوائم البيضاء
 * يدير ثلاث قوائم بيضاء: Block Entities, Entities, Tick Culling
 */
public class WhitelistManager {
    private static WhitelistManager INSTANCE;
    
    // Block Entity Whitelist - استثناء Block Entities التي ترسم خارج حدودها
    private final Set<String> blockEntityWhitelist;
    
    // Entity Whitelist - استثناء الكيانات الخاصة
    private final Set<String> entityWhitelist;
    
    // Tick Culling Whitelist - استثناء من تحسين Tick
    private final Set<String> tickCullingWhitelist;
    
    // Dynamic Predicates - محددات ديناميكية
    private final Set<Predicate<Entity>> entityPredicates;
    private final Set<Predicate<BlockEntity>> blockEntityPredicates;
    
    private WhitelistManager() {
        this.blockEntityWhitelist = new HashSet<>();
        this.entityWhitelist = new HashSet<>();
        this.tickCullingWhitelist = new HashSet<>();
        this.entityPredicates = new HashSet<>();
        this.blockEntityPredicates = new HashSet<>();
        
        initializeWhitelists();
        initializeDynamicRules();
    }
    
    public static WhitelistManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new WhitelistManager();
        }
        return INSTANCE;
    }
    
    /**
     * Initialize all whitelists with default values
     * تهيئة جميع القوائم بالقيم الافتراضية
     */
    private void initializeWhitelists() {
        // Block Entity Whitelist
        blockEntityWhitelist.add("minecraft:beacon");
        blockEntityWhitelist.add("create:rope_pulley");
        blockEntityWhitelist.add("create:hose_pulley");
        blockEntityWhitelist.add("botania:magic_missile");
        blockEntityWhitelist.add("create:mechanical_arm");
        blockEntityWhitelist.add("create:gantry_carriage");
        blockEntityWhitelist.add("create:linear_chassis");
        blockEntityWhitelist.add("create:radial_chassis");
        blockEntityWhitelist.add("create:sticker");
        blockEntityWhitelist.add("immersiveengineering:watermill");
        blockEntityWhitelist.add("immersiveengineering:windmill");
        
        // Entity Whitelist
        entityWhitelist.add("botania:mana_burst");
        entityWhitelist.add("drg_flares:drg_flares");
        entityWhitelist.add("quark:soul_bead");
        entityWhitelist.add("minecraft:ender_pearl");
        entityWhitelist.add("minecraft:eye_of_ender");
        
        // Tick Culling Whitelist
        tickCullingWhitelist.add("minecraft:firework_rocket");
        tickCullingWhitelist.add("minecraft:boat");
        tickCullingWhitelist.add("minecraft:oak_boat");
        tickCullingWhitelist.add("minecraft:spruce_boat");
        tickCullingWhitelist.add("minecraft:birch_boat");
        tickCullingWhitelist.add("minecraft:jungle_boat");
        tickCullingWhitelist.add("minecraft:acacia_boat");
        tickCullingWhitelist.add("minecraft:dark_oak_boat");
        tickCullingWhitelist.add("minecraft:mangrove_boat");
        tickCullingWhitelist.add("minecraft:cherry_boat");
        tickCullingWhitelist.add("minecraft:bamboo_raft");
        tickCullingWhitelist.add("minecraft:oak_chest_boat");
        tickCullingWhitelist.add("minecraft:spruce_chest_boat");
        tickCullingWhitelist.add("minecraft:birch_chest_boat");
        tickCullingWhitelist.add("minecraft:jungle_chest_boat");
        tickCullingWhitelist.add("minecraft:acacia_chest_boat");
        tickCullingWhitelist.add("minecraft:dark_oak_chest_boat");
        tickCullingWhitelist.add("minecraft:mangrove_chest_boat");
        tickCullingWhitelist.add("minecraft:cherry_chest_boat");
        tickCullingWhitelist.add("minecraft:bamboo_chest_raft");
        tickCullingWhitelist.add("create:contraption");
        tickCullingWhitelist.add("create:carriage_contraption");
        tickCullingWhitelist.add("create:mounted_contraption");
    }
    
    /**
     * Check if block entity is whitelisted
     * فحص ما إذا كان Block Entity في القائمة البيضاء
     */
    public boolean isBlockEntityWhitelisted(String id) {
        return blockEntityWhitelist.contains(id);
    }
    
    /**
     * Check if entity is whitelisted
     * فحص ما إذا كان Entity في القائمة البيضاء
     */
    public boolean isEntityWhitelisted(String id) {
        return entityWhitelist.contains(id);
    }
    
    /**
     * Check if entity is whitelisted for tick culling
     * فحص ما إذا كان Entity في قائمة Tick Culling البيضاء
     */
    public boolean isTickCullingWhitelisted(String id) {
        return tickCullingWhitelist.contains(id);
    }
    
    /**
     * Add to block entity whitelist
     */
    public void addBlockEntityWhitelist(String id) {
        blockEntityWhitelist.add(id);
    }
    
    /**
     * Add to entity whitelist
     */
    public void addEntityWhitelist(String id) {
        entityWhitelist.add(id);
    }
    
    /**
     * Add to tick culling whitelist
     */
    public void addTickCullingWhitelist(String id) {
        tickCullingWhitelist.add(id);
    }
    
    /**
     * Remove from block entity whitelist
     */
    public void removeBlockEntityWhitelist(String id) {
        blockEntityWhitelist.remove(id);
    }
    
    /**
     * Remove from entity whitelist
     */
    public void removeEntityWhitelist(String id) {
        entityWhitelist.remove(id);
    }
    
    /**
     * Remove from tick culling whitelist
     */
    public void removeTickCullingWhitelist(String id) {
        tickCullingWhitelist.remove(id);
    }
    
    /**
     * Get all block entity whitelist entries
     */
    public Set<String> getBlockEntityWhitelist() {
        return new HashSet<>(blockEntityWhitelist);
    }
    
    /**
     * Get all entity whitelist entries
     */
    public Set<String> getEntityWhitelist() {
        return new HashSet<>(entityWhitelist);
    }
    
    /**
     * Get all tick culling whitelist entries
     */
    public Set<String> getTickCullingWhitelist() {
        return new HashSet<>(tickCullingWhitelist);
    }
    
    /**
     * Initialize dynamic whitelisting rules
     * تهيئة قواعد القوائم البيضاء الديناميكية
     */
    private void initializeDynamicRules() {
        // مثال: استثناء الكيانات الصغيرة جداً
        entityPredicates.add(entity -> {
            double width = entity.getBoundingBox().getLengthX();
            double height = entity.getBoundingBox().getLengthY();
            return width < 0.1 && height < 0.1;
        });
        
        // مثال: استثناء الكيانات السريعة جداً
        entityPredicates.add(entity -> {
            double velocity = entity.getVelocity().length();
            return velocity > 2.0;
        });
        
        // مثال: استثناء الكيانات المشتعلة
        entityPredicates.add(Entity::isOnFire);
    }
    
    /**
     * Add dynamic entity predicate
     * إضافة محدد ديناميكي للكيانات
     */
    public void addEntityPredicate(@NonNull Predicate<Entity> predicate) {
        entityPredicates.add(predicate);
    }
    
    /**
     * Add dynamic block entity predicate
     * إضافة محدد ديناميكي للكيانات الكتلية
     */
    public void addBlockEntityPredicate(@NonNull Predicate<BlockEntity> predicate) {
        blockEntityPredicates.add(predicate);
    }
    
    /**
     * Check if entity matches any dynamic predicate
     * فحص ما إذا كان الكيان يطابق أي محدد ديناميكي
     */
    public boolean matchesEntityPredicate(@NonNull Entity entity) {
        return entityPredicates.stream().anyMatch(predicate -> {
            try {
                return predicate.test(entity);
            } catch (Exception e) {
                return false;
            }
        });
    }
    
    /**
     * Check if block entity matches any dynamic predicate
     * فحص ما إذا كان الكيان الكتلي يطابق أي محدد ديناميكي
     */
    public boolean matchesBlockEntityPredicate(@NonNull BlockEntity blockEntity) {
        return blockEntityPredicates.stream().anyMatch(predicate -> {
            try {
                return predicate.test(blockEntity);
            } catch (Exception e) {
                return false;
            }
        });
    }
    
    /**
     * Clear all dynamic predicates
     * مسح جميع المحددات الديناميكية
     */
    public void clearDynamicPredicates() {
        entityPredicates.clear();
        blockEntityPredicates.clear();
        initializeDynamicRules();
    }
}
