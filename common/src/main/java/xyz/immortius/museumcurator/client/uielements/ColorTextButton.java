package xyz.immortius.museumcurator.client.uielements;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;

/** Button that lets the mod choose a custom active/inactive text color. */
public class ColorTextButton extends Button {
    private static final WidgetSprites SPRITES = new WidgetSprites(
            Identifier.withDefaultNamespace("widget/button"),
            Identifier.withDefaultNamespace("widget/button_disabled"),
            Identifier.withDefaultNamespace("widget/button_highlighted"));
    private int activeTextColor = 0xFFFFFF;
    private int textColor = 0xA0A0A0;

    public ColorTextButton(int x, int y, int width, int height, Component label, OnPress onPress) {
        this(x, y, width, height, label, onPress, DEFAULT_NARRATION);
    }

    public ColorTextButton(int x, int y, int width, int height, Component label, OnPress onPress, CreateNarration narration) {
        super(x, y, width, height, label, onPress, narration);
    }

    @Override
    protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SPRITES.get(this.active, this.isHoveredOrFocused()),
                getX(), getY(), getWidth(), getHeight(), ARGB.white(this.alpha));
        int color = this.active ? activeTextColor : textColor;
        graphics.centeredText(Minecraft.getInstance().font, getMessage(),
                getX() + getWidth() / 2, getY() + (getHeight() - 8) / 2, color);
    }

    public void setTextColor(int textColor) { this.textColor = textColor; }
    public void setActiveTextColor(int activeTextColor) { this.activeTextColor = activeTextColor; }
}
