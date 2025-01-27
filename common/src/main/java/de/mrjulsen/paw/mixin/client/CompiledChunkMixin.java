package de.mrjulsen.paw.mixin.client;

import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher.CompiledChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.At;

import de.mrjulsen.paw.util.CompiledChunkExtension;

@Mixin(CompiledChunk.class)
public class CompiledChunkMixin implements CompiledChunkExtension {

	private boolean hasWires = false;

	@Override
	public boolean hasWires() {
		return hasWires;
	}

	@Override
	public void setHasWires(boolean b) {
		this.hasWires = b;
	}

	@Inject(method = "hasNoRenderableLayers", at = @At(value = "RETURN"), cancellable = true)
	public void hasNoRenderableLayers(CallbackInfoReturnable<Boolean> cir) {
		if (hasWires()) {
			cir.setReturnValue(false);
		}
	}
}
