package net.just_s.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.just_s.PossessiveModClient;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.TickRateManager;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Environment(EnvType.CLIENT)
@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin {

    @Shadow protected abstract void renderEntity(Entity entity, double d, double e, double f, float g, PoseStack poseStack, MultiBufferSource multiBufferSource);

    @Shadow @Final private RenderBuffers renderBuffers;

    @Inject(method = "renderEntity", at = @At("HEAD"), cancellable = true)
    private void possessive$onRenderEntity(Entity entity, double d, double e, double f, float g, PoseStack poseStack, MultiBufferSource multiBufferSource, CallbackInfo ci) {
        if (PossessiveModClient.cameraHandler.isEnabled()) {
            if (!PossessiveModClient.cameraHandler.getCamera().shouldRenderEntity(entity)) {
                ci.cancel();
            }
        }
    }

    @Redirect(
            method = "initTransparency",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/resources/ResourceLocation;withDefaultNamespace(Ljava/lang/String;)Lnet/minecraft/resources/ResourceLocation;"
            )
    )
    private ResourceLocation possessive$onRenderLevel(String string) {
        if (PossessiveModClient.cameraHandler.isEnabled()) {
            return PossessiveModClient.cameraHandler.getCamera().onCameraShader(string);
        }
        return ResourceLocation.withDefaultNamespace("shaders/post/transparency.json");
    }

    @Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;checkPoseStack(Lcom/mojang/blaze3d/vertex/PoseStack;)V", ordinal = 0), locals = LocalCapture.CAPTURE_FAILHARD)
    private void onRender(DeltaTracker deltaTracker, boolean bl, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f matrix4f, Matrix4f matrix4f2, CallbackInfo ci, TickRateManager tickRateManager, float partialTick, ProfilerFiller profilerFiller, Vec3 cameraPosition, double x, double y, double z, boolean frustumNotNull, Frustum frustum, float renderDistance, boolean fog, Matrix4fStack modelViewStack, boolean bl4, PoseStack poseStack, MultiBufferSource.BufferSource bufferSource) {
        if (PossessiveModClient.cameraHandler.isEnabled()) {
            renderEntity(Minecraft.getInstance().player, x, y, z, partialTick, poseStack, renderBuffers.bufferSource());
        }
    }
}
