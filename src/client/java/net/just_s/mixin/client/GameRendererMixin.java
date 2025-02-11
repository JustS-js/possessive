package net.just_s.mixin.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.just_s.PossessiveModClient;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin(GameRenderer.class)
public class GameRendererMixin {

    @Inject(method = "shouldRenderBlockOutline", at = @At("HEAD"), cancellable = true)
    private void possessive$onShouldRenderBlockOutline(CallbackInfoReturnable<Boolean> cir) {
        if (PossessiveModClient.cameraHandler.isEnabled() && !PossessiveModClient.cameraHandler.getCamera().shouldRenderBlockOutline()) {
            cir.setReturnValue(false);
        }
    }
}
