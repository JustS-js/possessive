package net.just_s;

import com.mojang.blaze3d.platform.InputConstants;
import com.mrbysco.armorposer.data.SyncData;
import com.mrbysco.armorposer.packets.ArmorStandSyncPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.just_s.camera.AbstractCamera;
import net.just_s.camera.AstralProjectionCamera;
import net.just_s.camera.CameraHandler;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.ClientInput;
import net.minecraft.client.player.KeyboardInput;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PossessiveModClient implements ClientModInitializer {
	public static final String MOD_ID = "possessive";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static ArmorStand targetedArmorStand = null;
	private static float animationAngle = 0f;

	private static KeyMapping keyBinding;

	public static CameraHandler cameraHandler = new CameraHandler();

	@Override
	public void onInitializeClient() {
		keyBinding = KeyBindingHelper.registerKeyBinding(new KeyMapping(
				"key.possessive.test", // The translation key of the keybinding's name
				InputConstants.Type.KEYSYM, // The type of the keybinding, KEYSYM for keyboard, MOUSE for mouse.
				GLFW.GLFW_KEY_R, // The keycode of the key
				"category.possessive" // The translation key of the keybinding's category.
		));

		ClientTickEvents.END_CLIENT_TICK.register(PossessiveModClient::possessKeyPress);

		ClientTickEvents.START_CLIENT_TICK.register(PossessiveModClient::preTick);
		ClientTickEvents.END_CLIENT_TICK.register(PossessiveModClient::postTick);
	}

	private static void preTick(Minecraft client) {
		if (cameraHandler.isEnabled()) {
			// Prevent player from being controlled when freecam is enabled
			if (client.player != null && client.player.input instanceof KeyboardInput) {
				ClientInput input = new ClientInput();
				Input keyPresses = client.player.input.keyPresses;
				input.keyPresses = new Input(
						false,
						false,
						false,
						false,
						false,
						keyPresses.shift(),
						false
				);
				client.player.input = input;
			}

			client.gameRenderer.setRenderHand(cameraHandler.getCamera().shouldRenderHand());
		}
	}

	private static void postTick(Minecraft client) {
		// noop
	}

	private static void possessKeyPress(Minecraft client) {
		if (client.player == null) {
			return;
		}
		while (keyBinding.consumeClick()) {
			if (client.options.keyShift.isDown()) {
				if (cameraHandler.isEnabled()) {
					return;
				}
				cameraHandler.enableCamera(
						new AstralProjectionCamera(client, client.player)
				);
				client.player.displayClientMessage(Component.literal("astral projected!"), false);
				return;
			}
			if (!cameraHandler.isEnabled()) {
				return;
			}

			LocalPlayer entity = (LocalPlayer) cameraHandler.getCamera().getCrosshairEntity((e) -> e instanceof LocalPlayer, 3);
			if (entity == null || !entity.equals(client.player)) {
				return;
			}

			cameraHandler.disableCamera();
			client.player.displayClientMessage(Component.literal("returned in body!"), false);
		}
	}

//	private static void sendPossessedMovementIfNeeded(Minecraft client) {
//		if (client.player == null) {
//			return;
//		}
//		if (targetedArmorStand == null || !targetedArmorStand.isAlive()) {
//			return;
//		}
//		if (!isMoving(client.player)) {
//			return;
//		}
//		client.cameraEntity
//		SyncData data = new SyncData(
//				targetedArmorStand.getUUID(),
//				possessedCompoundTag(
//						client.player,
//						targetedArmorStand
//				)
//		);
//		ClientPlayNetworking.send(new ArmorStandSyncPayload(data));
//	}
//
//	private static boolean isMoving(LocalPlayer player) {
//		Vec2 vec2 = player.input.getMoveVector();
//		return vec2.x != 0.0F || vec2.y != 0.0F;
//	}
//
//	public static CompoundTag possessedCompoundTag(LocalPlayer player, ArmorStand armorStandEntity) {
//		CompoundTag armorStandCompound = armorStandEntity.saveWithoutId(new CompoundTag()).copy();
//		//CompoundTag playerCompound = player.saveWithoutId(new CompoundTag()).copy();
//
//		ListTag posTag = armorStandCompound.getList("Pos", 6);
//		posTag.set(0, DoubleTag.valueOf(posTag.getDouble(0) + 1.5d));
//		armorStandCompound.put("Pos", posTag);
//
//		ListTag rotationTag = playerCompound.getList("Rotation", 5);
//		rotationTag.set(0, FloatTag.valueOf(player.yBodyRot));
//		armorStandCompound.put("Rotation", rotationTag);
//
//		CompoundTag poseTag = armorStandCompound.getCompound("Pose");
//
//		ListTag headTag = armorStandCompound.getList("Head", 5);
//		ListTag rotation = playerCompound.getList("Rotation", 5);
//		headTag.add(FloatTag.valueOf(rotation.getFloat(1)));
//		headTag.add(FloatTag.valueOf(player.yHeadRot - rotation.getFloat(0)));
//		headTag.add(FloatTag.valueOf(0));
//
//		poseTag.put("Head", headTag);
//
//		animationAngle = (float) ((animationAngle + 0.6f) % (2 * Math.PI));
//		float animationIntensity = Math.clamp((float)(Math.abs(player.xOld - player.getX()) + Math.abs(player.zOld - player.getZ())) * 3, 0f, 1f);
//		float animationState = (float) Math.sin(animationAngle) * 30f * animationIntensity;
//		if (Float.compare(0f, animationIntensity) == 1) {
//			animationAngle = 0;
//		}
//
//		ListTag leftArmTag = armorStandCompound.getList("LeftArm", 5);
//		leftArmTag.add(FloatTag.valueOf(animationState));
//		leftArmTag.add(FloatTag.valueOf(0));
//		leftArmTag.add(FloatTag.valueOf(0));
//
//		poseTag.put("LeftArm", leftArmTag);
//
//		ListTag rightArmTag = armorStandCompound.getList("RightArm", 5);
//		rightArmTag.add(FloatTag.valueOf(animationState * -1));
//		rightArmTag.add(FloatTag.valueOf(0));
//		rightArmTag.add(FloatTag.valueOf(0));
//
//		poseTag.put("RightArm", rightArmTag);
//
//		ListTag leftLegTag = armorStandCompound.getList("LeftLeg", 5);
//		leftLegTag.add(FloatTag.valueOf(animationState * -1));
//		leftLegTag.add(FloatTag.valueOf(0));
//		leftLegTag.add(FloatTag.valueOf(0));
//
//		poseTag.put("LeftLeg", leftLegTag);
//
//		ListTag rightLegTag = armorStandCompound.getList("rightLeg", 5);
//		rightLegTag.add(FloatTag.valueOf(animationState));
//		rightLegTag.add(FloatTag.valueOf(0));
//		rightLegTag.add(FloatTag.valueOf(0));
//
//		poseTag.put("RightLeg", rightLegTag);
//	}
//
//	public static CompoundTag copyPlayerCompound(LocalPlayer player, ArmorStand armorStandEntity) {
//		CompoundTag armorStandCompound = armorStandEntity.saveWithoutId(new CompoundTag()).copy();
//		CompoundTag playerCompound = player.saveWithoutId(new CompoundTag()).copy();
//
//		ListTag posTag = playerCompound.getList("Pos", 6);
//		posTag.set(0, DoubleTag.valueOf(posTag.getDouble(0) + 1.5d));
//		armorStandCompound.put("Pos", posTag);
//
//		ListTag rotationTag = playerCompound.getList("Rotation", 5);
//		rotationTag.set(0, FloatTag.valueOf(player.yBodyRot));
//		armorStandCompound.put("Rotation", rotationTag);
//
//		CompoundTag poseTag = armorStandCompound.getCompound("Pose");
//
//		ListTag headTag = armorStandCompound.getList("Head", 5);
//		ListTag rotation = playerCompound.getList("Rotation", 5);
//		headTag.add(FloatTag.valueOf(rotation.getFloat(1)));
//		headTag.add(FloatTag.valueOf(player.yHeadRot - rotation.getFloat(0)));
//		headTag.add(FloatTag.valueOf(0));
//
//		poseTag.put("Head", headTag);
//
//		animationAngle = (float) ((animationAngle + 0.6f) % (2 * Math.PI));
//		ListTag motionTag = playerCompound.getList("Motion", 6);
//		float animationIntensity = Math.clamp((float)(Math.abs(player.xOld - player.getX()) + Math.abs(player.zOld - player.getZ())) * 3, 0f, 1f);
//		float animationState = (float) Math.sin(animationAngle) * 30f * animationIntensity;
//		if (Float.compare(0f, animationIntensity) == 1) {
//			animationAngle = 0;
//		}
//
//		ListTag leftArmTag = armorStandCompound.getList("LeftArm", 5);
//		leftArmTag.add(FloatTag.valueOf(animationState));
//		leftArmTag.add(FloatTag.valueOf(0));
//		leftArmTag.add(FloatTag.valueOf(0));
//
//		poseTag.put("LeftArm", leftArmTag);
//
//		ListTag rightArmTag = armorStandCompound.getList("RightArm", 5);
//		rightArmTag.add(FloatTag.valueOf(animationState * -1));
//		rightArmTag.add(FloatTag.valueOf(0));
//		rightArmTag.add(FloatTag.valueOf(0));
//
//		poseTag.put("RightArm", rightArmTag);
//
//		ListTag leftLegTag = armorStandCompound.getList("LeftLeg", 5);
//		leftLegTag.add(FloatTag.valueOf(animationState * -1));
//		leftLegTag.add(FloatTag.valueOf(0));
//		leftLegTag.add(FloatTag.valueOf(0));
//
//		poseTag.put("LeftLeg", leftLegTag);
//
//		ListTag rightLegTag = armorStandCompound.getList("rightLeg", 5);
//		rightLegTag.add(FloatTag.valueOf(animationState));
//		rightLegTag.add(FloatTag.valueOf(0));
//		rightLegTag.add(FloatTag.valueOf(0));
//
//		poseTag.put("RightLeg", rightLegTag);
//
//		//LOGGER.info(player.yHeadRot + " | " + player.yBodyRot);
//		//LOGGER.info(playerCompound.getList("Rotation", 5).toString());
//		return armorStandCompound;
//	}
}