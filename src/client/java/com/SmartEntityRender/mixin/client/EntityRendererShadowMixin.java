package com.SmartEntityRender.mixin.client;

import com.SmartEntityRender.config.Config;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.render.entity.state.ItemEntityRenderState;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererShadowMixin {
    @Inject(method = "getShadowRadius(Lnet/minecraft/client/render/entity/state/EntityRenderState;)F", at = @At("HEAD"), cancellable = true)
    private void ser$shadowRadius(EntityRenderState state, CallbackInfoReturnable<Float> cir) {
        Config config = Config.getInstance();
        if ((config.isEntityShadowDropsEnabled() && state instanceof ItemEntityRenderState)
                || (config.isEntityShadowMobsEnabled() && state instanceof LivingEntityRenderState)) {
            // Settings are enabled, let shadows render normally
            return;
        }
        // Settings are disabled, remove shadows
        if (state instanceof ItemEntityRenderState || state instanceof LivingEntityRenderState) {
            cir.setReturnValue(0.0F);
        }
    }

    @Inject(method = "getShadowOpacity(Lnet/minecraft/client/render/entity/state/EntityRenderState;)F", at = @At("HEAD"), cancellable = true)
    private void ser$shadowOpacity(EntityRenderState state, CallbackInfoReturnable<Float> cir) {
        Config config = Config.getInstance();
        if ((config.isEntityShadowDropsEnabled() && state instanceof ItemEntityRenderState)
                || (config.isEntityShadowMobsEnabled() && state instanceof LivingEntityRenderState)) {
            // Settings are enabled, let shadows render normally
            return;
        }
        // Settings are disabled, remove shadows
        if (state instanceof ItemEntityRenderState || state instanceof LivingEntityRenderState) {
            cir.setReturnValue(0.0F);
        }
    }
}