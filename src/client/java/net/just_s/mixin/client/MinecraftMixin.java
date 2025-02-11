package net.just_s.mixin.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.just_s.PossessiveModClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin(Minecraft.class)
public class MinecraftMixin {
    @Inject(method = "startAttack", at = @At("HEAD"), cancellable = true)
    private void possessive$onStartAttack(CallbackInfoReturnable<Boolean> cir) {
        if (PossessiveModClient.cameraHandler.isEnabled()) {
            PossessiveModClient.cameraHandler.getCamera().onStartAttack();
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "pickBlock", at = @At("HEAD"), cancellable = true)
    private void possessive$onPickBlock(CallbackInfo ci) {
        if (PossessiveModClient.cameraHandler.isEnabled()) {
            PossessiveModClient.cameraHandler.getCamera().onPickBlock();
            ci.cancel();
        }
    }

    @Inject(method = "continueAttack", at = @At("HEAD"), cancellable = true)
    private void possessive$onContinueAttack(CallbackInfo ci) {
        if (PossessiveModClient.cameraHandler.isEnabled()) {
            PossessiveModClient.cameraHandler.getCamera().onContinueAttack();
            ci.cancel();
        }
    }

    @ModifyVariable(method = "setScreen", at = @At(value = "HEAD"))
    private Screen possessive$onSetScreen(Screen value) {
        if (PossessiveModClient.cameraHandler.isEnabled()) {
            return PossessiveModClient.cameraHandler.getCamera().onSetScreen(value);
        }
        return value;
    }

    // Disables Camera if the player disconnects.
    @Inject(method = "disconnect()V", at = @At(value = "HEAD"))
    private void possessive$onDisconnect(CallbackInfo ci) {
        PossessiveModClient.LOGGER.info("disconnect");
        PossessiveModClient.cameraHandler.disableCamera();
    }
}
