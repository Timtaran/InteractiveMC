/*
 * This file is part of InteractiveMC.
 * Licensed under LGPL 3.0.
 */
package net.timtaran.interactivemc.body.player.renderer;

import com.github.stephengold.joltjni.Quat;
import com.github.stephengold.joltjni.Vec3;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.MultiBufferSource;
import net.timtaran.interactivemc.body.player.physics.PlayerBodyPartRigidBody;
import net.timtaran.interactivemc.util.client.render.WireframeRenderer;
import net.xmx.velthoric.core.body.VxBody;
import net.xmx.velthoric.core.body.client.VxRenderState;
import net.xmx.velthoric.core.body.client.renderer.VxBodyRenderer;
import org.joml.Quaternionf;

/**
 * Renderer for player body part rigid bodies.
 * <p>
 * Renders player body parts as wireframe cubes with the appropriate dimensions and orientation.
 *
 * @author timtaran
 */
@Environment(EnvType.CLIENT)
public class PlayerBodyPartRenderer extends VxBodyRenderer<VxBody> {
    /**
     * Renders a player body part rigid body as a wireframe cube.
     *
     * @param body         the body to render
     * @param poseStack    the pose stack for transformations
     * @param bufferSource the buffer source for rendering
     * @param partialTicks the partial ticks for interpolation
     * @param packedLight  the packed light value
     * @param renderState  the render state containing transform and other data
     */
    @Override
    public void render(VxBody body, PoseStack poseStack, MultiBufferSource bufferSource, float partialTicks, int packedLight, VxRenderState renderState) {
        Vec3 halfExtents = body.get(PlayerBodyPartRigidBody.DATA_HALF_EXTENTS);
        float hx = halfExtents.getX();
        float hy = halfExtents.getY();
        float hz = halfExtents.getZ();

        float fullWidth = hx * 2.0f;
        float fullHeight = hy * 2.0f;
        float fullDepth = hz * 2.0f;

        poseStack.pushPose();

        Quat renderRotation = renderState.transform.getRotation();
        poseStack.mulPose(new Quaternionf(renderRotation.getX(), renderRotation.getY(), renderRotation.getZ(), renderRotation.getW()));

        poseStack.translate(-hx, -hy, -hz);
        poseStack.scale(fullWidth, fullHeight, fullDepth);

        WireframeRenderer.renderUnitCubeWireframe(poseStack, bufferSource, packedLight, 1.0f, 1.0f, 0.0f, 1.0f);

        poseStack.popPose();
    }
}
