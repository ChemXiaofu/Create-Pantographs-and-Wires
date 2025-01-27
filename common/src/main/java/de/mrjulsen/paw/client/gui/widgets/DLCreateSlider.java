package de.mrjulsen.paw.client.gui.widgets;

import java.util.function.Consumer;

import de.mrjulsen.paw.client.gui.ModGuiUtils;
import de.mrjulsen.mcdragonlib.client.ITickable;
import de.mrjulsen.mcdragonlib.client.gui.widgets.DLSlider;
import de.mrjulsen.mcdragonlib.client.util.Graphics;
import de.mrjulsen.mcdragonlib.client.util.GuiUtils;
import de.mrjulsen.mcdragonlib.core.EAlignment;
import de.mrjulsen.mcdragonlib.util.TextUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class DLCreateSlider extends DLSlider implements ITickable {

    protected static final ResourceLocation TEXTURE = new ResourceLocation("create:textures/gui/value_settings.png");
    protected final Font font = Minecraft.getInstance().font;

    protected final Screen parent;
    protected boolean isScrolling;
    protected Component title;

    public DLCreateSlider(Screen parent, int x, int y, int width, Component title, Component suffix, double minValue, double maxValue, double currentValue, double stepSize, int precision, Consumer<DLSlider> onUpdateMessage) {
        super(x, y, width, 8, title, suffix, minValue, maxValue, currentValue, stepSize, precision, false, onUpdateMessage);
        this.parent = parent;
    }

    @Override
    public void renderMainLayer(Graphics graphics, int mouseX, int mouseY, float partialTick) {
        GuiUtils.drawTexture(TEXTURE, graphics, x(), y(), 0, 0, 7, 8);
        GuiUtils.drawTexture(TEXTURE, graphics, x() + width() - 7, y(), 0, 0, 7, 8);
        GuiUtils.drawTexture(TEXTURE, graphics, x() + 7, y(), 7, 0, width() - 14, 8);

        String txt = "" + getValueInt();
        int textWidth = Math.max(font.width(String.valueOf((int)minValue)), font.width(String.valueOf((int)maxValue))) + 4;
        int x = x() + (int)(this.value * (double)(this.getWidth() - 8));
        GuiUtils.drawTexture(TEXTURE, graphics, x + 4 - textWidth / 2, y() - 3, 4, 9, textWidth, 14);
        GuiUtils.drawTexture(TEXTURE, graphics, x + 4 - textWidth / 2 - 3, y() - 3, 0, 9, 3, 14);
        GuiUtils.drawTexture(TEXTURE, graphics, x + 4 + textWidth / 2, y() - 3, 61, 9, 3, 14);
        GuiUtils.drawString(graphics, font, x + 4, y(), txt, 0xFF442000, EAlignment.CENTER, false);

        if (isScrolling && parent.isDragging()) {
            Component valueTxt = TextUtils.text(String.valueOf(getValueString())).withStyle(suffix.getStyle()).append(suffix);
            int halfWidth = Math.max(font.width(prefix) / 2, font.width(valueTxt) / 2);
            ModGuiUtils.renderRoundedBox(graphics, x + 4 - halfWidth - 3, y() - 4 - font.lineHeight * 2 - 6, halfWidth * 2 + 6, font.lineHeight * 2 + 5, 0x77000000);
            GuiUtils.drawString(graphics, font, x + 4, y() - 4 - font.lineHeight - 2, valueTxt, 0xFF94B5DD, EAlignment.CENTER, false);
            GuiUtils.drawString(graphics, font, x + 4, y() - 4 - font.lineHeight * 2 - 4, prefix, 0xFFFFFFFF, EAlignment.CENTER, false);
        }

        GuiUtils.resetTint();
    }

    @Override
    protected void onDrag(double mouseX, double mouseY, double dragX, double dragY) {
        isScrolling = true;
        super.onDrag(mouseX, mouseY, dragX, dragY);
    }

    @Override
    public void tick() {
        if (!parent.isDragging()) {
            isScrolling = false;
        }
    }
}
