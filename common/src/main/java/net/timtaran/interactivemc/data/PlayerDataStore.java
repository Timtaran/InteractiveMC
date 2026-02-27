/*
 * This file is part of InteractiveMC.
 * Licensed under LGPL 3.0.
 */
package net.timtaran.interactivemc.data;


import org.vivecraft.api.data.VRPose;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Stores all data that are used on both sides and equal on both sides and related to a player.
 *
 * @author timtaran
 */
public class PlayerDataStore {
    /**
     * The VR poses of the players by UUID.
     */
    public static Map<UUID, VRPose> vrPoses = new HashMap<>();
}
