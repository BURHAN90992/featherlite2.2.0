package com.featherlite.mixin;

import com.featherlite.Features;
import net.minecraft.client.render.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Freelook: replace the camera rotation right after vanilla sets it. */
@Mixin(Camera.class)
public abstract class CameraMixin {
    @Shadow
    protected abstract void setRotation(float yaw, float pitch);

    @Inject(method = "update",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/render/Camera;setRotation(FF)V",
                    ordinal = 0, shift = At.Shift.AFTER))
    private void featherlite$freelook(CallbackInfo ci) {
        if (Features.freelookActive) {
            this.setRotation(Features.flYaw, Features.flPitch);
        }
    }
}
