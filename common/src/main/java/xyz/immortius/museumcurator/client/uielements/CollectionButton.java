package xyz.immortius.museumcurator.client.uielements;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import xyz.immortius.museumcurator.common.data.MuseumCollection;
import xyz.immortius.museumcurator.common.data.MuseumCollections;

public class CollectionButton extends ColorTextButton {
    private final MuseumCollection collection;

    public CollectionButton(int x, int y, int width, int height, MuseumCollection collection, OnPress onPress) {
        super(x, y, width, height, collection.getName(), onPress);
        this.collection = collection;
    }

    public CollectionButton(int x, int y, int width, int height, MuseumCollection collection, OnPress onPress, CreateNarration narration) {
        super(x, y, width, height, collection.getName(), onPress, narration);
        this.collection = collection;
    }

    @Override
    protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        int totalCount = collection.getExhibits().stream().map(x -> x.getItems().size()).reduce(0, Integer::sum);
        int checkedCount = collection.getExhibits().stream().map(x -> MuseumCollections.countChecked(x.getItems())).reduce(0L, Long::sum).intValue();
        setMessage(Component.literal("").append(collection.getName()).append(" (" + checkedCount + " / " + totalCount + ")"));
        setActiveTextColor(checkedCount == totalCount ? 0xFFDD00 : 0xFFFFFF);
        super.extractContents(graphics, mouseX, mouseY, partialTick);
    }
}
