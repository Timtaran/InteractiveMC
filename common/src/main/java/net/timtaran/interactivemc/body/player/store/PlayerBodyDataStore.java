/*
 * This file is part of InteractiveMC.
 * Licensed under LGPL 3.0.
 */
package net.timtaran.interactivemc.body.player.store;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.timtaran.interactivemc.body.player.PlayerBodyPart;
import net.timtaran.interactivemc.body.player.PlayerBodyPartData;
import org.vivecraft.api.data.VRPose;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

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
    public static final Map<UUID, EnumMap<PlayerBodyPart, PlayerBodyPartData>> playersBodies = new HashMap<>(); // read/write in physics thread

    /**
     * Contains the Jolt body IDs of all player bodies for quick lookup during interactions.
     */
    public static final Map<UUID, Set<Integer>> playersJoltBodies = new HashMap<>(); // read/write in physics thread

    /**
     * Contains the Jolt body IDs of all grabbed player bodies for quick lookup during interactions.
     */
    public static final IntSet grabbedBodies = new IntOpenHashSet();  // read/write in physics thread

    /**
     * The VR poses of the players by UUID.
     */
    public static Map<UUID, VRPose> vrPoses = new ConcurrentHashMap<>(); // read/write in networking and physics threads, so using ConcurrentHashMap

    public static boolean isPlayerControlledBody(UUID playerId, int bodyId) {
        Set<Integer> bodies = playersJoltBodies.get(playerId);
        return bodies != null && bodies.contains(bodyId);
    }

    public static boolean isBodyGrabbed(int bodyId) {
        return grabbedBodies.contains(bodyId);
    }
}
