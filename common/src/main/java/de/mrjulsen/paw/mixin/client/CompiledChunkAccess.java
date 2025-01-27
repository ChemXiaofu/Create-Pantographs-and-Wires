package de.mrjulsen.paw.mixin.client;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher.CompiledChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Set;

@Mixin(CompiledChunk.class)
public interface CompiledChunkAccess {
	@Accessor("hasBlocks")
	Set<RenderType> getHasBlocks();
}
