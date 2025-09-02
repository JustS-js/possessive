package net.just_s.mixin.client;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.mojang.blaze3d.vertex.PoseStack;
import net.just_s.PossessiveModClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(ItemInHandRenderer.class)
public class ItemInHandRendererMixin {

    @Inject(
            method = "renderHandsWithItems",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/util/Mth;lerp(FFF)F",
                    ordinal = 0
            )
    )
    private void possessive$replaceBooleansForRenderingHands(float f, PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, LocalPlayer localPlayer, int i, CallbackInfo ci, @Local LocalRef<ItemInHandRenderer.HandRenderSelection> localRef){
        // if mod not in use then just skip it altogether
        if (!PossessiveModClient.cameraHandler.isEnabled()) {
            return;
        }

        boolean finalShouldRenderMainHand = PossessiveModClient.cameraHandler.getCamera().shouldRenderItemInMainHand(localRef.get().renderMainHand);
        boolean finalShouldRenderOffHand = PossessiveModClient.cameraHandler.getCamera().shouldRenderItemInOffHand(localRef.get().renderOffHand);

        if (finalShouldRenderMainHand && finalShouldRenderOffHand) {
            localRef.set(ItemInHandRenderer.HandRenderSelection.RENDER_BOTH_HANDS);
        } else if (finalShouldRenderMainHand) {
            localRef.set(ItemInHandRenderer.HandRenderSelection.RENDER_MAIN_HAND_ONLY);
        } else if (finalShouldRenderOffHand) {
            localRef.set(ItemInHandRenderer.HandRenderSelection.RENDER_OFF_HAND_ONLY);
        }
    }

    @ModifyArgs(
            method = "renderHandsWithItems",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/ItemInHandRenderer;renderArmWithItem(Lnet/minecraft/client/player/AbstractClientPlayer;FFLnet/minecraft/world/InteractionHand;FLnet/minecraft/world/item/ItemStack;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V"
            )
    )
    private void possessive$replaceItemToRender(Args args){
        if (PossessiveModClient.cameraHandler.isEnabled()) {
            ItemStack item = PossessiveModClient.cameraHandler.getCamera().getItemToRender(args.get(3));
            args.set(5, item);
        }
    }
}
