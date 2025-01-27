package de.mrjulsen.paw.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import de.mrjulsen.paw.block.abstractions.IRotatableBlock;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Fixes too many particles with very detailed voxel shapes */
@Mixin(ParticleEngine.class)
public class ParticleEngineMixin {

    private BlockState state;

    @Inject(method = "destroy", at = @At(value = "HEAD"), cancellable = true)
    public void onDestroy(BlockPos pos, BlockState state, CallbackInfo ci) {
        this.state = state;
    }

    @ModifyVariable(method = "destroy", at = @At("STORE"), ordinal = 0)
    private VoxelShape modifyVoxelShape(VoxelShape voxelShape) {
        if (state.getBlock() instanceof IRotatableBlock) {
            state = null;
            return Shapes.block();
        }
        state = null;
        return voxelShape;
    }
}
