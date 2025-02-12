package net.just_s.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.just_s.PossessiveModClient;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(PlayerRenderer.class)
public class PlayerRendererMixin {

    @Inject(method = "renderHand", at = @At("HEAD"), cancellable = true)
    private void possessive$onRenderHand(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, ResourceLocation resourceLocation, ModelPart modelPart, boolean bl, CallbackInfo ci) {
        if (PossessiveModClient.cameraHandler.isEnabled()) {
            PossessiveModClient.cameraHandler.getCamera().onRenderHand(
                    poseStack, multiBufferSource, i, resourceLocation, modelPart, bl
            );
            ci.cancel();
        }
    }
}
