package xyz.immortius.museumcurator.client.uielements;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.util.List;

public interface ScrollContainerEntry {
    void extractContent(GuiGraphicsExtractor graphics, int top, int left, int width, int height,
                        int mouseX, int mouseY, boolean hovered, float delta);
    int getHeight(int width);
    boolean mouseClicked(MouseButtonEvent event, boolean doubleClick);
    List<Component> getTooltip(int mouseX, int mouseY);
}
