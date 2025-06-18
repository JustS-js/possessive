package net.just_s.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.just_s.PossessiveModClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(ItemInHandRenderer.class)
public class ItemInHandRendererMixin {

    @Unique private float possessive$tickDelta;

    @Redirect(method = "renderHandsWithItems", at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/ItemInHandRenderer$HandRenderSelection;renderMainHand:Z"))
    private boolean possessive$shouldRenderItemInMainHand(ItemInHandRenderer.HandRenderSelection instance) {
        boolean value = instance.renderMainHand;
        if (PossessiveModClient.cameraHandler.isEnabled()) {
            return PossessiveModClient.cameraHandler.getCamera().shouldRenderItemInMainHand(value);
        }
        return value;
    }

    @Redirect(method = "renderHandsWithItems", at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/ItemInHandRenderer$HandRenderSelection;renderOffHand:Z"))
    private boolean possessive$shouldRenderItemInOffHand(ItemInHandRenderer.HandRenderSelection instance){
        boolean value = instance.renderOffHand;
        if (PossessiveModClient.cameraHandler.isEnabled()) {
            return PossessiveModClient.cameraHandler.getCamera().shouldRenderItemInOffHand(value);
        }
        return value;
    }

    @Inject(method = "renderHandsWithItems", at = @At("HEAD"))
    private void possessive$storeTickDelta(float tickDelta, PoseStack matrices, MultiBufferSource.BufferSource vertexConsumers, LocalPlayer player, int light, CallbackInfo ci) {
        this.possessive$tickDelta = tickDelta;
    }

    @ModifyVariable(method = "renderHandsWithItems", at = @At("HEAD"), argsOnly = true)
    private int possessive$getLight(int light) {
        if (PossessiveModClient.cameraHandler.isEnabled()) {
            return Minecraft.getInstance().getEntityRenderDispatcher().getPackedLightCoords(PossessiveModClient.cameraHandler.getCamera(), possessive$tickDelta);
        }
        return light;
    }

    @Redirect(method = "renderHandsWithItems", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/ItemInHandRenderer;renderArmWithItem(Lnet/minecraft/client/player/AbstractClientPlayer;FFLnet/minecraft/world/InteractionHand;FLnet/minecraft/world/item/ItemStack;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V"))
    private void possessive$onRenderHandsWithItems(ItemInHandRenderer instance, AbstractClientPlayer abstractClientPlayer, float f, float g, InteractionHand interactionHand, float h, ItemStack itemStack, float i, PoseStack poseStack, MultiBufferSource multiBufferSource, int j){
        if (PossessiveModClient.cameraHandler.isEnabled()) {
            instance.renderArmWithItem(abstractClientPlayer, f, h, interactionHand, h,
                    PossessiveModClient.cameraHandler.getCamera().getItemToRender(interactionHand),
                    i, poseStack, multiBufferSource, j);
            return;
        }
        instance.renderArmWithItem(abstractClientPlayer, f, h, interactionHand, h, itemStack, i, poseStack, multiBufferSource, j);
    }
}
