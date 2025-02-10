package net.just_s.mixin.client;

import com.mrbysco.armorposer.data.SyncData;
import com.mrbysco.armorposer.packets.ArmorStandSyncPayload;
import com.mrbysco.armorposer.platform.Services;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.just_s.PossessiveModClient;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class)
public class LocalPlayerMixin {
	@Inject(at = @At("HEAD"), method = "sendPosition")
	private void init(CallbackInfo info) {
		if (PossessiveModClient.targetedArmorStand != null && PossessiveModClient.targetedArmorStand.isAlive()) {
//			Services.PLATFORM.updateEntity(
//					PossessiveModClient.targetedArmorStand,
//					PossessiveModClient.copyPlayerCompound((LocalPlayer)(Object)this, PossessiveModClient.targetedArmorStand)
//			);
			SyncData data = new SyncData(
					PossessiveModClient.targetedArmorStand.getUUID(),
					PossessiveModClient.copyPlayerCompound((LocalPlayer)(Object)this, PossessiveModClient.targetedArmorStand)
			);
			ClientPlayNetworking.send(new ArmorStandSyncPayload(data));
		}
	}
}