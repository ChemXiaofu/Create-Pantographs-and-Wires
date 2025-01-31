package de.mrjulsen.wires;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import de.mrjulsen.wires.network.WireChunkLoadingData;
import de.mrjulsen.wires.network.WiresNetworkSyncData.WireSyncDataEntry;
import de.mrjulsen.wires.render.WireSegmentRenderDataBatch;
import de.mrjulsen.wires.WireCollision.WireBlockCollision;
import de.mrjulsen.mcdragonlib.config.ECachingPriority;
import de.mrjulsen.mcdragonlib.data.MapCache;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;

public final class WireClientNetwork {

    private WireClientNetwork() {}

    private static final Map<UUID, WireConnection> connectionsById = new HashMap<>();
    
    private static final Multimap<ChunkPos, WireCollision> collisionByChunk = MultimapBuilder.hashKeys().hashSetValues().build();
    private static final Multimap<SectionPos, WireCollision> collisionBySection = MultimapBuilder.hashKeys().hashSetValues().build();
    private static final Multimap<BlockPos, WireCollision> collisionByBlock = MultimapBuilder.hashKeys().hashSetValues().build();
    
    private static final Multimap<UUID, WireSegmentRenderDataBatch> renderDataById = MultimapBuilder.hashKeys().hashSetValues().build();
    private static final Multimap<ChunkPos, WireSegmentRenderDataBatch> renderDataByChunk = MultimapBuilder.hashKeys().hashSetValues().build();
    private static final Multimap<SectionPos, WireSegmentRenderDataBatch> renderDataBySection = MultimapBuilder.hashKeys().hashSetValues().build();

    private static final MapCache<Collection<WireConnection>, BlockPos, BlockPos> connectionsTroughBlockCache = new MapCache<>((pos) -> {
        Collection<WireCollision> w = collisionByBlock.get(pos);
        Collection<WireConnection> connections = new ArrayList<>(w.size());
        for (WireCollision c : w) {
            connections.add(connectionsById.get(c.getId()));
        }
        return connections;
    }, BlockPos::hashCode, ECachingPriority.LOW);

    private static final MapCache<Collection<WireConnection>, SectionPos, SectionPos> connectionsTroughSectionCache = new MapCache<>((pos) -> {
        Collection<WireCollision> w = collisionBySection.get(pos);
        Collection<WireConnection> connections = new ArrayList<>(w.size());
        for (WireCollision c : w) {
            connections.add(connectionsById.get(c.getId()));
        }
        return connections;
    }, SectionPos::hashCode, ECachingPriority.LOW);

    private static final MapCache<Collection<WireConnection>, ChunkPos, ChunkPos> connectionsTroughChunkCache = new MapCache<>((pos) -> {
        Collection<WireCollision> w = collisionByChunk.get(pos);
        Collection<WireConnection> connections = new ArrayList<>(w.size());
        for (WireCollision c : w) {
            connections.add(connectionsById.get(c.getId()));
        }
        return connections;
    }, ChunkPos::hashCode, ECachingPriority.LOW);

    private static final MapCache<Collection<WireBlockCollision>, BlockPos, BlockPos> collisionsInBlockCache = new MapCache<>((pos) -> {
        Collection<WireCollision> w = collisionByBlock.get(pos);
        Collection<WireBlockCollision> connections = new ArrayList<>(w.size());
        for (WireCollision c : w) {
            connections.addAll(c.collisionsInBlock(pos));
        }
        return connections;
    }, BlockPos::hashCode, ECachingPriority.LOW);

    public static void clearConnectionCaches() {
        connectionsTroughBlockCache.clearAll();
        connectionsTroughSectionCache.clearAll();
        connectionsTroughChunkCache.clearAll();
        collisionsInBlockCache.clearAll();
    }

    public static String debug_text() {
        return String.format("Wires[C]: Col: [%s,%s,%s], R: [%s,%s,%s], Id: %s",
            collisionByChunk.size(),
            collisionBySection.size(),
            collisionByBlock.size(),

            renderDataById.size(),
            renderDataBySection.size(),
            renderDataByChunk.size(),

            connectionsById.size()
        );
    }

    public static void clear() {
        collisionByBlock.clear();
        collisionByChunk.clear();
        collisionBySection.clear();
        connectionsById.clear();
        renderDataById.clear();
        renderDataByChunk.clear();
        renderDataBySection.clear();
        clearConnectionCaches();
    }

    public static Collection<WireConnection> getConnectionsTroughBlock(BlockPos pos) {
        return connectionsTroughBlockCache.get(pos, pos);
    }

    public static Collection<WireConnection> getConnectionsTroughSection(SectionPos pos) {
        return connectionsTroughSectionCache.get(pos, pos);
    }

