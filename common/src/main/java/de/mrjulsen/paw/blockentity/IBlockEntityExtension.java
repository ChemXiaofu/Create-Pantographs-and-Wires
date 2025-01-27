package de.mrjulsen.paw.blockentity;

public interface IBlockEntityExtension {
    default void onChunkUnloaded() {}
}
