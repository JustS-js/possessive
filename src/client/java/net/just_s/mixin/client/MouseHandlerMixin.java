package net.just_s.mixin.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.just_s.PossessiveModClient;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(MouseHandler.class)
public class MouseHandlerMixin {

    @Inject(
            method = "onScroll",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/player/LocalPlayer;getInventory()Lnet/minecraft/world/entity/player/Inventory;",
                    shift = At.Shift.AFTER
            ),
            cancellable = true
    )
    private void possessive$restrictHotbarScrolling(long l, double d, double e, CallbackInfo ci) {
        if (PossessiveModClient.cameraHandler.isEnabled()) {
            ci.cancel();
        }
    }

}