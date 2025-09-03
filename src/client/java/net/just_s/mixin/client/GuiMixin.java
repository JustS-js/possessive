package net.just_s.mixin.client;

import net.just_s.PossessiveModClient;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Gui.class)
public class GuiMixin {

    @Inject(method = "renderHotbarAndDecorations", at = @At("HEAD"), cancellable = true)
    private void possessive$onRenderHotbarAndDecorations(GuiGraphics guiGraphics, float f, CallbackInfo ci) {
        if (PossessiveModClient.cameraHandler.isEnabled()) {
            if (PossessiveModClient.cameraHandler.getCamera().onRenderHotbarAndDecorations(guiGraphics, f)) {
                ci.cancel();
            }
        }
    }

    @Inject( method = "renderPlayerHealth", at = @At("HEAD"), cancellable = true )
    private void possessive$disableRenderPlayerHealth(GuiGraphics guiGraphics, CallbackInfo ci) {
        if (PossessiveModClient.cameraHandler.isEnabled()) {
            ci.cancel();
        }
    }

    @Inject(method = "isExperienceBarVisible", at = @At("HEAD"), cancellable = true)
    private void possessive$isExperienceBarVisible(CallbackInfoReturnable<Boolean> cir) {
        if (PossessiveModClient.cameraHandler.isEnabled()) {
            cir.setReturnValue(PossessiveModClient.cameraHandler.getCamera().isExperienceBarVisible());
        }
    }
}
