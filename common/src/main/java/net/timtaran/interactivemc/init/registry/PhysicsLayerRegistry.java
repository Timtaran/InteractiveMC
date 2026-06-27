package net.timtaran.interactivemc.init.registry;

import net.xmx.velthoric.core.physics.VxPhysicsLayers;

public class PhysicsLayerRegistry {
    private static short ghostLayer = -1;

    /**
     * @return ghost layer with disabled collisions with everything
     */
    public static short getGhostLayer() {
        if (ghostLayer == -1) {
            ghostLayer = VxPhysicsLayers.claimLayer();

            // Map it to the moving broad-phase layer as the spawned box is dynamic.
            VxPhysicsLayers.setBroadPhaseMapping(ghostLayer, VxPhysicsLayers.BP_MOVING);

            // Configure selective collisions
            VxPhysicsLayers.setCollision(ghostLayer, VxPhysicsLayers.NON_MOVING, false);
            VxPhysicsLayers.setCollision(ghostLayer, VxPhysicsLayers.MOVING, false);
            VxPhysicsLayers.setCollision(ghostLayer, VxPhysicsLayers.TERRAIN, false);
            VxPhysicsLayers.setCollision(ghostLayer, ghostLayer, false);
        }

        return ghostLayer;
    }
}
