package com.SmartEntityRender.mixin.client.memory;

import com.SmartEntityRender.memory.MemoryOptimizationClient;
import net.minecraft.client.render.model.json.ModelVariant;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ModelVariant.class)
public abstract class ModelVariantMixin {
    @Shadow
    @Final
    @Mutable
    private Identifier modelId;

    @Inject(method = "<init>(Lnet/minecraft/util/Identifier;)V", at = @At("RETURN"))
    private void ser$internModelId(Identifier id, CallbackInfo ci) {
        this.modelId = MemoryOptimizationClient.internModelId(this.modelId);
    }

    @Inject(method = "<init>(Lnet/minecraft/util/Identifier;Lnet/minecraft/client/render/model/json/ModelVariant$ModelState;)V", at = @At("RETURN"))
    private void ser$internModelId(Identifier id, ModelVariant.ModelState state, CallbackInfo ci) {
        this.modelId = MemoryOptimizationClient.internModelId(this.modelId);
    }
}
