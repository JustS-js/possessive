package net.just_s.camera;

import net.just_s.PossessiveModClient;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.ClientInput;
import net.minecraft.client.player.KeyboardInput;

public class CameraHandler {
    private AbstractCamera camera = null;

    public AbstractCamera getCamera() {
        return camera;
    }

    public boolean isEnabled() {
        return camera != null && camera.equals(Minecraft.getInstance().cameraEntity);
    }

    public void enableCamera(AbstractCamera newCamera) {
        if (this.isEnabled()) {
            this.disableCamera();
        }
        this.camera = newCamera;

        Minecraft client = Minecraft.getInstance();
        //client.smartCull = false;
        client.gameRenderer.setRenderHand(newCamera.shouldRenderHand());

        if (client.gameRenderer.getMainCamera().isDetached()) {
            client.options.setCameraType(CameraType.FIRST_PERSON);
        }

        newCamera.spawn();
        client.setCameraEntity(newCamera);
    }

    public void disableCamera() {
        PossessiveModClient.LOGGER.info("disable");
        Minecraft client = Minecraft.getInstance();
        //client.smartCull = true;
        client.gameRenderer.setRenderHand(true);
        client.setCameraEntity(client.player);
        PossessiveModClient.LOGGER.info("camera != null? " + (camera != null));
        if (camera != null) {
            camera.despawn();
            camera.input = new ClientInput();
            camera = null;
        }

        if (client.player != null) {
            client.player.input = new KeyboardInput(client.options);
        }
    }
}
