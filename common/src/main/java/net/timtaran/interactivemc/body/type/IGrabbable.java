/*
 * This file is part of InteractiveMC.
 * Licensed under LGPL 3.0.
 */
package net.timtaran.interactivemc.body.type;

import com.github.stephengold.joltjni.Vec3;

/**
 * An interface for bodies that can be grabbed by other bodies.
 * <p>
 * Implementations of this interface provide specific points for grabbing,
 * defining where and how the body will be held when interacted with.
 *
 * @author timtaran
 */
public interface IGrabbable {
    /**
     * Gets the main grab point position in local space.
     * <p>
     * This defines where on the body a grabber will grab it. When pulled, the grabber's
     * hand will align with this point.
     * </p>
     *
     * @return the local position of the grab point
     */
    Vec3 getGrabPointPosition();

    /**
     * Gets the grab point rotation reference in local space.
     * <p>
     * This defines how the body will be rotated relative to the grabber's hand.
     * It provides the rotation axis/reference for the grabbed body.
     * </p>
     *
     * @return the local rotation reference of the grab point
     */
    Vec3 getGrabPointRotation();
}