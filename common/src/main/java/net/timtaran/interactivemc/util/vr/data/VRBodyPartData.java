/*
 * This file is part of InteractiveMC.
 * Licensed under LGPL 3.0.
 *
 * Original code from Vivecraft.
 */
package net.timtaran.interactivemc.util.vr.data;

import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionfc;

/**
 * Represents the data for a body part, or a device usually tied to a body part in VR, such as the HMD or a
 * controller.
 */
public interface VRBodyPartData {

    /**
     * Gets the world space position for this body part.
     *
     * @return The position of this body part in Minecraft world coordinates.
     */
    Vec3 getPos();

    /**
     * Gets the forward direction this body part is facing.
     *
     * @return The forward direction of this body part.
     */
    Vec3 getDir();

    /**
     * Gets the quaternion representing the rotation of this body part.
     *
     * @return The quaternion representing the rotation of this body part.
     */
    Quaternionfc getRotation();
}