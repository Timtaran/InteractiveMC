package net.timtaran.interactivemc.init.registry;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.phys.Vec3;
import net.timtaran.interactivemc.event.player.PlayerLifecycleEvents;
import net.timtaran.interactivemc.event.tick.ServerTickEvents;
import net.xmx.velthoric.event.api.VxRenderEvent;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/**
 * Registry for mod events.
 * <p>
 * This class is responsible for registering and initializing all event listeners
 * used by the InteractiveMC mod.
 *
 * @author timtaran
 */
public class EventRegistry {
    public static Vec3 pointPos = new Vec3(0f, 128f, 0f);
    /**
     * Registers common events that apply to both client and server.
     */
    public static void register() {
        PlayerLifecycleEvents.init();
        ServerTickEvents.init();
    }

    /**
     * Registers client-side specific events.
     */
    @Environment(EnvType.CLIENT)
    public static void registerClient() {
//        VxRenderEvent.ClientRenderLevelStageEvent.EVENT.register(event -> {
//            if (event.getStage() != VxRenderEvent.ClientRenderLevelStageEvent.Stage.AFTER_ENTITIES) return;
//            renderMarkerAt(event.getStage(), event.getLevelRenderer(), event.getPoseStack(), event.getPartialTick(), event.getProjectionMatrix(), event.getLightTexture(),
//                    pointPos.x, pointPos.y, pointPos.z, // world coords
//                    1.0f, 0.2f, 0.2f, 1.0f); // color RGBA
//        });
    }

    /**
     * Render a marker at world coordinates (wx, wy, wz). Depth test is disabled during drawing so the marker
     * appears above world geometry. The method uses the provided PoseStack and projection matrix.
     *
     * @param stage            render stage from event (not used here but kept for signature parity)
     * @param levelRenderer    level renderer from event (kept for parity / future use)
     * @param matrices         pose stack supplied by the event
     * @param partialTick      partial tick supplied by the event (not needed for static marker)
     * @param projectionMatrix projection matrix supplied by the event
     * @param lightTexture     light texture supplied by the event (not needed here)
     * @param wx               world X coordinate
     * @param wy               world Y coordinate
     * @param wz               world Z coordinate
     * @param r                red 0..1
     * @param g                green 0..1
     * @param b                blue 0..1
     * @param a                alpha 0..1
     */
    public static void renderMarkerAt(
            Object stage, // keep generic type if your event's stage type differs
            LevelRenderer levelRenderer,
            PoseStack matrices,
            float partialTick,
            Matrix4f projectionMatrix,
            LightTexture lightTexture,
            double wx, double wy, double wz,
            float r, float g, float b, float a
    ) {
        Minecraft mc = Minecraft.getInstance();
        Camera camera = mc.gameRenderer.getMainCamera();
        Vec3 cameraPos = camera.getPosition();

        // position relative to camera
        float rx = (float) (wx - cameraPos.x);
        float ry = (float) (wy - cameraPos.y);
        float rz = (float) (wz - cameraPos.z);

        matrices.pushPose();
        // Translate relative to camera
        matrices.translate(rx - cameraPos.x, ry - cameraPos.y, rz - cameraPos.z);

        // Disable Depth Test to render on top
        RenderSystem.disableDepthTest();

        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();

        // Draw your point here (e.g., using a VertexConsumer to draw a small box)
        // drawPoint(matrices, context.consumers());
        drawBox(bufferSource.getBuffer(RenderType.lines()), matrices.last().pose(), rx, ry, rz, 0.5f);

        // Re-enable Depth Test
        RenderSystem.enableDepthTest();
        matrices.popPose();
        bufferSource.endBatch();
    }

    private static void drawBox(VertexConsumer buffer, Matrix4f matrix, float x, float y, float z, float size) {
        // Простейший квадрат, ориентированный на игрока или просто в пространстве
        buffer.addVertex(matrix, x - size, y - size, z - size).setColor(255, 0, 0, 255).setNormal(0f, 1f, 0f);
        buffer.addVertex(matrix, x + size, y - size, z - size).setColor(255, 0, 0, 255).setNormal(0f, 1f, 0f);
        buffer.addVertex(matrix, x + size, y + size, z - size).setColor(255, 0, 0, 255).setNormal(0f, 1f, 0f);
        buffer.addVertex(matrix, x - size, y + size, z - size).setColor(255, 0, 0, 255).setNormal(0f, 1f, 0f);
    }

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
