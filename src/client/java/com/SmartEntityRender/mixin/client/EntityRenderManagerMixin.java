package com.SmartEntityRender.mixin.client;

import com.SmartEntityRender.config.Config;
import com.SmartEntityRender.culling.OcclusionCullingInstance;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.entity.EntityRenderManager;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Hooks entity rendering eligibility so SmartEntityRender can actually cull
 * entities.
 */
@Mixin(EntityRenderManager.class)
public class EntityRenderManagerMixin {

    @Inject(method = "shouldRender(Lnet/minecraft/entity/Entity;Lnet/minecraft/client/render/Frustum;DDD)Z", at = @At("HEAD"), cancellable = true)
    private void smartentityrender$shouldRender(Entity entity, Frustum frustum, double x, double y, double z,
            CallbackInfoReturnable<Boolean> cir) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.world == null) {
            return;
        }

        // Strict camera culling: never render entities outside the current camera
        // frustum.
        if (Config.getInstance().isStrictFrustumCulling() && frustum != null && entity != null
                && !frustum.isVisible(entity.getBoundingBox())) {
            cir.setReturnValue(false);
            return;
        }

        // Never cull the local player / camera.
        if (entity == client.player || entity == client.getCameraEntity()) {
            return;
        }

        // Delegate to our culling decision.
        if (!OcclusionCullingInstance.getInstance().shouldRenderEntity(entity)) {
            cir.setReturnValue(false);
        }
    }
}
