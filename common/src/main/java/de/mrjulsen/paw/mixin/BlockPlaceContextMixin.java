package de.mrjulsen.paw.mixin;

import org.spongepowered.asm.mixin.Mixin;

import de.mrjulsen.paw.block.extended.BlockPlaceContextExtension;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(BlockPlaceContext.class)
public class BlockPlaceContextMixin implements BlockPlaceContextExtension { 
    private BlockPos placedOnPos;
    private BlockState placedOnState;

    @Override
    public void setPlacedOnPos(BlockPos placedOnPos) {
        this.placedOnPos = placedOnPos;
    }

    @Override
    public void setPlacedOnState(BlockState placedOnState) {
        this.placedOnState = placedOnState;
    }

    @Override
    public BlockPos getPlacedOnPos() {
        return placedOnPos;
    }

    @Override
    public BlockState getPlacedOnState() {
        return placedOnState;
    }    
}
