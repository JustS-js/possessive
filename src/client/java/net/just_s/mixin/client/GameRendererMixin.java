package net.just_s.mixin.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.just_s.PossessiveModClient;
import net.just_s.camera.GameRendererAccess;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin(GameRenderer.class)
public class GameRendererMixin implements GameRendererAccess {

    @Unique
    private boolean possessive$hideHand = true;

    @Override
    public void possessive$setRenderHand(boolean bl) {
        possessive$hideHand = !bl;
    }

    @ModifyArg(
            method = "renderLevel",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/GameRenderer;renderItemInHand(FZLorg/joml/Matrix4f;)V"
            ),
            index = 1
    )
    private boolean possessive$applyShouldRenderHandBoolean(boolean bl) {
        return bl || possessive$hideHand;
    }

    @Inject(method = "shouldRenderBlockOutline", at = @At("HEAD"), cancellable = true)
    private void possessive$onShouldRenderBlockOutline(CallbackInfoReturnable<Boolean> cir) {
        if (PossessiveModClient.cameraHandler.isEnabled() && !PossessiveModClient.cameraHandler.getCamera().shouldRenderBlockOutline()) {
            cir.setReturnValue(false);
        }
    }
}