    public static Collection<WireConnection> getConnectionsTroughChunk(ChunkPos pos) {
        return connectionsTroughChunkCache.get(pos, pos);
    }

    public static Collection<WireCollision> getCollisionsTroughBlock(BlockPos pos) {
        return collisionByBlock.get(pos);
    }

    public static Collection<WireCollision> getCollisionsTroughSection(SectionPos pos) {
        return collisionBySection.get(pos);
    }

    public static Collection<WireCollision> getCollisionsTroughChunk(ChunkPos pos) {
        return collisionByChunk.get(pos);
    }

    public static Collection<WireBlockCollision> getCollisionsInBlock(BlockPos pos) {
        return collisionsInBlockCache.get(pos, pos);
    }

    public synchronized static boolean hasConnectionsInSection(SectionPos section) {
        return renderDataBySection.containsKey(section);
    }

    public synchronized static boolean hasConnectionsInBlock(BlockPos pos) {
        return collisionByBlock.containsKey(pos);
    }

    public synchronized static Collection<WireSegmentRenderDataBatch> connectionsInSection(SectionPos section) {
        if (!hasConnectionsInSection(section)) {
            return List.of();
        }
        return renderDataBySection.get(section);
    }

    public static void createClientConnection(@Nullable ChunkPos chunk, WireSyncDataEntry in) {
        if (in.forceUpdate()) {
            removeClientConnection(in.data().getConnectionId());
        } else if (renderDataById.containsKey(in.data().getConnectionId())) {
            if (chunk != null && renderDataByChunk.containsKey(chunk)) {
                for (WireSegmentRenderDataBatch renderdata : renderDataByChunk.get(chunk)) {
                    renderdata.setUnloaded(false);
                }
            }
            return;
        }
        
        Set<SectionPos> sectionsIn = new HashSet<>();
        IWireType renderer = WireTypeRegistry.get(in.data().getWireType());

        WireBatch batch = renderer.buildWire(WireCreationContext.BOTH, Minecraft.getInstance().level, in.data());
        batch.splitRenderDataInChunkSections(in.data().getConnectionId(), in.data().getOriginChunkSection()).entrySet().forEach(x -> {
            renderDataByChunk.put(x.getKey().chunk(), x.getValue());
            renderDataBySection.put(x.getKey(), x.getValue());  
            renderDataById.put(in.data().getConnectionId(), x.getValue());
            sectionsIn.add(x.getKey());
        });
        new WireCollision(collisionByChunk, collisionBySection, collisionByBlock, in.data().getConnectionId(), in.data().getStartBlockPos(), batch.getCollisions());
        clearConnectionCaches();

        for (SectionPos section : sectionsIn) {
            Minecraft.getInstance().levelRenderer.setSectionDirty(section.getX(), section.getY(), section.getZ());
        }
    }

    public static void removeClientConnections(UUID[] connectionIds) {
        for (UUID id : connectionIds) {
            removeClientConnection(id);
        }
    }

    public static void removeClientConnection(UUID connectionId) {
        if (!renderDataById.containsKey(connectionId)) {
            return;
        }

        Collection<WireSegmentRenderDataBatch> renderdata = renderDataById.removeAll(connectionId);
        renderDataBySection.values().removeAll(renderdata);
        renderDataByChunk.values().removeAll(renderdata);
        collisionByBlock.values().removeIf(x -> x.getId().equals(connectionId));
        collisionByChunk.values().removeIf(x -> x.getId().equals(connectionId));
        collisionBySection.values().removeIf(x -> x.getId().equals(connectionId));
        connectionsById.remove(connectionId);
        clearConnectionCaches();
        
        for (WireSegmentRenderDataBatch batch : renderdata) {
            SectionPos section = batch.getSection();
            Minecraft.getInstance().levelRenderer.setSectionDirty(section.getX(), section.getY(), section.getZ());
        }
    }

    public static void onClientChunkLoading(WireChunkLoadingData in) {
        synchronized (renderDataByChunk) {
            Set<UUID> emptyConnections = new HashSet<>();
            for (WireSegmentRenderDataBatch renderdata : renderDataByChunk.get(in.pos())) {
                renderdata.setUnloaded(!in.load());
                if (!renderDataById.containsKey(renderdata.getId()) || renderDataById.get(renderdata.getId()).stream().allMatch(WireSegmentRenderDataBatch::isUnloaded)) {
                    emptyConnections.add(renderdata.getId());
                }
            }
            clearConnectionCaches();

            for (UUID id : emptyConnections) {                
                removeClientConnection(id);
            }
        }
    }
   
}
