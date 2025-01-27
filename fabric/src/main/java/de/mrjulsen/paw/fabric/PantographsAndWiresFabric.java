package de.mrjulsen.paw.fabric;


import de.mrjulsen.paw.PantographsAndWires;
import net.fabricmc.api.ModInitializer;

public class PantographsAndWiresFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        PantographsAndWires.load();
        PantographsAndWires.init();
        PantographsAndWires.REGISTRATE.register();
        
    }
}
