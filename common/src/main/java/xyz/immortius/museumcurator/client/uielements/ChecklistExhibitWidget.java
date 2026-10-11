package xyz.immortius.museumcurator.client.uielements;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import xyz.immortius.museumcurator.client.screens.AbstractChecklistScreen;
import xyz.immortius.museumcurator.common.data.CollectionItem;
import xyz.immortius.museumcurator.common.data.MuseumCollections;
import xyz.immortius.museumcurator.common.data.MuseumExhibit;
import xyz.immortius.museumcurator.common.network.ChecklistChangeRequest;
import xyz.immortius.museumcurator.interop.Services;

import java.util.Collections;
import java.util.List;

public class ChecklistExhibitWidget extends AbstractWidget {
    public static final int TITLE_HEIGHT = 12;
    public final MuseumExhibit exhibit;

    public ChecklistExhibitWidget(int x, int y, int width, int height, MuseumExhibit exhibit) {
        super(x, y, width, height, exhibit.getName());
        this.exhibit = exhibit;
    }

    public List<Component> getTooltip(int mouseX, int mouseY) {
        CollectionItem item = mouseOverItem(mouseX, mouseY);
        if (item != null && Minecraft.getInstance().level != null && Minecraft.getInstance().player != null) {
            return item.itemStack().getTooltipLines(Item.TooltipContext.of(Minecraft.getInstance().level),
                    Minecraft.getInstance().player, TooltipFlag.Default.NORMAL);
        }
        return null;
    }

    private CollectionItem mouseOverItem(int mouseX, int mouseY) {
        if (mouseY < getY() + TITLE_HEIGHT) return null;
        if ((mouseX - getX()) % 20 >= 18 || (mouseY - getY() - TITLE_HEIGHT) % 20 >= 18) return null;
        int row = (mouseY - getY() - TITLE_HEIGHT) / 20;
        int column = (mouseX - getX()) / 20;
        int perRow = itemsPerRow();
        int index = perRow * row + column;
        return index >= 0 && index < exhibit.getItems().size() ? exhibit.getItems().get(index) : null;
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        int offsetY = TITLE_HEIGHT;
        int offsetX = 0;
        long checked = MuseumCollections.countChecked(exhibit.getItems());
        Component title = Component.literal("").append(exhibit.getName())
                .append("  (" + checked + " / " + exhibit.getItems().size() + ")");
        graphics.text(Minecraft.getInstance().font, title, getX(), getY(), 0x404040, false);
        for (CollectionItem item : exhibit.getItems()) {
            if (offsetX + 18 > getWidth()) {
                offsetX = 0;
                offsetY += 20;
            }
            graphics.item(item.itemStack(), getX() + offsetX + 1, getY() + offsetY + 1);
            boolean checkedItem = MuseumCollections.isChecked(item);
            graphics.blit(RenderPipelines.GUI_TEXTURED, AbstractChecklistScreen.CONTAINER_TEXTURE,
                    getX() + offsetX, getY() + offsetY, checkedItem ? 18 : 0, 256,
                    18, 18, AbstractChecklistScreen.TEXTURE_DIM, AbstractChecklistScreen.TEXTURE_DIM);
            offsetX += 20;
        }
    }

    @Override
    public void onClick(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() != 0) return;
        CollectionItem item = mouseOverItem((int) event.x(), (int) event.y());
        if (item == null) return;
        if (MuseumCollections.isChecked(item)) {
            MuseumCollections.uncheckItems(Collections.singleton(item.itemStack()));
            Services.PLATFORM.sendClientChecklistChange(ChecklistChangeRequest.uncheck(item.itemStack()));
        } else {
            MuseumCollections.checkItems(Collections.singleton(item.itemStack()));
            Services.PLATFORM.sendClientChecklistChange(ChecklistChangeRequest.check(item.itemStack()));
        }
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
    }

    public int calcHeight() {
        int perRow = itemsPerRow();
        return TITLE_HEIGHT + 20 * (exhibit.getItems().size() / perRow
                + Integer.signum(exhibit.getItems().size() % perRow)) + 2;
    }

    private int itemsPerRow() {
        int perRow = Math.max(1, getWidth() / 20);
        if (getWidth() - perRow * 20 >= 18) perRow++;
        return perRow;
    }
}
