package com.SmartEntityRender.culling;

import com.SmartEntityRender.config.Config;
import net.minecraft.block.BlockState;
import net.minecraft.block.LeavesBlock;
import net.minecraft.block.BambooBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.ChickenEntity;
import net.minecraft.entity.passive.CowEntity;
import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.SilverfishEntity;
import net.minecraft.entity.mob.SlimeEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jspecify.annotations.NonNull;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Enhanced Density-Based Entity Culling System
 * نظام محسّن لإخفاء الكيانات بناءً على الكثافة
 * 
 * يقلل بشكل كبير من عدد الكيانات المرسومة في المناطق الكثيفة
 * مثل غابات البامبو، الغابات الكثيفة، والأغراض المتجمعة
 * 
 * تحسينات جديدة:
 * - كشف ذكي للغابات الكثيفة
 * - معالجة خاصة للبامبو
 * - دمج الأغراض المتجمعة (ItemEntity LOD)
 * - أولوية ديناميكية بناءً على نوع الكيان
 * - معالجة ذكية للحيوانات المتجمعة (الدجاج، الأبقار، الخراف)
 * - LOD للـ passive mobs في المزارع الكبيرة
 * - معالجة خاصة للـ hostile mobs المتجمعة (silverfish, slimes)
 */
public class DensityBasedCulling {

    // خريطة الكثافة - تخزن عدد الكيانات في كل خلية
    private final Map<CellPosition, CellDensityInfo> densityMap = new ConcurrentHashMap<>();

    // كشف البيئات الكثيفة (غابات، بامبو)
    private final Map<CellPosition, EnvironmentType> environmentCache = new ConcurrentHashMap<>();
    private long lastEnvironmentScan = 0;
    private static final int ENVIRONMENT_SCAN_INTERVAL = 100; // مسح البيئة كل 100 فريم

    // عداد الفريمات لتحديث الخريطة
    private long lastUpdateFrame = 0;
    private static final int UPDATE_INTERVAL = 20; // تحديث كل 20 فريم

    private final Config config;

    public DensityBasedCulling() {
        this.config = Config.getInstance();
    }

    /**
     * أنواع البيئات الكثيفة
     */
    private enum EnvironmentType {
        NORMAL, // بيئة عادية
        DENSE_FOREST, // غابة كثيفة
        BAMBOO_FOREST, // غابة بامبو
        ITEM_CLUSTER, // تجمع أغراض
        PASSIVE_MOB_FARM, // مزرعة حيوانات (دجاج، أبقار، خراف)
        HOSTILE_MOB_CLUSTER // تجمع وحوش (silverfish, slimes)
    }

    /**
     * تحديث خريطة الكثافة بناءً على الكيانات المرئية
     */
    public void updateDensityMap(@NonNull Iterable<Entity> entities, long currentFrame) {
        // تحديث كل فترة فقط لتوفير الأداء
        if (currentFrame - lastUpdateFrame < UPDATE_INTERVAL) {
            return;
        }

        lastUpdateFrame = currentFrame;

        // مسح البيانات القديمة
        densityMap.clear();

        // حساب الكثافة لكل خلية
        for (Entity entity : entities) {
            if (entity == null)
                continue;

            CellPosition cell = getCellPosition(entity);
            CellDensityInfo info = densityMap.computeIfAbsent(cell, k -> new CellDensityInfo());
            info.entityCount++;

            // تتبع أنواع الكيانات
            EntityType<?> type = entity.getType();
            info.entityTypes.merge(type, 1, Integer::sum);

            // تتبع الأغراض الساقطة
            if (entity instanceof ItemEntity) {
                info.itemCount++;
            }

            // تتبع الحيوانات الأليفة
            if (entity instanceof AnimalEntity) {
                info.animalCount++;
                if (entity instanceof ChickenEntity)
                    info.chickenCount++;
                else if (entity instanceof CowEntity)
                    info.cowCount++;
                else if (entity instanceof SheepEntity)
                    info.sheepCount++;
                else if (entity instanceof PigEntity)
                    info.pigCount++;
            }

            // تتبع الوحوش العدائية
            if (entity instanceof HostileEntity) {
                info.hostileCount++;
                if (entity instanceof SilverfishEntity)
                    info.silverfishCount++;
                else if (entity instanceof SlimeEntity)
                    info.slimeCount++;
            }
        }

        // مسح البيئة دورياً
        if (currentFrame - lastEnvironmentScan > ENVIRONMENT_SCAN_INTERVAL) {
            lastEnvironmentScan = currentFrame;
            environmentCache.clear();
        }
    }

