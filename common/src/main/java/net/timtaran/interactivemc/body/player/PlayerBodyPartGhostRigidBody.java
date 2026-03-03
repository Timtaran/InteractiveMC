/*
 * This file is part of InteractiveMC.
 * Licensed under LGPL 3.0.
 */
package net.timtaran.interactivemc.body.player;

import com.github.stephengold.joltjni.*;
import com.github.stephengold.joltjni.enumerate.EMotionType;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.timtaran.interactivemc.data.ClientDataStore;
import net.timtaran.interactivemc.data.PlayerDataStore;
import net.timtaran.interactivemc.network.sync.DataSerializers;
import net.xmx.velthoric.core.body.client.VxClientBodyManager;
import net.xmx.velthoric.core.network.synchronization.VxDataSerializers;
import net.xmx.velthoric.core.network.synchronization.VxSynchronizedData;
import net.xmx.velthoric.core.network.synchronization.accessor.VxServerAccessor;
import net.xmx.velthoric.core.physics.VxJoltBridge;
import net.xmx.velthoric.core.physics.VxPhysicsLayers;
import net.xmx.velthoric.math.VxConversions;
import net.xmx.velthoric.network.VxByteBuf;
import net.xmx.velthoric.core.body.registry.VxBodyType;
import net.xmx.velthoric.core.body.type.VxRigidBody;
import net.xmx.velthoric.core.body.type.factory.VxRigidBodyFactory;
import net.xmx.velthoric.core.physics.world.VxPhysicsWorld;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.vivecraft.api.data.VRBodyPartData;
import org.vivecraft.api.data.VRPose;

import java.util.UUID;

/**
 * Kinematic ghost rigid body without any physics which will be used to be linked with dynamic rigid body via constraint.
 *
 * @author timtaran 
 */
public class PlayerBodyPartGhostRigidBody extends VxRigidBody {
    private static final float FIXED_TIME_STEP = 1f / 60; // check VxPhysicsWorld
    private static short selectiveGhostLayer = -1;

    private boolean isIndexSaved = false;

    public static final VxServerAccessor<Vec3> DATA_HALF_EXTENTS = VxServerAccessor.create(PlayerBodyPartGhostRigidBody.class, VxDataSerializers.VEC3);
    public static final VxServerAccessor<PlayerBodyPart> DATA_BODY_PART = VxServerAccessor.create(PlayerBodyPartGhostRigidBody.class, DataSerializers.BODY_PART);
    public static final VxServerAccessor<UUID> DATA_PLAYER_ID = VxServerAccessor.create(PlayerBodyPartGhostRigidBody.class, VxDataSerializers.UUID);

    /**
     * Server-side constructor.
     */
    public PlayerBodyPartGhostRigidBody(VxBodyType<PlayerBodyPartGhostRigidBody> type, VxPhysicsWorld world, UUID id) {
        super(type, world, id);
        setPersistent(false);
    }

    /**
     * Client-side constructor.
     */
    @Environment(EnvType.CLIENT)
    public PlayerBodyPartGhostRigidBody(VxBodyType<PlayerBodyPartGhostRigidBody> type, UUID id) {
        super(type, id);
    }

    @Override
    protected void defineSyncData(VxSynchronizedData.Builder builder) {
        builder.define(DATA_HALF_EXTENTS, new Vec3(0.25f, 0.25f, 0.25f));
        builder.define(DATA_BODY_PART, PlayerBodyPart.HEAD);
        builder.define(DATA_PLAYER_ID, UUID.randomUUID());
    }

    public int createJoltBody(VxRigidBodyFactory factory) {
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

        PlayerBodyPart partType = get(DATA_BODY_PART);
        Vec3 fullSize = partType.getSize();

        try (ShapeSettings shapeSettings = new BoxShapeSettings(new Vec3(fullSize.getX() / 2, fullSize.getY() / 2, fullSize.getZ() / 2)); BodyCreationSettings bcs = new BodyCreationSettings()) {
            bcs.setMotionType(EMotionType.Kinematic);
            bcs.setObjectLayer(selectiveGhostLayer);
            bcs.setAllowSleeping(false);
            return factory.create(shapeSettings, bcs);
        }
    }

    @Override
    public void onPhysicsTick(VxPhysicsWorld world) {
        super.onPhysicsTick(world);

        VRPose pose = PlayerDataStore.vrPoses.get(get(DATA_PLAYER_ID));
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

    @Override
    public void onBodyAdded(ClientLevel level) {
        addBodyIndexToClientStorage();
    }

    private void addBodyIndexToClientStorage() {
        Integer index = VxClientBodyManager.getInstance().getStore().getIndexForNetworkId(getNetworkId());

        if (index == null)
            return;

        isIndexSaved = true;
        ClientDataStore.playerControlledBodies.add(index);
    }

    @Override
    public void onBodyRemoved(ClientLevel level) {
        Integer index = VxClientBodyManager.getInstance().getStore().getIndexForNetworkId(getNetworkId());

        if (index == null)
            return;

        ClientDataStore.playerControlledBodies.remove(index);
    }

    @Override
    public void onClientTick() {
        if (!isIndexSaved)
            addBodyIndexToClientStorage();
    }

    @Override
    public void writePersistenceData(VxByteBuf buf) {
        super.writePersistenceData(buf);
        VxDataSerializers.VEC3.write(buf, get(DATA_HALF_EXTENTS));
        DataSerializers.BODY_PART.write(buf, get(DATA_BODY_PART));
        VxDataSerializers.UUID.write(buf, get(DATA_PLAYER_ID));
    }

    @Override
    public void readPersistenceData(VxByteBuf buf) {
        super.readPersistenceData(buf);
        setServerData(DATA_HALF_EXTENTS, VxDataSerializers.VEC3.read(buf));
        setServerData(DATA_BODY_PART, DataSerializers.BODY_PART.read(buf));
        setServerData(DATA_PLAYER_ID, VxDataSerializers.UUID.read(buf));
    }
}