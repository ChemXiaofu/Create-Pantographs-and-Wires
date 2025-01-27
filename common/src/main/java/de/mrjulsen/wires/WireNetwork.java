package de.mrjulsen.wires;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.google.common.io.Files;

import de.mrjulsen.paw.PantographsAndWires;
import de.mrjulsen.paw.config.ModServerConfig;
import de.mrjulsen.paw.registry.ModNetworkAccessor;
import de.mrjulsen.wires.block.IWireConnector;
import de.mrjulsen.wires.network.WireChunkLoadingData;
import de.mrjulsen.wires.network.WireConnectionSyncData;
import de.mrjulsen.wires.network.WiresNetworkSyncData;
import de.mrjulsen.wires.network.WiresNetworkSyncData.WireSyncDataEntry;
import de.mrjulsen.wires.render.WireSegmentRenderDataBatch;
import de.mrjulsen.wires.WireCollision.WireBlockCollision;
import de.mrjulsen.mcdragonlib.config.ECachingPriority;
import de.mrjulsen.mcdragonlib.data.MapCache;
import de.mrjulsen.mcdragonlib.util.accessor.DataAccessor;
import dev.architectury.utils.GameInstance;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.phys.Vec3;

public class WireNetwork {    

    private static final String FILENAME = PantographsAndWires.MOD_ID + "_wire_network.nbt"; 
    private static final String NBT_CONNECTIONS = "Connections"; 
    
    private static final Multimap<ChunkPos, UUID> playersWatchingChunk = MultimapBuilder.hashKeys().hashSetValues().build();

    // Connections
    private static final Map<UUID, WireConnection> connectionsById = new HashMap<>();
    private static final Multimap<SectionPos, WireConnection> connectionsBySection = MultimapBuilder.hashKeys().hashSetValues().build();
    private static final Multimap<BlockPos, WireConnection> connectionsByBlock = MultimapBuilder.hashKeys().hashSetValues().build();
    private static final Multimap<Integer, WireConnection> connectionsByHash = MultimapBuilder.hashKeys().hashSetValues().build();

    // Collision
    private static final Multimap<ChunkPos, WireCollision> collisionByChunk = MultimapBuilder.hashKeys().hashSetValues().build();
    private static final Multimap<SectionPos, WireCollision> collisionBySection = MultimapBuilder.hashKeys().hashSetValues().build();
    private static final Multimap<BlockPos, WireCollision> collisionByBlock = MultimapBuilder.hashKeys().hashSetValues().build();

