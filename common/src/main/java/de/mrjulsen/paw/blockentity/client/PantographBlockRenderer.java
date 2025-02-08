package de.mrjulsen.paw.blockentity.client;

import org.joml.Vector3f;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import de.mrjulsen.paw.block.PantographBlock;
import de.mrjulsen.paw.blockentity.PantographBlockEntity;
import de.mrjulsen.paw.util.ClientUtils;
import de.mrjulsen.wires.debug.WireDebugRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class PantographBlockRenderer extends GeoBlockRenderer<PantographBlockEntity> {
    
    public PantographBlockRenderer(BlockEntityRendererProvider.Context context) {
        super(new PantographBlockModel());
    }

    @Override
    public void defaultRender(PoseStack poseStack, PantographBlockEntity animatable, MultiBufferSource bufferSource, RenderType renderType, VertexConsumer buffer, float yaw, float partialTick, int packedLight) {
        super.defaultRender(poseStack, animatable, bufferSource, renderType, buffer, yaw, partialTick, packedLight);

        if (!WireDebugRenderer.enabled()) {
            return;
        }
        
        poseStack.pushPose();
        poseStack.translate(0.5D, 0, 0.5D);
        Direction dir = animatable.getBlockState().getValue(PantographBlock.FACING);
        poseStack.mulPose(Axis.YP.rotationDegrees((dir.getAxis() == net.minecraft.core.Direction.Axis.X ? dir.getOpposite() : dir).toYRot()));
        poseStack.translate(0, PantographBlockEntity.MIN_HEIGHT, PantographBlockEntity.FORWARD_OFFSET);
        
        MultiBufferSource.BufferSource mbs = Minecraft.getInstance().renderBuffers().bufferSource();
		VertexConsumer consumer = mbs.getBuffer(RenderType.lines());

        Vector3f rightVec = new Vector3f((float)PantographBlockEntity.BASE_RIGHT_VECTOR.x(), (float)PantographBlockEntity.BASE_RIGHT_VECTOR.y(), (float)PantographBlockEntity.BASE_RIGHT_VECTOR.z());
        Vector3f left_Vec = new Vector3f(rightVec).negate();
        Vector3f upVec = new Vector3f((float)PantographBlockEntity.BASE_UP_VECTOR.x(), (float)PantographBlockEntity.BASE_UP_VECTOR.y(), (float)PantographBlockEntity.BASE_UP_VECTOR.z());
        Vector3f hitVec = new Vector3f(upVec).normalize().mul((float)animatable.debug_hitHeight);

        ClientUtils.renderDebugLine(poseStack, consumer, rightVec, left_Vec, 1f, 1f, 1f, 1f);
        ClientUtils.renderDebugLine(poseStack, consumer, new Vector3f(rightVec).add(upVec), new Vector3f(left_Vec).add(upVec), 1f, 1f, 1f, 1f);      
        ClientUtils.renderDebugLine(poseStack, consumer, rightVec, new Vector3f(rightVec).add(upVec), 1f, 1f, 1f, 1f);
        ClientUtils.renderDebugLine(poseStack, consumer, left_Vec, new Vector3f(left_Vec).add(upVec), 1f, 1f, 1f, 1f);

        ClientUtils.renderDebugLine(poseStack, consumer, new Vector3f(rightVec).add(hitVec), new Vector3f(left_Vec).add(hitVec), 1f, 0f, 0f, 1f);  

        WireDebugRenderer.highlightWire(animatable.debug_wireCollisionA, animatable.debug_wireCollisionB);

        poseStack.popPose();
    }
}
