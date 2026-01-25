package com.SmartEntityRender.mixin.memory;

import com.SmartEntityRender.memory.MemoryOptimizationCommon;
import net.minecraft.block.AbstractBlock;
import net.minecraft.util.shape.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractBlock.AbstractBlockState.class)
public abstract class AbstractBlockStateShapeMixin {
    @Shadow
    private VoxelShape cullingShape;

    @Shadow
    private VoxelShape[] cullingFaces;

    @Inject(method = "initShapeCache", at = @At("TAIL"))
    private void ser$dedupShapes(CallbackInfo ci) {
        if (!MemoryOptimizationCommon.isSystemEnabled()
                || !com.SmartEntityRender.config.Config.getInstance().isMemoryShapeCacheDedupEnabled()) {
            return;
        }

        this.cullingShape = MemoryOptimizationCommon.internVoxelShape(this.cullingShape);

        if (this.cullingFaces != null) {
            for (int i = 0; i < this.cullingFaces.length; i++) {
                this.cullingFaces[i] = MemoryOptimizationCommon.internVoxelShape(this.cullingFaces[i]);
            }
        }

    }
}