    /**
     * كشف نوع البيئة للخلية
     */
    private EnvironmentType detectEnvironmentType(@NonNull Entity entity) {
        CellPosition cell = getCellPosition(entity);

        // التحقق من الـ cache أولاً
        EnvironmentType cached = environmentCache.get(cell);
        if (cached != null) {
            return cached;
        }

        // الحصول على العالم من MinecraftClient
        net.minecraft.client.MinecraftClient client = net.minecraft.client.MinecraftClient.getInstance();
        World world = client != null ? client.world : null;
        if (world == null) {
            return EnvironmentType.NORMAL;
        }

        BlockPos entityPos = entity.getBlockPos();
        int leavesCount = 0;
        int bambooCount = 0;
        int totalBlocks = 0;

        // مسح المنطقة المحيطة (3x3x3 خلايا)
        int radius = 8;
        for (int x = -radius; x <= radius; x += 4) {
            for (int y = -radius; y <= radius; y += 4) {
                for (int z = -radius; z <= radius; z += 4) {
                    BlockPos checkPos = entityPos.add(x, y, z);
                    BlockState state = world.getBlockState(checkPos);

                    if (state.getBlock() instanceof LeavesBlock) {
                        leavesCount++;
                    } else if (state.getBlock() instanceof BambooBlock) {
                        bambooCount++;
                    }
                    totalBlocks++;
                }
            }
        }

        // تحديد نوع البيئة
        EnvironmentType type = EnvironmentType.NORMAL;

        // فحص الكثافة أولاً
        CellDensityInfo info = densityMap.get(cell);

        // فحص مزارع الحيوانات (أولوية عالية)
        if (info != null) {
            int passiveMobTotal = info.chickenCount + info.cowCount + info.sheepCount + info.pigCount;
            if (passiveMobTotal > 15) {
                type = EnvironmentType.PASSIVE_MOB_FARM;
                environmentCache.put(cell, type);
                return type;
            }

            // فحص تجمع الوحوش العدائية
            if (info.silverfishCount > 10 || info.slimeCount > 8 || info.hostileCount > 12) {
                type = EnvironmentType.HOSTILE_MOB_CLUSTER;
                environmentCache.put(cell, type);
                return type;
            }
        }

        // فحص البيئة الطبيعية
        if (bambooCount > totalBlocks * 0.15) {
            type = EnvironmentType.BAMBOO_FOREST;
        } else if (leavesCount > totalBlocks * 0.25) {
            type = EnvironmentType.DENSE_FOREST;
        }

        // فحص تجمع الأغراض
        if (info != null && info.itemCount > 15) {
            type = EnvironmentType.ITEM_CLUSTER;
        }

        environmentCache.put(cell, type);
        return type;
    }

    /**
     * تحديد ما إذا كان يجب رسم هذا الكيان بناءً على الكثافة
     * Enhanced with environment-aware rendering
     * 
     * @return true إذا كان يجب رسم الكيان
     */
    public boolean shouldRenderInDenseArea(@NonNull Entity entity, @NonNull Vec3d cameraPos) {
        if (!config.isDensityCullingEnabled()) {
            return true; // النظام معطل - ارسم كل شيء
        }

        CellPosition cell = getCellPosition(entity);
        CellDensityInfo info = densityMap.get(cell);

        int maxEntities = config.getMaxEntitiesPerCell();
        if (info == null || info.entityCount <= maxEntities) {
            return true; // كثافة منخفضة - ارسم كل شيء
        }

        // كشف نوع البيئة
        EnvironmentType envType = detectEnvironmentType(entity);

        // معالجة خاصة حسب نوع الكيان والبيئة
        if (entity instanceof ItemEntity) {
            return shouldRenderItemEntity((ItemEntity) entity, cameraPos, info, envType);
        }

        if (entity instanceof ArmorStandEntity) {
            return shouldRenderArmorStand((ArmorStandEntity) entity, cameraPos, info);
        }

        // معالجة الحيوانات الأليفة (دجاج، أبقار، خراف)
        if (entity instanceof AnimalEntity) {
            return shouldRenderPassiveMob((AnimalEntity) entity, cameraPos, info, envType);
        }

        // معالجة الـ slimes (منفصلة لأنها ليست HostileEntity)
        if (entity instanceof SlimeEntity) {
            return shouldRenderSlime((SlimeEntity) entity, cameraPos, info, envType);
        }

        // معالجة الوحوش العدائية (silverfish وغيرها)
        if (entity instanceof HostileEntity) {
            return shouldRenderHostileMob((HostileEntity) entity, cameraPos, info, envType);
        }

        // حساب مستوى الكثافة
        float densityLevel = (float) info.entityCount / maxEntities;

        // تعديل نسبة الرسم بناءً على نوع البيئة
        float renderRatio = calculateRenderRatio(densityLevel, envType);

        // استخدام hash ثابت للكيان لضمان اختيار متسق
        int entityHash = entity.getId();
        float chance = (entityHash % 100) / 100.0f;

        // أولوية للكيانات الأقرب
        double distance = entity.squaredDistanceTo(cameraPos);
        renderRatio = adjustRatioByDistance(renderRatio, distance);

        return chance < renderRatio;
    }

