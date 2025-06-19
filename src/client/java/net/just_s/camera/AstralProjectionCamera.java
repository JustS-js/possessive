package net.just_s.camera;

import net.just_s.mixin.client.LevelRendererAccessor;
import net.minecraft.client.GraphicsStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class AstralProjectionCamera extends AbstractCamera {
    private GraphicsStatus storedGraphicStatus;
    private double savedGamma;

    public AstralProjectionCamera(Minecraft minecraft, Entity entity) {
        super(minecraft, -100);

        // Lets us fly
        getAbilities().flying = true;
        setPose(Pose.SWIMMING);
        this.moveInfrontOf(entity);
        this.addDeltaMovement(Vec3.ZERO.add(0, 1, 0));
    }

    private void moveInfrontOf(Entity entity) {
        this.copyPosition(entity);
        CameraPosition position = new CameraPosition(this);
        this.moveForwardUntilCollision(position, -0.5);
    }

    private boolean moveForwardUntilCollision(CameraPosition position, double maxDistance) {
        boolean negative = maxDistance < 0;
        maxDistance = negative ? -1 * maxDistance : maxDistance;
        double increment = 0.1;

        // Move forward by increment until we reach maxDistance or hit a collision
        for (double distance = 0.0; distance < maxDistance; distance += increment) {
            CameraPosition oldPosition = new CameraPosition(this);

            position.moveForward(negative ? -1 * increment : increment);
            applyPosition(position);

            if (!canEnterPose(getPose())) {
                // Revert to last non-colliding position and return whether we were unable to move at all
                applyPosition(oldPosition);
                return distance > 0;
            }
        }

        return true;
    }

    private boolean suffocatesAt(BlockPos blockPos) {
        AABB aABB = this.getBoundingBox();
        AABB aABB2 = (new AABB((double)blockPos.getX(), aABB.minY, (double)blockPos.getZ(), (double)blockPos.getX() + 1.0, aABB.maxY, (double)blockPos.getZ() + 1.0)).deflate(1.0E-7);
        return this.level().collidesWithSuffocatingBlock(this, aABB2);
    }

    @Override
    public void spawn() {
        super.spawn();
        storedGraphicStatus = this.minecraft.options.graphicsMode().get();
        if (!(storedGraphicStatus.equals(GraphicsStatus.FABULOUS))) {
            this.minecraft.options.graphicsMode().set(GraphicsStatus.FABULOUS);
            this.minecraft.options.save();
            ((LevelRendererAccessor)this.minecraft.levelRenderer).invokeInitTransparency();
        }
        savedGamma = this.minecraft.options.gamma().get();
        this.minecraft.options.gamma().set(100d);
    }

    @Override
    public void despawn() {
        super.despawn();
        this.minecraft.options.gamma().set(savedGamma);
        if (!(storedGraphicStatus.equals(GraphicsStatus.FABULOUS))) {
            this.minecraft.options.graphicsMode().set(storedGraphicStatus);
            this.minecraft.options.save();
            ((LevelRendererAccessor)this.minecraft.levelRenderer).invokeDeinitTransparency();
        }
    }

    @Override
    public void aiStep() {
        super.aiStep();
        getAbilities().flying = true;
        setOnGround(false);
    }

    // Prevents fall damage sound when AstralCamera touches ground.
    @Override
    protected void checkFallDamage(double d, boolean bl, BlockState blockState, BlockPos blockPos) {
        //noop
    }

    // Prevents slow down from ladders/vines.
    @Override
    public boolean onClimbable() {
        return false;
    }

    // Prevents slow down from water.
    @Override
    public boolean isInWater() {
        return false;
    }

    // Prevents collision with solid entities (shulkers, boats)
    @Override
    public boolean canCollideWith(Entity entity) {
        return false;
    }

    // Prevents water submersion sounds from playing.
    @Override
    protected boolean updateIsUnderwater() {
        this.wasUnderwater = this.isEyeInFluid(FluidTags.WATER);
        return this.wasUnderwater;
    }

    // Prevents water submersion sounds from playing.
    @Override
    protected void doWaterSplashEffect() {
        // noop
    }

    // Ensures that the AstralCamera is always in the swimming pose.
    @Override
    public void setPose(Pose pose) {
        super.setPose(Pose.SWIMMING);
    }

    // Ensures that the speed is constant.
    @Override
    public boolean isMovingSlowly() {
        return false;
    }

    @Override
    public Screen onSetScreen(Screen screen) {
        if (screen instanceof InventoryScreen) {
            return null;
        }
        return super.onSetScreen(screen);
    }

    @Override
    public boolean onCameraShader() {
        return true;
    }

    @Override
    public boolean onRenderHotbarAndDecorations(GuiGraphics guiGraphics, float f) {
        return true;
    }
}
