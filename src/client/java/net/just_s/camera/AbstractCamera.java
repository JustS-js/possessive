package net.just_s.camera;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.just_s.PossessiveModClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.CommonListenerCookie;
import net.minecraft.client.player.KeyboardInput;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.ServerLinks;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Collections;
import java.util.UUID;
import java.util.function.Predicate;

// heavily (absolutely) inspired by (stripped from) freecam https://github.com/MinecraftFreecam/Freecam
public abstract class AbstractCamera extends LocalPlayer {
    private static final ClientPacketListener NETWORK_HANDLER;

    private boolean pushable = false;
    private boolean changeablePerspective = false;
    private boolean renderBlockOutline = false;
    private boolean renderHand = false;

    static {
        NETWORK_HANDLER = new ClientPacketListener(
                Minecraft.getInstance(),
                Minecraft.getInstance().getConnection().getConnection(),
                new CommonListenerCookie(
                        new GameProfile(UUID.randomUUID(), "Camera"),
                        Minecraft.getInstance().getTelemetryManager().createWorldSessionManager(false, null, null),
                        Minecraft.getInstance().player.registryAccess().freeze(),
                        FeatureFlagSet.of(),
                        null,
                        Minecraft.getInstance().getCurrentServer(),
                        Minecraft.getInstance().screen,
                        Collections.emptyMap(),
                        Minecraft.getInstance().gui.getChat().storeState(),
                        Collections.emptyMap(),
                        ServerLinks.EMPTY
                )
        ) {
            @Override
            public void send(Packet<?> packet) {
                // noop
            }
        };
    }

    public AbstractCamera(Minecraft client, int id) {
        super(client, client.level, NETWORK_HANDLER, client.player.getStats(), client.player.getRecipeBook(), false, false);

        // Not to interfere with real entities in world. Should be negative.
        setId(id);
        // Otherwise input is frozen until timeout
        setClientLoaded(true);
        input = new KeyboardInput(client.options);
    }

    public void tick() {
        super.tick();
    }

    public void spawn() {
        if (clientLevel != null) {
            clientLevel.addEntity(this);
        }
    }

    public void despawn() {
        if (clientLevel != null && clientLevel.getEntity(getId()) != null) {
            clientLevel.removeEntity(getId(), RemovalReason.DISCARDED);
        }
    }

    @Override
    public void copyPosition(Entity entity) {
        applyPosition(new CameraPosition(entity));
    }

    public void applyPosition(CameraPosition position) {
        moveTo(position.x, position.y, position.z, position.yaw, position.pitch);
        xBob = getXRot();
        yBob = getYRot();
        xBobO = xBob;
        yBobO = yBob;
    }

    public Entity getCrosshairEntity(Predicate<Entity> predicate, double maxDistance) {
        Vec3 start = this.getEyePosition(1f);
        Vec3 offset = this.getViewVector(1f).scale(maxDistance);
        Vec3 end = start.add(offset);
        AABB aabb = this.getBoundingBox().expandTowards(offset).inflate(1);

        EntityHitResult result = ProjectileUtil.getEntityHitResult(this, start, end, aabb, predicate, maxDistance * maxDistance);
        return (result == null) ? null : result.getEntity();
    }

    @Override
    public boolean isPushable() {
        return pushable;
    }

    public void setPushable(boolean pushable) {
        this.pushable = pushable;
    }

    public boolean shouldRenderBlockOutline() {
        return renderBlockOutline;
    }

    public void setRenderBlockOutline(boolean renderBlockOutline) {
        this.renderBlockOutline = renderBlockOutline;
    }

    public boolean shouldRenderHand() {
        return renderHand;
    }

    public void setRenderHand(boolean renderHand) {
        this.renderHand = renderHand;
    }

    public boolean canChangePerspective() {
        return changeablePerspective;
    }

    public void setAbilityToChangePerspective(boolean changeablePerspective) {
        this.changeablePerspective = changeablePerspective;
    }

    public void onStartAttack() {
        // noop
    }

    public void onAttack(Player player, Entity target) {
        // noop
    }

    public void onPickBlock() {
        // noop
    }

    public void onContinueAttack() {
        // noop
    }

    // copied from PlayerRenderer.renderHand()
    public void onRenderHand(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, ResourceLocation resourceLocation, ModelPart modelPart, boolean bl) {
        EntityRenderDispatcher entityRenderDispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        LocalPlayer entityToRender = Minecraft.getInstance().player;
        PlayerRenderer entityRenderer = (PlayerRenderer) entityRenderDispatcher.getRenderer(entityToRender);

        PlayerModel playerModel = entityRenderer.getModel();
        modelPart.resetPose();
        modelPart.visible = true;
        playerModel.leftSleeve.visible = bl;
        playerModel.rightSleeve.visible = bl;
        playerModel.leftArm.zRot = -0.1F;
        playerModel.rightArm.zRot = 0.1F;
        modelPart.render(poseStack, multiBufferSource.getBuffer(RenderType.entityTranslucent(resourceLocation)), i, OverlayTexture.NO_OVERLAY);
    }

    public boolean onSendPosition() {
        return false;
    }

    public Screen onSetScreen(Screen screen) {
        return screen;
    }

    public InteractionResult onUseItemOn(LocalPlayer player, InteractionHand hand, BlockHitResult hitResult) {
        return InteractionResult.PASS;
    }

    public InteractionResult onInteract(Player player, Entity entity, InteractionHand hand) {
        return InteractionResult.PASS;
    }

    public InteractionResult onInteractAt(Player player, Entity entity, EntityHitResult hitResult, InteractionHand hand) {
        return InteractionResult.PASS;
    }

    public boolean shouldRenderEntity(Entity entity) {
        return true;
    }
}
