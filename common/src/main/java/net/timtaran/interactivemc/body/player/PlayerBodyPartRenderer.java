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

@Environment(EnvType.CLIENT)
public class PlayerBodyPartRenderer extends VxRigidBodyRenderer<PlayerBodyPartRigidBody> {
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

    // Some AI slop down
    // Call this from your client-side render method
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

    // Emit a single vertex using the set* API on the VertexConsumer (works with your decompiled interface)
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
