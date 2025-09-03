package net.just_s;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.just_s.camera.CameraHandler;
import net.just_s.camera.GameRendererAccess;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.ClientInput;
import net.minecraft.client.player.KeyboardInput;
import net.minecraft.world.entity.player.Input;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PossessiveModClient implements ClientModInitializer {
	public static final String MOD_ID = "possessive";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static CameraHandler cameraHandler = new CameraHandler();

	@Override
	public void onInitializeClient() {
		PossessiveKeyMappings.registerModKeyMappings();
		ClientTickEvents.START_CLIENT_TICK.register(PossessiveModClient::preTick);
		LOGGER.info("possessive addon loaded.");
	}

	private static void preTick(Minecraft client) {
		if (cameraHandler.isEnabled()) {
			// Prevent player from being controlled when any Camera is enabled
			if (client.player != null && client.player.input instanceof KeyboardInput) {
				ClientInput input = new ClientInput();
				input.keyPresses = new Input(
						false,
						false,
						false,
						false,
						false,
						true,
						false
				);
				client.player.input = input;
			}

			((GameRendererAccess)client.gameRenderer).possessive$setRenderHand(cameraHandler.getCamera().shouldRenderHand());
		}
	}

	public static boolean isOccupiedFlag(int bitFlag) {
		// 1XX...X (first bit from left)
		int bitIndex = 31;
		return 0 != (bitFlag & (1 << bitIndex));
	}

	public static int setOccupiedFlag(int bitFlag, boolean occupied) {
		int bitIndex = 31;
		if (occupied) {
			return bitFlag | (1 << bitIndex);
		}
		return bitFlag & ~(1 << bitIndex);
	}
}