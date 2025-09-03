package net.just_s.mixin.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.just_s.PossessiveModClient;
import net.xolt.freecam.Freecam;
import net.xolt.freecam.tripod.TripodSlot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

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
    @Inject(method = "toggleTripod", at = @At("HEAD"), cancellable = true)
    private static void possessive$restrictTripod(TripodSlot tripod, CallbackInfo ci) {
        if (PossessiveModClient.cameraHandler.isEnabled()) {
            ci.cancel();
        }
    }
}