package de.mrjulsen.paw.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import de.mrjulsen.paw.block.abstractions.IRotatableBlock;
import de.mrjulsen.paw.util.MixinVar;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(Level.class)
public class LevelMixin {

    @Shadow
    public void neighborChanged(BlockPos pos, Block block, BlockPos fromPos) {}
    
    @Inject(method = "updateNeighborsAt", at = @At(value = "HEAD"))
    private void onUpdateNeighboursAt(BlockPos pos, Block block, CallbackInfo ci) {
        if (block instanceof IRotatableBlock) {            
            this.neighborChanged(pos.west().north(), block, pos);
            this.neighborChanged(pos.west().south(), block, pos);
            this.neighborChanged(pos.east().north(), block, pos);
            this.neighborChanged(pos.east().south(), block, pos);
        }
    }

    /** Prepare diagonal block update fix */
    @SuppressWarnings("resource")
    @Inject(method = "setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;II)Z", at = @At(value = "HEAD"))
    private void onSetBlockPre(BlockPos pos, BlockState state, int flags, int recursionLeft, CallbackInfoReturnable<Boolean> cir) {
        Level level = (Level)(Object)this;
        MixinVar.oldState = level.getBlockState(pos);
    }
}
