package de.mrjulsen.paw.block.property;

import java.util.Arrays;
import net.minecraft.util.StringRepresentable;

public enum EPostPart implements StringRepresentable {
    BOTTOM(-1, "bottom"),
    IN_BETWEEN(0, "in_between"),
    TOP(1, "top");

    int index;
    String name;

    EPostPart(int index, String name) {
        this.index = index;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public int getIndex() {
        return index;
    }

    public static EPostPart getByIndex(int index) {
        return Arrays.stream(values()).filter(x -> x.getIndex() == index).findFirst().orElse(IN_BETWEEN);
    }

    @Override
    public String getSerializedName() {
        return name;
    }
    
}
