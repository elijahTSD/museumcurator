package xyz.immortius.museumcurator.client.uielements;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ScrollContainerWidget extends AbstractWidget {
    private final List<ScrollContainerEntry> entries = new ArrayList<>();
    private double scrollAmount;

    public ScrollContainerWidget(int x, int y, int width, int height, List<? extends ScrollContainerEntry> entries) {
        super(x, y, width, height, Component.empty());
        this.entries.addAll(entries);
    }

    public void addEntry(ScrollContainerEntry entry) {
        entries.add(entry);
    }

    private int getContentHeight() {
        return entries.stream().map(e -> e.getHeight(getWidth())).reduce(0, Integer::sum);
    }

    private double maxScroll() {
        return Math.max(0, getContentHeight() - getHeight());
    }

    private void setScrollAmount(double amount) {
        this.scrollAmount = Mth.clamp(amount, 0.0, maxScroll());
    }

    public List<Component> getTooltip(int mouseX, int mouseY) {
        int relativeY = (int) (mouseY - getY() + scrollAmount);
        int top = 0;
        for (ScrollContainerEntry entry : entries) {
            int rowHeight = entry.getHeight(getWidth());
            if (relativeY >= top && relativeY < top + rowHeight) {
                return entry.getTooltip(mouseX, (int) (mouseY + scrollAmount));
            }
            top += rowHeight;
        }
        return Collections.emptyList();
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        graphics.enableScissor(getX(), getY(), getX() + getWidth(), getY() + getHeight());
        int top = 0;
        for (ScrollContainerEntry entry : entries) {
            int rowHeight = entry.getHeight(getWidth());
            int y = getY() + top - (int) scrollAmount;
            if (y + rowHeight >= getY() && y <= getY() + getHeight()) {
                entry.extractContent(graphics, y, getX(), getWidth(), rowHeight, mouseX, mouseY,
                        mouseY >= y && mouseY < y + rowHeight, partialTick);
            }
            top += rowHeight;
        }
        graphics.disableScissor();
    }

    @Override
    public void onClick(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() != 0) return;
        int relativeY = (int) (event.y() - getY() + scrollAmount);
        int top = 0;
        for (ScrollContainerEntry entry : entries) {
            int rowHeight = entry.getHeight(getWidth());
            if (relativeY >= top && relativeY < top + rowHeight) {
                entry.mouseClicked(event, doubleClick);
                return;
            }
            top += rowHeight;
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (!isMouseOver(mouseX, mouseY)) return false;
        setScrollAmount(scrollAmount - scrollY * 20.0);
        return true;
    }

    public void scrollUp() { setScrollAmount(scrollAmount - 20); }
    public void scrollDown() { setScrollAmount(scrollAmount + 20); }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
    }
}
