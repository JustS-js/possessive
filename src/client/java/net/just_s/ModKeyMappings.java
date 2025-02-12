package net.just_s;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.just_s.camera.ArmorStandCamera;
import net.just_s.camera.AstralProjectionCamera;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;
import org.lwjgl.glfw.GLFW;

public class ModKeyMappings {
    private static KeyMapping possessKeyMapping;

    public static void registerModKeyMappings() {
        possessKeyMapping = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.possessive.possess", // The translation key of the keybinding's name
                InputConstants.Type.KEYSYM, // The type of the keybinding, KEYSYM for keyboard, MOUSE for mouse.
                GLFW.GLFW_KEY_R, // The keycode of the key
                "category.possessive" // The translation key of the keybinding's category.
        ));
        ClientTickEvents.END_CLIENT_TICK.register(ModKeyMappings::possessKeyPress);
    }

    private static void possessKeyPress(Minecraft client) {
        if (client.player == null) {
            return;
        }

        boolean isAstral = PossessiveModClient.cameraHandler.getCamera() instanceof AstralProjectionCamera;
        while (possessKeyMapping.consumeClick()) {
            if (client.options.keyShift.isDown()) {
                if (isAstral) {
                    return;
                }
                PossessiveModClient.cameraHandler.enableCamera(
                        new AstralProjectionCamera(client, client.cameraEntity)
                );
                client.player.displayClientMessage(Component.literal("astral projected!"), false);
                return;
            }
            if (!isAstral) {
                return;
            }

            Entity entity = PossessiveModClient.cameraHandler.getCamera().getCrosshairEntity(
                    (e) -> (e instanceof LocalPlayer && e.equals(Minecraft.getInstance().player)) || (e instanceof ArmorStand), 3
            );
            if (entity == null) {
                return;
            }

            PossessiveModClient.cameraHandler.disableCamera();
            switch (entity) {
                case LocalPlayer player -> {
                    client.player.displayClientMessage(Component.literal("returned in body!"), false);
                }
                case ArmorStand armorStand -> {
                    PossessiveModClient.cameraHandler.enableCamera(
                            new ArmorStandCamera(client, armorStand)
                    );
                    client.player.displayClientMessage(Component.literal("possessed!"), false);
                }
                default -> throw new IllegalStateException("Unexpected value: " + entity);
            }
        }
    }
}
