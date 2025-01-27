package de.mrjulsen.paw.block.abstractions;

import net.minecraft.core.BlockPos;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;

public interface IHorizontalExtensionConnectable {
    
    EPostType postConnectionType(LevelReader level, BlockState state, BlockPos pos, BlockState extensionState, BlockPos extensionPos);

    public static enum EPostType implements StringRepresentable {
        NONE("none"),
        LATTICE("lattice"),
        FENCE("fence"),
        WALL("wall");

        String name;

        EPostType(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }
}
