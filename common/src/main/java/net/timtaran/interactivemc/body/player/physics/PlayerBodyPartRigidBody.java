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
import net.timtaran.interactivemc.network.sync.DataSerializers;
import net.xmx.velthoric.core.body.VxBody;
import net.xmx.velthoric.core.body.VxBodyType;
import net.xmx.velthoric.core.body.client.VxClientBodyManager;
import net.xmx.velthoric.core.body.factory.VxRigidBodyFactory;
import net.xmx.velthoric.core.network.synchronization.VxDataSerializers;
import net.xmx.velthoric.core.network.synchronization.VxSynchronizedData;
import net.xmx.velthoric.core.network.synchronization.accessor.VxServerAccessor;
import net.xmx.velthoric.core.physics.VxPhysicsLayers;
import net.xmx.velthoric.core.physics.world.VxPhysicsWorld;
import net.xmx.velthoric.network.VxByteBuf;

import java.util.UUID;

/**
 * A dynamic rigid body representing a player's body part (head, hands, etc.).
 *
 * @author timtaran
 */
public class PlayerBodyPartRigidBody extends VxBody {
    private boolean isIndexSaved = false;

    /**
     * The half-extents (dimensions) of this body part.
     */
    public static final VxServerAccessor<Vec3> DATA_HALF_EXTENTS = VxServerAccessor.create(PlayerBodyPartRigidBody.class, VxDataSerializers.VEC3);
    /**
     * The type of body part (head, hands, etc.).
     */
    public static final VxServerAccessor<PlayerBodyPart> DATA_BODY_PART = VxServerAccessor.create(PlayerBodyPartRigidBody.class, DataSerializers.BODY_PART);
    /**
     * The UUID of the player who owns this body part.
     */
    public static final VxServerAccessor<UUID> DATA_PLAYER_ID = VxServerAccessor.create(PlayerBodyPartRigidBody.class, VxDataSerializers.UUID);

    /**
     * Server-side constructor.
     *
     * @param type  the body type
     * @param world the physics world
     * @param id    the unique identifier for this body
     */
    public PlayerBodyPartRigidBody(VxBodyType type, VxPhysicsWorld world, UUID id) {
        super(type, world, id);
    }

    /**
     * Client-side constructor.
     *
     * @param type the body type
     * @param id   the unique identifier for this body
     */
    @Environment(EnvType.CLIENT)
    public PlayerBodyPartRigidBody(VxBodyType type, UUID id) {
        super(type, id);
    }

    @Override
    protected void defineSyncData(VxSynchronizedData.Builder builder) {
        builder.define(DATA_HALF_EXTENTS, new Vec3(0.25f, 0.25f, 0.25f));
        builder.define(DATA_BODY_PART, PlayerBodyPart.HEAD);
        builder.define(DATA_PLAYER_ID, UUID.randomUUID());
    }

    public static int createJoltBody(VxBody body, VxRigidBodyFactory factory) {
        PlayerBodyPart partType = body.get(DATA_BODY_PART);
        Vec3 fullSize = partType.getSize();

        try (ShapeSettings shapeSettings = new BoxShapeSettings(new Vec3(fullSize.getX() / 2, fullSize.getY() / 2, fullSize.getZ() / 2)); BodyCreationSettings bcs = new BodyCreationSettings()) {
            bcs.setMotionType(EMotionType.Dynamic);
            bcs.setObjectLayer(VxPhysicsLayers.MOVING);
            // bcs.setCollisionGroup(new CollisionGroup(GroupFilters.PLAYER_BODY_FILTER, 0, getSubGroupId()));
            return factory.create(shapeSettings, bcs);
        }
    }

    /**
     * Called during each physics tick.
     * Override this method in subclasses if additional physics processing is needed.
     *
     * @param world the physics world
     */
    public void onPhysicsTick(VxPhysicsWorld world) {
        super.onPhysicsTick(world);
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

    /**
     * Adds this body's index to the client-side storage for tracking.
     */
    private void addBodyIndexToClientStorage() {
        Integer index = VxClientBodyManager.getInstance().getStore().getIndexForNetworkId(getNetworkId());

        if (index == null)
            return;

        ClientPlayerBodyDataStore.playerControlledBodies.add(index);
    }

    @Override
    public void onBodyRemoved(ClientLevel level) {
        Integer index = VxClientBodyManager.getInstance().getStore().getIndexForNetworkId(getNetworkId());

        if (index == null)
            return;

        isIndexSaved = true;
        ClientPlayerBodyDataStore.playerControlledBodies.remove(index);
    }

    @Override
    public void onClientTick() {
        if (!isIndexSaved)
            addBodyIndexToClientStorage();
    }
}