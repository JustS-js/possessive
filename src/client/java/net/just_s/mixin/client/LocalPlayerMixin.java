package net.just_s.mixin.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.just_s.PossessiveModClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin(LocalPlayer.class)
public class LocalPlayerMixin {

    @Inject(method = "sendPosition", at = @At("HEAD"), cancellable = true)
    private void possessive$onSendPosition(CallbackInfo ci) {
        if (PossessiveModClient.cameraHandler.isEnabled()) {
            if (PossessiveModClient.cameraHandler.getCamera().onSendPosition()) {
                ci.cancel();
            }
        }
    }

    @Inject(method = "isControlledCamera", at = @At("RETURN"), cancellable = true)
    private void possessive$ensureLocalPlayerSendingMovement(CallbackInfoReturnable<Boolean> cir) {
        if (PossessiveModClient.cameraHandler.isEnabled()) {
            boolean isOriginalBodyOfLocalPlayer = Minecraft.getInstance().player == ((LocalPlayer)(Object)this);
            cir.setReturnValue(cir.getReturnValue() || isOriginalBodyOfLocalPlayer);
        }
    }
}
