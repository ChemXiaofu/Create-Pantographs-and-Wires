package de.mrjulsen.paw.block.abstractions;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec2;

public interface IConicalShape {
    
    default Vec2 coneTarget(BlockAndTintGetter level, BlockPos pos, BlockState state) {
        return new Vec2(0.5f, 0.5f);
    }

    Vec2 coneOffset(BlockAndTintGetter level, BlockPos pos, BlockState state);
}
