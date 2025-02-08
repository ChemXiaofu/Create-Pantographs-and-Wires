package de.mrjulsen.paw.registry;

import java.util.UUID;

import de.mrjulsen.paw.PantographsAndWires;
import de.mrjulsen.paw.block.abstractions.AbstractCantileverBlock.ECantileverInsulatorsPlacement;
import de.mrjulsen.paw.block.abstractions.AbstractCantileverBlock.ECantileverRegistrationArmType;
import de.mrjulsen.paw.item.CantileverBlockItem;
import de.mrjulsen.wires.network.WireChunkLoadingData;
import de.mrjulsen.wires.network.WiresNetworkSyncData;
import de.mrjulsen.wires.network.WiresNetworkSyncData.WireSyncDataEntry;
import de.mrjulsen.wires.WireClientNetwork;
import de.mrjulsen.mcdragonlib.util.accessor.DataAccessorType;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;

public final class ModNetworkAccessor {

    public static void init() {}

    public static final DataAccessorType<WiresNetworkSyncData, Void, Void> WIRE_CONNECTOR_DATA_TRANSFER = DataAccessorType.register(new ResourceLocation(PantographsAndWires.MOD_ID, "wire_connection_data_transfer"), DataAccessorType.Builder.createEmptyResponse(
        (in, nbt) -> { 
            nbt.put(DataAccessorType.DEFAULT_NBT_DATA, in.toNbt());
        }, (nbt) -> {
            return WiresNetworkSyncData.fromNbt(nbt.getCompound(DataAccessorType.DEFAULT_NBT_DATA));
        }, (player, in, temp, nbt, iteration) -> {
            for (WireSyncDataEntry syncData : in.syncData()) {
                WireClientNetwork.createClientConnection(in.pos(), syncData);
            }
            return false;
        }
    ));
    
    public static final DataAccessorType<UUID[], Void, Void> DELETE_WIRE_CONNECTION = DataAccessorType.register(new ResourceLocation(PantographsAndWires.MOD_ID, "delete_wire_conection"), DataAccessorType.Builder.createEmptyResponse(
        (in, nbt) -> {
            ListTag list = new ListTag();
            for (int i = 0; i < in.length; i++) {
                UUID id = in[i];
                list.add(StringTag.valueOf(id.toString()));
            }
            nbt.put(DataAccessorType.DEFAULT_NBT_DATA, list);
        }, (nbt) -> {
            return nbt.getList(DataAccessorType.DEFAULT_NBT_DATA, Tag.TAG_STRING).stream().map(x -> UUID.fromString(((StringTag)x).getAsString())).toArray(UUID[]::new);
        }, (player, in, temp, nbt, iteration) -> {
            WireClientNetwork.removeClientConnections(in);
            return false;
        }
    ));
    
    public static final DataAccessorType<WireChunkLoadingData, Void, Void> WIRE_CONNECTION_CHUNK_LOADING = DataAccessorType.register(new ResourceLocation(PantographsAndWires.MOD_ID, "wire_connection_chunk_loading"), DataAccessorType.Builder.createEmptyResponse(
        (in, nbt) -> {
            nbt.put(DataAccessorType.DEFAULT_NBT_DATA, in.toNbt());
        }, (nbt) -> {
            return WireChunkLoadingData.fromNbt(nbt.getCompound(DataAccessorType.DEFAULT_NBT_DATA));
        }, (player, in, temp, nbt, iteration) -> {
            WireClientNetwork.onClientChunkLoading(in);
            return false;
        }
    ));

    public static record CantileverSettingsData(byte size, ECantileverRegistrationArmType cantileverType, ECantileverInsulatorsPlacement insulatorPlacement) {}
    public static final DataAccessorType<CantileverSettingsData, Void, Void> UPDATE_CANTILEVER_SETTINGS = DataAccessorType.register(new ResourceLocation(PantographsAndWires.MOD_ID, "update_cantilever_settings"), DataAccessorType.Builder.createEmptyResponse(
        (in, nbt) -> {
            nbt.putByte("Size", in.size());
            nbt.putInt("Type", in.cantileverType().ordinal());
            nbt.putInt("InsulatorPlacement", in.insulatorPlacement().ordinal());
        }, (nbt) -> {
            return new CantileverSettingsData(
                nbt.getByte("Size"),
                ECantileverRegistrationArmType.values()[nbt.getInt("Type")],
                ECantileverInsulatorsPlacement.values()[nbt.getInt("InsulatorPlacement")]
            );
        }, (player, in, temp, nbt, iteration) -> {
            if (!CantileverBlockItem.setNbt(player.getMainHandItem(), in)) {
                if (!CantileverBlockItem.setNbt(player.getOffhandItem(), in)) {
                    PantographsAndWires.LOGGER.warn("Could not set NBT for 'mainhand=" + player.getMainHandItem() + ",offhand=" + player.getOffhandItem() + "'' because this item is not a CantileverBlockItem.");                }
            }
            return false;
        }
    ));
    
}
