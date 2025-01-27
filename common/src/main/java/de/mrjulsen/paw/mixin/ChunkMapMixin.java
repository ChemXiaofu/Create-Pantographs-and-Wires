package de.mrjulsen.paw.mixin;

import org.apache.commons.lang3.mutable.MutableObject;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import de.mrjulsen.paw.event.ChunkLoadingEvents;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;

@Mixin(ChunkMap.class)
public class ChunkMapMixin {

    @Shadow
    private ServerLevel level;

    @Inject(method = "updateChunkTracking", at = @At(value = "HEAD"))
    protected void updateChunkTracking(ServerPlayer player, ChunkPos chunkPos, MutableObject<ClientboundLevelChunkWithLightPacket> packetCache, boolean wasLoaded, boolean load, CallbackInfo ci) {
        ChunkLoadingEvents.fireChunkWatch(wasLoaded, load, player, chunkPos, level);
    }
}
