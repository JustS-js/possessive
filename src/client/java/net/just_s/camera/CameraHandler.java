package net.just_s.camera;

import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.Input;
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
        client.gameRenderer.setRenderHand(newCamera.shouldRenderHand());

        if (client.gameRenderer.getMainCamera().isDetached()) {
            client.options.setCameraType(CameraType.FIRST_PERSON);
        }

        newCamera.spawn();
        client.setCameraEntity(newCamera);
    }

    public void disableCamera() {
        Minecraft client = Minecraft.getInstance();
        client.gameRenderer.setRenderHand(true);
        client.setCameraEntity(client.player);
        if (camera != null) {
            camera.despawn();
            camera.input = new Input();
            camera = null;
        }

        if (client.player != null) {
            client.player.input = new KeyboardInput(client.options);
        }
    }
}
