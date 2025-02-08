package de.mrjulsen.wires;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import de.mrjulsen.mcdragonlib.data.Cache;
import de.mrjulsen.wires.render.WireRenderData;
import de.mrjulsen.wires.render.WireSegmentRenderData;
import de.mrjulsen.wires.render.WireSegmentRenderDataBatch;
import net.minecraft.core.SectionPos;

/** A collection of several individual wires combined to one cable connection. */
public class WireBatch {
    private final Set<Wire> subWires = new HashSet<>();

    private final Cache<Set<WirePoints>> collisionCache = new Cache<>(() -> {
        Set<WirePoints> points = new HashSet<>(subWires.size());
        for (Wire wire : subWires) {
            if (!wire.getCollisionData().isPresent()) {
                continue;
            }
            points.add(wire.collisionData());
        }
        return points;
    });
    private final Cache<Set<WireRenderData>> renderCache = new Cache<>(() -> {
        Set<WireRenderData> points = new HashSet<>(subWires.size());
        for (Wire wire : subWires) {
            if (!wire.getRenderData().isPresent()) {
                continue;
            }
            points.add(wire.renderData());
        }
        return points;
    });

    /**
     * Create a new collection of wires with initial values.
     * @param mainWire The first wire
     */
    public WireBatch(Wire mainWire) {
        this.subWires.add(mainWire);        
    }

    /**
     * Create a new collection of wires with initial values.
     * @param wires The first wires
     * @return
     */
    public static WireBatch of(Wire... wires) {
        if (wires.length <= 0) {
            throw new IllegalArgumentException("At least one wire must be provided!");
        }
        WireBatch batch = new WireBatch(wires[0]);
        for (int i = 1; i < wires.length; i++) {
            batch.addSubWire(wires[i]);
        }
        return batch;
    }

    /**
     * Add additional wire to this collection.
     * @param subWire The new wire
     */
    public void addSubWire(Wire subWire) {
        this.subWires.add(subWire);
    }

    public int count() {
        return subWires.size();
    }

    public Set<Wire> getWires() {
        return subWires;
    }

    public Set<WirePoints> getCollisions() {
        return collisionCache.get();
    }

    public Set<WireRenderData> getRenderData() {
        return renderCache.get();
    }

    public Map<SectionPos, WireSegmentRenderDataBatch> splitRenderDataInChunkSections(UUID id, SectionPos origin) {
        Map<SectionPos, WireSegmentRenderDataBatch> result = new HashMap<>();
        
        for (Wire wire : subWires) {
            Optional<WireRenderData> data = wire.getRenderData();
            if (!data.isPresent()) continue;
            Map<SectionPos, WireSegmentRenderData> segments = data.get().splitInChunkSections(origin);
            for (Map.Entry<SectionPos, WireSegmentRenderData> segment : segments.entrySet()) {
                result.computeIfAbsent(segment.getKey(), x -> new WireSegmentRenderDataBatch(id, segment.getKey())).addSegment(segment.getValue());
            }
        }

        return result;
    }
}
