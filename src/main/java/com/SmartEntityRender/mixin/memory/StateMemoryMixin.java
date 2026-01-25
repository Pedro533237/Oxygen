package com.SmartEntityRender.mixin.memory;

import com.SmartEntityRender.config.Config;
import com.SmartEntityRender.memory.MemoryOptimizationCommon;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.state.State;
import net.minecraft.state.property.Property;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.UnaryOperator;

import com.mojang.datafixers.util.Either;

@Mixin(State.class)
public abstract class StateMemoryMixin<O, S> {
    @Shadow
    @Final
    @Mutable
    private Reference2ObjectArrayMap<Property<?>, Comparable<?>> propertyMap;

    @Shadow
    private Map<Property<?>, S[]> withMap;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void ser$internPropertyMap(O owner,
            Reference2ObjectArrayMap<Property<?>, Comparable<?>> propertyMap,
            com.mojang.serialization.MapCodec<S> codec,
            CallbackInfo ci) {
        this.propertyMap = MemoryOptimizationCommon.internPropertyMap(this.propertyMap);
    }

    @Inject(method = "createWithMap", at = @At("TAIL"))
    private void ser$internWithMap(Map<Map<Property<?>, Comparable<?>>, S> map, CallbackInfo ci) {
        this.withMap = MemoryOptimizationCommon.internWithMap(this.withMap);
    }
}

@Mixin(ExperienceOrbEntity.class)
abstract class ExperienceOrbMergeMixin {
    private static final double SER$MERGE_RADIUS = 1.5;

    @Shadow(remap = false)
    private int method_5919() {
        return 0; // getValue
    }

    @Shadow(remap = false)
    private void method_66666(int value) {
        // setValue
    }

    @Shadow(remap = false)
    private int field_6164; // orbAge

    @Inject(method = "method_31498", at = @At("TAIL"), remap = false)
    private void ser$mergeOnExpensiveUpdate(CallbackInfo ci) {
        if (!Config.getInstance().isMergeXpOrbsEnabled()) {
            return;
        }

        ExperienceOrbEntity self = (ExperienceOrbEntity) (Object) this;
        World world = self.getEntityWorld();
        if (world == null || world.isClient() || self.isRemoved()) {
            return;
        }

        BiPredicate<ExperienceOrbEntity, ExperienceOrbEntity> canMerge = (a, b) -> b != a && !b.isRemoved()
                && !a.isRemoved();

        UnaryOperator<Integer> clamp = value -> Math.max(1, Math.min(32767, value));

        Box area = self.getBoundingBox().expand(SER$MERGE_RADIUS);
        Optional<ExperienceOrbEntity> target = world.getEntitiesByClass(
                ExperienceOrbEntity.class,
                area,
                orb -> canMerge.test(self, orb))
                .stream()
                .min(Comparator.comparingDouble(self::squaredDistanceTo));

        Either<ExperienceOrbEntity, Integer> decision = target
                .<Either<ExperienceOrbEntity, Integer>>map(Either::left)
                .orElseGet(() -> Either.<ExperienceOrbEntity, Integer>right(method_5919()));

        decision.ifLeft(orb -> {
            ser$addValue(orb, method_5919(), clamp);
            self.discard();
        });
    }

    @org.spongepowered.asm.mixin.Unique
    private static void ser$addValue(ExperienceOrbEntity orb, int amount, UnaryOperator<Integer> clamp) {
        ExperienceOrbMergeMixin mixin = (ExperienceOrbMergeMixin) (Object) orb;
        int combined = clamp.apply(mixin.method_5919() + amount);
        mixin.method_66666(combined);
        mixin.field_6164 = 0;
    }
}
