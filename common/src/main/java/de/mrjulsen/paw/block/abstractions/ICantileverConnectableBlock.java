package de.mrjulsen.paw.block.abstractions;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

public interface ICantileverConnectableBlock {
    boolean canCantileverConnect(BlockAndTintGetter level, BlockPos pos, BlockState state, Direction direction);
}
