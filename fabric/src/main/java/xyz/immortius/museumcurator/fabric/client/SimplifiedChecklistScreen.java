package xyz.immortius.museumcurator.fabric.client;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import xyz.immortius.museumcurator.common.MuseumCuratorConstants;
import xyz.immortius.museumcurator.common.data.CollectionItem;
import xyz.immortius.museumcurator.common.data.MuseumCollection;
import xyz.immortius.museumcurator.common.data.MuseumCollections;
import xyz.immortius.museumcurator.common.data.MuseumExhibit;
import xyz.immortius.museumcurator.common.menus.MuseumChecklistMenu;
import xyz.immortius.museumcurator.common.network.ChecklistChangeRequest;

import java.util.List;
import java.util.Comparator;

/**
 * Minecraft 26.x checklist screen using the mod's original collection/exhibit layout.
 */
public final class SimplifiedChecklistScreen extends AbstractContainerScreen<MuseumChecklistMenu> {
    private static final Identifier CHECKLIST_TEXTURE =
            Identifier.fromNamespaceAndPath(MuseumCuratorConstants.MOD_ID, "textures/gui/container/checklist.png");
    private static final int TEXTURE_SIZE = 512;
    private static final int VIEW_X = 16;
    private static final int VIEW_Y = 46;
    private static final int VIEW_WIDTH = 226;
    private static final int VIEW_HEIGHT = 173;
    private static final int CELL = 20;
    private static final int ITEM_SIZE = 18;
    private static final int TITLE_HEIGHT = 12;
    private static final int ITEMS_PER_ROW = VIEW_WIDTH / CELL;
    private static final int COLLECTIONS_PER_PAGE = 7;

    private List<MuseumCollection> collections = List.of();
    private MuseumCollection selectedCollection;
    private int currentPage;
    private int scrollAmount;

