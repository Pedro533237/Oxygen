package com.SmartEntityRender.culling;

import net.minecraft.entity.Entity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import org.jspecify.annotations.NonNull;

/**
 * Ray Casting system for occlusion detection
 * نظام تتبع الأشعة لاكتشاف الانسداد
 */
public class RayCaster {

    /**
     * Check if entity is visible from camera using ray casting
     * فحص ما إذا كان الكيان مرئياً من الكاميرا باستخدام تتبع الأشعة
     * 
     * @param entity    الكيان المراد فحصه
     * @param cameraPos موقع الكاميرا
     * @param world     العالم
     * @return true إذا كان الكيان مرئياً
     */
    public static boolean isEntityVisible(@NonNull Entity entity, @NonNull Vec3d cameraPos, @NonNull World world) {
        // الحصول على مركز الكيان
        Box entityBox = entity.getBoundingBox();
        Vec3d entityCenter = entityBox.getCenter();

        // تتبع الشعاع من الكاميرا إلى الكيان
        RaycastContext context = new RaycastContext(
                cameraPos,
                entityCenter,
                RaycastContext.ShapeType.COLLIDER,
                RaycastContext.FluidHandling.NONE,
                entity);

        BlockHitResult result = world.raycast(context);

        // إذا لم يكن هناك اصطدام أو كان الاصطدام بعد الكيان
        if (result.getType() == HitResult.Type.MISS) {
            return true;
        }

        // فحص المسافة - إذا كان الاصطدام أبعد من الكيان
        double hitDistance = result.getPos().squaredDistanceTo(cameraPos);
        double entityDistance = entityCenter.squaredDistanceTo(cameraPos);

        return hitDistance >= entityDistance;
    }

    /**
     * Multi-point ray casting for better accuracy
     * تتبع أشعة متعددة لدقة أفضل
     * 
     * @param entity         الكيان
     * @param cameraPos      موقع الكاميرا
     * @param world          العالم
     * @param samplingPoints عدد نقاط العينة
     * @return true إذا كان أي شعاع يصل إلى الكيان
     */
    public static boolean isEntityVisibleMultiPoint(@NonNull Entity entity, @NonNull Vec3d cameraPos,
            @NonNull World world, int samplingPoints) {
        Box entityBox = entity.getBoundingBox();

        // نقاط العينة في الـ Bounding Box
        Vec3d[] testPoints = getSamplingPoints(entityBox, samplingPoints);

        // فحص كل نقطة
        for (Vec3d testPoint : testPoints) {
            RaycastContext context = new RaycastContext(
                    cameraPos,
                    testPoint,
                    RaycastContext.ShapeType.COLLIDER,
                    RaycastContext.FluidHandling.NONE,
                    entity);

            BlockHitResult result = world.raycast(context);

            // إذا وصل أي شعاع، الكيان مرئي
            if (result.getType() == HitResult.Type.MISS) {
                return true;
            }

            double hitDistance = result.getPos().squaredDistanceTo(cameraPos);
            double testDistance = testPoint.squaredDistanceTo(cameraPos);

            if (hitDistance >= testDistance) {
                return true;
            }
        }

        return false;
    }

    /**
     * Get sampling points from bounding box
     * الحصول على نقاط عينة من الـ Bounding Box
     */
    private static Vec3d[] getSamplingPoints(Box box, int count) {
        if (count <= 1) {
            return new Vec3d[] { box.getCenter() };
        }

        Vec3d[] points = new Vec3d[count];

        // المركز دائماً
        points[0] = box.getCenter();

        if (count >= 2) {
            // الزوايا
            points[1] = new Vec3d(box.minX, box.minY, box.minZ);
        }
        if (count >= 3) {
            points[2] = new Vec3d(box.maxX, box.minY, box.minZ);
        }
        if (count >= 4) {
            points[3] = new Vec3d(box.minX, box.maxY, box.minZ);
        }
        if (count >= 5) {
            points[4] = new Vec3d(box.minX, box.minY, box.maxZ);
        }
        if (count >= 6) {
            points[5] = new Vec3d(box.maxX, box.maxY, box.maxZ);
        }
        if (count >= 7) {
            points[6] = new Vec3d(box.maxX, box.minY, box.maxZ);
        }
        if (count >= 8) {
            points[7] = new Vec3d(box.minX, box.maxY, box.maxZ);
        }

        // ملء باقي النقاط عشوائياً داخل الـ Box
        for (int i = 8; i < count; i++) {
            points[i] = box.getCenter();
        }

        return points;
    }

    /**
     * Fast visibility check using simple distance
     * فحص سريع للرؤية باستخدام المسافة البسيطة
     */
    public static boolean isInRenderDistance(@NonNull Entity entity, @NonNull Vec3d cameraPos, double maxDistance) {
        Vec3d entityPos = new Vec3d(entity.getX(), entity.getY(), entity.getZ());
        double distanceSq = entityPos.squaredDistanceTo(cameraPos);
        double maxDistanceSq = maxDistance * maxDistance;
        return distanceSq <= maxDistanceSq;
    }
}
