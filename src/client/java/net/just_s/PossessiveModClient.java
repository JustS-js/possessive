package net.just_s;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.just_s.camera.CameraHandler;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
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
		ModKeyMappings.registerModKeyMappings();
		ClientTickEvents.START_CLIENT_TICK.register(PossessiveModClient::preTick);
		HudRenderCallback.EVENT.register(PossessiveModClient::cameraShaderApplier);
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

			client.gameRenderer.setRenderHand(cameraHandler.getCamera().shouldRenderHand());
		}
	}

	private static void cameraShaderApplier(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
		if (cameraHandler.isEnabled()) {
			cameraHandler.getCamera().onCameraShader(guiGraphics, deltaTracker);
		}
	}
}