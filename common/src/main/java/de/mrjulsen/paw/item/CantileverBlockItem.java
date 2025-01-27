package de.mrjulsen.paw.item;

import de.mrjulsen.paw.block.DoubleCantileverBlock;
import de.mrjulsen.paw.block.abstractions.AbstractCantileverBlock;
import de.mrjulsen.paw.block.abstractions.AbstractCantileverBlock.ECantileverInsulatorsPlacement;
import de.mrjulsen.paw.block.abstractions.AbstractCantileverBlock.ECantileverRegistrationArmType;
import de.mrjulsen.paw.block.property.EInsulatorType;
import de.mrjulsen.paw.event.ClientWrapper;
import de.mrjulsen.paw.registry.ModBlocks;
import de.mrjulsen.paw.registry.ModBlocks.CantileverKey;
import de.mrjulsen.paw.registry.ModNetworkAccessor.CantileverSettingsData;
import de.mrjulsen.mcdragonlib.util.MathUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class CantileverBlockItem<T extends AbstractCantileverBlock> extends BlockItem {

    protected static final String NBT_SIZE = "Size";
    protected static final String NBT_CANTILEVER_TYPE = "CantileverType";
    protected static final String NBT_INSULATOR_PLACEMENT = "InsulatorPlacement";

    private final EInsulatorType insulatorType;

    public CantileverBlockItem(T block, EInsulatorType insulatorType, Item.Properties properties) {
		super(block, properties);
        this.insulatorType = insulatorType;
	}

    @SuppressWarnings("unchecked")
    public AbstractCantileverBlock getCantilever() {
        return (T)getBlock();
    }

    public EInsulatorType getInsulatorType() {
        return insulatorType;
    }

    @Override
    protected boolean placeBlock(BlockPlaceContext context, BlockState state) {
        CompoundTag nbt = getNbt(context.getItemInHand());
        
        BlockState base = ModBlocks.getCantilever(new CantileverKey(nbt.getByte(NBT_SIZE), insulatorType)).getDefaultState();
        if (state.getBlock() instanceof DoubleCantileverBlock) {
            base = ModBlocks.getDoubleCantilever(new CantileverKey(nbt.getByte(NBT_SIZE), insulatorType)).getDefaultState();
        }

        state = getCantilever().copyProperties(state, base)
            .setValue(AbstractCantileverBlock.REGISTRATION_ARM, ECantileverRegistrationArmType.values()[nbt.getInt(NBT_CANTILEVER_TYPE)])
            .setValue(AbstractCantileverBlock.INSULATORS_PLACEMENT, ECantileverInsulatorsPlacement.values()[nbt.getInt(NBT_INSULATOR_PLACEMENT)])
        ;
        return super.placeBlock(context, state);
    }

    public static CompoundTag getNbt(ItemStack stack) {
        CompoundTag nbt = stack.getOrCreateTag();
        if (!nbt.contains(NBT_SIZE))
            nbt.putByte(NBT_SIZE, getSize(null));
        else 
            nbt.putByte(NBT_SIZE, MathUtils.clamp(nbt.getByte(NBT_SIZE), AbstractCantileverBlock.MIN_SIZE, AbstractCantileverBlock.MAX_SIZE));

        if (!nbt.contains(NBT_CANTILEVER_TYPE))
            nbt.putInt(NBT_CANTILEVER_TYPE, getCantileverType(null).ordinal());
        else
            nbt.putInt(NBT_CANTILEVER_TYPE, MathUtils.clamp(nbt.getInt(NBT_CANTILEVER_TYPE), 0, ECantileverRegistrationArmType.values().length - 1));
        
        if (!nbt.contains(NBT_INSULATOR_PLACEMENT))
            nbt.putInt(NBT_INSULATOR_PLACEMENT, getInsulatorPlacement(null).ordinal());
        else
            nbt.putInt(NBT_INSULATOR_PLACEMENT, MathUtils.clamp(nbt.getInt(NBT_INSULATOR_PLACEMENT), 0, ECantileverInsulatorsPlacement.values().length - 1));

        return nbt;
    }

    public static boolean setNbt(ItemStack stack, CantileverSettingsData data) {
        if (stack.getItem() instanceof CantileverBlockItem) {
            CompoundTag nbt = getNbt(stack);
            nbt.putByte(NBT_SIZE, data.size());
            nbt.putInt(NBT_CANTILEVER_TYPE, data.cantileverType().ordinal());
            nbt.putInt(NBT_INSULATOR_PLACEMENT, data.insulatorPlacement().ordinal());
            return true;
        } 
        return false;
    }

    public static byte getSize(ItemStack stack) {
        if (stack != null && stack.getItem() instanceof CantileverBlockItem) {
            return getNbt(stack).getByte(NBT_SIZE);
        }
        return AbstractCantileverBlock.MIN_SIZE;
    }

    public static ECantileverRegistrationArmType getCantileverType(ItemStack stack) {
        if (stack != null && stack.getItem() instanceof CantileverBlockItem) {
            return ECantileverRegistrationArmType.values()[getNbt(stack).getInt(NBT_CANTILEVER_TYPE)];
        }
        return ECantileverRegistrationArmType.def();
    }

    public static ECantileverInsulatorsPlacement getInsulatorPlacement(ItemStack stack) {
        if (stack != null && stack.getItem() instanceof CantileverBlockItem) {
            return ECantileverInsulatorsPlacement.values()[getNbt(stack).getInt(NBT_INSULATOR_PLACEMENT)];
        }
        return ECantileverInsulatorsPlacement.def();
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        ItemStack stack = player.getItemInHand(usedHand);
        if (level.isClientSide) {
            ClientWrapper.showCantileverSettingsScreen(stack);
        }
        return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
    }
}
