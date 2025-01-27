package de.mrjulsen.paw.event;

import de.mrjulsen.mcdragonlib.client.gui.DLScreen;
import de.mrjulsen.paw.client.gui.screens.CantileverSettingsScreen;
import net.minecraft.world.item.ItemStack;

public class ClientWrapper {

    public static void showCantileverSettingsScreen(ItemStack stack) {
        DLScreen.setScreen(new CantileverSettingsScreen(stack));
    }
    
}
