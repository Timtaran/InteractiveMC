/*
 * This file is part of InteractiveMC.
 * Licensed under LGPL 3.0.
 */
package net.timtaran.interactivemc.body.duck;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.xmx.velthoric.core.body.VxBody;
import net.xmx.velthoric.core.body.client.VxRenderState;
import net.xmx.velthoric.core.body.client.renderer.VxBodyRenderer;
import net.xmx.velthoric.math.VxConversions;

public class TestDuckRigidBodyRenderer extends VxBodyRenderer<VxBody> {
    @Override
    public void render(VxBody body, PoseStack poseStack, MultiBufferSource bufferSource, float partialTicks, int packedLight, VxRenderState renderState) {
        BlockState blockState = Blocks.YELLOW_CONCRETE.defaultBlockState();

        float hx = 0.2f;
        float hy = 0.2f;
        float hz = 0.2f;

        float fullWidth = hx * 2.0f;
        float fullHeight = hy * 2.0f;
        float fullDepth = hz * 2.0f;

        poseStack.pushPose();

        poseStack.mulPose(VxConversions.toJoml(renderState.transform.getRotation()));

        poseStack.translate(-hx, -hy, -hz);
        poseStack.scale(fullWidth, fullHeight, fullDepth);

        Minecraft.getInstance().getBlockRenderer().renderSingleBlock(
                blockState,
                poseStack,
                bufferSource,
                packedLight,
                OverlayTexture.NO_OVERLAY
        );

        poseStack.popPose();
    }
}
