package net.just_s;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PossessiveModClient implements ClientModInitializer {
	public static final String MOD_ID = "possessive";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static ArmorStand targetedArmorStand = null;
	private static float animationAngle = 0f;

	private static KeyMapping keyBinding;
	@Override
	public void onInitializeClient() {
		keyBinding = KeyBindingHelper.registerKeyBinding(new KeyMapping(
				"key.possessive.test", // The translation key of the keybinding's name
				InputConstants.Type.KEYSYM, // The type of the keybinding, KEYSYM for keyboard, MOUSE for mouse.
				GLFW.GLFW_KEY_R, // The keycode of the key
				"category.possessive" // The translation key of the keybinding's category.
		));

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (keyBinding.consumeClick()) {
				Entity entity = Minecraft.getInstance().crosshairPickEntity;
				if (!(entity instanceof ArmorStand armorStandEntity)) {
					return;
				}

				targetedArmorStand = armorStandEntity;
				client.player.displayClientMessage(Component.literal(targetedArmorStand.getStringUUID() + " selected!"), false);
			}
		});
	}

	public static CompoundTag copyPlayerCompound(LocalPlayer player, ArmorStand armorStandEntity) {
		CompoundTag armorStandCompound = armorStandEntity.saveWithoutId(new CompoundTag()).copy();
		CompoundTag playerCompound = player.saveWithoutId(new CompoundTag()).copy();

		ListTag posTag = playerCompound.getList("Pos", 6);
		posTag.set(0, DoubleTag.valueOf(posTag.getDouble(0) + 1.5d));
		armorStandCompound.put("Pos", posTag);

		ListTag rotationTag = playerCompound.getList("Rotation", 5);
		rotationTag.set(0, FloatTag.valueOf(player.yBodyRot));
		armorStandCompound.put("Rotation", rotationTag);

		CompoundTag poseTag = armorStandCompound.getCompound("Pose");

		ListTag headTag = armorStandCompound.getList("Head", 5);
		ListTag rotation = playerCompound.getList("Rotation", 5);
		headTag.add(FloatTag.valueOf(rotation.getFloat(1)));
		headTag.add(FloatTag.valueOf(player.yHeadRot - rotation.getFloat(0)));
		headTag.add(FloatTag.valueOf(0));

		poseTag.put("Head", headTag);

		animationAngle = (float) ((animationAngle + 0.6f) % (2 * Math.PI));
		ListTag motionTag = playerCompound.getList("Motion", 6);
		float animationIntensity = Math.clamp((float)(Math.abs(player.xOld - player.getX()) + Math.abs(player.zOld - player.getZ())) * 3, 0f, 1f);
		float animationState = (float) Math.sin(animationAngle) * 30f * animationIntensity;
		if (Float.compare(0f, animationIntensity) == 1) {
			animationAngle = 0;
		}

		ListTag leftArmTag = armorStandCompound.getList("LeftArm", 5);
		leftArmTag.add(FloatTag.valueOf(animationState));
		leftArmTag.add(FloatTag.valueOf(0));
		leftArmTag.add(FloatTag.valueOf(0));

		poseTag.put("LeftArm", leftArmTag);

		ListTag rightArmTag = armorStandCompound.getList("RightArm", 5);
		rightArmTag.add(FloatTag.valueOf(animationState * -1));
		rightArmTag.add(FloatTag.valueOf(0));
		rightArmTag.add(FloatTag.valueOf(0));

		poseTag.put("RightArm", rightArmTag);

		ListTag leftLegTag = armorStandCompound.getList("LeftLeg", 5);
		leftLegTag.add(FloatTag.valueOf(animationState * -1));
		leftLegTag.add(FloatTag.valueOf(0));
		leftLegTag.add(FloatTag.valueOf(0));

		poseTag.put("LeftLeg", leftLegTag);

		ListTag rightLegTag = armorStandCompound.getList("rightLeg", 5);
		rightLegTag.add(FloatTag.valueOf(animationState));
		rightLegTag.add(FloatTag.valueOf(0));
		rightLegTag.add(FloatTag.valueOf(0));

		poseTag.put("RightLeg", rightLegTag);

		//LOGGER.info(player.yHeadRot + " | " + player.yBodyRot);
		//LOGGER.info(playerCompound.getList("Rotation", 5).toString());
		return armorStandCompound;
	}
}