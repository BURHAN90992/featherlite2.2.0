package com.featherlite.mixin;

import com.featherlite.Features;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Freelook: mouse movement turns the camera, not the player. */
@Mixin(Entity.class)
public abstract class EntityMixin {
    @Inject(method = "changeLookDirection", at = @At("HEAD"), cancellable = true)
    private void featherlite$freelookMouse(double dx, double dy, CallbackInfo ci) {
        if (Features.freelookActive && (Object) this == MinecraftClient.getInstance().player) {
            Features.flYaw += (float) dx * 0.15f;
            Features.flPitch = MathHelper.clamp(Features.flPitch + (float) dy * 0.15f, -90.0f, 90.0f);
            ci.cancel();
        }
    }
}
