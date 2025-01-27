package de.mrjulsen.paw.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public abstract class AbstractPlaceableHangingInsulatorBlock extends AbstractPlaceableInsulatorBlock {

    public AbstractPlaceableHangingInsulatorBlock(Properties properties) {
        super(properties);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockPos clickPos = context.getClickedPos();
        Level level = context.getLevel();
        BlockState state = super.getStateForPlacement(context);
        BlockPos abovePos = clickPos.above();
        BlockState aboveState = level.getBlockState(abovePos);
        
        if (aboveState.getBlock() instanceof PowerLineBracketBlock) {
            state = state
                .setValue(MULTIPART_SEGMENT, aboveState.getValue(MULTIPART_SEGMENT))
                .setValue(FACING, aboveState.getValue(FACING))
                .setValue(ROTATION, aboveState.getValue(ROTATION))
            ;
        }
        return state;
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockPos abovePos = pos.above();
        BlockState aboveState = level.getBlockState(abovePos);
        return canSupportCenter(level, abovePos, Direction.DOWN) || (aboveState.getBlock() instanceof PowerLineBracketBlock);
    }

    @SuppressWarnings("deprecation")
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos currentPos, BlockPos neighborPos) {
        return !this.canSurvive(state, level, currentPos) ? Blocks.AIR.defaultBlockState() : super.updateShape(state, direction, neighborState, level, currentPos, neighborPos);
    }
}
