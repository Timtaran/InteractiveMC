/*
 * This file is part of InteractiveMC.
 * Licensed under LGPL 3.0.
 */
package net.timtaran.interactivemc.body.player;

import com.github.stephengold.joltjni.RVec3;
import com.github.stephengold.joltjni.Vec3;
import net.minecraft.world.InteractionHand;
import net.timtaran.interactivemc.body.player.physics.PlayerBodyPartGhostRigidBody;
import net.timtaran.interactivemc.body.player.physics.PlayerBodyPartRigidBody;
import org.joml.Vector3f;
import org.vivecraft.api.data.VRBodyPart;

/**
 * Enumeration of player body parts that can be controlled and interacted with in VR.
 * <p>
 * This enum defines all the different body parts that make up a player's physical representation
 * in the physics world. Each body part has associated physics shapes, pivot points for joints,
 * grab points for interaction, and tracking offsets for VR controller positioning.
 *
 * @author timtaran
 * @see net.timtaran.interactivemc.body.player.PlayerBodyManager
 * @see PlayerBodyPartRigidBody
 * @see PlayerBodyPartGhostRigidBody
 */
public enum PlayerBodyPart {
    HEAD(new Vec3(0.5f, 0.5f, 0.5f)),

    MAIN_HAND(new Vec3(0.25f, 0.25f, 0.75f)),
    OFF_HAND(new Vec3(0.25f, 0.25f, 0.75f));
    // todo add elbow

    /**
     * The full size (width, height, depth) of the physics shape for this body part.
     */
    private final Vec3 size;

    /**
     * Constructs a new player body part with the specified properties.
     *
     * @param size the full size (width, height, depth) of the physics shape
     */
    PlayerBodyPart(Vec3 size) {
        this.size = size;
    }

    /**
     * Gets the full size of this body part.
     * <p>
     * The size represents the dimensions of the physics shape (width x height x depth).
     * This is the full size, not half-extents.
     * </p>
     *
     * @return a vector representing the width, height, and depth
     */
    public Vec3 getSize() {
        return size;
    }

    /**
     * Calculates the local pivot point on this body part for its joint connection.
     * This is typically at the top-center for limbs and the bottom-center for the head.
     *
     * @return A vector representing the local pivot point.
     */
    public net.minecraft.world.phys.Vec3 getLocalPivot() {
        return switch (this) {
            case HEAD -> new net.minecraft.world.phys.Vec3(0f, -0.1f, -0.1f);
            case MAIN_HAND -> new net.minecraft.world.phys.Vec3(0.5f, 0f, -0.2f);
            case OFF_HAND -> new net.minecraft.world.phys.Vec3(-0.5f, 0f, -0.2f);
        };
    }

    /**
     * Gets the local grab point on this body part in RVec3 format.
     * <p>
     * This point defines where objects are grabbed when a player tries to grab something
     * with this body part. For hands, it's at the tip.
     * </p>
     *
     * @return a vector in local space representing the grab point (RVec3 format)
     */
    public RVec3 getLocalGrabPoint() {
        return switch (this) {
            case HEAD -> new RVec3(0f, 0f, 0f);
            case MAIN_HAND, OFF_HAND -> new RVec3(0, 0f, -0.34f);
        };
    }

    /**
     * Gets the local grab point on this body part in Vector3f format.
     * <p>
     * This is the same as {@link #getLocalGrabPoint()} but in JOML Vector3f format
     * for convenience when working with JOML math operations.
     * </p>
     *
     * @return a vector in local space representing the grab point (Vector3f format)
     */
    public Vector3f getLocalGrabPointVec3f() {
        return switch (this) {
            case HEAD -> new Vector3f(0f, 0f, 0f);
            case MAIN_HAND, OFF_HAND -> new Vector3f(0, 0f, -0.34f);
        };
    }

    /**
     * Gets the tracking offset applied to VR controller data for this body part.
     * <p>
     * This offset is applied when tracking the body part's position from VR controller data.
     * It compensates for the difference between where the VR controller is held and where
     * the actual body part should be physically located.
     * </p>
     *
     * @return a vector representing the offset to apply to tracked position
     */
    public net.minecraft.world.phys.Vec3 getTrackingOffset() {
        return switch (this) {
            case HEAD -> new net.minecraft.world.phys.Vec3(0f, 0.035f, 0.1f);
            case MAIN_HAND, OFF_HAND -> new net.minecraft.world.phys.Vec3(0f, 0f, 0.35f);
        };
    }

    /**
     * Converts an {@link InteractionHand} to its corresponding {@link PlayerBodyPart}.
     * <p>
     * This is a convenience method to map the standard Minecraft hand types to
     * the appropriate player body parts. The head body part has no corresponding hand.
     * </p>
     *
     * @param interactionHand the interaction hand (main or off-hand)
     * @return the corresponding player body part
     * @throws NullPointerException if interactionHand is null
     */
    public static PlayerBodyPart fromInteractionHand(InteractionHand interactionHand) {
        return switch (interactionHand) {
            case MAIN_HAND -> MAIN_HAND;
            case OFF_HAND -> OFF_HAND;
        };
    }

    public InteractionHand toInteractionHand() {
        return switch (this) {
            case MAIN_HAND -> InteractionHand.MAIN_HAND;
            case OFF_HAND -> InteractionHand.OFF_HAND;
            default -> null;
        };
    }

    /**
     * Converts this {@link PlayerBodyPart} to its corresponding {@link VRBodyPart}.
     * <p>
     * This is used for integration with the ViveCraft API to get VR-specific data
     * for this body part, such as VR pose information.
     * </p>
     *
     * @return the corresponding VR body part from the ViveCraft API
     */
    public VRBodyPart toVRBodyPart() {
        return VRBodyPart.valueOf(name());
    }
}
