package de.mrjulsen.paw.block;

import javax.annotation.Nullable;

import de.mrjulsen.paw.block.abstractions.AbstractCantileverBlock;
import de.mrjulsen.paw.block.property.EInsulatorType;
import de.mrjulsen.paw.item.CantileverBlockItem;
import de.mrjulsen.paw.registry.ModBlocks;
import de.mrjulsen.paw.registry.ModBlocks.CantileverKey;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class CantileverBlock extends AbstractCantileverBlock {

    private final byte size;
    private final EInsulatorType insulatorType;

    public CantileverBlock(Properties properties, byte size, EInsulatorType insulatorType) {
        super(properties);
        this.size = size;
        this.insulatorType = insulatorType;
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
        return new ItemStack(ModBlocks.CANTILEVER_ITEMS.get(insulatorType));
    }

    public byte getSize() {
        return size;
    }

    public EInsulatorType getInsulatorType() {
        return insulatorType;
    }

    @Override
    public Vec3 defaultWireAttachPoint(Level level, BlockPos pos, BlockState state, CompoundTag itemData, boolean firstPoint) {
        return switch (state.getValue(REGISTRATION_ARM)) {
            case OUTER -> new Vec3(0, -(int)(size / 3), 0.75f - size);
            case INNER -> new Vec3(0, -(int)(size / 3), 1.25f - size);
            default -> new Vec3(0, -(int)(size / 3), 1 - size);
        };
    }

    @Override
    public Vec3 tensionWireAttachPoint(Level level, BlockPos pos, BlockState state, CompoundTag itemData, boolean firstPoint) {
        return switch (state.getValue(REGISTRATION_ARM)) {
            case OUTER -> new Vec3(0, 1f / 16f * 11f, 1f - size);
            case INNER -> new Vec3(0, 1f / 16f * 11f, 1f - size);
            default -> new Vec3(0, 1f / 16f * 11f, 1f - size);
        };
    }

    @Override
    public Vec3 multiblockSize() {
        return new Vec3(1, 1, 1);
    }

    
	@SuppressWarnings("deprecation")
    public boolean canBeReplaced(BlockState state, BlockPlaceContext useContext) {
        // make double cantilever
		return !useContext.isSecondaryUseActive() && useContext.getItemInHand().getItem() instanceof CantileverBlockItem ? true : super.canBeReplaced(state, useContext);
	}

	@Nullable
	public BlockState getStateForPlacement(BlockPlaceContext context) {
        // make double cantilever
		BlockState blockstate = context.getLevel().getBlockState(context.getClickedPos());
		return blockstate.getBlock() instanceof CantileverBlock ? copyProperties(blockstate, ModBlocks.getDoubleCantilever(new CantileverKey(getSize(), getInsulatorType())).getDefaultState()) : super.getStateForPlacement(context);
	}
}
