package de.mrjulsen.paw.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import de.mrjulsen.paw.block.abstractions.IRotatableBlock;
import de.mrjulsen.paw.util.MixinVar;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour.BlockStateBase;
import net.minecraft.world.level.block.state.BlockState;

/** Update block shape diagonal */
@Mixin(BlockStateBase.class)
public abstract class BlockStateBaseMixin {

    @Shadow
    public Block getBlock() { throw new AssertionError(); }

    @Shadow
    public BlockState asState() { throw new AssertionError(); }

    @Inject(method = "updateNeighbourShapes(Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/core/BlockPos;II)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/BlockPos$MutableBlockPos;setWithOffset(Lnet/minecraft/core/Vec3i;Lnet/minecraft/core/Direction;)Lnet/minecraft/core/BlockPos$MutableBlockPos;", shift = Shift.AFTER), locals = LocalCapture.CAPTURE_FAILHARD)
    private void onUpdateNeighbourShapes(LevelAccessor level, BlockPos pos, int flag, int recursionLeft, CallbackInfo ci, MutableBlockPos mutableBlockPos, Direction[] directions, int length, int index, Direction direction) {
        if (MixinVar.oldState != null && direction.getAxis() != Axis.Y && MixinVar.oldState.getBlock() instanceof IRotatableBlock) {
            BlockPos newPos = pos.relative(direction).relative(direction.getCounterClockWise());
            BlockState blockState3 = level.getBlockState(newPos);
            BlockState blockState4 = blockState3.updateShape(direction.getOpposite(), this.asState(), level, newPos, pos);
            Block.updateOrDestroy(blockState3, blockState4, level, newPos, flag, recursionLeft);
        }
    }
}