    /**
     * معالجة ذكية للأغراض الساقطة (ItemEntity)
     */
    private boolean shouldRenderItemEntity(@NonNull ItemEntity item, @NonNull Vec3d cameraPos,
            @NonNull CellDensityInfo info, @NonNull EnvironmentType envType) {
        // إذا كان نظام LOD للأغراض معطل، استخدم النظام الافتراضي
        if (!config.isItemEntityLOD()) {
            return true;
        }

        double distance = item.squaredDistanceTo(cameraPos);

        // الأغراض القريبة جداً يجب رسمها دائماً
        if (distance < 8 * 8) {
            return true;
        }

        // في تجمعات الأغراض الكبيرة، استخدم LOD عدواني
        if (config.isAggressiveItemClustering() &&
                (envType == EnvironmentType.ITEM_CLUSTER || info.itemCount > 20)) {
            // رسم 30% فقط من الأغراض البعيدة في التجمعات
            if (distance > 16 * 16) {
                return (item.getId() % 10) < 3; // 30% فقط
            }
            // رسم 60% من الأغراض المتوسطة البعد
            if (distance > 12 * 12) {
                return (item.getId() % 10) < 6; // 60%
            }
        }

        // استخدام النظام الافتراضي
        float ratio = info.itemCount > 30 ? 0.4f : 0.7f;
        return (item.getId() % 100) / 100.0f < ratio;
    }

    /**
     * معالجة ذكية للـ armor stands
     */
    private boolean shouldRenderArmorStand(@NonNull ArmorStandEntity armorStand, @NonNull Vec3d cameraPos,
            @NonNull CellDensityInfo info) {
        double distance = armorStand.squaredDistanceTo(cameraPos);

        // armor stands قريبة دائماً مرئية
        if (distance < 16 * 16) {
            return true;
        }

        // في المناطق الكثيفة، قلل عدد armor stands البعيدة
        if (info.entityCount > 30 && distance > 24 * 24) {
            return (armorStand.getId() % 5) < 2; // 40% فقط
        }

        return true;
    }

    /**
     * معالجة ذكية للحيوانات الأليفة (Passive Mobs)
     * دجاج، أبقار، خراف، خنازير في المزارع الكبيرة
     */
    private boolean shouldRenderPassiveMob(@NonNull AnimalEntity animal, @NonNull Vec3d cameraPos,
            @NonNull CellDensityInfo info, @NonNull EnvironmentType envType) {

        // إذا كان النظام معطل، ارسم كل شيء
        if (!config.isPassiveMobCulling()) {
            return true;
        }

        double distance = animal.squaredDistanceTo(cameraPos);

        // الحيوانات القريبة جداً دائماً مرئية (ضمن 12 بلوك)
        if (distance < 12 * 12) {
            return true;
        }

        // في مزارع الحيوانات الكبيرة
        if (envType == EnvironmentType.PASSIVE_MOB_FARM) {
            int totalPassive = info.chickenCount + info.cowCount + info.sheepCount + info.pigCount;

            // مزارع ضخمة جداً (30+ حيوان)
            if (totalPassive > 30) {
                if (distance > 24 * 24) {
                    return (animal.getId() % 10) < 2; // 20% فقط للبعيدة جداً
                } else if (distance > 16 * 16) {
                    return (animal.getId() % 10) < 4; // 40% للمتوسطة
                } else {
                    return (animal.getId() % 10) < 6; // 60% للقريبة
                }
            }

            // مزارع كبيرة (15-30 حيوان)
            if (totalPassive > 15) {
                if (distance > 20 * 20) {
                    return (animal.getId() % 5) < 2; // 40% فقط
                } else if (distance > 14 * 14) {
                    return (animal.getId() % 5) < 3; // 60%
                }
            }
        }

        // في الغابات الكثيفة مع حيوانات كثيرة
        if (envType == EnvironmentType.DENSE_FOREST && info.animalCount > 10) {
            if (distance > 32 * 32) {
                return (animal.getId() % 3) == 0; // 33% فقط
            }
        }

        return true;
    }

