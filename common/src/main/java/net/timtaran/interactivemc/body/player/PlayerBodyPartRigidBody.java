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
import net.timtaran.interactivemc.body.Grabber;
import net.timtaran.interactivemc.body.GroupFilters;
import net.timtaran.interactivemc.data.ClientDataStore;
import net.timtaran.interactivemc.network.sync.DataSerializers;
import net.xmx.velthoric.core.body.client.VxClientBodyManager;
import net.xmx.velthoric.core.network.synchronization.VxDataSerializers;
import net.xmx.velthoric.core.network.synchronization.VxSynchronizedData;
import net.xmx.velthoric.core.network.synchronization.accessor.VxServerAccessor;
import net.xmx.velthoric.core.physics.VxPhysicsLayers;
import net.xmx.velthoric.network.VxByteBuf;
import net.xmx.velthoric.core.body.registry.VxBodyType;
import net.xmx.velthoric.core.body.type.VxRigidBody;
import net.xmx.velthoric.core.body.type.factory.VxRigidBodyFactory;
import net.xmx.velthoric.core.physics.world.VxPhysicsWorld;

import java.util.UUID;

public class PlayerBodyPartRigidBody extends VxRigidBody implements Grabber {
    private boolean isIndexSaved = false;

    public static final VxServerAccessor<Vec3> DATA_HALF_EXTENTS = VxServerAccessor.create(PlayerBodyPartRigidBody.class, VxDataSerializers.VEC3);
    public static final VxServerAccessor<PlayerBodyPart> DATA_BODY_PART = VxServerAccessor.create(PlayerBodyPartRigidBody.class, DataSerializers.BODY_PART);
    public static final VxServerAccessor<UUID> DATA_PLAYER_ID = VxServerAccessor.create(PlayerBodyPartRigidBody.class, VxDataSerializers.UUID);

    /**
     * Server-side constructor.
     */
    public PlayerBodyPartRigidBody(VxBodyType<PlayerBodyPartRigidBody> type, VxPhysicsWorld world, UUID id) {
        super(type, world, id);
        setPersistent(false);
    }

    /**
     * Client-side constructor.
     */
    @Environment(EnvType.CLIENT)
    public PlayerBodyPartRigidBody(VxBodyType<PlayerBodyPartRigidBody> type, UUID id) {
        super(type, id);
    }

    @Override
    protected void defineSyncData(VxSynchronizedData.Builder builder) {
        builder.define(DATA_HALF_EXTENTS, new Vec3(0.25f, 0.25f, 0.25f));
        builder.define(DATA_BODY_PART, PlayerBodyPart.HEAD);
        builder.define(DATA_PLAYER_ID, UUID.randomUUID());
    }

    @Override
    public int createJoltBody(VxRigidBodyFactory factory) {
        PlayerBodyPart partType = get(DATA_BODY_PART);
        Vec3 fullSize = partType.getSize();

        try (ShapeSettings shapeSettings = new BoxShapeSettings(new Vec3(fullSize.getX() / 2, fullSize.getY() / 2, fullSize.getZ() / 2)); BodyCreationSettings bcs = new BodyCreationSettings()) {
            bcs.setMotionType(EMotionType.Dynamic);
            bcs.setObjectLayer(VxPhysicsLayers.MOVING);
            bcs.setCollisionGroup(new CollisionGroup(GroupFilters.PLAYER_BODY_FILTER, 0, getSubGroupId()));
            return factory.create(shapeSettings, bcs);
        }
    }

    public void onPhysicsTick(VxPhysicsWorld world) {
        super.onPhysicsTick(world);
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
    private void addBodyIndexToClientStorage() {
        Integer index = VxClientBodyManager.getInstance().getStore().getIndexForNetworkId(getNetworkId());

        if (index == null)
            return;

        ClientDataStore.playerControlledBodies.add(index);
    }

    @Override
    public void onBodyRemoved(ClientLevel level) {
        Integer index = VxClientBodyManager.getInstance().getStore().getIndexForNetworkId(getNetworkId());

        if (index == null)
            return;

        isIndexSaved = true;
        ClientDataStore.playerControlledBodies.remove(index);
    }

    @Override
    public void onClientTick() {
        if (!isIndexSaved)
            addBodyIndexToClientStorage();
    }

    @Override
    public int getGroupId() {
        return GroupFilters.PLAYER_BODY_GROUP;
    }

    @Override
    public int getSubGroupId() {
        return get(DATA_BODY_PART).getSubGroupId();
    }
}