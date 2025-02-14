package net.just_s.mixin.client;

import com.mojang.blaze3d.framegraph.FrameGraphBuilder;
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import com.mojang.blaze3d.resource.RenderTargetDescriptor;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.just_s.PossessiveModClient;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

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
    /*@Inject(
            method = "renderLevel",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/PostChain;addToFrame(Lcom/mojang/blaze3d/framegraph/FrameGraphBuilder;IILnet/minecraft/client/renderer/PostChain$TargetBundle;)V",
                    ordinal = 1,
                    shift = At.Shift.BEFORE
            ),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void possessive$onRenderLevel(
            GraphicsResourceAllocator graphicsResourceAllocator,
            DeltaTracker deltaTracker,
            boolean bl,
            Camera camera,
            GameRenderer gameRenderer,
            Matrix4f matrix4f,
            Matrix4f matrix4f2,
            CallbackInfo ci,
            // locals
            float f,
            ProfilerFiller profilerFiller,
            Vec3 vec3,
            double d, double e, double g,
            boolean bl2,
            Frustum frustum,
            float h,
            boolean bl3,
            Vector4f vector4f,
            FogParameters fogParameters,
            FogParameters fogParameters2,
            boolean bl4,
            Matrix4fStack matrix4fStack,
            FrameGraphBuilder frameGraphBuilder,
            int i, int j,
            RenderTargetDescriptor renderTargetDescriptor,
            PostChain postChain
    ) {
        if (PossessiveModClient.cameraHandler.isEnabled()) {
            PossessiveModClient.cameraHandler.getCamera().onCameraShader(
                    frameGraphBuilder,
                    i, j,
                    this.targets
            );
        }
    }*/
}
