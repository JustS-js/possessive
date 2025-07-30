package net.just_s;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.just_s.camera.ArmorStandCamera;
import net.just_s.camera.AstralProjectionCamera;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.particles.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import org.lwjgl.glfw.GLFW;

public class PossessiveKeyMappings {
    private static KeyMapping possessKeyMapping;
    private static KeyMapping savePoseKeyMapping;
    private static KeyMapping loadPoseKeyMapping;
    private static KeyMapping returnToPlayerKeyMapping;
    
    private static long lastKPressTime = 0;
    private static final long DOUBLE_CLICK_TIME = 300;

    public static void registerModKeyMappings() {
        possessKeyMapping = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.possessive.possess", // The translation key of the keybinding's name
                InputConstants.Type.KEYSYM, // The type of the keybinding, KEYSYM for keyboard, MOUSE for mouse.
                GLFW.GLFW_KEY_R, // The keycode of the key
                "category.possessive" // The translation key of the keybinding's category.
        ));
        ClientTickEvents.END_CLIENT_TICK.register(PossessiveKeyMappings::possessKeyPress);

        savePoseKeyMapping = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.possessive.save_pose",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_LEFT_BRACKET,
                "category.possessive"
        ));
        ClientTickEvents.END_CLIENT_TICK.register(PossessiveKeyMappings::savePoseKeyPress);

        loadPoseKeyMapping = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.possessive.load_pose",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_BRACKET,
                "category.possessive"
        ));
        ClientTickEvents.END_CLIENT_TICK.register(PossessiveKeyMappings::loadPoseKeyPress);
        
        returnToPlayerKeyMapping = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.possessive.return_to_player",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_K,
                "category.possessive"
        ));
        ClientTickEvents.END_CLIENT_TICK.register(PossessiveKeyMappings::returnToPlayerKeyPress);
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
                playFeedback(
                        PossessiveModClient.cameraHandler.getCamera(),
                        ParticleTypes.POOF,
                        SoundEvents.APPLY_EFFECT_TRIAL_OMEN, 1f, 1f,
                        "possessive.message.vessel_empty"
                );
                return;
            }
            if (!isAstral) {
                return;
            }

            Entity entity = PossessiveModClient.cameraHandler.getCamera().getCrosshairEntity(
                    (e) -> (e instanceof LivingEntity), 3
            );
            if (entity == null) {
                return;
            }

            switch (entity) {
                case LocalPlayer player -> {
                    if (!player.getUUID().equals(client.player.getUUID())) {
                        playBadAttemptToPossess(player);
                        return;
                    }
                    PossessiveModClient.cameraHandler.disableCamera();
                    playGoodAttemptToPossess(player);
                }
                case ArmorStand armorStand -> {
                    CompoundTag armorStandTag = armorStand.saveWithoutId(new CompoundTag());
                    int disabledSlotsAsFlag = armorStandTag.getIntOr("DisabledSlots", 0);
                    if (PossessiveModClient.isOccupiedFlag(disabledSlotsAsFlag)) {
                        playBadAttemptToPossess(armorStand);
                        return;
                    }

                    PossessiveModClient.cameraHandler.enableCamera(
                            new ArmorStandCamera(client, armorStand)
                    );
                    playGoodAttemptToPossess(armorStand);
                }
                default -> playBadAttemptToPossess(entity);
            }
        }
    }

    private static void playGoodAttemptToPossess(Entity entity) {
        playFeedback(
                entity,
                ParticleTypes.POOF,
                SoundEvents.APPLY_EFFECT_TRIAL_OMEN, 1f, 1f,
                "possessive.message.vessel_success"
        );
    }

    private static void playBadAttemptToPossess(Entity entity) {
        playFeedback(
                entity,
                DustParticleOptions.REDSTONE,
                SoundEvents.PLAYER_BREATH, 0.5f, 0.5f,
                "possessive.message.vessel_fail"
        );
    }

    private static void playFeedback(Entity entity, ParticleOptions particleOptions, SoundEvent soundEvent, float volume, float pitch, String translatable) {
        Entity camera = Minecraft.getInstance().cameraEntity;
        for(int i = 0; i < 20; ++i) {
            double d = camera.getRandom().nextGaussian() * 0.02;
            double e = camera.getRandom().nextGaussian() * 0.02;
            double f = camera.getRandom().nextGaussian() * 0.02;
            camera.level().addParticle(
                    particleOptions,
                    entity.getRandomX(1.0) - d * 10.0,
                    entity.getRandomY() - e * 10.0,
                    entity.getRandomZ(1.0) - f * 10.0,
                    d, e, f
            );
        }
        camera.playSound(soundEvent, volume, pitch);
        Minecraft.getInstance().gui.setOverlayMessage(Component.translatable(translatable), false);
    }

    private static void savePoseKeyPress(Minecraft client) {
        if (client.player == null) {
            return;
        }

        if (!PossessiveModClient.cameraHandler.isEnabled()) {
            return;
        }

        if (!(PossessiveModClient.cameraHandler.getCamera() instanceof ArmorStandCamera camera)) {
            return;
        }

        while (savePoseKeyMapping.consumeClick()) {
            CompoundTag compoundTag = camera.getPossessed().saveWithoutId(new CompoundTag());
            if (compoundTag.contains("Pose")) {
                camera.savePose(compoundTag.getCompoundOrEmpty("Pose"));
                Minecraft.getInstance().gui.setOverlayMessage(Component.translatable("possessive.message.save_pose"), false);
                camera.playSound(SoundEvents.ARMOR_STAND_PLACE, 0.5f, 1.2f);
            }
        }
    }

    private static void loadPoseKeyPress(Minecraft client) {
        if (client.player == null) {
            return;
        }

        if (!PossessiveModClient.cameraHandler.isEnabled()) {
            return;
        }

        if (!(PossessiveModClient.cameraHandler.getCamera() instanceof ArmorStandCamera camera)) {
            return;
        }

        while (loadPoseKeyMapping.consumeClick()) {
            camera.applySavedPose();
            Minecraft.getInstance().gui.setOverlayMessage(Component.translatable("possessive.message.load_pose"), false);
            camera.playSound(SoundEvents.ARMOR_STAND_HIT, 0.5f, 1.2f);
        }
    }

    private static void returnToPlayerKeyPress(Minecraft client) {
        if (client.player == null) {
            return;
        }

        while (returnToPlayerKeyMapping.consumeClick()) {
            long currentTime = System.currentTimeMillis();
            
            if (currentTime - lastKPressTime < DOUBLE_CLICK_TIME) {
                if (PossessiveModClient.cameraHandler.isEnabled()) {
                    PossessiveModClient.cameraHandler.disableCamera();
                    playFeedback(
                            client.player,
                            ParticleTypes.POOF,
                            SoundEvents.APPLY_EFFECT_TRIAL_OMEN, 1f, 1f,
                            "possessive.message.return_to_player"
                    );
                }
                lastKPressTime = 0;
            } else {
                lastKPressTime = currentTime;
            }
        }
    }
}
