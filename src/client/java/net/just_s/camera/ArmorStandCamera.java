package net.just_s.camera;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mrbysco.armorposer.data.SyncData;
import com.mrbysco.armorposer.packets.ArmorStandSyncPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.just_s.mixin.client.LocalPlayerAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.model.ArmorStandArmorModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ArmorStandRenderer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.nbt.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;

import java.util.UUID;

public class ArmorStandCamera extends AbstractCamera {
    private final ArmorStand possessedArmorStand;

    private boolean animateMoving = false;
    private static float animationSpeed = 0.3f;
    private static float animationMultiplier = 3;
    private static float animationMaxAngle = 30f;
    private float animationAngle = 0f;
    private float animationIntensity = 0;
    private float animationState = 0;

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
    public void copyPosition(Entity entity) {
        if (!(entity instanceof ArmorStand armorStand)) {
            super.copyPosition(entity);
            return;
        }

        float preBodyYRot = 0;
        float preHeadXRot = armorStand.getYRot();
        float preHeadYRot = 0;

        CompoundTag compoundTag = armorStand.saveWithoutId(new CompoundTag());
        if (compoundTag.contains("Rotation")) {
            ListTag rotationTag = compoundTag.getList("Rotation", 5);
            preBodyYRot = rotationTag.getFloat(0);
        }
        if (compoundTag.contains("Pose")) {
            CompoundTag poseTag = compoundTag.getCompound("Pose");
            if (poseTag.contains("Head")) {
                ListTag headTag = poseTag.getList("Head", 5);
                preHeadYRot = headTag.getFloat(0);
                preHeadXRot = headTag.getFloat(1) + preBodyYRot;
            }
        }

        CameraPosition position = new CameraPosition(entity.getX(), entity.getY(), entity.getZ());
        position.setRotation(preHeadXRot, preHeadYRot);
        applyPosition(position);
        this.yBodyRot = preBodyYRot;
    }

