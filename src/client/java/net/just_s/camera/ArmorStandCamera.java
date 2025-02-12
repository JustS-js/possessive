package net.just_s.camera;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mrbysco.armorposer.data.SyncData;
import com.mrbysco.armorposer.packets.ArmorStandSyncPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.just_s.PossessiveModClient;
import net.just_s.mixin.client.LocalPlayerAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ArmorStandArmorModel;
import net.minecraft.client.model.ArmorStandModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ArmorStandRenderer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;

public class ArmorStandCamera extends AbstractCamera {
    private final ArmorStand possessedArmorStand;
    public ArmorStandCamera(Minecraft client, ArmorStand possessedArmorStand) {
        super(client, -120);

        this.possessedArmorStand = possessedArmorStand;
        this.copyPosition(possessedArmorStand);

        this.setPushable(true);
        this.setAbilityToChangePerspective(true);
        this.setRenderHand(true);
        this.setRenderBlockOutline(true);
    }

    @Override
    public boolean onSendPosition() {
        double dX = this.getX() - ((LocalPlayerAccessor)this).getXLast();
        double dY = this.getY() - ((LocalPlayerAccessor)this).getYLast();
        double dZ = this.getZ() - ((LocalPlayerAccessor)this).getZLast();
        double dYRot = (double) (this.getYRot() - ((LocalPlayerAccessor)this).getYRotLast());
        double dXRot = (double) (this.getXRot() - ((LocalPlayerAccessor)this).getXRotLast());
        int positionReminder = ((LocalPlayerAccessor)this).getPositionReminder() + 1;
        boolean shouldUpdateMovement = Mth.lengthSquared(dX, dY, dZ) > Mth.square(2.0E-4); // || positionReminder >= 20;
        boolean shouldUpdateAngle = dYRot != 0.0 || dXRot != 0.0;

        if (shouldUpdateMovement || shouldUpdateAngle) {
            this.sendCompound(this.generateCompoundFromCamera());
        }

        return false;
    }

    private void sendCompound(CompoundTag armorStandCompound) {
        SyncData data = new SyncData(
            possessedArmorStand.getUUID(),
            armorStandCompound
		);
		ClientPlayNetworking.send(new ArmorStandSyncPayload(data));
    }

    private CompoundTag generateCompoundFromCamera() {
        CompoundTag compoundTag = possessedArmorStand.saveWithoutId(new CompoundTag()).copy();

        PossessiveModClient.LOGGER.info(compoundTag.toString());
        return compoundTag;
    }

    @Override
    public boolean shouldRenderEntity(Entity entity) {
        if (this.equals(entity)) {
            return false;
        }
        if (possessedArmorStand.equals(entity)) {
            if (Minecraft.getInstance().options.getCameraType().isFirstPerson()) {
                return false;
            }
        }
        return super.shouldRenderEntity(entity);
    }

    @Override
    public void onRenderHand(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, ResourceLocation resourceLocation, ModelPart modelPart, boolean bl) {
        EntityRenderDispatcher entityRenderDispatcher = Minecraft.getInstance().getEntityRenderDispatcher();

        ArmorStandRenderer entityRenderer = (ArmorStandRenderer) entityRenderDispatcher.getRenderer(possessedArmorStand);
        PlayerRenderer playerRenderer = (PlayerRenderer) entityRenderDispatcher.getRenderer(Minecraft.getInstance().player);

        PlayerModel playerModel = playerRenderer.getModel();
        ArmorStandArmorModel armorStandModel = entityRenderer.getModel();

        ModelPart armorStandArm;
        if (modelPart.equals(playerModel.rightArm)) {
            armorStandArm = armorStandModel.rightArm;
        } else {
            armorStandArm = armorStandModel.leftArm;
        }
        armorStandArm.resetPose();
        armorStandArm.visible = true;
        armorStandModel.leftArm.zRot = -0.1F;
        armorStandModel.rightArm.zRot = 0.1F;

        armorStandArm.render(poseStack, multiBufferSource.getBuffer(RenderType.entityTranslucent(ArmorStandRenderer.DEFAULT_SKIN_LOCATION)), i, OverlayTexture.NO_OVERLAY);
    }
}
