package net.just_s.camera;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.block.state.BlockState;

public class AstralProjectionCamera extends AbstractCamera {
    public AstralProjectionCamera(Minecraft minecraft, Entity entity) {
        super(minecraft, -100);

        setPose(Pose.SWIMMING);
        this.copyPosition(entity);
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
}
