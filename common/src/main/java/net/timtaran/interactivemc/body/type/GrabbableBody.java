/*
 * This file is part of InteractiveMC.
 * Licensed under LGPL 3.0.
 */
package net.timtaran.interactivemc.body.type;

import com.github.stephengold.joltjni.CollisionGroup;
import com.github.stephengold.joltjni.Vec3;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.timtaran.interactivemc.body.Grabber;
import net.timtaran.interactivemc.util.velthoric.GroupFilters;
import net.xmx.velthoric.core.network.synchronization.VxDataSerializers;
import net.xmx.velthoric.core.network.synchronization.accessor.VxServerAccessor;
import net.xmx.velthoric.core.physics.VxJoltBridge;
import net.xmx.velthoric.network.VxByteBuf;
import net.xmx.velthoric.core.body.registry.VxBodyType;
import net.xmx.velthoric.core.body.type.VxRigidBody;
import net.xmx.velthoric.core.body.type.factory.VxRigidBodyFactory;
import net.xmx.velthoric.core.physics.world.VxPhysicsWorld;

import java.util.UUID;

/**
 * A body that can be grabbed by other bodies.
 * <p>
 * Unlike a regular rigid-body, this body allows you to select a specific area of interaction.
 *
 * @author timtaran
 */
public abstract class GrabbableBody extends VxRigidBody {
    /**
     * Server-side accessor for the grab point in local space.
     * <p>
     * When pulling body, this is where player will grab the body.
     */
    public static final VxServerAccessor<Vec3> DATA_MAIN_GRAB_POINT_POSITION = VxServerAccessor.create(GrabbableBody.class, VxDataSerializers.VEC3);

    /**
     * Server-side accessor for the body part.
     * <p>
     * When pulling body, this is how body will be rotated.
     */
    public static final VxServerAccessor<Vec3> DATA_MAIN_GRAB_POINT_ROTATION = VxServerAccessor.create(GrabbableBody.class, VxDataSerializers.VEC3);

    /**
     * Server-side constructor for a rigid body.
     *
     * @param type  The body type definition.
     * @param world The physics world this body belongs to.
     * @param id    The unique UUID for this body.
     */
    protected GrabbableBody(VxBodyType<? extends GrabbableBody> type, VxPhysicsWorld world, UUID id) {
        super(type, world, id);
    }

    /**
     * Client-side constructor for a rigid body.
     *
     * @param type The body type definition.
     * @param id The unique UUID for this body.
     */
    @Environment(EnvType.CLIENT)
    protected GrabbableBody(VxBodyType<? extends GrabbableBody> type, UUID id) {
        super(type, id);
    }

    /**
     * Defines and creates the Jolt physics body using the provided factory.
     * This method must be implemented by subclasses to define the shape and
     * properties of the rigid body.
     *
     * @param factory The factory provided by the VxBodyManager to create the body.
     * @return The body ID assigned by Jolt.
     */
    public abstract int createJoltBody(VxRigidBodyFactory factory);

    public <T extends VxRigidBody & Grabber> void grab(T grabber, Vec3 grabPoint, Vec3 grabRotation) {
        // VxJoltBridge.INSTANCE.getJoltBody(physicsWorld, this).setCollisionGroup(new CollisionGroup(GroupFilters.PLAYER_BODY_FILTER, 0, grabber.getSubGroupId()));
    }

    public void release() {
        VxJoltBridge.INSTANCE.getJoltBody(physicsWorld, this).setCollisionGroup(new CollisionGroup());
    }


    @Override
    public void writePersistenceData(VxByteBuf buf) {
        super.writePersistenceData(buf);
        VxDataSerializers.VEC3.write(buf, get(DATA_MAIN_GRAB_POINT_POSITION));
        VxDataSerializers.VEC3.write(buf, get(DATA_MAIN_GRAB_POINT_ROTATION));
    }

    @Override
    public void readPersistenceData(VxByteBuf buf) {
        super.readPersistenceData(buf);
        setServerData(DATA_MAIN_GRAB_POINT_POSITION, VxDataSerializers.VEC3.read(buf));
        setServerData(DATA_MAIN_GRAB_POINT_ROTATION, VxDataSerializers.VEC3.read(buf));

    }
}
