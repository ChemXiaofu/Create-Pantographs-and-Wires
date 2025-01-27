package de.mrjulsen.paw.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.Vec3;

public final class Utils {
    public static void putNbtBlockPos(CompoundTag compound, String name, BlockPos pos) {
        CompoundTag nbt = new CompoundTag();
        nbt.putInt("X", pos.getX());
        nbt.putInt("Y", pos.getY());
        nbt.putInt("Z", pos.getZ());
        compound.put(name, nbt);
    }

    public static SectionPos getNbtSectionPos(CompoundTag compound, String name) {
        CompoundTag nbt = compound.getCompound(name);
        return SectionPos.of(nbt.getInt("X"), nbt.getInt("Y"), nbt.getInt("Z"));
    }
    
    public static void putNbtSectionPos(CompoundTag compound, String name, SectionPos pos) {
        CompoundTag nbt = new CompoundTag();
        nbt.putInt("X", pos.getX());
        nbt.putInt("Y", pos.getY());
        nbt.putInt("Z", pos.getZ());
        compound.put(name, nbt);
    }

    public static ChunkPos getNbtChunkPos(CompoundTag compound, String name) {
        CompoundTag nbt = compound.getCompound(name);
        return new ChunkPos(nbt.getInt("X"), nbt.getInt("Z"));
    }
    
    public static void putNbtChunkPos(CompoundTag compound, String name, ChunkPos pos) {
        CompoundTag nbt = new CompoundTag();
        nbt.putInt("X", pos.x);
        nbt.putInt("Z", pos.z);
        compound.put(name, nbt);
    }

    public static BlockPos getNbtBlockPos(CompoundTag compound, String name) {
        CompoundTag nbt = compound.getCompound(name);
        return new BlockPos(nbt.getInt("X"), nbt.getInt("Y"), nbt.getInt("Z"));
    }
    
    public static void putNbtVec3(CompoundTag compound, String name, Vec3 vec) {
        CompoundTag nbt = new CompoundTag();
        nbt.putDouble("X", vec.x());
        nbt.putDouble("Y", vec.y());
        nbt.putDouble("Z", vec.z());
        compound.put(name, nbt);
    }

    public static Vec3 getNbtVec3(CompoundTag compound, String name) {
        CompoundTag nbt = compound.getCompound(name);
        return new Vec3(nbt.getDouble("X"), nbt.getDouble("Y"), nbt.getDouble("Z"));
    }

    public boolean isSectionInChunk(SectionPos section, ChunkPos chunk) {
        return section.getX() == chunk.x && section.getZ() == chunk.z;
    }

    public ChunkPos getChunkOfSection(SectionPos section) {
        return new ChunkPos(section.getX(), section.getZ());
    }

    
}
