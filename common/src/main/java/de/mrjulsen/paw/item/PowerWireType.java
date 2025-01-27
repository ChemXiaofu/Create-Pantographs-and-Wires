package de.mrjulsen.paw.item;

import de.mrjulsen.paw.block.AbstractPlaceableInsulatorBlock;
import de.mrjulsen.paw.config.ModServerConfig;
import de.mrjulsen.paw.util.Const;
import de.mrjulsen.wires.block.IWireConnector;
import de.mrjulsen.wires.network.WireConnectionSyncData;
import de.mrjulsen.wires.Wire;
import de.mrjulsen.wires.WireBatch;
import de.mrjulsen.wires.WireBuilder;
import de.mrjulsen.wires.WireCreationContext;
import de.mrjulsen.wires.WireBuilder.CableType;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.phys.Vec3;

public class PowerWireType extends AbstractWireType {

	private static final float HANG_FAC = 0.025f;
	private static final float THICKNESS = Const.PIXEL;	

	public PowerWireType(ResourceLocation location) {
		super(location);
	}

	@Override
	public boolean isValidConnector(BlockAndTintGetter level, BlockPos pos, IWireConnector connector) {
		return connector instanceof AbstractPlaceableInsulatorBlock;
	}

	@Override
	public boolean allowMultiConnections() {
		return false;
	}

	@Override
	public int getMaxLength() {
		return ModServerConfig.CATENARY_WIRE_MAX_LENGTH.get();
	}

	@Override
	public WireBatch buildWire(WireCreationContext context, BlockAndTintGetter level, WireConnectionSyncData data) {
		Vec3 start = data.getStartPos();
		Vec3 end = data.getEndPos();
		Vec3 wireAttachPointA = data.getWireAttachPointA();
		Vec3 wireAttachPointB = data.getWireAttachPointB();
	
		float length = (float)Math.abs(end.subtract(start).length());
		Wire wire = WireBuilder.createWire(context, start.add(wireAttachPointA), end.add(wireAttachPointB), CableType.HANGING, THICKNESS, HANG_FAC * length, WireBuilder.SEGMENTS_AUTO);
		WireBatch batch = WireBatch.of(wire);
		return batch;
	}
}
