package net.just_s.mixin.client;

import com.mojang.blaze3d.framegraph.FrameGraphBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.just_s.PossessiveModClient;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.LevelRenderState;
import net.minecraft.world.TickRateManager;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin {

    @Shadow protected abstract EntityRenderState extractEntity(Entity entity, float g);

    @Shadow @Final private LevelTargetBundle targets;

    @Inject(method = "extractVisibleEntities", at = @At("TAIL"))
    private void possessive$renderPlayerOriginalBodyWhenPossessing(Camera camera, Frustum frustum, DeltaTracker deltaTracker, LevelRenderState levelRenderState, CallbackInfo ci) {
        if (PossessiveModClient.cameraHandler.isEnabled()) {
            LocalPlayer entity = Minecraft.getInstance().player;
            if (entity == null) {
                return;
            }

            TickRateManager tickRateManager = Minecraft.getInstance().level.tickRateManager();
            float g = deltaTracker.getGameTimeDeltaPartialTick(!tickRateManager.isEntityFrozen(entity));
            EntityRenderState entityRenderState = extractEntity(entity, g);
            levelRenderState.entityRenderStates.add(entityRenderState);
            if (entityRenderState.appearsGlowing() && entity.isCurrentlyGlowing()) {
                levelRenderState.haveGlowingEntities = true;
            }
        }
    }

    @Redirect(
            method = "extractVisibleEntities",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/entity/EntityRenderDispatcher;shouldRender(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/client/renderer/culling/Frustum;DDD)Z"
            )
    )
    private boolean possessive$onRenderEntity(EntityRenderDispatcher instance, Entity entity, Frustum frustum, double d, double e, double f) {
        if (PossessiveModClient.cameraHandler.isEnabled()) {
            return PossessiveModClient.cameraHandler.getCamera().shouldRenderEntity(entity);
        }
        return instance.shouldRender(entity, frustum, d, e, f);
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
                    instance,
                    frameGraphBuilder,
                    i, j,
                    this.targets
            );
        } else {
            instance.addToFrame(frameGraphBuilder, i, j, targetBundle);
        }
    }
}
