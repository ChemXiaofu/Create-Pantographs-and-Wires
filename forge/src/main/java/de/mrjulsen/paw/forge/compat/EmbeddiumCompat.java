package de.mrjulsen.paw.forge.compat;

import java.util.Collection;
import java.util.function.Function;

import org.embeddedt.embeddium.api.ChunkMeshEvent;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import de.mrjulsen.wires.render.WireSegmentRenderDataBatch;
import de.mrjulsen.wires.WireNetwork;
import me.jellysquid.mods.sodium.client.render.chunk.compile.ChunkBuildBuffers;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.BlockAndTintGetter;

public class EmbeddiumCompat {
    
    public static void register() {
        ChunkMeshEvent.BUS.addListener(EmbeddiumCompat::meshAppendEvent);
    }
	
    static void meshAppendEvent(ChunkMeshEvent event) {
        if (WireNetwork.hasConnectionsInSection(event.getSectionOrigin())) {
			event.addMeshAppender(c -> {
                renderConnectionsInSection(c.vertexConsumerProvider(), c.sodiumBuildBuffers(), c.blockRenderView(), c.sectionOrigin());
            });
        }
    }

    public static void renderConnectionsInSection(Function<RenderType, VertexConsumer> layers, ChunkBuildBuffers buffers, BlockAndTintGetter region, SectionPos origin) {
		BlockPos chunkOrigin = origin.origin();
		SectionPos chunkSection = SectionPos.of(chunkOrigin);
		if (!WireNetwork.hasConnectionsInSection(chunkSection)) {
			return;
		}

		RenderType renderType = RenderType.solid();//ModRenderType.WIRE;
		VertexConsumer vertexConsumer = layers.apply(renderType);
		PoseStack poseStack = new PoseStack();

		Collection<WireSegmentRenderDataBatch> connections = WireNetwork.connectionsInSection(chunkSection);

		for (WireSegmentRenderDataBatch connection : connections) {
			connection.render(vertexConsumer);

			/*
			for (WireSegmentRenderData segment : connection.getSubWireSegments()) {	
				Vec3 point = segment.getPoint(0).vertex(VertexCorner.CENTER);
				BlockPos pos = chunkOrigin.offset(point.x(), point.y(), point.z());
				poseStack.pushPose();
				poseStack.translate((double)(chunkOrigin.getX() & 15), (double)(chunkOrigin.getY() & 15), (double)(chunkOrigin.getZ() & 15));
				poseStack.translate(point.x(), point.y(), point.z());
				//poseStack.scale(0.2f, 0.2f, 0.2f);
				poseStack.mulPose(Vector3f.XN.rotationDegrees(90));
				poseStack.translate(-0.5f, -0.5f, -0.5f);
				//Minecraft.getInstance().getBlockRenderer().renderBatched(ModBlocks.I_INSULATOR_BROWN.get().defaultBlockState(), pos, region, poseStack, vertexConsumer, false, new Random());

				//LevelRenderer.renderLineBox(poseStack, vertexConsumer, new AABB(pos), SEGMENTS_AUTO, SEGMENTS_AUTO, SEGMENTS_AUTO, SEGMENTS_AUTO);

				BlockRenderDispatcher blockRenderer = Minecraft.getInstance().getBlockRenderer();
				blockRenderer.getModelRenderer().renderModel(
					poseStack.last(),
					buffers.builder(RenderType.solid()),
					ModBlocks.I_INSULATOR_BROWN.get().defaultBlockState(),
					blockRenderer.getBlockModel(ModBlocks.I_INSULATOR_BROWN.get().defaultBlockState()),
					1,
					1,
					1,
					LevelRenderer.getLightColor(Minecraft.getInstance().level, chunkOrigin),
					0
				);
				poseStack.popPose();
			}
			*/
			//renderCatenary(vertexConsumer, connection.getRelativeStart(), connection.getRelativeEnd());
		}
		
		//CompiledChunkExtension ext = (CompiledChunkExtension)renderChunk.compiled.get();
		//ext.setHasWires(true);
	}
}