    /**
     * معالجة ذكية للوحوش العدائية (Hostile Mobs)
     * silverfish, slimes وغيرها من الوحوش المتجمعة
     */
    private boolean shouldRenderHostileMob(@NonNull HostileEntity hostile, @NonNull Vec3d cameraPos,
            @NonNull CellDensityInfo info, @NonNull EnvironmentType envType) {

        // إذا كان النظام معطل، ارسم كل شيء
        if (!config.isHostileMobCulling()) {
            return true;
        }

        double distance = hostile.squaredDistanceTo(cameraPos);

        // الوحوش القريبة جداً دائماً مرئية (للأمان - ضمن 10 بلوك)
        if (distance < 10 * 10) {
            return true;
        }

        // معالجة خاصة للـ silverfish (تتجمع بأعداد كبيرة جداً)
        if (hostile instanceof SilverfishEntity && envType == EnvironmentType.HOSTILE_MOB_CLUSTER) {
            if (info.silverfishCount > 20) {
                // تجمع ضخم جداً
                if (distance > 16 * 16) {
                    return (hostile.getId() % 10) < 2; // 20% فقط
                } else if (distance > 12 * 12) {
                    return (hostile.getId() % 10) < 4; // 40%
                }
            } else if (info.silverfishCount > 10) {
                // تجمع كبير
                if (distance > 16 * 16) {
                    return (hostile.getId() % 5) < 2; // 40%
                }
            }
        }

        // للوحوش الأخرى في تجمعات
        if (envType == EnvironmentType.HOSTILE_MOB_CLUSTER && info.hostileCount > 15) {
            if (distance > 24 * 24) {
                return (hostile.getId() % 4) < 2; // 50%
            }
        }

        return true;
    }

    /**
     * معالجة خاصة للـ slimes (تُستدعى بشكل منفصل لأن Slime ليست HostileEntity)
     */
    private boolean shouldRenderSlime(@NonNull SlimeEntity slime, @NonNull Vec3d cameraPos,
            @NonNull CellDensityInfo info, @NonNull EnvironmentType envType) {

        if (!config.isHostileMobCulling()) {
            return true;
        }

        double distance = slime.squaredDistanceTo(cameraPos);

        // القريبة جداً دائماً مرئية
        if (distance < 10 * 10) {
            return true;
        }

        // معالجة خاصة للـ slimes (تنقسم وتتكاثر)
        if (envType == EnvironmentType.HOSTILE_MOB_CLUSTER && info.slimeCount > 12) {
            // تجمع كبير من slimes
            if (distance > 20 * 20) {
                return (slime.getId() % 5) < 2; // 40% فقط
            } else if (distance > 14 * 14) {
                return (slime.getId() % 5) < 3; // 60%
            }
        }

        return true;
    }

    /**
     * حساب نسبة الرسم بناءً على الكثافة ونوع البيئة
     */
    private float calculateRenderRatio(float densityLevel, @NonNull EnvironmentType envType) {
        float baseRatio;

        if (densityLevel > 4.0f) {
            // كثافة عالية جداً
            baseRatio = config.getHighDensityRenderRatio();
        } else if (densityLevel > 2.0f) {
            // كثافة متوسطة
            baseRatio = config.getMediumDensityRenderRatio();
        } else {
            // كثافة طفيفة
            baseRatio = config.getLowDensityRenderRatio();
        }

        // تعديل حسب نوع البيئة
        switch (envType) {
            case BAMBOO_FOREST:
                // غابات البامبو كثيفة جداً - نقلل أكثر
                return config.isSmartBambooCulling() ? baseRatio * 0.7f : baseRatio;
            case DENSE_FOREST:
                // الغابات الكثيفة - نقلل قليلاً
                return config.isSmartForestCulling() ? baseRatio * 0.85f : baseRatio;
            case ITEM_CLUSTER:
                // تجمع الأغراض - نقلل الأغراض البعيدة
                return config.isAggressiveItemClustering() ? baseRatio * 0.75f : baseRatio;
            case PASSIVE_MOB_FARM:
                // مزارع الحيوانات - نقلل بشكل متوسط
                return config.isPassiveMobCulling() ? baseRatio * 0.8f : baseRatio;
            case HOSTILE_MOB_CLUSTER:
                // تجمعات الوحوش - نقلل أكثر للأداء
                return config.isHostileMobCulling() ? baseRatio * 0.75f : baseRatio;
            default:
                return baseRatio;
        }
    }

