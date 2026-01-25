package com.SmartEntityRender.mixin.client.memory;

import com.SmartEntityRender.memory.MemoryOptimizationClient;
import net.minecraft.client.render.model.json.MultipartModelComponent;
import net.minecraft.client.render.model.json.MultipartModelCondition;
import net.minecraft.state.State;
import net.minecraft.state.StateManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;
import java.util.function.Predicate;

@Mixin(MultipartModelComponent.class)
public abstract class MultipartModelComponentMixin {
    @Shadow
    @Final
    private Optional<MultipartModelCondition> selector;

    @Inject(method = "init", at = @At("HEAD"), cancellable = true)
    private <O, S extends State<O, S>> void ser$getCachedPredicate(StateManager<O, S> stateManager,
            CallbackInfoReturnable<Predicate<S>> cir) {
        Predicate<S> cached = MemoryOptimizationClient.getCachedMultipartPredicate(selector, stateManager);
        if (cached != null) {
            cir.setReturnValue(cached);
        }
    }

    @Inject(method = "init", at = @At("RETURN"))
    private <O, S extends State<O, S>> void ser$storePredicate(StateManager<O, S> stateManager,
            CallbackInfoReturnable<Predicate<S>> cir) {
        MemoryOptimizationClient.cacheMultipartPredicate(selector, stateManager, cir.getReturnValue());
    }
}
