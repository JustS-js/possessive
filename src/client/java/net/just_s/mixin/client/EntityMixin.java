package net.just_s.mixin.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.just_s.PossessiveModClient;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Environment(EnvType.CLIENT)
@Mixin(Entity.class)
public class EntityMixin {

    // Makes mouse input rotate the Camera.
    @Inject(method = "turn", at = @At("HEAD"), cancellable = true)
    private void possessive$onTurn(double x, double y, CallbackInfo ci) {
        if (PossessiveModClient.cameraHandler.isEnabled() && this.equals(Minecraft.getInstance().player)) {
            Minecraft.getInstance().cameraEntity.turn(x, y);
            ci.cancel();
        }
    }

    // Prevents Camera from pushing/getting pushed by entities.
    @Inject(method = "push(Lnet/minecraft/world/entity/Entity;)V", at = @At("HEAD"), cancellable = true)
    private void possessive$onPush(Entity entity, CallbackInfo ci) {
        boolean cameraInteraction = (entity.equals(Minecraft.getInstance().cameraEntity) ||
                this.equals(Minecraft.getInstance().cameraEntity));
        if (PossessiveModClient.cameraHandler.isEnabled() && cameraInteraction) {
            ci.cancel();
        }
    }
}
