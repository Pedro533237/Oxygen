package com.SmartEntityRender.mixin.client.memory;

import com.SmartEntityRender.memory.MemoryOptimizationClient;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.render.model.BakedQuadFactory;
import net.minecraft.client.render.model.Baker;
import net.minecraft.client.render.model.ModelBakeSettings;
import net.minecraft.client.render.model.json.ModelElementFace;
import net.minecraft.client.render.model.json.ModelElementRotation;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.math.Direction;
import org.joml.Vector3fc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BakedQuadFactory.class)
public abstract class BakedQuadFactoryMixin {
    @Inject(method = "bake", at = @At("RETURN"), cancellable = true)
    private static void ser$internQuad(Baker.Vec3fInterner interner,
            Vector3fc from,
            Vector3fc to,
            ModelElementFace face,
            Sprite sprite,
            Direction direction,
            ModelBakeSettings settings,
            ModelElementRotation rotation,
            boolean shade,
            int lightEmission,
            CallbackInfoReturnable<BakedQuad> cir) {
        cir.setReturnValue(MemoryOptimizationClient.internBakedQuad(cir.getReturnValue()));
    }
}
