/*
 * This file is part of InteractiveMC.
 * Licensed under LGPL 3.0.
 */
package net.timtaran.interactivemc.body.player.physics;

import com.github.stephengold.joltjni.BodyCreationSettings;
import com.github.stephengold.joltjni.BoxShapeSettings;
import com.github.stephengold.joltjni.ShapeSettings;
import com.github.stephengold.joltjni.Vec3;
import com.github.stephengold.joltjni.enumerate.EMotionType;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.timtaran.interactivemc.body.player.PlayerBodyPart;
import net.timtaran.interactivemc.body.player.store.ClientPlayerBodyDataStore;
import net.timtaran.interactivemc.body.player.store.PlayerBodyDataStore;
import net.timtaran.interactivemc.network.sync.DataSerializers;
import net.xmx.velthoric.core.body.VxBody;
import net.xmx.velthoric.core.body.VxBodyType;
import net.xmx.velthoric.core.body.client.VxClientBodyManager;
import net.xmx.velthoric.core.body.factory.VxRigidBodyFactory;
import net.xmx.velthoric.core.network.synchronization.VxDataSerializers;
import net.xmx.velthoric.core.network.synchronization.VxSynchronizedData;
import net.xmx.velthoric.core.network.synchronization.accessor.VxServerAccessor;
import net.xmx.velthoric.core.physics.VxJoltBridge;
import net.xmx.velthoric.core.physics.VxPhysicsLayers;
import net.xmx.velthoric.core.physics.world.VxPhysicsWorld;
import net.xmx.velthoric.math.VxConversions;
import net.xmx.velthoric.network.VxByteBuf;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.vivecraft.api.data.VRBodyPartData;
import org.vivecraft.api.data.VRPose;

import java.util.UUID;

/**
 * Kinematic ghost rigid body without any physics which will be used to be linked with dynamic rigid body via constraint.
 * <p>
 * This ghost body tracks the player's VR controller position and orientation, and is connected to the
 * dynamic body through a six-degree-of-freedom constraint.
 *
 * @author timtaran
 */
public class PlayerBodyPartGhostRigidBody extends VxBody {
    private static final float FIXED_TIME_STEP = VxPhysicsWorld.getFixedTimeStep();
    private static short selectiveGhostLayer = -1;

    private boolean isIndexSaved = false;

    /**
     * The half-extents (dimensions) of this ghost body part.
     */
    public static final VxServerAccessor<Vec3> DATA_HALF_EXTENTS = VxServerAccessor.create(PlayerBodyPartGhostRigidBody.class, VxDataSerializers.VEC3);
    /**
     * The type of body part (head, hands, etc.).
     */
    public static final VxServerAccessor<PlayerBodyPart> DATA_BODY_PART = VxServerAccessor.create(PlayerBodyPartGhostRigidBody.class, DataSerializers.BODY_PART);
    /**
     * The UUID of the player who owns this ghost body part.
     */
    public static final VxServerAccessor<UUID> DATA_PLAYER_ID = VxServerAccessor.create(PlayerBodyPartGhostRigidBody.class, VxDataSerializers.UUID);

    /**
     * Server-side constructor.
     *
     * @param type  the body type
     * @param world the physics world
     * @param id    the unique identifier for this body
     */
    public PlayerBodyPartGhostRigidBody(VxBodyType type, VxPhysicsWorld world, UUID id) {
        super(type, world, id);
    }

    /**
     * Client-side constructor.
     *
     * @param type the body type
     * @param id   the unique identifier for this body
     */
    @Environment(EnvType.CLIENT)
    public PlayerBodyPartGhostRigidBody(VxBodyType type, UUID id) {
        super(type, id);
    }

    @Override
    protected void defineSyncData(VxSynchronizedData.Builder builder) {
        builder.define(DATA_HALF_EXTENTS, new Vec3(0.25f, 0.25f, 0.25f));
        builder.define(DATA_BODY_PART, PlayerBodyPart.HEAD);
        builder.define(DATA_PLAYER_ID, UUID.randomUUID());
    }

