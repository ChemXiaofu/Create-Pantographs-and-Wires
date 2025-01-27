package de.mrjulsen.paw.block;

import java.util.Objects;

import de.mrjulsen.paw.blockentity.CantileverBlockEntity;
import de.mrjulsen.paw.blockentity.IMultiblockBlockEntity;
import de.mrjulsen.paw.block.abstractions.AbstractCantileverBlock;
import de.mrjulsen.paw.registry.ModBlocks;
import de.mrjulsen.paw.util.Const;
import de.mrjulsen.paw.util.ModMath;
import de.mrjulsen.mcdragonlib.config.ECachingPriority;
import de.mrjulsen.mcdragonlib.data.MapCache;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class TensioningDeviceBlock extends AbstractCantileverBlock {

    public static final int HEIGHT = 6;

    public static final String NBT_TENSION = "Tension";

    public static final BooleanProperty HELPER = BooleanProperty.create("helper");

    private static record TransformationShapeKey(Direction direction, BlockState state) {
        @Override
        public final boolean equals(Object other) {
            if (other instanceof TransformationShapeKey o) {
                return direction().equals(o.direction());
            }
            return false;
        }
        @Override
        public final int hashCode() {
            return Objects.hash(direction());
        }
    }

    private static final VoxelShape DEFAULT_SHAPE = Block.box(0.5d, 0, 1.75d, 15.5d, 16, 18);
    private static final MapCache<VoxelShape, TransformationShapeKey, TransformationShapeKey> shapesCache = new MapCache<>((key) -> {
        VoxelShape baseShape = DEFAULT_SHAPE;        
        Direction direction = key.direction();
        VoxelShape shape = ModMath.rotateShape(baseShape, Axis.Y, (int)direction.getOpposite().toYRot());
        return shape;
    }, TransformationShapeKey::hashCode, ECachingPriority.ALWAYS);

    public TensioningDeviceBlock(Properties properties) {
        super(properties);
        registerDefaultState(this.defaultBlockState()
            .setValue(HELPER, false)
        );
    }

    @Override
    protected void createBlockStateDefinition(Builder<Block, BlockState> pBuilder) {        
        super.createBlockStateDefinition(pBuilder);
        pBuilder.add(HELPER);
    }

    @Override
    public RenderShape getRenderShape(BlockState pState) {
        return pState.getValue(HELPER) ? RenderShape.INVISIBLE : RenderShape.MODEL;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {     
        Level level = context.getLevel();   
        for (int i = 1; i <= HEIGHT; i++) {
            BlockPos p = context.getClickedPos().relative(Direction.DOWN, i);
            if (!level.getBlockState(p).canBeReplaced(context) || level.isOutsideBuildHeight(p)) {
                return null;
            }
        }
        return super.getStateForPlacement(context);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        for (int i = 1; i <= HEIGHT; i++) {
            BlockPos p = pos.relative(Direction.DOWN, i);
            level.setBlock(p, state.setValue(HELPER, true), 0, 0);
            if (level.getBlockEntity(p) instanceof CantileverBlockEntity be) {
                be.setOffset(1, i, 1);
            }
        }
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos currentPos, BlockPos neighborPos) {        
        return canSurvive(state, level, currentPos) ? super.updateShape(state, direction, neighborState, level, currentPos, neighborPos) : Blocks.AIR.defaultBlockState();
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof IMultiblockBlockEntity be) {
            int yOffset = be.getYOffset();
            BlockPos abovePos = pos.above();
            BlockPos belowPos = pos.below();
            boolean b1 = yOffset <= 0 || (level.getBlockState(abovePos).is(this) && level.getBlockEntity(abovePos) instanceof IMultiblockBlockEntity b && b.getYOffset() == yOffset - 1);
            boolean b2 = yOffset >= be.getYSize() || (level.getBlockState(belowPos).is(this) && level.getBlockEntity(belowPos) instanceof IMultiblockBlockEntity b && b.getYOffset() == yOffset + 1);
            if (!b1 || !b2) return false;
        }
        return super.canSurvive(state, level, pos);
    }

    @Override
    protected TagKey<Block> getSupportBlockTag() {
        return ModBlocks.TAG_TENSIONING_DEVICE_CONNECTABLE;
    }

    @Override
    public VoxelShape getBaseShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        TransformationShapeKey key = new TransformationShapeKey(state.getValue(FACING), state);
        return shapesCache.get(key, key);
    }

    @Override
    public Vec3 defaultWireAttachPoint(Level level, BlockPos pos, BlockState state, CompoundTag itemData, boolean firstPoint) {
        return new Vec3(Const.PIXEL * 3.5f - 0.5f, Const.PIXEL * 0.25, Const.PIXEL * 10.25f - 0.5f);
    }

    @Override
    public Vec3 tensionWireAttachPoint(Level level, BlockPos pos, BlockState state, CompoundTag itemData, boolean firstPoint) {
        return new Vec3(Const.PIXEL * 12.5f - 0.5f, Const.PIXEL * 10.25f, Const.PIXEL * 10.25f - 0.5f);
    }

    @Override
    public CompoundTag wireRenderData(Level level, BlockPos pos, BlockState state, CompoundTag itemData, boolean firstPoint) {
        CompoundTag nbt = super.wireRenderData(level, pos, state, itemData, firstPoint);
        nbt.putBoolean(NBT_TENSION, true);
        return nbt;
    }

    @Override
    public Vec3 multiblockSize() {
        return new Vec3(1, HEIGHT, 1);
    }
}
