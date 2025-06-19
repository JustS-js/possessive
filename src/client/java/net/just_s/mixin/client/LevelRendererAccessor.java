package net.just_s.mixin.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.LevelRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Environment(EnvType.CLIENT)
@Mixin(LevelRenderer.class)
public interface LevelRendererAccessor {
    @Invoker("initTransparency")
    public void invokeInitTransparency();

    @Invoker("deinitTransparency")
    public void invokeDeinitTransparency();
}
