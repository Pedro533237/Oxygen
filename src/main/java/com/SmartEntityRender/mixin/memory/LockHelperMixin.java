package com.SmartEntityRender.mixin.memory;

import com.SmartEntityRender.memory.MemoryOptimizationCommon;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.thread.LockHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LockHelper.class)
public abstract class LockHelperMixin {
    @Inject(method = "crash", at = @At("RETURN"), cancellable = true)
    private static void ser$internCrash(String name, Thread thread, CallbackInfoReturnable<CrashException> cir) {
        cir.setReturnValue(MemoryOptimizationCommon.internLockCrash(name, thread, cir.getReturnValue()));
    }
}
