package de.mrjulsen.paw.block;

import java.util.Objects;

import de.mrjulsen.paw.block.abstractions.AbstractMultipartPostBlock;
import de.mrjulsen.paw.block.property.EPostPart;
import de.mrjulsen.mcdragonlib.data.MapCache;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class FlatLatticeMastBlock extends AbstractMultipartPostBlock {

    protected static final VoxelShape SHAPE_Z = Block.box(6, 0, 4, 10, 16, 12);
    protected static final VoxelShape SHAPE_X = Block.box(4, 0, 6, 12, 16, 10);
    protected static final VoxelShape SHAPE_FOUNDATION_Z = Block.box(4, -4, 2, 12, 4, 14);
    protected static final VoxelShape SHAPE_FOUNDATION_X = Block.box(2, -4, 4, 14, 4, 12);

    protected static record ShapeKey(Direction direction, boolean isFoundation) {
        @Override
        public final int hashCode() {
            return Objects.hash(direction(), isFoundation());
        }

        @Override
        public final boolean equals(Object other) {
            if (other instanceof ShapeKey o) {
                return direction() == o.direction() && isFoundation() == o.isFoundation();
            }
            return false;
        }
    }

    protected static final MapCache<VoxelShape, ShapeKey, ShapeKey> shapeCache = new MapCache<>((key) -> {
        VoxelShape shape = key.direction().getAxis() == Axis.Z ? SHAPE_Z : SHAPE_X;
        if (key.isFoundation()) {
            shape = key.direction().getAxis() == Axis.Z ? Shapes.or(shape, SHAPE_FOUNDATION_Z) : Shapes.or(shape, SHAPE_FOUNDATION_X);
        }
        return shape;
    }, ShapeKey::hashCode);
    
    public FlatLatticeMastBlock(Properties properties) {
        super(properties
            .mapColor(MapColor.METAL)
        );
    }

    @Override
    public VoxelShape getBaseShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        ShapeKey key = new ShapeKey(state.getValue(FACING), state.getValue(PART) == EPostPart.BOTTOM);
        return shapeCache.get(key, key);
    }

    @Override
    public EPostType postConnectionType(LevelReader level, BlockState state, BlockPos pos, BlockState cantileverState, BlockPos cantileverPos) {
        return EPostType.WALL;
    }
}
