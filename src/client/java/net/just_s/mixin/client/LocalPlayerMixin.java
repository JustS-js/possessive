package net.just_s.mixin.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.just_s.PossessiveModClient;
import net.minecraft.client.player.LocalPlayer;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(LocalPlayer.class)
public class LocalPlayerMixin {

    @Shadow @Final public static Logger LOGGER;

    @Inject(method = "sendPosition", at = @At("HEAD"), cancellable = true)
    private void possessive$onSendPosition(CallbackInfo ci) {
        if (PossessiveModClient.cameraHandler.isEnabled()) {
            /*if (PossessiveModClient.cameraHandler.getCamera().onSendPosition()) {
                ci.cancel();
            }*/
        }
    }
}
