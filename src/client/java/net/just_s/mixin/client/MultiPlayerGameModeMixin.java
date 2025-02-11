package net.just_s.mixin.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.just_s.PossessiveModClient;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin(MultiPlayerGameMode.class)
public class MultiPlayerGameModeMixin {
    @Inject(method = "useItemOn", at = @At("HEAD"), cancellable = true)
    private void possessive$onUseItemOn(LocalPlayer player, InteractionHand hand, BlockHitResult hitResult, CallbackInfoReturnable<InteractionResult> cir) {
        if (PossessiveModClient.cameraHandler.isEnabled()) {
            cir.setReturnValue(
                    PossessiveModClient.cameraHandler.getCamera().onUseItemOn(
                            player, hand, hitResult
                    )
            );
        }
    }

    @Inject(method = "interact", at = @At("HEAD"), cancellable = true)
    private void possessive$onInteract(Player player, Entity entity, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        if (PossessiveModClient.cameraHandler.isEnabled()) {
            cir.setReturnValue(
                    PossessiveModClient.cameraHandler.getCamera().onInteract(
                            player, entity, hand
                    )
            );
        }
    }

    @Inject(method = "interactAt", at = @At("HEAD"), cancellable = true)
    private void possessive$onInteractAt(Player player, Entity entity, EntityHitResult hitResult, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        if (PossessiveModClient.cameraHandler.isEnabled()) {
            cir.setReturnValue(
                    PossessiveModClient.cameraHandler.getCamera().onInteractAt(
                            player, entity, hitResult, hand
                    )
            );
        }
    }

    @Inject(method = "attack", at = @At("HEAD"), cancellable = true)
    private void possessive$onAttack(Player player, Entity target, CallbackInfo ci) {
        if (PossessiveModClient.cameraHandler.isEnabled()) {
            PossessiveModClient.cameraHandler.getCamera().onAttack(player, target);
            ci.cancel();
        }
    }
}