    /**
     * تعديل نسبة الرسم حسب المسافة
     */
    private float adjustRatioByDistance(float baseRatio, double distanceSq) {
        if (distanceSq < 16 * 16) {
            // قريب جداً - زيادة فرصة الرسم
            return Math.min(1.0f, baseRatio + 0.3f);
        } else if (distanceSq < 32 * 32) {
            // متوسط البعد
            return Math.min(1.0f, baseRatio + 0.15f);
        } else if (distanceSq > 64 * 64) {
            // بعيد جداً - تقليل فرصة الرسم
            return baseRatio * 0.8f;
        }
        return baseRatio;
    }

    /**
     * تحسين خاص لكيانات البامبو
     */
    public boolean shouldRenderBamboo(@NonNull Entity entity, @NonNull Vec3d cameraPos) {
        CellPosition cell = getCellPosition(entity);
        CellDensityInfo info = densityMap.get(cell);

        if (info == null) {
            return true;
        }

        // في غابات البامبو الكثيفة، نرسم فقط 20% من البامبو
        if (info.entityCount > 50) {
            int entityHash = entity.getId();
            return (entityHash % 5) == 0; // فقط 1 من كل 5
        }

        return shouldRenderInDenseArea(entity, cameraPos);
    }

    /**
     * الحصول على موقع الخلية للكيان
     */
    private CellPosition getCellPosition(@NonNull Entity entity) {
        BlockPos pos = entity.getBlockPos();
        int cellSize = config.getDensityCellSize();
        return new CellPosition(
                Math.floorDiv(pos.getX(), cellSize),
                Math.floorDiv(pos.getY(), cellSize),
                Math.floorDiv(pos.getZ(), cellSize));
    }

    /**
     * الحصول على معلومات الكثافة للـ debug
     */
    public int getDensityAt(BlockPos pos) {
        int cellSize = config.getDensityCellSize();
        CellPosition cell = new CellPosition(
                Math.floorDiv(pos.getX(), cellSize),
                Math.floorDiv(pos.getY(), cellSize),
                Math.floorDiv(pos.getZ(), cellSize));

        CellDensityInfo info = densityMap.get(cell);
        return info != null ? info.entityCount : 0;
    }

    /**
     * الحصول على إجمالي عدد الخلايا المكتشفة
     */
    public int getTotalCells() {
        return densityMap.size();
    }

    /**
     * مسح جميع البيانات
     */
    public void clear() {
        densityMap.clear();
        lastUpdateFrame = 0;
    }

    /**
     * موقع الخلية في الشبكة الثلاثية الأبعاد
     */
    private static class CellPosition {
        final int x, y, z;

        CellPosition(int x, int y, int z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof CellPosition other))
                return false;
            return x == other.x && y == other.y && z == other.z;
        }

        @Override
        public int hashCode() {
            return x * 31 * 31 + y * 31 + z;
        }
    }

    /**
     * معلومات كثافة الخلية
     * Enhanced with detailed entity type tracking including specific mob types
     */
    private static class CellDensityInfo {
        int entityCount = 0;
        int itemCount = 0; // عدد الأغراض الساقطة
        int animalCount = 0; // عدد الحيوانات الكلي

        // تفاصيل الحيوانات الأليفة
        int chickenCount = 0; // عدد الدجاج
        int cowCount = 0; // عدد الأبقار
        int sheepCount = 0; // عدد الخراف
        int pigCount = 0; // عدد الخنازير

        // تفاصيل الوحوش العدائية
        int hostileCount = 0; // عدد الوحوش الكلي
        int silverfishCount = 0; // عدد الـ silverfish
        int slimeCount = 0; // عدد الـ slimes

        Map<EntityType<?>, Integer> entityTypes = new HashMap<>();
    }
}
