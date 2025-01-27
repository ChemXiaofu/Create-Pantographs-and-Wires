package de.mrjulsen.paw.forge;

import de.mrjulsen.paw.PantographsAndWires;
import dev.architectury.platform.forge.EventBuses;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(PantographsAndWires.MOD_ID)
public class PantographsAndWiresForge {
    public PantographsAndWiresForge() {
        // Submit our event bus to let architectury register our content on the right time
        EventBuses.registerModEventBus(PantographsAndWires.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());
        PantographsAndWires.load();
        PantographsAndWires.REGISTRATE.registerEventListeners(FMLJavaModLoadingContext.get().getModEventBus());
        PantographsAndWires.init();
    }
}
