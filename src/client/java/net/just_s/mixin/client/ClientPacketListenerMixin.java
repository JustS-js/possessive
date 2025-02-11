package net.just_s.mixin.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.just_s.PossessiveModClient;
import net.minecraft.client.multiplayer.ClientPacketListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin {

    // Disables Camera when the player respawns/switches dimensions.
    @Inject(method = "handleRespawn", at = @At("HEAD"))
    private void possessive$onHandleRespawn(CallbackInfo ci) {
        if (PossessiveModClient.cameraHandler.isEnabled()) {
            PossessiveModClient.cameraHandler.disableCamera();
        }
    }
}
