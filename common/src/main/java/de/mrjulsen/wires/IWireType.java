package de.mrjulsen.wires;

import de.mrjulsen.wires.block.IWireConnector;
import de.mrjulsen.wires.network.WireConnectionSyncData;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.BlockAndTintGetter;

/**
 * The base for all custom wire types.
 */
public interface IWireType {
    /**
     * Checks if the selected connector block is valid for this wire type.
     * @param level The level the block is placed in.
     * @param pos The position of the connector block.
     * @param connector The connector block itself.
     * @return {@code true} if this wire can be attached to this connector, {@code false} otherwise.
     */
    boolean isValidConnector(BlockAndTintGetter level, BlockPos pos, IWireConnector connector);

    /**
     * Creates all components of the wire connections between two connectors. Can be one single wire or multiple wires.
     * @param context The current build context. Declares whether only the collision data, the rendering data or both is required. Use this to reduce calculations and improving performance by only calculating data that is really needed. If you don't provide the data that is needed, then the wire may not work as expected.
     * @param level The level the wire is in.
     * @param data All data about the connection points of the wire and all custom data that has been saved while placing the wire.
     * @return A collection of all wires that should be used in this connection.
     */
    WireBatch buildWire(WireCreationContext context, BlockAndTintGetter level, WireConnectionSyncData data);

    /**
     * Specifies whether multiple identical connections should be allowed. Connections are considered equal if they start at exactly the same points.
     */
    default boolean allowMultiConnections() {
        return false;
    }
    
    /**
     * The maximum length of the wire between two connectors.
     * @return The maximum length of the wire in blocks.
     */
    int getMaxLength();

    /**
     * How steep the wire can be placed on the y axis. {@code < 0} to disable this check.
     * @return The max steepness.
     */
    default double maxSteepness() {
        return -1;
    }

    /**
     * The ID of this wire type.
     */
    ResourceLocation getRegistryId();
}
