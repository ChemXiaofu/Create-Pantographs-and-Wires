package de.mrjulsen.paw.item;

import de.mrjulsen.wires.IWireType;
import net.minecraft.resources.ResourceLocation;

public abstract class AbstractWireType implements IWireType {
    private final ResourceLocation location;

    public AbstractWireType(ResourceLocation location) {
        this.location = location;
    }

    @Override
    public final ResourceLocation getRegistryId() {
        return location;
    }
}
