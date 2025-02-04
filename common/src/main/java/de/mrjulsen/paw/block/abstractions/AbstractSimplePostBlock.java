package de.mrjulsen.paw.block.abstractions;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

public abstract class AbstractSimplePostBlock extends AbstractRotatableBlock implements IHorizontalExtensionConnectable, ICantileverConnectableBlock {
    
    public AbstractSimplePostBlock(Properties properties) {
        super(properties);
    }

    @Override
    public boolean canCantileverConnect(BlockAndTintGetter level, BlockPos pos, BlockState state, Direction direction) {
        return true;
    }
}