    /**
     * Creates the Jolt physics body for this ghost rigid body.
     * <p>
     * The body is configured as kinematic (non-physics) and uses a selective collision layer
     * to avoid collisions with other bodies.
     * </p>
     *
     * @param factory the rigid body factory
     * @return the Jolt body ID
     */
    public static int createJoltBody(VxBody body, VxRigidBodyFactory factory) {
        if (selectiveGhostLayer == -1) {
            selectiveGhostLayer = VxPhysicsLayers.claimLayer();

            // Map it to the moving broad-phase layer as the spawned box is dynamic.
            VxPhysicsLayers.setBroadPhaseMapping(selectiveGhostLayer, VxPhysicsLayers.BP_MOVING);

            // Configure selective collisions
            VxPhysicsLayers.setCollision(selectiveGhostLayer, VxPhysicsLayers.NON_MOVING, false);
            VxPhysicsLayers.setCollision(selectiveGhostLayer, VxPhysicsLayers.MOVING, false);
            VxPhysicsLayers.setCollision(selectiveGhostLayer, VxPhysicsLayers.TERRAIN, false);
            VxPhysicsLayers.setCollision(selectiveGhostLayer, selectiveGhostLayer, false);
        }

        PlayerBodyPart partType = body.get(DATA_BODY_PART);
        Vec3 fullSize = partType.getSize();

        try (
                ShapeSettings shapeSettings = new BoxShapeSettings(new Vec3(fullSize.getX() / 2, fullSize.getY() / 2, fullSize.getZ() / 2));
                BodyCreationSettings bcs = new BodyCreationSettings()) {
            bcs.setMotionType(EMotionType.Kinematic);
            bcs.setObjectLayer(selectiveGhostLayer);
            bcs.setAllowSleeping(false);
            return factory.create(shapeSettings, bcs);
        }
    }

    @Override
    public void onPrePhysicsTick(VxPhysicsWorld world) {
        VRPose pose = PlayerBodyDataStore.vrPoses.get(get(DATA_PLAYER_ID));
        if (pose == null) return;

        PlayerBodyPart bodyPart = get(DATA_BODY_PART);

        VRBodyPartData bodyPartData = pose.getBodyPartData(bodyPart.toVRBodyPart());
        if (bodyPartData == null) return;

        Quaternionf targetRot = new Quaternionf(bodyPartData.getRotation());

        Vector3f controllerPos = new Vector3f(bodyPartData.getPos().toVector3f());
        Vector3f offset = new Vector3f(bodyPart.getTrackingOffset().toVector3f());

        targetRot.transform(offset);

        Vector3f targetPos = controllerPos.add(offset);

        VxJoltBridge.INSTANCE.getJoltBody(world, this).moveKinematic(
                VxConversions.toJolt(targetPos).toRVec3(),
                VxConversions.toJolt(targetRot),
                FIXED_TIME_STEP
        );
    }

    /**
     * Called when the body is added to the client world.
     *
     * @param level the client level
     */
    @Override
    public void onBodyAdded(ClientLevel level) {
        addBodyIndexToClientStorage();
    }

    /**
     * Adds this body's index to the client-side storage for tracking.
     */
    private void addBodyIndexToClientStorage() {
        Integer index = VxClientBodyManager.getInstance().getStore().getIndexForNetworkId(getNetworkId());

        if (index == null)
            return;

        isIndexSaved = true;
        ClientPlayerBodyDataStore.playerControlledBodies.add(index);
    }

    /**
     * Called when the body is removed from the client world.
     *
     * @param level the client level
     */
    @Override
    public void onBodyRemoved(ClientLevel level) {
        Integer index = VxClientBodyManager.getInstance().getStore().getIndexForNetworkId(getNetworkId());

        if (index == null)
            return;

        ClientPlayerBodyDataStore.playerControlledBodies.remove(index);
    }

    /**
     * Called every client tick to ensure the body index is saved.
     */
    @Override
    public void onClientTick() {
        if (!isIndexSaved)
            addBodyIndexToClientStorage();
    }

    public static void writePersistenceData(VxBody body, VxByteBuf buf) {
        VxDataSerializers.VEC3.write(buf, body.get(DATA_HALF_EXTENTS));
        DataSerializers.BODY_PART.write(buf, body.get(DATA_BODY_PART));
        VxDataSerializers.UUID.write(buf, body.get(DATA_PLAYER_ID));
    }

    public static void readPersistenceData(VxBody body, VxByteBuf buf) {
        body.setServerData(DATA_HALF_EXTENTS, VxDataSerializers.VEC3.read(buf));
        body.setServerData(DATA_BODY_PART, DataSerializers.BODY_PART.read(buf));
        body.setServerData(DATA_PLAYER_ID, VxDataSerializers.UUID.read(buf));
    }
}