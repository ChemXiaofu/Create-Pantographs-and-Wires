package de.mrjulsen.wires.render;

import java.util.Map;

import net.minecraft.core.SectionPos;
import net.minecraft.world.phys.Vec3;

public class WireRenderPoint {
    private final Map<VertexCorner, Vec3> vertices;

    public WireRenderPoint(Map<VertexCorner, Vec3> data) {
        this.vertices = data;
    }

    public Vec3 vertex(VertexCorner corner) {
        return vertices.get(corner);
    }

	public static enum VertexCorner { CENTER, TOP_RIGHT, BOTTOM_RIGHT, TOP_LEFT, BOTTOM_LEFT; }

    public WireRenderPoint offset(SectionPos rawSection) {
        Vec3 sub = new Vec3(rawSection.x() * SectionPos.SECTION_SIZE, rawSection.y() * SectionPos.SECTION_SIZE, rawSection.z() * SectionPos.SECTION_SIZE);
        return new WireRenderPoint(Map.of(
            VertexCorner.CENTER, vertex(VertexCorner.CENTER).subtract(sub),
            VertexCorner.TOP_RIGHT, vertex(VertexCorner.TOP_RIGHT).subtract(sub),
            VertexCorner.BOTTOM_RIGHT, vertex(VertexCorner.BOTTOM_RIGHT).subtract(sub),
            VertexCorner.TOP_LEFT, vertex(VertexCorner.TOP_LEFT).subtract(sub),
            VertexCorner.BOTTOM_LEFT, vertex(VertexCorner.BOTTOM_LEFT).subtract(sub)
        ));
    }

}
