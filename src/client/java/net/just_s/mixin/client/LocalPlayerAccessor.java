package net.just_s.mixin.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Environment(EnvType.CLIENT)
@Mixin(LocalPlayer.class)
public interface LocalPlayerAccessor {

    @Accessor
    double getXLast();
    @Accessor
    double getYLast1();
    @Accessor
    double getZLast();

    @Accessor
    int getPositionReminder();
    @Accessor("positionReminder")
    void setPositionReminder(int positionReminder);

    @Accessor
    float getXRotLast();
    @Accessor
    float getYRotLast();
}
