package net.just_s.mixin.client;

import com.mojang.blaze3d.pipeline.RenderTarget;
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
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.objectweb.asm.Opcodes;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
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

    @Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;checkPoseStack(Lcom/mojang/blaze3d/vertex/PoseStack;)V", ordinal = 0), locals = LocalCapture.CAPTURE_FAILHARD)
    private void possessive$renderPlayer(DeltaTracker deltaTracker, boolean bl, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f matrix4f, Matrix4f matrix4f2, CallbackInfo ci, TickRateManager tickRateManager, float partialTick, ProfilerFiller profilerFiller, Vec3 cameraPosition, double x, double y, double z, boolean frustumNotNull, Frustum frustum, float renderDistance, boolean fog, Matrix4fStack modelViewStack, boolean bl4, PoseStack poseStack, MultiBufferSource.BufferSource bufferSource) {
        if (PossessiveModClient.cameraHandler.isEnabled()) {
            renderEntity(Minecraft.getInstance().player, x, y, z, partialTick, poseStack, renderBuffers.bufferSource());
        }
    }

    // shader

    @Unique @Nullable
    private RenderTarget astralTranslucentTarget;
    @Unique @Nullable
    private RenderTarget astralItemEntityTarget;
    @Unique @Nullable
    private RenderTarget astralParticlesTarget;
    @Unique @Nullable
    private RenderTarget astralWeatherTarget;
    @Unique @Nullable
    private RenderTarget astralCloudsTarget;
    @Unique @Nullable
    private PostChain astralChain;

    @Final @Shadow
    private Minecraft minecraft;

    @Shadow @Final private static Logger LOGGER;

    @Shadow @Nullable private PostChain transparencyChain;

    @Shadow @Nullable private RenderTarget translucentTarget;

    @Shadow @Nullable private RenderTarget itemEntityTarget;

    @Shadow @Nullable private RenderTarget particlesTarget;

    @Shadow @Nullable private RenderTarget weatherTarget;

    @Shadow @Nullable private RenderTarget cloudsTarget;

    @Inject(
            method = "initTransparency",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/PostChain;getTempTarget(Ljava/lang/String;)Lcom/mojang/blaze3d/pipeline/RenderTarget;",
                    ordinal = 4,
                    shift = At.Shift.BY
            )
    )
    private void possessive$initAstral(CallbackInfo ci) {
        try {
            ResourceLocation resourceLocation = ResourceLocation.fromNamespaceAndPath(PossessiveModClient.MOD_ID, "shaders/post/astral.json");
            astralChain = new PostChain(this.minecraft.getTextureManager(), this.minecraft.getResourceManager(), this.minecraft.getMainRenderTarget(), resourceLocation);
            astralChain.resize(this.minecraft.getWindow().getWidth(), this.minecraft.getWindow().getHeight());
            this.astralTranslucentTarget = astralChain.getTempTarget("translucent");
            this.astralItemEntityTarget = astralChain.getTempTarget("itemEntity");
            this.astralParticlesTarget = astralChain.getTempTarget("particles");
            this.astralWeatherTarget = astralChain.getTempTarget("weather");
            this.astralCloudsTarget = astralChain.getTempTarget("clouds");
        } catch (Exception e) {
            LOGGER.warn("Could not initiate shader astral.json");
        }
    }

    @Inject(
            method = "deinitTransparency",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/pipeline/RenderTarget;destroyBuffers()V",
                    ordinal = 4,
                    shift = At.Shift.BY
            )
    )
    private void possessive$deinitAstral(CallbackInfo ci) {
        if (this.astralChain != null) {
            this.astralChain.close();
            this.astralTranslucentTarget.destroyBuffers();
            this.astralItemEntityTarget.destroyBuffers();
            this.astralParticlesTarget.destroyBuffers();
            this.astralWeatherTarget.destroyBuffers();
            this.astralCloudsTarget.destroyBuffers();
            this.astralChain = null;
            this.astralTranslucentTarget = null;
            this.astralItemEntityTarget = null;
            this.astralParticlesTarget = null;
            this.astralWeatherTarget = null;
            this.astralCloudsTarget = null;
        }
    }

    @Inject(
            method = "resize",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/PostChain;resize(II)V",
                    shift = At.Shift.BY
            )
    )
    private void possessive$resizeAstral(int i, int j, CallbackInfo ci) {
        if (this.astralChain != null) {
            this.astralChain.resize(i, j);
        }
    }

    @Redirect(
            method = "renderLevel",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/client/renderer/LevelRenderer;transparencyChain:Lnet/minecraft/client/renderer/PostChain;",
                    opcode = Opcodes.GETFIELD)
    )
    private PostChain possessive$getPostChain(LevelRenderer instance) {
        if (PossessiveModClient.cameraHandler.isEnabled()) {
            if (PossessiveModClient.cameraHandler.getCamera().onCameraShader()) {
                if (this.astralChain != null) {
                    return this.astralChain;
                }
            }
        }
        return transparencyChain;
    }

    @Redirect(
            method = "renderLevel",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/client/renderer/LevelRenderer;translucentTarget:Lcom/mojang/blaze3d/pipeline/RenderTarget;",
                    opcode = Opcodes.GETFIELD)
    )
    private RenderTarget possessive$getTranslucentTarget(LevelRenderer instance) {
        if (PossessiveModClient.cameraHandler.isEnabled()) {
            if (PossessiveModClient.cameraHandler.getCamera().onCameraShader()) {
                if (astralTranslucentTarget != null) {
                    return astralTranslucentTarget;
                }
            }
        }
        return translucentTarget;
    }

    @Redirect(
            method = "renderLevel",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/client/renderer/LevelRenderer;itemEntityTarget:Lcom/mojang/blaze3d/pipeline/RenderTarget;",
                    opcode = Opcodes.GETFIELD)
    )
    private RenderTarget possessive$getItemEntityTarget(LevelRenderer instance) {
        if (PossessiveModClient.cameraHandler.isEnabled()) {
            if (PossessiveModClient.cameraHandler.getCamera().onCameraShader()) {
                if (astralItemEntityTarget != null) {
                    return astralItemEntityTarget;
                }
            }
        }
        return itemEntityTarget;
    }

    @Redirect(
            method = "renderLevel",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/client/renderer/LevelRenderer;particlesTarget:Lcom/mojang/blaze3d/pipeline/RenderTarget;",
                    opcode = Opcodes.GETFIELD)
    )
    private RenderTarget possessive$getParticlesTarget(LevelRenderer instance) {
        if (PossessiveModClient.cameraHandler.isEnabled()) {
            if (PossessiveModClient.cameraHandler.getCamera().onCameraShader()) {
                if (astralParticlesTarget != null) {
                    return astralParticlesTarget;
                }
            }
        }
        return particlesTarget;
    }

    @Redirect(
            method = "renderLevel",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/client/renderer/LevelRenderer;weatherTarget:Lcom/mojang/blaze3d/pipeline/RenderTarget;",
                    opcode = Opcodes.GETFIELD)
    )
    private RenderTarget possessive$getWeatherTarget(LevelRenderer instance) {
        if (PossessiveModClient.cameraHandler.isEnabled()) {
            if (PossessiveModClient.cameraHandler.getCamera().onCameraShader()) {
                if (astralWeatherTarget != null) {
                    return astralWeatherTarget;
                }
            }
        }
        return weatherTarget;
    }

    @Redirect(
            method = "renderLevel",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/client/renderer/LevelRenderer;cloudsTarget:Lcom/mojang/blaze3d/pipeline/RenderTarget;",
                    opcode = Opcodes.GETFIELD)
    )
    private RenderTarget possessive$getCloudsTarget(LevelRenderer instance) {
        if (PossessiveModClient.cameraHandler.isEnabled()) {
            if (PossessiveModClient.cameraHandler.getCamera().onCameraShader()) {
                if (astralCloudsTarget != null) {
                    return astralCloudsTarget;
                }
            }
        }
        return cloudsTarget;
    }

    @Inject(method = "getTranslucentTarget", at = @At("HEAD"), cancellable = true)
    private void possessive$getTranslucentTarget(CallbackInfoReturnable<RenderTarget> cir) {
        if (PossessiveModClient.cameraHandler.isEnabled()) {
            if (PossessiveModClient.cameraHandler.getCamera().onCameraShader()) {
                if (astralTranslucentTarget != null) {
                    cir.setReturnValue(astralTranslucentTarget);
                }
            }
        }
    }

    @Inject(method = "getItemEntityTarget", at = @At("HEAD"), cancellable = true)
    private void possessive$getItemEntityTarget(CallbackInfoReturnable<RenderTarget> cir) {
        if (PossessiveModClient.cameraHandler.isEnabled()) {
            if (PossessiveModClient.cameraHandler.getCamera().onCameraShader()) {
                if (astralItemEntityTarget != null) {
                    cir.setReturnValue(astralItemEntityTarget);
                }
            }
        }
    }

    @Inject(method = "getParticlesTarget", at = @At("HEAD"), cancellable = true)
    private void possessive$getParticlesTarget(CallbackInfoReturnable<RenderTarget> cir) {
        if (PossessiveModClient.cameraHandler.isEnabled()) {
            if (PossessiveModClient.cameraHandler.getCamera().onCameraShader()) {
                if (astralParticlesTarget != null) {
                    cir.setReturnValue(astralParticlesTarget);
                }
            }
        }
    }

    @Inject(method = "getWeatherTarget", at = @At("HEAD"), cancellable = true)
    private void possessive$getWeatherTarget(CallbackInfoReturnable<RenderTarget> cir) {
        if (PossessiveModClient.cameraHandler.isEnabled()) {
            if (PossessiveModClient.cameraHandler.getCamera().onCameraShader()) {
                if (astralWeatherTarget != null) {
                    cir.setReturnValue(astralWeatherTarget);
                }
            }
        }
    }

    @Inject(method = "getCloudsTarget", at = @At("HEAD"), cancellable = true)
    private void possessive$getCloudsTarget(CallbackInfoReturnable<RenderTarget> cir) {
        if (PossessiveModClient.cameraHandler.isEnabled()) {
            if (PossessiveModClient.cameraHandler.getCamera().onCameraShader()) {
                if (astralCloudsTarget != null) {
                    cir.setReturnValue(astralCloudsTarget);
                }
            }
        }
    }
}
