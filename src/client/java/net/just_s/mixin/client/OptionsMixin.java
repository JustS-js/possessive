package net.just_s.mixin.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.just_s.PossessiveModClient;
import net.minecraft.client.Options;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(Options.class)
public class OptionsMixin {
    // Prevents switching to third person in Camera.
    @Inject(method = "setCameraType", at = @At("HEAD"), cancellable = true)
    private void possessive$onSetPerspective(CallbackInfo ci) {
        if (PossessiveModClient.cameraHandler.isEnabled() && !PossessiveModClient.cameraHandler.getCamera().canChangePerspective()) {
            ci.cancel();
        }
    }
}
