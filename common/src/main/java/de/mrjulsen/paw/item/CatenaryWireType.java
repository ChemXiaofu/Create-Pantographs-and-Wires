package de.mrjulsen.paw.item;

import de.mrjulsen.paw.block.TensioningDeviceBlock;
import de.mrjulsen.paw.block.abstractions.ICatenaryWireConnector;
import de.mrjulsen.paw.config.ModServerConfig;
import de.mrjulsen.paw.util.Utils;
import de.mrjulsen.wires.block.IWireConnector;
import de.mrjulsen.wires.network.WireConnectionSyncData;
import de.mrjulsen.wires.render.WireRenderData;
import de.mrjulsen.wires.render.WireRenderPoint.VertexCorner;
import de.mrjulsen.wires.Wire;
import de.mrjulsen.wires.WireBatch;
import de.mrjulsen.wires.WireBuilder;
import de.mrjulsen.wires.WireCreationContext;
import de.mrjulsen.wires.WireBuilder.CableType;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.phys.Vec3;

public class CatenaryWireType extends AbstractWireType {

	private static final float HANG_FAC = 0.025f;
	private static final float THICKNESS = 0.5f / 16f;

	public CatenaryWireType(ResourceLocation location) {
		super(location);
	}

	@Override
	public boolean isValidConnector(BlockAndTintGetter level, BlockPos pos, IWireConnector connector) {
		return connector instanceof ICatenaryWireConnector;
	}

	@Override
	public boolean allowMultiConnections() {
		return false;
	}

	@Override
	public int getMaxLength() {
		return ModServerConfig.ENERGY_WIRE_MAX_LENGTH.get();
	}

	@Override
	public WireBatch buildWire(WireCreationContext context, BlockAndTintGetter level, WireConnectionSyncData data) {
		Vec3 start = data.getStartPos();
		Vec3 end = data.getEndPos();
		Vec3 contactWireAttachPointA = data.getWireAttachPointA();
		Vec3 contactWireAttachPointB = data.getWireAttachPointB();
		Vec3 tensionWireAttachPointA = Utils.getNbtVec3(data.getConnectorAData(), ICatenaryWireConnector.NBT_TENSION_WIRE_ATTACH_POINT);
		Vec3 tensionWireAttachPointB = Utils.getNbtVec3(data.getConnectorBData(), ICatenaryWireConnector.NBT_TENSION_WIRE_ATTACH_POINT);

		float length = (float)Math.abs(end.subtract(start).length());
		float hang = data.getConnectorAData().contains(TensioningDeviceBlock.NBT_TENSION) || data.getConnectorBData().contains(TensioningDeviceBlock.NBT_TENSION) ? 0.5f : HANG_FAC * length;
		
		Wire tensionWire = WireBuilder.createWire(context, start.add(tensionWireAttachPointA), end.add(tensionWireAttachPointB), CableType.TENSION, THICKNESS * 0.75f, hang, (int)(length / 4f));
		Wire contactWire = WireBuilder.createWire(context, start.add(contactWireAttachPointA), end.add(contactWireAttachPointB), CableType.TIGHT, THICKNESS, 0, (int)(length / 4f));
		WireBatch batch = WireBatch.of(contactWire, tensionWire);

		if (context.renderingRequired() && tensionWire.getRenderData().isPresent() && contactWire.getRenderData().isPresent()) {
			WireRenderData tensionRenderData = tensionWire.renderData();
			WireRenderData contactRenderData = contactWire.renderData();
			for (int i = 1; i < tensionRenderData.count() - 1 && i < contactRenderData.count() - 1; i++) {
				batch.addSubWire(WireBuilder.createWire(WireCreationContext.RENDERING, contactRenderData.getPoint(i).vertex(VertexCorner.CENTER), tensionRenderData.getPoint(i).vertex(VertexCorner.CENTER), CableType.TIGHT, THICKNESS * 0.4f, 0, 1));
			}
		}
		return batch;
	}
}
