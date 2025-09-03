package net.just_s.mixin.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.just_s.PossessiveModClient;
import net.xolt.freecam.Freecam;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin(value = Freecam.class, remap = false)
@Pseudo
public class FreecamCompatMixin {

    // Prohibit freecam while using AbstractCamera
    @Inject(method = "toggle", at = @At("HEAD"), cancellable = true)
    private static void possessive$restrictToggle(CallbackInfo ci) {
        if (PossessiveModClient.cameraHandler.isEnabled()) {
            ci.cancel();
        }
    }

    // Prohibit tripod while using AbstractCamera
    @Inject(method = "activateTripodHandler", at = @At("HEAD"), cancellable = true)
    private static void possessive$restrictTripod(CallbackInfoReturnable<Boolean> cir) {
        if (PossessiveModClient.cameraHandler.isEnabled()) {
            cir.setReturnValue(false);
            cir.cancel();
        }
    }
}
