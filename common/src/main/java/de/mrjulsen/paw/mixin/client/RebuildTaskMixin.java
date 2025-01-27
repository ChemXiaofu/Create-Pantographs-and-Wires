package de.mrjulsen.paw.mixin.client;

import java.util.Set;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import de.mrjulsen.paw.PantographsAndWires;
import de.mrjulsen.paw.mixin.ext.CaptureOwner;
import de.mrjulsen.wires.render.WireRenderer;
import net.minecraft.client.renderer.ChunkBufferBuilderPack;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher.RenderChunk;
import net.minecraft.client.renderer.chunk.RenderChunkRegion;
import net.minecraft.world.level.BlockAndTintGetter;

/** @see ImmersiveEngineering */
@Mixin(targets = "net.minecraft.client.renderer.chunk.ChunkRenderDispatcher$RenderChunk$RebuildTask")
public class RebuildTaskMixin {

	private BlockAndTintGetter paw$region;
	private Set<RenderType> paw$layers;

	@Shadow(aliases = {"field_20839", "f_112859_", "this$1"})
	@Final
	private RenderChunk paw$self;

	// Extraction is called for every block, so we just extract the region here and render later
	/*
	@CaptureOwner(
		method = "compile",
		at = {
			// Vanilla
			@At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/chunk/RenderChunkRegion;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;", ordinal = 0),
			// Vanilla, remapped manually. The Mixin AP is not aware of CaptureOwner, and this is less work than
			// creating an AP just for this
			@At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/chunk/RenderChunkRegion;m_8055_(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;", ordinal = 0),
			@At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/chunk/RenderChunkRegion;m_8055_(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;", ordinal = 0),
			// Optifine
			@At(value = "INVOKE", target = "Lnet/optifine/override/ChunkCacheOF;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;"),
			// Optifine, remapped manually. An AP would not help here, since the Optifine chunk cache class
			// isn't known at compile time.
			@At(value = "INVOKE", target = "Lnet/optifine/override/ChunkCacheOF;m_8055_(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;"),
		}
	)
	public void paw$extractRegion(BlockAndTintGetter region) {
		paw$region = region;
	}
		*/
	
	@ModifyVariable(method = "compile", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/RandomSource;create()Lnet/minecraft/util/RandomSource;"))
	public Set<RenderType> paw$extractLayers(Set<RenderType> set) {
		paw$layers = set;
		return set;
	}

	@ModifyVariable(method = "compile", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/chunk/RenderChunkRegion;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;", ordinal = 0))
	public RenderChunkRegion paw$extractRegion(RenderChunkRegion set) {
		paw$region = set;
		return set;
	}

	@Inject(method = "compile", at = @At(value = "INVOKE", target = "Ljava/util/Set;iterator()Ljava/util/Iterator;", remap = false))
	public void paw$addConnectionQuads(float pX, float pY, float pZ, ChunkBufferBuilderPack pBuffers, CallbackInfoReturnable<?> cir) {
		if (!PantographsAndWires.isEmbeddiumLoaded()) {
			WireRenderer.renderConnectionsInSection(this.paw$layers, pBuffers, this.paw$region, paw$self);
		}
		this.paw$region = null;
		this.paw$layers = null;
	}
}