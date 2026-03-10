/*
 * This file is part of InteractiveMC.
 * Licensed under LGPL 3.0.
 */
package net.timtaran.interactivemc.body.player.store;

import net.timtaran.interactivemc.body.player.PlayerBodyPart;
import net.timtaran.interactivemc.body.player.PlayerBodyPartData;
import org.vivecraft.api.data.VRPose;

import java.util.*;

/**
 * Global storage for player-related physics body data and VR pose information.
 *
 * <p>
 * This class maintains static collections that associate a player's {@link UUID}
 * with different pieces of runtime data used by the physics and VR systems.
 *
 * <ul>
 *     <li>Mappings between players and their physics body parts.</li>
 *     <li>Lists of Jolt physics body IDs belonging to each player.</li>
 *     <li>The latest VR pose state for each player.</li>
 * </ul>
 *
 * @author timtaran
 */
public class PlayerBodyDataStore {
    /**
     * Contains all bodies associated with each player, indexed by their UUID.
     */
    public static final HashMap<UUID, EnumMap<PlayerBodyPart, PlayerBodyPartData>> playersBodies = new HashMap<>();

    /**
     * Contains the Jolt body IDs of all player bodies for quick lookup during interactions.
     */
    public static final HashMap<UUID, List<Integer>> playersJoltBodies = new HashMap<>();

    /**
     * The VR poses of the players by UUID.
     */
    public static Map<UUID, VRPose> vrPoses = new HashMap<>();
}
