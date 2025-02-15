package net.just_s.mixin.client;

import com.mojang.blaze3d.framegraph.FrameGraphBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.just_s.PossessiveModClient;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

import static org.spongepowered.asm.mixin.injection.callback.LocalCapture.CAPTURE_FAILHARD;

@Environment(EnvType.CLIENT)
@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin {

    @Shadow protected abstract void renderEntity(Entity entity, double d, double e, double f, float g, PoseStack poseStack, MultiBufferSource multiBufferSource);

    @Shadow @Final private RenderBuffers renderBuffers;

    @Shadow @Final private LevelTargetBundle targets;

    @Inject(method = "renderEntities", at = @At("TAIL"), locals = CAPTURE_FAILHARD)
    private void possessive$onRenderEntities(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, Camera camera, DeltaTracker deltaTracker, List<Entity> list, CallbackInfo ci) {
        if (PossessiveModClient.cameraHandler.isEnabled()) {
            Vec3 position = camera.getPosition();
            float partialTick = deltaTracker.getGameTimeDeltaPartialTick(false);

            renderEntity(Minecraft.getInstance().player, position.x, position.y, position.z, partialTick, poseStack, renderBuffers.bufferSource());
        }
    }

    @Inject(method = "renderEntity", at = @At("HEAD"), cancellable = true)
    private void possessive$onRenderEntity(Entity entity, double d, double e, double f, float g, PoseStack poseStack, MultiBufferSource multiBufferSource, CallbackInfo ci) {
        if (PossessiveModClient.cameraHandler.isEnabled()) {
            if (!PossessiveModClient.cameraHandler.getCamera().shouldRenderEntity(entity)) {
                ci.cancel();
            }
        }
    }

    @Redirect(
            method = "renderLevel",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/PostChain;addToFrame(Lcom/mojang/blaze3d/framegraph/FrameGraphBuilder;IILnet/minecraft/client/renderer/PostChain$TargetBundle;)V",
                    ordinal = 1
            )
    )
    private void possessive$onRenderLevel(PostChain instance, FrameGraphBuilder frameGraphBuilder, int i, int j, PostChain.TargetBundle targetBundle) {
        if (PossessiveModClient.cameraHandler.isEnabled()) {
            PossessiveModClient.cameraHandler.getCamera().onCameraShader(
                    frameGraphBuilder,
                    i, j,
                    this.targets
            );
        } else {
            instance.addToFrame(frameGraphBuilder, i, j, targetBundle);
        }
    }
}
