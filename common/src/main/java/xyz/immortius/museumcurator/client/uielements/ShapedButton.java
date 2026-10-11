package xyz.immortius.museumcurator.client.uielements;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.function.Consumer;

public class ShapedButton extends AbstractWidget {
    private final Identifier texture;
    private final int textureDim;
    private final int normalX;
    private final int normalY;
    private final int overX;
    private final int overY;
    private final Consumer<ShapedButton> clickHandler;

    public ShapedButton(int x, int y, int width, int height, Component label, Identifier texture, int textureDim,
                        int normalX, int normalY, int overX, int overY, Consumer<ShapedButton> onClick) {
        super(x, y, width, height, label);
        this.texture = texture;
        this.textureDim = textureDim;
        this.normalX = normalX;
        this.normalY = normalY;
        this.overX = overX;
        this.overY = overY;
        this.clickHandler = onClick;
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        boolean hovered = isMouseOver(mouseX, mouseY);
        graphics.blit(RenderPipelines.GUI_TEXTURED, texture, getX(), getY(),
                hovered ? overX : normalX, hovered ? overY : normalY,
                getWidth(), getHeight(), textureDim, textureDim);
    }

    @Override
    public void onClick(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() == 0) clickHandler.accept(this);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
    }
}
