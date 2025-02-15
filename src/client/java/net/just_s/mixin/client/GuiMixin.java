package net.just_s.mixin.client;

import net.just_s.PossessiveModClient;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class GuiMixin {

    @Inject(method = "renderHotbarAndDecorations", at = @At("HEAD"), cancellable = true)
    private void possessive$onRenderHotbarAndDecorations(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (PossessiveModClient.cameraHandler.isEnabled()) {
            if (PossessiveModClient.cameraHandler.getCamera().onRenderHotbarAndDecorations(guiGraphics, deltaTracker)) {
                ci.cancel();
            }
        }
    }
}
