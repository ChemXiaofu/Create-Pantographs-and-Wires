package de.mrjulsen.paw.block.property;

import net.minecraft.util.StringRepresentable;

public enum EInsulatorType implements StringRepresentable {
    GREEN("green"),
    BROWN("brown");

    final String name;

    EInsulatorType(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}