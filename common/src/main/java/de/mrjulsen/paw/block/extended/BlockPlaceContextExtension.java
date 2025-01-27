package de.mrjulsen.paw.block.extended;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public interface BlockPlaceContextExtension {
    BlockPos getPlacedOnPos();
    BlockState getPlacedOnState();
    void setPlacedOnPos(BlockPos pos);
    void setPlacedOnState(BlockState state);
}