    public ArmorStand getPossessed() {
        return possessedArmorStand;
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

        this.tickAnimation();
        if (shouldUpdateMovement || shouldUpdateAngle) {
            CompoundTag compoundTag = this.generateCompoundFromCamera(this.getX(), this.getY(), this.getZ());
            LOGGER.info(compoundTag.toString());
            this.sendCompound(compoundTag);
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

    private CompoundTag generateCompoundFromCamera(double x, double y, double z) {
        CompoundTag original = possessedArmorStand.saveWithoutId(new CompoundTag());
        CompoundTag compoundTag = new CompoundTag();

        CompoundTag poseTag = new CompoundTag();
        ListTag poseHeadTag = new ListTag();
        poseHeadTag.add(FloatTag.valueOf(this.getXRot()));
        poseHeadTag.add(FloatTag.valueOf(this.yHeadRot - this.yBodyRot));
        float headTilt = 0;
        if (original.contains("Pose")) {
            CompoundTag originalPoseTag = original.getCompound("Pose");
            if (originalPoseTag.contains("Head")) {
                ListTag originalHeadTag = originalPoseTag.getList("Head", 5);
                if (originalHeadTag.size() > 2) {
                    headTilt = originalHeadTag.getFloat(2);
                }
            }
        }
        poseHeadTag.add(FloatTag.valueOf(headTilt));
        poseTag.put("Head", poseHeadTag);
        if (shouldAnimateMoving()) {
            poseTag.merge(getAnimatedPoseState());
        }
        compoundTag.put("Pose", poseTag);

        ListTag rotationTag = new ListTag();
		rotationTag.add(FloatTag.valueOf(this.yBodyRot));
        rotationTag.add(FloatTag.valueOf(0));
        compoundTag.put("Rotation", rotationTag);

        ListTag positionOffset = new ListTag();
        positionOffset.add(DoubleTag.valueOf(x));
        positionOffset.add(DoubleTag.valueOf(y));
        positionOffset.add(DoubleTag.valueOf(z));
        compoundTag.put("Pos", positionOffset);

        this.putPossessionTag(compoundTag);

        return possessedArmorStand.saveWithoutId(new CompoundTag()).merge(compoundTag);
    }

    private void putPossessionTag(CompoundTag compoundTag) {
        this.updatePossessionTag(compoundTag, false);
    }

    private void removePossessionTag(CompoundTag compoundTag) {
        this.updatePossessionTag(compoundTag, true);
    }

    private void updatePossessionTag(CompoundTag compoundTag, boolean remove) {
        String prefix = "PossessedBy-";
        ListTag tagList;
        LOGGER.info((compoundTag.contains("Tags")) + " | " + compoundTag.toString());
        if (compoundTag.contains("Tags")) {
            tagList = compoundTag.getList("Tags", 8);
        } else {
            tagList = new ListTag();
        }
        int i = Math.min(tagList.size(), 1024);
        LOGGER.info("amount of tags: " + i);
        for(int j = 0; j < i; ++j) {
            String tag = tagList.getString(j);
            LOGGER.info(tag);
            if (tag.startsWith(prefix)) {
                String stringUUID = (tag.length() > prefix.length() ) ? tag.substring(prefix.length()) : "";
                LOGGER.info(stringUUID);
                if (UUID.fromString(stringUUID).equals(Minecraft.getInstance().player.getUUID())) {
                    LOGGER.info("found it!!");
                    if (remove) {
                        LOGGER.info("removing");
                        tagList.remove(j);
                        compoundTag.put("Tags", tagList);
                        LOGGER.info(compoundTag.toString());
                    }
                    return;
                }
            }
        }
        if (!remove) {
            tagList.add(StringTag.valueOf(prefix + Minecraft.getInstance().player.getUUID().toString()));
            compoundTag.put("Tags", tagList);
        }
    }

    public void tickAnimation() {
        animationAngle = (float) ((animationAngle + animationSpeed) % (2 * Math.PI));
        animationIntensity = Math.clamp((float)(Math.abs(this.xOld - this.getX()) + Math.abs(this.zOld - this.getZ())) * animationMultiplier, 0f, 1f);
        animationState = (float) Math.sin(animationAngle) * animationMaxAngle * animationIntensity;
        if (Float.compare(0f, animationIntensity) == 1) {
            animationAngle = 0;
        }
    }

    private CompoundTag getAnimatedPoseState() {
        CompoundTag poseTag = new CompoundTag();
        ListTag poseBodyTag = new ListTag();
        poseBodyTag.add(FloatTag.valueOf(0));
        poseBodyTag.add(FloatTag.valueOf(0));
        poseBodyTag.add(FloatTag.valueOf(0));
        poseTag.put("Body", poseBodyTag);

        ListTag poseLeftLegTag = new ListTag();
        poseLeftLegTag.add(FloatTag.valueOf(animationState * -1));
        poseLeftLegTag.add(FloatTag.valueOf(0));
        poseLeftLegTag.add(FloatTag.valueOf(0));
        poseTag.put("LeftLeg", poseLeftLegTag);

        ListTag poseRightLegTag = new ListTag();
        poseRightLegTag.add(FloatTag.valueOf(animationState));
        poseRightLegTag.add(FloatTag.valueOf(0));
        poseRightLegTag.add(FloatTag.valueOf(0));
        poseTag.put("RightLeg", poseRightLegTag);

        ListTag poseLeftArmTag = new ListTag();
        poseLeftArmTag.add(FloatTag.valueOf(animationState));
        poseLeftArmTag.add(FloatTag.valueOf(0));
        poseLeftArmTag.add(FloatTag.valueOf(0));
        poseTag.put("LeftArm", poseLeftArmTag);

        ListTag poseRightArmTag = new ListTag();
        poseRightArmTag.add(FloatTag.valueOf(animationState * -1));
        poseRightArmTag.add(FloatTag.valueOf(0));
        poseRightArmTag.add(FloatTag.valueOf(0));
        poseTag.put("RightArm", poseRightArmTag);

        return poseTag;
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

    @Override
    public Screen onSetScreen(Screen screen) {
        if (screen instanceof InventoryScreen) {
            return new AnimatableArmorStandScreen(this);
        }
        return super.onSetScreen(screen);
    }

    public boolean shouldAnimateMoving() {
        return animateMoving;
    }

    public void setAnimateMoving(boolean animateMoving) {
        this.animateMoving = animateMoving;
    }

    @Override
    public void despawn() {
        super.despawn();

        LOGGER.info("ewww");
        CompoundTag compoundTag = this.possessedArmorStand.saveWithoutId(new CompoundTag());
        this.removePossessionTag(compoundTag);
        //compoundTag.remove("PossessedBy");
        sendCompound(compoundTag);
    }
}
