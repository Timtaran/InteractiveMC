/*
 * This file is part of InteractiveMC.
 * Licensed under LGPL 3.0.
 */
package net.timtaran.interactivemc.bridge.vr;

import net.minecraft.world.entity.player.Player;
import net.timtaran.interactivemc.util.vr.VRPoseHistory;
import net.timtaran.interactivemc.util.vr.data.VRPose;
import org.jetbrains.annotations.Nullable;

public interface VRPlayerDataProvider {
    /**
     * Retrieves the VR player data for the given player.
     *
     * @param player the player to retrieve the VR player data for
     * @return the VR player data, or {@code null} if the player is not a VR player
     */
    @Nullable VRPose getVrPose(Player player);

    /**
     * Checks if the given player is a VR player.
     *
     * @param player the player to check
     * @return {@code true} if the player is a VR player, {@code false} otherwise
     */
    boolean isVRPlayer(Player player);

    VRPoseHistory getPoseHistory(Player player);
}
