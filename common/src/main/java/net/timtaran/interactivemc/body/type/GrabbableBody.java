/*
 * This file is part of InteractiveMC.
 * Licensed under LGPL 3.0.
 */
package net.timtaran.interactivemc.body.type;

import com.github.stephengold.joltjni.Vec3;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.xmx.velthoric.core.network.synchronization.VxDataSerializers;
import net.xmx.velthoric.core.network.synchronization.accessor.VxServerAccessor;
import net.xmx.velthoric.network.VxByteBuf;
import net.xmx.velthoric.core.body.registry.VxBodyType;
import net.xmx.velthoric.core.body.type.VxRigidBody;
import net.xmx.velthoric.core.physics.world.VxPhysicsWorld;

import java.util.UUID;

/**
 * An abstract base class for bodies that can be grabbed by other bodies.
 * <p>
 * Unlike a regular rigid body, this body allows you to specify a custom grab point
 * and grab rotation point, determining exactly where and how the body will be grabbed
 * when another entity interacts with it.
 *
 * @author timtaran
 */
public abstract class GrabbableBody extends VxRigidBody {
    /**
     * Server-side accessor for the main grab point position in local space.
     * <p>
     * This defines where on the body a grabber will grab it. When pulled, the grabber's
     * hand will align with this point.
     * </p>
     */
    public static final VxServerAccessor<Vec3> DATA_MAIN_GRAB_POINT_POSITION = VxServerAccessor.create(GrabbableBody.class, VxDataSerializers.VEC3);

    /**
     * Server-side accessor for the grab point rotation reference in local space.
     * <p>
     * This defines how the body will be rotated relative to the grabber's hand.
     * It provides the rotation axis/reference for the grabbed body.
     * </p>
     */
    public static final VxServerAccessor<Vec3> DATA_MAIN_GRAB_POINT_ROTATION = VxServerAccessor.create(GrabbableBody.class, VxDataSerializers.VEC3);

    /**
     * Server-side constructor for a grabbable rigid body.
     *
     * @param type the body type definition
     * @param world the physics world this body belongs to
     * @param id the unique UUID for this body
     */
    protected GrabbableBody(VxBodyType<? extends GrabbableBody> type, VxPhysicsWorld world, UUID id) {
        super(type, world, id);
    }

    /**
     * Client-side constructor for a grabbable rigid body.
     *
     * @param type the body type definition
     * @param id the unique UUID for this body
     */
    @Environment(EnvType.CLIENT)
    protected GrabbableBody(VxBodyType<? extends GrabbableBody> type, UUID id) {
        super(type, id);
    }

    /**
     * Writes the grab point data to the persistence buffer.
     *
     * @param buf the buffer to write to
     */
    @Override
    public void writePersistenceData(VxByteBuf buf) {
        super.writePersistenceData(buf);
        VxDataSerializers.VEC3.write(buf, get(DATA_MAIN_GRAB_POINT_POSITION));
        VxDataSerializers.VEC3.write(buf, get(DATA_MAIN_GRAB_POINT_ROTATION));
    }

    /**
     * Reads the grab point data from the persistence buffer.
     *
     * @param buf the buffer to read from
     */
    @Override
    public void readPersistenceData(VxByteBuf buf) {
        super.readPersistenceData(buf);
        setServerData(DATA_MAIN_GRAB_POINT_POSITION, VxDataSerializers.VEC3.read(buf));
        setServerData(DATA_MAIN_GRAB_POINT_ROTATION, VxDataSerializers.VEC3.read(buf));

    }
}
