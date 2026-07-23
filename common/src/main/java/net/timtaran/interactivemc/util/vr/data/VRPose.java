/*
 * This file is part of InteractiveMC.
 * Licensed under LGPL 3.0.
 */
package net.timtaran.interactivemc.util.vr.data;

import net.minecraft.world.phys.Vec3;
import net.timtaran.interactivemc.body.player.PlayerBodyPart;

/**
 * Represents the pose of a player in VR, including the data for each body part and additional information such as handedness and player scale.
 */
public interface VRPose {
    VRBodyPartData getBodyPartData(PlayerBodyPart bodyPart);

    boolean isLeftHanded();

    /**
     * Gets the scale of the player in VR, calculated using {@code playerHeightScale * worldScale}
     * @return player scale
     */
    float getPlayerScale();

    /**
     * Returns a new {@code VRPose} whose body part positions are offset relative to the given position.
     *
     * @param position the reference position
     * @return a new {@code VRPose} with positions relative to the reference position
     */
    VRPose relativeToPosition(Vec3 position);
}
