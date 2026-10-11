package xyz.immortius.museumcurator.fabric.client;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import xyz.immortius.museumcurator.common.data.CollectionItem;
import xyz.immortius.museumcurator.common.data.MuseumCollection;
import xyz.immortius.museumcurator.common.data.MuseumCollections;
import xyz.immortius.museumcurator.common.data.MuseumExhibit;
import xyz.immortius.museumcurator.common.menus.MuseumChecklistMenu;
import xyz.immortius.museumcurator.common.network.ChecklistChangeRequest;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

import java.util.ArrayList;
import java.util.List;

/**
 * Lightweight 26.x-native checklist UI. Displays collection items in a scrollable grid.
 */
public final class SimplifiedChecklistScreen extends AbstractContainerScreen<MuseumChecklistMenu> {
    private static final int COLS = 8;
    private static final int ROWS = 6;
    private static final int CELL = 20;
    private static final int GRID_X = 8;
    private static final int GRID_Y = 28;
    private final List<CollectionItem> items = new ArrayList<>();
    private int scrollRow;

    public SimplifiedChecklistScreen(MuseumChecklistMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        for (MuseumCollection collection : MuseumCollections.getCollections()) {
            for (MuseumExhibit exhibit : collection.getExhibits()) {
                items.addAll(exhibit.getItems());
            }
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractRenderState(graphics, mouseX, mouseY, delta);
        graphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, 0xFF20242A);
        graphics.fill(leftPos + 1, topPos + 1, leftPos + imageWidth - 1, topPos + imageHeight - 1, 0xFF343A42);
        int checked = 0;
        for (CollectionItem item : items) {
            if (MuseumCollections.isChecked(item)) checked++;
        }
        graphics.centeredText(font, Component.literal("Museum Curator"), leftPos + imageWidth / 2, topPos + 7, 0xFFFFFFFF);
        graphics.text(font, Component.literal(checked + " / " + items.size() + " gesammelt"), leftPos + GRID_X, topPos + 17, 0xFFE3E8EF, false);

        int start = scrollRow * COLS;
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                int index = start + row * COLS + col;
                if (index >= items.size()) break;
                int x = leftPos + GRID_X + col * CELL;
                int y = topPos + GRID_Y + row * CELL;
                CollectionItem entry = items.get(index);
                boolean isChecked = MuseumCollections.isChecked(entry);
                graphics.fill(x, y, x + 18, y + 18, isChecked ? 0xFF426D4B : 0xFF16191E);
                graphics.item(entry.itemStack(), x + 1, y + 1);
                if (isChecked) {
                    graphics.text(font, "✓", x + 10, y + 9, 0xFF8CFF9B, true);
                }
            }
        }
        graphics.text(font, Component.literal("Klicken: abhaken  |  Mausrad: scrollen"), leftPos + GRID_X, topPos + imageHeight - 12, 0xFFE3E8EF, false);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() == 0) {
            int relX = (int) event.x() - (leftPos + GRID_X);
            int relY = (int) event.y() - (topPos + GRID_Y);
            if (relX >= 0 && relX < COLS * CELL && relY >= 0 && relY < ROWS * CELL) {
                int col = relX / CELL;
                int row = relY / CELL;
                int index = scrollRow * COLS + row * COLS + col;
                if (index >= 0 && index < items.size()) {
                    ItemStack stack = items.get(index).itemStack().copy();
                    if (MuseumCollections.isChecked(items.get(index))) {
                        MuseumCollections.uncheckItems(List.of(stack));
                        ClientPlayNetworking.send(ChecklistChangeRequest.uncheck(stack));
                    } else {
                        MuseumCollections.checkItems(List.of(stack));
                        ClientPlayNetworking.send(ChecklistChangeRequest.check(stack));
                    }
                    return true;
                }
            }
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (mouseX >= leftPos && mouseX <= leftPos + imageWidth && mouseY >= topPos && mouseY <= topPos + imageHeight) {
            int totalRows = (items.size() + COLS - 1) / COLS;
            int maxScroll = Math.max(0, totalRows - ROWS);
            scrollRow = Math.max(0, Math.min(maxScroll, scrollRow + (scrollY < 0 ? 1 : -1)));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }
}
