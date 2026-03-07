/*
 * This file is part of InteractiveMC.
 * Licensed under LGPL 3.0.
 */
package net.timtaran.interactivemc.body.player;

import com.github.stephengold.joltjni.Quat;
import com.github.stephengold.joltjni.Vec3;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.xmx.velthoric.core.body.client.VxRenderState;
import net.xmx.velthoric.core.body.client.renderer.VxRigidBodyRenderer;
import org.joml.Quaternionf;

/**
 * Renderer for player body part rigid bodies.
 * <p>
 * Renders player body parts as wireframe cubes with the appropriate dimensions and orientation.
 *
 * @author timtaran
 */
@Environment(EnvType.CLIENT)
public class PlayerBodyPartRenderer extends VxRigidBodyRenderer<PlayerBodyPartRigidBody> {
    /**
     * Renders a player body part rigid body as a wireframe cube.
     *
     * @param body the body to render
     * @param poseStack the pose stack for transformations
     * @param bufferSource the buffer source for rendering
     * @param partialTicks the partial ticks for interpolation
     * @param packedLight the packed light value
     * @param renderState the render state containing transform and other data
     */
    @Override
    public void render(PlayerBodyPartRigidBody body, PoseStack poseStack, MultiBufferSource bufferSource, float partialTicks, int packedLight, VxRenderState renderState) {
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

        renderUnitCubeWireframe(poseStack, bufferSource, packedLight, 1.0f, 1.0f, 0.0f, 1.0f);

        poseStack.popPose();
    }

    /**
     * Renders a unit cube wireframe with the specified color.
     * <p>
     * This method draws all 12 edges of a unit cube from (0,0,0) to (1,1,1).
     * The pose stack should be positioned and scaled appropriately before calling this method.
     *
     * @param poseStack the pose stack for transformations
     * @param bufferSource the buffer source for rendering
     * @param packedLight the packed light value
     * @param r the red color component (0.0 to 1.0)
     * @param g the green color component (0.0 to 1.0)
     * @param b the blue color component (0.0 to 1.0)
     * @param a the alpha component (0.0 to 1.0)
     */
    public static void renderUnitCubeWireframe(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, float r, float g, float b, float a) {
        PoseStack.Pose pose = poseStack.last();
        VertexConsumer builder = bufferSource.getBuffer(RenderType.lines());

        float min = 0f;
        float max = 1f;

        // 12 edges: each entry is {x1,y1,z1, x2,y2,z2}
        float[][] edges = {
                // bottom face
                {min, min, min,  max, min, min},
                {max, min, min,  max, min, max},
                {max, min, max,  min, min, max},
                {min, min, max,  min, min, min},

                // top face
                {min, max, min,  max, max, min},
                {max, max, min,  max, max, max},
                {max, max, max,  min, max, max},
                {min, max, max,  min, max, min},

                // verticals
                {min, min, min,  min, max, min},
                {max, min, min,  max, max, min},
                {max, min, max,  max, max, max},
                {min, min, max,  min, max, max}
        };

        for (float[] e : edges) {
            emitVertex(pose, builder, e[0], e[1], e[2], r, g, b, a, packedLight);
            emitVertex(pose, builder, e[3], e[4], e[5], r, g, b, a, packedLight);
        }
    }

    /**
     * Emits a single vertex to the vertex consumer.
     *
     * @param pose the current pose for transformations
     * @param builder the vertex consumer
     * @param x the x coordinate
     * @param y the y coordinate
     * @param z the z coordinate
     * @param r the red color component (0.0 to 1.0)
     * @param g the green color component (0.0 to 1.0)
     * @param b the blue color component (0.0 to 1.0)
     * @param a the alpha component (0.0 to 1.0)
     * @param packedLight the packed light value
     */
    private static void emitVertex(PoseStack.Pose pose,
                                   VertexConsumer builder,
                                   float x, float y, float z,
                                   float r, float g, float b, float a,
                                   int packedLight) {
        // transform position and emit; chain the setters (they modify the last vertex)
        builder.addVertex(pose, x, y, z)
                .setColor(r, g, b, a)                           // set color (float overload)
                .setUv(0f, 0f)                            // UV not used for lines but set anyway
                .setOverlay(OverlayTexture.NO_OVERLAY)          // overlay
                .setLight(packedLight)                          // packed light
                .setNormal(0f, 1f, 0f); // normal (ignored for lines)
    }
}
