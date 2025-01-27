package de.mrjulsen.paw.block;

import de.mrjulsen.paw.block.abstractions.AbstractMultipartPostBlock;
import de.mrjulsen.paw.block.property.EPostPart;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class LatticeMastBlock extends AbstractMultipartPostBlock {

    private final VoxelShape BASE_SHAPE = Block.box(2, 0, 2, 14, 16, 14);
    private final VoxelShape FOUNDATION_SHAPE = Shapes.or(BASE_SHAPE, Block.box(0.5d, -5, 0.5d, 15.5d, 4, 15.5d));

    public LatticeMastBlock(Properties properties) {
        super(properties
            .mapColor(MapColor.METAL)
        );
    }
    
    @Override
    public VoxelShape getBaseShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(PART) == EPostPart.BOTTOM ? FOUNDATION_SHAPE : BASE_SHAPE;
    }

    @Override
    public EPostType postConnectionType(LevelReader level, BlockState state, BlockPos pos, BlockState cantileverState, BlockPos cantileverPos) {
        return EPostType.LATTICE;
    }
}
