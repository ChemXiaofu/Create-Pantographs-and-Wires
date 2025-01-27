package de.mrjulsen.paw.util;

import net.minecraft.world.level.block.state.BlockState;

public final class MixinVar {
    private MixinVar() {}

    public static BlockState oldState = null;
}
