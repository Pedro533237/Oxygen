package com.SmartEntityRender.mixin.client;

import com.SmartEntityRender.debug.DebugRenderer;
import com.SmartEntityRender.video_settings.includes.Display;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.DebugHud;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import java.util.ArrayList;
import java.util.List;

/**
 * Mixin to add debug info to F3 screen
 * ميكسين لإضافة معلومات التصحيح إلى شاشة F3
 */
@Mixin(DebugHud.class)
public class DebugHudMixin {

    /**
     * Inject debug information into left debug text
     * حقن معلومات التصحيح في النص الأيسر للتصحيح
     */
    @ModifyArgs(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/DebugHud;drawText(Lnet/minecraft/client/gui/DrawContext;Ljava/util/List;Z)V"))
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void smartentityrender$onDrawDebugText(Args args) {
        boolean isLeftText = (boolean) args.get(2);
        if (!isLeftText) {
            return;
        }

        List<String> lines = (List) args.get(1);
        if (lines == null) {
            return;
        }

        try {
            List<String> mutableLines = new ArrayList<>(lines);
            DebugRenderer.getInstance().addDebugText(mutableLines);
            args.set(1, mutableLines);
        } catch (Exception e) {
            // تجاهل الأخطاء في وضع التصحيح
        }
    }
}

@Mixin(InGameHud.class)
class InGameHudFpsOverlayMixin {
    @Inject(method = "render", at = @At("TAIL"))
    private void ser$renderFpsOverlay(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        Display.FPSOverlay.render(context);
    }
}
