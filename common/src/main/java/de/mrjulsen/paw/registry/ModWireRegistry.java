package de.mrjulsen.paw.registry;

import de.mrjulsen.paw.PantographsAndWires;
import de.mrjulsen.paw.item.CatenaryWireType;
import de.mrjulsen.paw.item.PowerWireType;
import de.mrjulsen.wires.IWireType;
import de.mrjulsen.wires.WireTypeRegistry;

public class ModWireRegistry {
    
    public static final IWireType CATENARY_WIRE = WireTypeRegistry.register(PantographsAndWires.MOD_ID, "catenary_wire", CatenaryWireType::new);
    public static final IWireType ENERGY_WIRE = WireTypeRegistry.register(PantographsAndWires.MOD_ID, "energy_wire", PowerWireType::new);

    public static void init() {}
}
