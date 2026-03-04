/*
 * This file is part of InteractiveMC.
 * Licensed under LGPL 3.0.
 */
package net.timtaran.interactivemc.body.player;

import com.github.stephengold.joltjni.RVec3;
import com.github.stephengold.joltjni.Vec3;
import net.minecraft.world.InteractionHand;
import org.joml.Vector3f;
import org.vivecraft.api.data.VRBodyPart;

public enum PlayerBodyPart {
    HEAD(2, new Vec3(0.5f, 0.5f, 0.5f)),

    MAIN_HAND(0, new Vec3(0.25f, 0.25f, 0.75f)),
    OFF_HAND(1, new Vec3(0.25f, 0.25f, 0.75f));
    // todo add elbow

    private final int subGroupId;
    private final Vec3 size;

    public static final int BODY_PARTS_AMOUNT = PlayerBodyPart.values().length;

    /**
     * Defines a body part for a VR player body.
     *
     * @param size The full size (width, height, depth) of the physics shape for this part.
     */
    PlayerBodyPart(int subGroupId, Vec3 size) {
        this.subGroupId = subGroupId;
        this.size = size;
    }

    /**
     * Gets the subgroup ID of this body part.
     *
     * @return The subgroup ID.
     */
    public int getSubGroupId() {
        return subGroupId;
    }

    /**
     * Gets the full size of this body part.
     *
     * @return A vector representing the width, height, and depth.
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

    public RVec3 getLocalGrabPoint() {
        return switch (this) {
            case HEAD -> new RVec3(0f, 0f, 0f);
            case MAIN_HAND, OFF_HAND -> new RVec3(0, 0f, -0.34f);
        };
    }

    public Vector3f getLocalGrabPointVec3f() {
        return switch (this) {
            case HEAD -> new Vector3f(0f, 0f, 0f);
            case MAIN_HAND, OFF_HAND -> new Vector3f(0, 0f, -0.34f);
        };
    }

    public net.minecraft.world.phys.Vec3 getTrackingOffset() {
        return switch (this) {
            case HEAD -> new net.minecraft.world.phys.Vec3(0f, 0.035f, 0.1f);
            case MAIN_HAND, OFF_HAND -> new net.minecraft.world.phys.Vec3(0f, 0f, 0.35f);
        };
    }

    public static PlayerBodyPart fromInteractionHand(InteractionHand interactionHand) {
        return switch (interactionHand) {
            case MAIN_HAND -> MAIN_HAND;
            case OFF_HAND -> OFF_HAND;
        };
    }

    public VRBodyPart toVRBodyPart() {
        return VRBodyPart.valueOf(name());
    }
}
