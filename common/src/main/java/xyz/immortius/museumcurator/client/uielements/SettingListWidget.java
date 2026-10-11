package xyz.immortius.museumcurator.client.uielements;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Minimal config panel for the 26.x port. Gameplay settings remain loaded from config files. */
public class SettingListWidget extends AbstractWidget {
    public SettingListWidget(Minecraft minecraft, Screen screen, int width, int top, int height, int entryWidth) {
        super((width - entryWidth) / 2, top, entryWidth, Math.max(20, height), Component.empty());
    }

    public void reset() {
    }

    public void tick() {
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        graphics.text(Minecraft.getInstance().font,
                Component.literal("Settings are configured in the museumcurator.toml file."),
                getX() + 8, getY() + 8, 0xFFFFFF, false);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
    }
}
