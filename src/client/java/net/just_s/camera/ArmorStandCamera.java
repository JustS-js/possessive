package net.just_s.camera;

import com.danrus.render.models.PASModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mrbysco.armorposer.Reference;
import com.mrbysco.armorposer.data.SyncData;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.loader.api.FabricLoader;
import net.just_s.PossessiveModClient;
import net.just_s.mixin.client.LocalPlayerAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.model.ArmorStandArmorModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ArmorStandRenderer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.*;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class ArmorStandCamera extends AbstractCamera {
    private final ArmorStand possessedArmorStand;

    private CompoundTag savedPose;
    private boolean animateMoving = false;
    private static float animationSpeed = 0.3f;
    private static float animationMultiplier = 3;
    private static float animationMaxAngle = 30f;
    private float animationAngle = 0f;
    private float animationIntensity = 0;
    private float animationState = 0;

    private double prevX;
    private double prevY;
    private double prevZ;
    private boolean hadGravity;

    private static final ResourceLocation ITEM_SLOT_SPRITE = new ResourceLocation(PossessiveModClient.MOD_ID, "textures/gui/sprites/item_slot.png");

    public ArmorStandCamera(Minecraft client, ArmorStand possessedArmorStand) {
        super(client, -120);

        this.possessedArmorStand = possessedArmorStand;
        this.copyPosition(possessedArmorStand);

        this.prevX = this.getX();
        this.prevY = this.getY();
        this.prevZ = this.getZ();
        this.hadGravity = !possessedArmorStand.isNoGravity();

        this.setPushable(true);
        this.setAbilityToChangePerspective(true);
        this.setRenderHand(true);
        this.setRenderBlockOutline(false);

        CompoundTag compoundTag = this.getPossessed().saveWithoutId(new CompoundTag());
        if (compoundTag.contains("Pose")) {
            this.savePose(compoundTag.getCompound("Pose"));
        }
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
    public void tick() {
        if (this.possessedArmorStand == null || !possessedArmorStand.isAlive()) {
            PossessiveModClient.cameraHandler.enableCamera(
                    new AstralProjectionCamera(minecraft, this)
            );
            AbstractCamera camera = PossessiveModClient.cameraHandler.getCamera();
            for(int i = 0; i < 20; ++i) {
                double d = camera.getRandom().nextGaussian() * 0.02;
                double e = camera.getRandom().nextGaussian() * 0.02;
                double f = camera.getRandom().nextGaussian() * 0.02;
                camera.level().addParticle(
                        ParticleTypes.POOF,
                        camera.getRandomX(1.0) - d * 10.0,
                        camera.getRandomY() - e * 10.0,
                        camera.getRandomZ(1.0) - f * 10.0,
                        d, e, f
                );
            }
            camera.playSound(SoundEvents.PLAYER_BREATH, 1f, 1f);
            Minecraft.getInstance().gui.setOverlayMessage(Component.translatable("possessive.message.vessel_broken"), false);
            return;
        }

        //LOGGER.info(this.getScale() + " | " + this.possessedArmorStand.getScale() + " | " + this.possessedArmorStand.getAgeScale());
        /*float asScale = this.possessedArmorStand.getScale() * this.possessedArmorStand.getScale();
        if (this.getScale() != asScale) {
            AttributeMap attributeMap = this.getAttributes();
            //attributeMap.getInstance(Attributes.).setBaseValue(asScale);
            //attributeMap.getInstance(Attributes.STEP_HEIGHT).setBaseValue(asScale * 0.6f);
            //attributeMap.getInstance(Attributes.JUMP_STRENGTH).setBaseValue(0.41f + 0.1f * asScale);
        }*/
        super.tick();
    }

    public void syncArmorStandPos() {
        CompoundTag compoundTag = this.generateCompoundFromCamera(
                this.getX() - possessedArmorStand.getX(),
                this.getY() - possessedArmorStand.getY(),
                this.getZ() - possessedArmorStand.getZ()
        );
        this.sendCompound(compoundTag);
    }

    @Override
    public boolean onSendPosition() {
        // check for player movement
        int positionReminder = ((LocalPlayerAccessor)this).getPositionReminder() + 1;

        double dX = this.getX() - prevX;
        double dY = this.getY() - prevY;
        double dZ = this.getZ() - prevZ;
        prevX = this.getX();
        prevY = this.getY();
        prevZ = this.getZ();
        double dYRot = (double) (this.getYRot() - ((LocalPlayerAccessor)this).getYRotLast());
        double dXRot = (double) (this.getXRot() - ((LocalPlayerAccessor)this).getXRotLast());

        boolean shouldUpdateMovement = Mth.lengthSquared(dX, dY, dZ) > Mth.square(2.0E-4); // || positionReminder >= 3;
        boolean shouldUpdateAngle = dYRot != 0.0 || dXRot != 0.0;

        this.tickAnimation();
        if (shouldUpdateMovement || shouldUpdateAngle) {
            CompoundTag compoundTag = this.generateCompoundFromCamera(this.getX(), this.getY(), this.getZ());
            this.sendCompound(compoundTag);
        }

        return false;
    }

    private void sendCompound(CompoundTag armorStandCompound) {
        FriendlyByteBuf buf = PacketByteBufs.create();
        SyncData data = new SyncData(
                possessedArmorStand.getUUID(),
                armorStandCompound
        );
        data.encode(buf);
        ClientPlayNetworking.send(Reference.SYNC_PACKET_ID, buf);
    }

    private CompoundTag generateCompoundFromCamera(double dx, double dy, double dz) {
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
        positionOffset.add(DoubleTag.valueOf(dx));
        positionOffset.add(DoubleTag.valueOf(dy));
        positionOffset.add(DoubleTag.valueOf(dz));
        compoundTag.put("Pos", positionOffset);

        compoundTag.putBoolean("NoGravity", true);

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
        int disabledSlotsAsFlag = compoundTag.getInt("DisabledSlots");
        compoundTag.putInt(
                "DisabledSlots",
                PossessiveModClient.setOccupiedFlag(disabledSlotsAsFlag, !remove)
        );
        if (remove) {
            compoundTag.putBoolean("NoGravity", !this.hadGravity);
        }
    }

    public void tickAnimation() {
        float size = this.possessedArmorStand.getScale() * this.possessedArmorStand.getScale();
        float appliedSpeed = animationSpeed * 1 / size;
        animationAngle = (float) ((animationAngle + appliedSpeed) % (2 * Math.PI));
        animationIntensity = Math.max(0f, Math.min((float)(Math.abs(this.xOld - this.getX()) + Math.abs(this.zOld - this.getZ())) * animationMultiplier, 1f));
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
    public void onRenderHand(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, AbstractClientPlayer abstractClientPlayer, ModelPart modelPart, ModelPart modelPart2) {
        EntityRenderDispatcher entityRenderDispatcher = Minecraft.getInstance().getEntityRenderDispatcher();

        ArmorStandRenderer entityRenderer = (ArmorStandRenderer) entityRenderDispatcher.getRenderer(possessedArmorStand);
        PlayerRenderer playerRenderer = (PlayerRenderer) entityRenderDispatcher.getRenderer(Minecraft.getInstance().player);

        PlayerModel playerModel = playerRenderer.getModel();
        ArmorStandArmorModel armorStandModel = entityRenderer.getModel();

        ModelPart armorStandArm;
        if (modelPart.equals(playerModel.rightArm)) {
            armorStandArm = (FabricLoader.getInstance().isModLoaded("pas")) ? ((PASModel)armorStandModel).originalRightArm : armorStandModel.rightArm;
        } else {
            armorStandArm = (FabricLoader.getInstance().isModLoaded("pas")) ? ((PASModel)armorStandModel).originalLeftArm :armorStandModel.leftArm;
        }
        armorStandArm.resetPose();
        armorStandArm.visible = true;
        armorStandModel.leftArm.zRot = -0.1F;
        armorStandModel.rightArm.zRot = 0.1F;

        armorStandArm.render(poseStack, multiBufferSource.getBuffer(RenderType.entityTranslucent(ArmorStandRenderer.DEFAULT_SKIN_LOCATION)), i, OverlayTexture.NO_OVERLAY);
    }

    public void applySavedPose() {
        if (this.savedPose != null) {
            CompoundTag compoundTag = possessedArmorStand.saveWithoutId(new CompoundTag());
            compoundTag.put("Pose", this.savedPose);
            this.sendCompound(compoundTag);
        }
    }

    public void savePose(@Nullable CompoundTag pose) {
        this.savedPose = pose;
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

        CompoundTag compoundTag = this.possessedArmorStand.saveWithoutId(new CompoundTag());
        this.removePossessionTag(compoundTag);
        sendCompound(compoundTag);
    }

    @Override
    public boolean onRenderHotbarAndDecorations(GuiGraphics guiGraphics, float f) {
        renderArmorStandItemHotbar(guiGraphics, f);
        return true;
    }

    private void renderArmorStandItemHotbar(GuiGraphics guiGraphics, float f) {
        if (minecraft.player == null) {
            return;
        }
        ItemStack leftHandItemStack = possessedArmorStand.getOffhandItem();
        ItemStack rightHandItemStack = possessedArmorStand.getMainHandItem();

        int x_mid = guiGraphics.guiWidth() / 2;
        guiGraphics.pose().pushPose();

        guiGraphics.pose().translate(0.0F, 0.0F, -90.0F);
        guiGraphics.blit(ITEM_SLOT_SPRITE, x_mid - 46 - 11, guiGraphics.guiHeight() - 22, 0, 0, 0, 22, 22, 22, 22);
        guiGraphics.blit(ITEM_SLOT_SPRITE, x_mid + 46 - 11, guiGraphics.guiHeight() - 22, 0, 0, 0, 22, 22, 22, 22);

        guiGraphics.pose().popPose();

        int y = guiGraphics.guiHeight() - 16 - 3;
        if (!leftHandItemStack.isEmpty()) {
            this.renderSlot(guiGraphics, x_mid - 46 - 8, y, f, leftHandItemStack);
        }
        if (!rightHandItemStack.isEmpty()) {
            this.renderSlot(guiGraphics, x_mid + 46 - 8, y, f, rightHandItemStack);
        }
    }

    private void renderSlot(GuiGraphics guiGraphics, int i, int j, float f, ItemStack itemStack) {
        if (!itemStack.isEmpty()) {
            if (f > 0.0F) {
                float g = 1.0F + f / 5.0F;
                guiGraphics.pose().pushPose();
                guiGraphics.pose().translate((float)(i + 8), (float)(j + 12), 0.0F);
                guiGraphics.pose().scale(1.0F / g, (g + 1.0F) / 2.0F, 1.0F);
                guiGraphics.pose().translate((float)(-(i + 8)), (float)(-(j + 12)), 0.0F);
            }

            guiGraphics.renderFakeItem(itemStack, i, j);
            if (f > 0.0F) {
                guiGraphics.pose().popPose();
            }

            guiGraphics.renderItemDecorations(this.minecraft.font, itemStack, i, j);
        }
    }

    @Override
    public boolean shouldRenderItemInMainHand(boolean original) {
        return original;
    }

    @Override
    public boolean shouldRenderItemInOffHand(boolean original) {
        if (minecraft.options.mainHand().get().equals(HumanoidArm.RIGHT)) {
            return !possessedArmorStand.getOffhandItem().isEmpty();
        }
        return !possessedArmorStand.getMainHandItem().isEmpty();
    }

    @Override
    public ItemStack getItemToRender(InteractionHand hand) {
        boolean bl = minecraft.options.mainHand().get().equals(HumanoidArm.RIGHT);
        if (hand.equals(InteractionHand.MAIN_HAND)) {
            return bl ? possessedArmorStand.getMainHandItem() : possessedArmorStand.getOffhandItem();
        } else {
            return bl ? possessedArmorStand.getOffhandItem() : possessedArmorStand.getMainHandItem();
        }
    }
}
