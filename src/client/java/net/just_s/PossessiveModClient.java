package net.just_s;

import com.mojang.blaze3d.platform.InputConstants;
import com.mrbysco.armorposer.platform.Services;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class PossessiveModClient implements ClientModInitializer {
	public static final String MOD_ID = "possessive";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

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

				Services.PLATFORM.updateEntity(
						armorStandEntity,
						copyPlayerCompound(client, armorStandEntity)
				);
			}
		});
	}

	private static CompoundTag copyPlayerCompound(Minecraft client, ArmorStand armorStandEntity) {
		CompoundTag compound = armorStandEntity.saveWithoutId(new CompoundTag()).copy();
		ListTag test = compound.getList("Motion", 6);
		test.set(1, DoubleTag.valueOf(1));
		compound.put("Motion", test);
		LOGGER.info(test.toString());
		return compound;
	}
}