    // Client only
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
        return String.format("Wires: Con: [%s,%s,%s], Col: [%s,%s,%s], R: [%s,%s,%s], P: %s, Id: %s",
            connectionsByBlock.size(),
            connectionsBySection.size(),
            connectionsByHash.size(),

            collisionByChunk.size(),
            collisionBySection.size(),
            collisionByBlock.size(),

            renderDataById.size(),
            renderDataBySection.size(),
            renderDataByChunk.size(),
            
            playersWatchingChunk.size(),
            connectionsById.size()
        );
    }

    public static void clear() {
        playersWatchingChunk.clear();
        connectionsByBlock.clear();
        connectionsBySection.clear();
        connectionsByHash.clear();
        collisionByBlock.clear();
        collisionByChunk.clear();
        collisionBySection.clear();
        connectionsById.clear();
        renderDataById.clear();
        renderDataByChunk.clear();
        renderDataBySection.clear();
        clearConnectionCaches();
    }
    
    
    public static synchronized void save(MinecraftServer server) {
        CompoundTag nbt = new CompoundTag();
        ListTag connections = new ListTag();
        for (WireConnection connection : connectionsByHash.values()) {
            connections.add(connection.toNbt());
        }
        nbt.put(NBT_CONNECTIONS, connections);
        
        try {
            String path = server.getWorldPath(new LevelResource("data/" + FILENAME)).toString();
            File outFile = new File(path);
            File tempFile = new File(path + ".bak");
            if (outFile.exists()) {
                Files.copy(outFile, tempFile);
            }
            NbtIo.writeCompressed(nbt, outFile);
            PantographsAndWires.LOGGER.debug("Saved wire network data.");
            if (tempFile.exists()) {
                tempFile.delete();
            }
        } catch (IOException e) {
            PantographsAndWires.LOGGER.error("Error while saving wire network data.", e);
        } 
    }

    public static void load() {   
        File settingsFile = new File(GameInstance.getServer().getWorldPath(new LevelResource("data/" + FILENAME)).toString());  
        if (!settingsFile.exists()) {
            return;
        }
        
        try {
            CompoundTag nbt = NbtIo.readCompressed(settingsFile);
            nbt.getList(NBT_CONNECTIONS, Tag.TAG_COMPOUND).stream().map(x -> WireConnection.fromNbt((CompoundTag)x)).forEach(x -> {
                if (x.isPresent()) {
                    setWireConnection(null, x.get());
                }
            });
        } catch (IOException e) {
            PantographsAndWires.LOGGER.error("Unable to load wire network data.", e);
        }
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

    public synchronized static boolean addConnection(Level level, CompoundTag itemData, BlockPos posA, BlockPos posB, IWireConnector connectorA, IWireConnector connectorB, IWireType wireType) {
        CompoundTag connectionANbt = connectorA.wireRenderData(level, posA, level.getBlockState(posA), itemData, true);
        CompoundTag connectionBNbt = connectorB.wireRenderData(level, posB, level.getBlockState(posB), itemData, false);

        WireConnection wireConnection = createWireConnection(posA, posB, wireType, connectionANbt, connectionBNbt, itemData);

        if (!wireType.allowMultiConnections() && connectionsByHash.containsKey(wireConnection.hashCode())) {
            return false;
        }
        
        return setWireConnection(level, wireConnection);
    }

    protected synchronized static boolean setWireConnection(@Nullable Level level, WireConnection wireConnection) {
        connectionsById.put(wireConnection.getId(), wireConnection);
        connectionsByBlock.put(wireConnection.getPointA(), wireConnection);
        connectionsByBlock.put(wireConnection.getPointB(), wireConnection);
        connectionsBySection.put(SectionPos.of(wireConnection.getPointA()), wireConnection);
        connectionsBySection.put(SectionPos.of(wireConnection.getPointB()), wireConnection);
        connectionsByHash.put(wireConnection.hashCode(), wireConnection);
        
        WireConnectionSyncData syncData = WireConnectionSyncData.of(wireConnection);
        WireCollision collision = new WireCollision(collisionByChunk, collisionBySection, collisionByBlock, wireConnection.getId(), wireConnection.getPointA(), wireConnection.getWireType().buildWire(WireCreationContext.COLLISION, level, syncData).getCollisions());
        wireConnection.setCollisionData(collision);
        wireConnection.setWireConnectionSyncData(syncData);

        clearConnectionCaches();
        
        if (level != null) {
            WiresNetworkSyncData netData = new WiresNetworkSyncData(null, List.of(new WireSyncDataEntry(syncData, true)));

            Set<UUID> updatePlayers = new HashSet<>();
            for (SectionPos section : collision.sectionsIn()) {
                updatePlayers.addAll(playersWatchingChunk.get(section.chunk()));
            }
            
            for (UUID playerId : updatePlayers) {
                if (level.getPlayerByUUID(playerId) instanceof ServerPlayer serverPlayer) {
                    DataAccessor.getFromClient(serverPlayer, netData, ModNetworkAccessor.WIRE_CONNECTOR_DATA_TRANSFER, $ -> {});
                }
            }
        }
        
        return true;
    }

    private synchronized static WireConnection createWireConnection(BlockPos posA, BlockPos posB, IWireType wireType, CompoundTag connectionANbt, CompoundTag connectionBNbt, CompoundTag itemData) {
        UUID id;
        do {
            id = UUID.randomUUID();
        } while (connectionsById.containsKey(id));
        
        WireConnection wireConnection = new WireConnection(id, posA, posB, wireType, connectionANbt, connectionBNbt, itemData);
        return wireConnection;
    }

    public synchronized static void removeConnector(Level level, BlockPos pos) {
        if (!connectionsByBlock.containsKey(pos)) {
            return;
        }

        Collection<WireConnection> blockConnections = connectionsByBlock.removeAll(pos);
        clearConnectionCaches();

        Set<UUID> updatePlayers = new HashSet<>();
        for (WireConnection connection : blockConnections) {
            updatePlayers.addAll(removeWireConnection(connection));
        }
        for (UUID playerId : updatePlayers) {
            if (level.getPlayerByUUID(playerId) instanceof ServerPlayer serverPlayer) {
                DataAccessor.getFromClient(serverPlayer, blockConnections.stream().map(x -> x.getId()).toArray(UUID[]::new), ModNetworkAccessor.DELETE_WIRE_CONNECTION, $ -> {});
            }
        }
    }

    private static synchronized Set<UUID> removeWireConnection(UUID connection) {
        return removeWireConnection(connectionsById.get(connection));
    }

    private static synchronized Set<UUID> removeWireConnection(WireConnection connection) {
        Set<UUID> updatePlayers = new HashSet<>();
        collisionByBlock.values().removeIf(x -> x.getId().equals(connection.getId()));
        collisionByChunk.values().removeIf(x -> x.getId().equals(connection.getId()));
        collisionBySection.values().removeIf(x -> x.getId().equals(connection.getId()));
        connectionsByBlock.values().removeIf(x -> x == connection);
        connectionsBySection.values().removeIf(x -> x == connection);
        connectionsByHash.values().removeIf(x -> x == connection);
        connectionsById.remove(connection.getId());
        
        clearConnectionCaches();

        for (SectionPos section : connection.getCollisionData().sectionsIn()) {
            ChunkPos chunk = section.chunk();
            if (playersWatchingChunk.containsKey(chunk)) {
                updatePlayers.addAll(playersWatchingChunk.get(chunk));
            }
        }
        return updatePlayers;
    }
    

    public synchronized static void removeBlockedConnection(Level level, BlockPos pos) {
        if (!collisionByBlock.containsKey(pos)) {
            return;
        }

        Collection<WireCollision> collisionsByBlock = collisionByBlock.removeAll(pos);
        clearConnectionCaches();

        Set<UUID> updatePlayers = new HashSet<>();
        for (WireCollision connection : collisionsByBlock) {
            updatePlayers.addAll(removeWireConnection(connection.getId()));
        }

        for (UUID playerId : updatePlayers) {
            if (level.getPlayerByUUID(playerId) instanceof ServerPlayer serverPlayer) {
                DataAccessor.getFromClient(serverPlayer, collisionsByBlock.stream().toArray(UUID[]::new), ModNetworkAccessor.DELETE_WIRE_CONNECTION, $ -> {});
            }
        }
    }

    //#region CLIENT
    /*
     * CLIENT AREA
     */

    public synchronized static boolean hasConnectionsInSection(SectionPos section) {
        return renderDataBySection.containsKey(section);
    }

    public synchronized static boolean hasConnectionsInBlock(BlockPos pos) { // TODO Server client
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
        clearConnectionCaches();
        
        for (WireSegmentRenderDataBatch batch : renderdata) {
            SectionPos section = batch.getSection();
            Minecraft.getInstance().levelRenderer.setSectionDirty(section.getX(), section.getY(), section.getZ());
        }
    }

    //#endregion


    /*
     * EVENTS
     */

    public static void notifyBlockUpdate(Level level, BlockPos pos, BlockState newState, int flags) {
        if (ModServerConfig.BLOCKS_BREAK_WIRES.get() && !level.isClientSide() && !newState.getCollisionShape(level, pos).isEmpty()) {
            Collection<WireConnection> connections = getConnectionsTroughBlock(pos);
            if (connections.isEmpty()) {
                return;
            }

            Map<WireConnection, BlockPos> connectionsToBreak = new HashMap<>();

            for (WireConnection connection : connections) {
                Collection<WireBlockCollision> collisions = connection.getCollisionData().collisionsInBlock(pos);
                for (WireBlockCollision collision : collisions) {
                    Vec3 vecA = collision.entryPointA();
                    Vec3 vecB = collision.entryPointB();
                    BlockPos dropPos = pos;
                    if (WireCollision.connectionBlocked(level, pos, newState, vecA, vecB)) {
                        for (Direction d : Direction.values()) {
                            if (level.isEmptyBlock(pos.relative(d))) {
                                dropPos = dropPos.relative(d);
                                break;
                            }
                        }								
                        connectionsToBreak.put(connection, dropPos);
                    }
                }
            }

            // TODO Drop wire item

            Set<UUID> updatePlayers = new HashSet<>();
            for (Map.Entry<WireConnection, BlockPos> connection : connectionsToBreak.entrySet()) {
                updatePlayers.addAll(removeWireConnection(connection.getKey()));                
            }
            
            for (UUID playerId : updatePlayers) {
                if (level.getPlayerByUUID(playerId) instanceof ServerPlayer serverPlayer) {
                    DataAccessor.getFromClient(serverPlayer, connectionsToBreak.keySet().stream().map(x -> x.getId()).toArray(UUID[]::new), ModNetworkAccessor.DELETE_WIRE_CONNECTION, $ -> {});
                }
            }
		}
    }

    public static void checkEntityCollision(Level level, BlockPos pos, Entity entity) {
        /*
		if (ModServerConfig.WIRE_ENTITY_DAMAGE.get() && !level.isClientSide() && entity instanceof LivingEntity living && !(living instanceof Player player && player.getAbilities().invulnerable)) {
            Collection<WireConnection> connections = collisionByBlock.get(pos);
            if (connections.isEmpty()) {
                return;
            }

			for (WireConnection connection : connections) {
                Collection<WireBlockCollision> collisions = connection.getCollisionData().collisionsInBlock(pos);
                for (WireBlockCollision collision : collisions) {
                    Vec3 vecA = collision.entryPointA();
                    Vec3 vecB = collision.entryPointB();
                    double extra = 0;// TODO shockWire.getDamageRadius();
                    AABB hitbox = entity.getBoundingBox();
                    AABB includingExtra = hitbox.inflate(extra).move(-pos.getX(), -pos.getY(), -pos.getZ());
                    if (includingExtra.contains(vecA) || includingExtra.contains(vecB) || includingExtra.clip(vecA, vecB).isPresent()) {
                        entity.hurt(level.damageSources().generic(), 100);
                    }
                }
            }
		}
        */
	}

    // Server side
    public static void onChunkLoad(Level level, ChunkPos pos, Player player) {
        playersWatchingChunk.put(pos, player.getUUID());
        synchronized (collisionByChunk) {
            if (collisionByChunk.containsKey(pos) && player instanceof ServerPlayer serverPlayer) {
                Collection<WireConnection> connections = new ArrayList<>(getConnectionsTroughChunk(pos));
                Collection<WireSyncDataEntry> syncData = new ArrayList<>(connections.size());
                for (WireConnection connection : connections) {
                    boolean b = connection.recalcAttachPoints(level, collisionByChunk, collisionBySection, collisionByBlock);
                    syncData.add(new WireSyncDataEntry(connection.getWireConnectionSyncData(), b));
                }
                DataAccessor.getFromClient(serverPlayer, new WiresNetworkSyncData(pos, syncData), ModNetworkAccessor.WIRE_CONNECTOR_DATA_TRANSFER, $ -> {});
            }
        }
    }

    public static void onChunkUnload(Level level, ChunkPos pos, Player player) {
        if (playersWatchingChunk.containsKey(pos)) {
            playersWatchingChunk.get(pos).removeIf(x -> x.equals(player.getUUID()));
        }
        synchronized (collisionByChunk) {
            if (collisionByChunk.containsKey(pos) && player instanceof ServerPlayer serverPlayer) {
                Collection<WireConnection> connections = getConnectionsTroughChunk(pos);
                if (connections.isEmpty()) return;
                DataAccessor.getFromClient(serverPlayer, new WireChunkLoadingData(pos, connections.stream().map(WireConnection::getId).collect(Collectors.toSet()), false), ModNetworkAccessor.WIRE_CONNECTION_CHUNK_LOADING, $ -> {});
            }
        }
    }

    // Client side
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
