package de.mrjulsen.paw.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public abstract class AbstractPlaceableStandingInsulatorBlock extends AbstractPlaceableInsulatorBlock {

    public AbstractPlaceableStandingInsulatorBlock(Properties properties) {
        super(properties);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockPos clickPos = context.getClickedPos();
        Level level = context.getLevel();
        BlockState state = super.getStateForPlacement(context);
        BlockPos belowPos = clickPos.below();
        BlockState belowState = level.getBlockState(belowPos);
        
        if (belowState.getBlock() instanceof PowerLineBracketBlock) {
            state = state
                .setValue(MULTIPART_SEGMENT, belowState.getValue(MULTIPART_SEGMENT))
                .setValue(FACING, belowState.getValue(FACING))
                .setValue(ROTATION, belowState.getValue(ROTATION))
            ;
        }
        return state;
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockPos belowPos = pos.below();
        BlockState belowState = level.getBlockState(belowPos);
        return canSupportCenter(level, belowPos, Direction.UP) || (belowState.getBlock() instanceof PowerLineBracketBlock);
    }

    @SuppressWarnings("deprecation")
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos currentPos, BlockPos neighborPos) {
        return !this.canSurvive(state, level, currentPos) ? Blocks.AIR.defaultBlockState() : super.updateShape(state, direction, neighborState, level, currentPos, neighborPos);
    }
}
