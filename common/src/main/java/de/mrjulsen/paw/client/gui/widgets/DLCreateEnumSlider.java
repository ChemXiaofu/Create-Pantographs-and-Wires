package de.mrjulsen.paw.client.gui.widgets;

import java.util.function.BiConsumer;

import de.mrjulsen.paw.PantographsAndWires;
import de.mrjulsen.paw.client.gui.ModGuiUtils;
import de.mrjulsen.mcdragonlib.client.gui.widgets.DLSlider;
import de.mrjulsen.mcdragonlib.client.util.Graphics;
import de.mrjulsen.mcdragonlib.client.util.GuiUtils;
import de.mrjulsen.mcdragonlib.core.EAlignment;
import de.mrjulsen.mcdragonlib.core.ITranslatableEnum;
import de.mrjulsen.mcdragonlib.util.TextUtils;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class DLCreateEnumSlider<T extends Enum<T> & IIconEnum & ITranslatableEnum> extends DLCreateSlider {

    private final Class<T> clazz;

    public DLCreateEnumSlider(Screen parent, int x, int y, int width, Class<T> clazz, T currentValue, BiConsumer<DLSlider, T> onUpdateMessage) {
        super(parent, x, y, width, TextUtils.translate(currentValue.getEnumTranslationKey(PantographsAndWires.MOD_ID)), TextUtils.empty(), 0, clazz.getEnumConstants().length - 1, currentValue.ordinal(), 1, 1, (s) -> {
            onUpdateMessage.accept(s, clazz.getEnumConstants()[s.getValueInt()]);
        });
        this.clazz = clazz;
    }

    @Override
    public void renderMainLayer(Graphics graphics, int mouseX, int mouseY, float partialTick) {
        GuiUtils.drawTexture(TEXTURE, graphics, x(), y(), 0, 0, 7, 8);
        GuiUtils.drawTexture(TEXTURE, graphics, x() + width() - 7, y(), 0, 0, 7, 8);
        GuiUtils.drawTexture(TEXTURE, graphics, x() + 7, y(), 7, 0, width() - 14, 8);

        int x = x() + (int)(this.value * (double)(this.getWidth() - 8));
        GuiUtils.drawTexture(TEXTURE, graphics, x + 4 - 22 / 2, y() - 7, 0, 43, 22, 22);
        clazz.getEnumConstants()[getValueInt()].getIcon().render(graphics, x + 4 - 16 / 2, y() - 4);
        
        if (isScrolling && parent.isDragging()) {
            Component valueTxt = TextUtils.translate(clazz.getEnumConstants()[getValueInt()].getValueTranslationKey(PantographsAndWires.MOD_ID));
            int halfWidth = Math.max(font.width(prefix) / 2, font.width(valueTxt) / 2);
            ModGuiUtils.renderRoundedBox(graphics, x + 4 - halfWidth - 3, y() - 7 - font.lineHeight * 2 - 6, halfWidth * 2 + 6, font.lineHeight * 2 + 5, 0x77000000);
            GuiUtils.drawString(graphics, font, x + 4, y() - 7 - font.lineHeight - 2, valueTxt, 0xFF94B5DD, EAlignment.CENTER, false);
            GuiUtils.drawString(graphics, font, x + 4, y() - 7 - font.lineHeight * 2 - 4, prefix, 0xFFFFFFFF, EAlignment.CENTER, false);
        }

        GuiUtils.resetTint();
    }
}
