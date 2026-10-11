package xyz.immortius.museumcurator.client.screens;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import xyz.immortius.museumcurator.client.uielements.CollectionButton;
import xyz.immortius.museumcurator.client.uielements.ShapedButton;
import xyz.immortius.museumcurator.common.data.MuseumCollection;
import xyz.immortius.museumcurator.common.data.MuseumCollections;
import xyz.immortius.museumcurator.common.menus.MuseumChecklistMenu;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ChecklistOverviewScreen extends AbstractChecklistScreen {
    private static final int ITEMS_PER_PAGE = 7;
    private int currentPage;
    private final List<CollectionButton> collectionButtons = new ArrayList<>();
    private ShapedButton nextButton;
    private ShapedButton prevButton;

    public ChecklistOverviewScreen(MuseumChecklistMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, null);
    }

    @Override
    protected void init() {
        super.init();
        collectionButtons.clear();
        List<MuseumCollection> collections = MuseumCollections.getCollections().stream()
                .sorted(Comparator.comparing(c -> c.getName().getString())).toList();
        int index = 0;
        for (MuseumCollection collection : collections) {
            CollectionButton button = new CollectionButton(-1000, -1000, 192, 20, collection,
                    ignored -> Minecraft.getInstance().gui.setScreen(
                            new ChecklistCollectionScreen(menu, playerInventory, collection.getName(), collection, this)));
            collectionButtons.add(button);
            addRenderableWidget(button);
            index++;
        }

        prevButton = new ShapedButton(leftPos + 14, topPos + 226, 8, 13, Component.empty(),
                CONTAINER_TEXTURE, TEXTURE_DIM, 54, 256, 54, 269, ignored -> changePage(-1));
        nextButton = new ShapedButton(leftPos + 232, topPos + 226, 8, 13, Component.empty(),
                CONTAINER_TEXTURE, TEXTURE_DIM, 62, 256, 62, 269, ignored -> changePage(1));
        addRenderableWidget(prevButton);
        addRenderableWidget(nextButton);
        currentPage = 0;
        updatePageButtons();
    }

    private int pageCount() {
        return Math.max(1, (collectionButtons.size() + ITEMS_PER_PAGE - 1) / ITEMS_PER_PAGE);
    }

    private void changePage(int amount) {
        currentPage = Math.max(0, Math.min(pageCount() - 1, currentPage + amount));
        updatePageButtons();
    }

    private void updatePageButtons() {
        int first = currentPage * ITEMS_PER_PAGE;
        for (int i = 0; i < collectionButtons.size(); i++) {
            CollectionButton button = collectionButtons.get(i);
            if (i >= first && i < first + ITEMS_PER_PAGE) {
                button.setPosition(leftPos + 16 + 226 / 2 - 96, topPos + 48 + 24 * (i - first));
            } else {
                button.setPosition(-1000, -1000);
            }
        }
        if (prevButton != null) prevButton.setPosition(currentPage > 0 ? leftPos + 14 : -1000, currentPage > 0 ? topPos + 226 : -1000);
        if (nextButton != null) nextButton.setPosition(currentPage + 1 < pageCount() ? leftPos + 232 : -1000, currentPage + 1 < pageCount() ? topPos + 226 : -1000);
    }
}
