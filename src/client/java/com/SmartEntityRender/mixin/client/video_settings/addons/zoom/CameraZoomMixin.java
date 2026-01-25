package com.SmartEntityRender.mixin.client.video_settings.addons.zoom;

import com.SmartEntityRender.video_settings.includes.addons.zoom.Zoom;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public class CameraZoomMixin {

    @Inject(method = "getFov", at = @At("RETURN"), cancellable = true)
    private void modifyFov(Camera camera, float tickDelta, boolean changingFov, CallbackInfoReturnable<Float> cir) {
        if (Zoom.isZooming()) {
            float originalFov = cir.getReturnValue();
            float zoomDivisor = Zoom.getZoomDivisor(tickDelta);

            // Divide FOV by zoom divisor (higher divisor = more zoomed in)
            float zoomedFov = originalFov / zoomDivisor;

            cir.setReturnValue(zoomedFov);
        }
    }
}
