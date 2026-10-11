package xyz.immortius.museumcurator.fabric.client;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** A small informational Mod Menu screen; detailed settings remain in the TOML config. */
public final class ConfigInfoScreen extends Screen {
    private final Screen parent;

    public ConfigInfoScreen(Screen parent) {
        super(Component.translatable("modmenu.title"));
        this.parent = parent;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractRenderState(graphics, mouseX, mouseY, delta);
        graphics.centeredText(font, Component.literal("Museum Curator"), width / 2, height / 2 - 24, 0xFFFFFFFF);
        graphics.centeredText(font, Component.literal("Einstellungen: config/museumcurator.toml"), width / 2, height / 2, 0xFFE0E0E0);
        graphics.centeredText(font, Component.literal("Escape zum Schließen"), width / 2, height / 2 + 20, 0xFFE0E0E0);
    }

    @Override
    public void onClose() {
        minecraft.gui.setScreen(parent);
    }
}
