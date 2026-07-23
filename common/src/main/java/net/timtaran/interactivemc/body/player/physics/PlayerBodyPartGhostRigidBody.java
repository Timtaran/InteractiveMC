/*
 * This file is part of InteractiveMC.
 * Licensed under LGPL 3.0.
 */
package net.timtaran.interactivemc.body.player.physics;

import com.github.stephengold.joltjni.*;
import com.github.stephengold.joltjni.enumerate.EAxis;
import com.github.stephengold.joltjni.enumerate.EMotionType;
import com.github.stephengold.joltjni.enumerate.EMotorState;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.timtaran.interactivemc.body.player.PlayerBodyPart;
import net.timtaran.interactivemc.body.player.data.PlayerData;
import net.timtaran.interactivemc.body.player.store.ClientPlayerBodyDataStore;
import net.timtaran.interactivemc.body.player.store.PlayerBodyDataStore;
import net.timtaran.interactivemc.init.registry.PhysicsLayerRegistry;
import net.timtaran.interactivemc.network.sync.DataSerializers;
import net.timtaran.interactivemc.util.PlayerBodyPartTransforms;
import net.timtaran.interactivemc.util.vr.data.VRPose;
import net.xmx.velthoric.core.body.VxBody;
import net.xmx.velthoric.core.body.VxBodyType;
import net.xmx.velthoric.core.body.client.VxClientBodyManager;
import net.xmx.velthoric.core.body.factory.VxRigidBodyFactory;
import net.xmx.velthoric.core.body.shape.VxBoxShape;
import net.xmx.velthoric.core.constraint.VxConstraint;
import net.xmx.velthoric.core.network.synchronization.VxDataSerializers;
import net.xmx.velthoric.core.network.synchronization.VxSynchronizedData;
import net.xmx.velthoric.core.network.synchronization.accessor.VxServerAccessor;
import net.xmx.velthoric.core.physics.VxJoltBridge;
import net.xmx.velthoric.core.physics.world.VxPhysicsWorld;
import net.xmx.velthoric.math.VxConversions;
import org.joml.Quaternionf;
import net.timtaran.interactivemc.util.vr.data.VRBodyPartData;

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
    private static final float FIXED_TIME_STEP = VxPhysicsWorld.getFixedTimeStep();
    private boolean isIndexSaved = false;

    private VxConstraint linkedConstraint = null;
    private boolean isConstraintConfigured = false;

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

    public void setLinkedConstraint(VxConstraint linkedConstraint) {
        this.linkedConstraint = linkedConstraint;
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
        Vec3 halfExtents = body.get(DATA_HALF_EXTENTS);

        VxBoxShape shape = new VxBoxShape(halfExtents);
        try (BodyCreationSettings bcs = new BodyCreationSettings()) {
            bcs.setMotionType(EMotionType.Kinematic);
            bcs.setObjectLayer(PhysicsLayerRegistry.getGhostLayer());
            bcs.setAllowSleeping(false);
            return factory.create(shape, bcs);
        }
    }

    public static void defineSyncData(VxSynchronizedData.Builder builder) {
        builder.define(DATA_HALF_EXTENTS, new Vec3(0.25f, 0.25f, 0.25f));
        builder.define(DATA_BODY_PART, PlayerBodyPart.HEAD);
        builder.define(DATA_PLAYER_ID, UUID.randomUUID());
    }

    @Override
    public void onPrePhysicsTick(VxPhysicsWorld world) {
        PlayerData playerData = PlayerBodyDataStore.playerData.get(get(DATA_PLAYER_ID));
        VRPose pose = playerData.getCurrentVrPose();
        // todo: predict pose on loss and use servertick data
        if (pose == null) return;

        PlayerBodyPart bodyPart = get(DATA_BODY_PART);

        VRBodyPartData bodyPartData = pose.getBodyPartData(bodyPart);
        if (bodyPartData == null) return;

        Quat rotation = VxConversions.toJolt(new Quaternionf(bodyPartData.getRotation()));
        RVec3 position = PlayerBodyPartTransforms.getTrackingOffsetWorld(VxConversions.toJolt(bodyPartData.getPos()), rotation, bodyPart, playerData.getPlayerScale());

        VxJoltBridge.INSTANCE.getJoltBody(world, this).moveKinematic(
                position,
                rotation,
                FIXED_TIME_STEP
        );
    }

    @Override
    public void onPhysicsTick(VxPhysicsWorld world) {
        if (!isConstraintConfigured) {
            configureConstraint();
        }
    }

    private void configureConstraint() {
        if (linkedConstraint == null) return;
        SixDofConstraint constraint = (SixDofConstraint) linkedConstraint.getJoltConstraint();
        if (constraint == null) return;

        constraint.setMotorState(EAxis.TranslationX, EMotorState.Position);
        constraint.setMotorState(EAxis.TranslationY, EMotorState.Position);
        constraint.setMotorState(EAxis.TranslationZ, EMotorState.Position);
        constraint.setTargetPositionCs(new Vec3(0f, 0f, 0f));

        constraint.setMotorState(EAxis.RotationX, EMotorState.Position);
        constraint.setMotorState(EAxis.RotationY, EMotorState.Position);
        constraint.setMotorState(EAxis.RotationZ, EMotorState.Position);

        constraint.setTargetOrientationCs(Quat.sIdentity());

        isConstraintConfigured = true;
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
}