    public SimplifiedChecklistScreen(MuseumChecklistMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 256, 256);
        this.titleLabelX = 14;
        this.titleLabelY = 34;
        refreshCollections();
    }

    @Override
    protected void init() {
        super.init();
        addRenderableWidget(Button.builder(Component.translatable("gui.back"), button -> {
            if (selectedCollection != null) {
                selectedCollection = null;
                scrollAmount = 0;
            } else {
                onClose();
            }
        }).bounds(leftPos + 96, topPos + 222, 64, 20).build());
    }

    private void refreshCollections() {
        List<MuseumCollection> latest = MuseumCollections.getCollections();
        if (latest.size() != collections.size() || !latest.equals(collections)) {
            collections = latest.stream().sorted(Comparator.comparing(c -> c.getName().getString())).toList();
            currentPage = Math.min(currentPage, Math.max(0, pageCount() - 1));
        }
    }

    private int pageCount() {
        return Math.max(1, (collections.size() + COLLECTIONS_PER_PAGE - 1) / COLLECTIONS_PER_PAGE);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, CHECKLIST_TEXTURE, leftPos, topPos,
                0, 0, imageWidth, imageHeight, TEXTURE_SIZE, TEXTURE_SIZE);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractRenderState(graphics, mouseX, mouseY, delta);
        refreshCollections();

        Component header = selectedCollection == null
                ? Component.literal("Museum Curator")
                : selectedCollection.getName();
        graphics.centeredText(font, header, width / 2, topPos + 18, 0xFF404040);

        if (selectedCollection == null) {
            drawCollectionOverview(graphics, mouseX, mouseY);
        } else {
            drawCollectionContents(graphics);
        }
    }

    private void drawCollectionOverview(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        if (collections.isEmpty()) {
            graphics.centeredText(font, Component.literal("Keine Sammlungen geladen"),
                    width / 2, topPos + 88, 0xFF404040);
            graphics.centeredText(font, Component.literal("Die Daten werden beim Weltbeitritt synchronisiert."),
                    width / 2, topPos + 103, 0xFF6A6254);
            return;
        }

        int first = currentPage * COLLECTIONS_PER_PAGE;
        int last = Math.min(collections.size(), first + COLLECTIONS_PER_PAGE);
        for (int i = first; i < last; i++) {
            MuseumCollection collection = collections.get(i);
            int row = i - first;
            int x = leftPos + VIEW_X;
            int y = topPos + 48 + row * 24;
            boolean hovered = mouseX >= x && mouseX < x + VIEW_WIDTH && mouseY >= y && mouseY < y + 20;

            graphics.fill(x - 1, y - 1, x + VIEW_WIDTH + 1, y + 21, 0xFF35210F);
            graphics.fill(x, y, x + VIEW_WIDTH, y + 20, hovered ? 0xFFFFF0CD : 0xFFFFE8BA);

            int total = collection.getExhibits().stream().mapToInt(e -> e.getItems().size()).sum();
            long checked = collection.getExhibits().stream()
                    .mapToLong(e -> MuseumCollections.countChecked(e.getItems())).sum();
            Component label = Component.literal(collection.getName().getString()
                    + " (" + checked + " / " + total + ")");
            graphics.text(font, label, x + 5, y + 6, checked == total && total > 0 ? 0xFF926000 : 0xFF30271D, false);
        }

        if (currentPage > 0) {
            graphics.centeredText(font, Component.literal("‹"), leftPos + 18, topPos + 225, 0xFF404040);
        }
        if (currentPage + 1 < pageCount()) {
            graphics.centeredText(font, Component.literal("›"), leftPos + 236, topPos + 225, 0xFF404040);
        }
    }

    private void drawCollectionContents(GuiGraphicsExtractor graphics) {
        int viewLeft = leftPos + VIEW_X;
        int viewTop = topPos + VIEW_Y;
        graphics.enableScissor(viewLeft, viewTop, viewLeft + VIEW_WIDTH, viewTop + VIEW_HEIGHT);

        int contentY = 0;
        for (MuseumExhibit exhibit : selectedCollection.getExhibits()) {
            int exhibitHeight = exhibitHeight(exhibit);
            int y = viewTop + contentY - scrollAmount;
            Component exhibitTitle = Component.literal("").append(exhibit.getName())
                    .append("  (" + MuseumCollections.countChecked(exhibit.getItems()) + " / "
                            + exhibit.getItems().size() + ")");
            graphics.text(font, exhibitTitle, viewLeft, y, 0xFF404040, false);

            for (int i = 0; i < exhibit.getItems().size(); i++) {
                int col = i % ITEMS_PER_ROW;
                int row = i / ITEMS_PER_ROW;
                int x = viewLeft + col * CELL;
                int itemY = y + TITLE_HEIGHT + row * CELL;
                CollectionItem item = exhibit.getItems().get(i);
                graphics.item(item.itemStack(), x + 1, itemY + 1);
                graphics.blit(RenderPipelines.GUI_TEXTURED, CHECKLIST_TEXTURE, x, itemY,
                        MuseumCollections.isChecked(item) ? ITEM_SIZE : 0, 256,
                        ITEM_SIZE, ITEM_SIZE, TEXTURE_SIZE, TEXTURE_SIZE);
            }
            contentY += exhibitHeight;
        }

        graphics.disableScissor();
    }

    private int exhibitHeight(MuseumExhibit exhibit) {
        int rows = Math.max(1, (exhibit.getItems().size() + ITEMS_PER_ROW - 1) / ITEMS_PER_ROW);
        return TITLE_HEIGHT + rows * CELL + 2;
    }

    private int contentHeight() {
        if (selectedCollection == null) return 0;
        return selectedCollection.getExhibits().stream().mapToInt(this::exhibitHeight).sum();
    }

    private CollectionItem collectionItemAt(int mouseX, int mouseY) {
        int relativeX = mouseX - (leftPos + VIEW_X);
        int relativeY = mouseY - (topPos + VIEW_Y) + scrollAmount;
        if (relativeX < 0 || relativeX >= VIEW_WIDTH || relativeY < 0
                || mouseY < topPos + VIEW_Y || mouseY >= topPos + VIEW_Y + VIEW_HEIGHT) {
            return null;
        }

        int contentY = 0;
        for (MuseumExhibit exhibit : selectedCollection.getExhibits()) {
            int height = exhibitHeight(exhibit);
            if (relativeY >= contentY + TITLE_HEIGHT && relativeY < contentY + height) {
                int localY = relativeY - contentY - TITLE_HEIGHT;
                int col = relativeX / CELL;
                if (relativeX % CELL >= ITEM_SIZE || localY % CELL >= ITEM_SIZE) return null;
                int index = (localY / CELL) * ITEMS_PER_ROW + col;
                return index >= 0 && index < exhibit.getItems().size() ? exhibit.getItems().get(index) : null;
            }
            contentY += height;
        }
        return null;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        int mouseX = (int) event.x();
        int mouseY = (int) event.y();

        if (event.button() == 0) {
            if (selectedCollection == null) {
                int first = currentPage * COLLECTIONS_PER_PAGE;
                int last = Math.min(collections.size(), first + COLLECTIONS_PER_PAGE);
                for (int i = first; i < last; i++) {
                    int row = i - first;
                    int x = leftPos + VIEW_X;
                    int y = topPos + 48 + row * 24;
                    if (mouseX >= x && mouseX < x + VIEW_WIDTH && mouseY >= y && mouseY < y + 20) {
                        selectedCollection = collections.get(i);
                        scrollAmount = 0;
                        return true;
                    }
                }
                if (mouseY >= topPos + 216 && mouseY <= topPos + 238) {
                    if (mouseX <= leftPos + 32 && currentPage > 0) {
                        currentPage--;
                        return true;
                    }
                    if (mouseX >= leftPos + 220 && currentPage + 1 < pageCount()) {
                        currentPage++;
                        return true;
                    }
                }
            } else {
                CollectionItem item = collectionItemAt(mouseX, mouseY);
                if (item != null) {
                    ItemStack stack = item.itemStack().copy();
                    if (MuseumCollections.isChecked(item)) {
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
        if (selectedCollection == null) {
            if (mouseX >= leftPos && mouseX <= leftPos + imageWidth
                    && mouseY >= topPos && mouseY <= topPos + imageHeight) {
                int maxPage = Math.max(0, pageCount() - 1);
                currentPage = Math.max(0, Math.min(maxPage, currentPage + (scrollY < 0 ? 1 : -1)));
                return true;
            }
        } else if (mouseX >= leftPos + VIEW_X && mouseX <= leftPos + VIEW_X + VIEW_WIDTH
                && mouseY >= topPos + VIEW_Y && mouseY <= topPos + VIEW_Y + VIEW_HEIGHT) {
            int maxScroll = Math.max(0, contentHeight() - VIEW_HEIGHT);
            scrollAmount = Math.max(0, Math.min(maxScroll, scrollAmount - (int) (scrollY * CELL)));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }
}
