package net.just_s.camera;

import com.mrbysco.armorposer.client.gui.ArmorStandScreen;
import com.mrbysco.armorposer.client.gui.widgets.ToggleButton;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;

public class AnimatableArmorStandScreen extends ArmorStandScreen {
    private final ArmorStandCamera camera;
    private ToggleButton animateButton;
    private Button syncButton;

    public AnimatableArmorStandScreen(ArmorStandCamera armorStandCamera) {
        super(armorStandCamera.getPossessed());
        camera = armorStandCamera;
    }

    @Override
    public void init() {
        super.init();

        this.addRenderableWidget(animateButton = new ToggleButton.Builder(camera.shouldAnimateMoving(), (button) -> {
            ToggleButton toggleButton = (ToggleButton)button;
            toggleButton.setValue(!toggleButton.getValue());
            camera.setAnimateMoving(toggleButton.getValue());
            this.textFieldUpdated();
        }).bounds(this.width - 20 - 100, 174, 100, 18).build());
        animateButton.setTooltip(Tooltip.create(Component.translatable("armorposer.gui.tooltip.animate_button")));
        this.addRenderableWidget(syncButton = new Button.Builder(Component.translatable("armorposer.gui.label.sync_button"),  (button) -> {
            camera.syncArmorStandPos();
        }).bounds(this.width - 20 - 100, 195, 100, 18).build());
        syncButton.setTooltip(Tooltip.create(Component.translatable("armorposer.gui.tooltip.sync_button")));
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
        String translatedLabel = I18n.get("armorposer.gui.label.animate_button", new Object[0]);
        guiGraphics.drawString(
                this.font,
                translatedLabel,
                this.width - 20 - 100 - this.font.width(translatedLabel) - 10,
                174 + (10 - 9 / 2),
                16777215,
                true
        );
    }
}
