package xyz.immortius.museumcurator.client.screens;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import xyz.immortius.museumcurator.common.MuseumCuratorConstants;
import xyz.immortius.museumcurator.common.menus.MuseumChecklistMenu;

public abstract class AbstractChecklistScreen extends AbstractContainerScreen<MuseumChecklistMenu> {
    public static final Identifier CONTAINER_TEXTURE = Identifier.fromNamespaceAndPath(MuseumCuratorConstants.MOD_ID, "textures/gui/container/checklist.png");
    public static final int TEXTURE_DIM = 512;
    protected final Inventory playerInventory;
    private final Screen lastScreen;

    public AbstractChecklistScreen(MuseumChecklistMenu menu, Inventory inventory, Component title, Screen lastScreen) {
        super(menu, inventory, title, 256, 256);
        this.titleLabelX = 14;
        this.titleLabelY = 34;
        this.playerInventory = inventory;
        this.lastScreen = lastScreen;
    }

    @Override
    protected void init() {
        super.init();
        addRenderableWidget(Button.builder(Component.translatable("gui.back"),
                button -> this.minecraft.gui.setScreen(lastScreen))
                .bounds((width - imageWidth) / 2 + 96, topPos + 222, 64, 20).build());
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        graphics.centeredText(this.font, this.title, width / 2, topPos + 18, 0x404040);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, CONTAINER_TEXTURE, leftPos, topPos,
                0, 0, imageWidth, imageHeight, TEXTURE_DIM, TEXTURE_DIM);
    }
}
