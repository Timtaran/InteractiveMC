/*
 * This file is part of InteractiveMC.
 * Licensed under LGPL 3.0.
 */
package net.timtaran.interactivemc.util.vivecraft;

import org.vivecraft.api.data.VRPose;

/**
 * Represents the VR data of a player in the Vivecraft mod.
 * <p>
 * This record contains the scaling factors and current VR pose of a player.
 *
 * @param worldScale the scaling factor for the world relative to the player
 * @param heightScale the scaling factor for the player's height
 * @param vrPose the current VR pose data
 * @author timtaran
 */
public record VRPlayerData(
        float worldScale,
        float heightScale,
        VRPose vrPose
) {
}
