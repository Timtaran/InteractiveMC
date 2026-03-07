/*
 * This file is part of InteractiveMC.
 * Licensed under LGPL 3.0.
 */
package net.timtaran.interactivemc.data;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.minecraft.world.InteractionHand;
import org.vivecraft.api.data.VRPose;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Client-side data store containing VR-related information.
 * <p>
 * This class stores data that is specific to the client side, such as grabbed bodies
 * and the current VR pose information.
 *
 * @author timtaran
 */
public final class ClientDataStore {
    /**
     * Tracks the UUIDs of bodies currently grabbed in each hand.
     * Maps from {@link InteractionHand} to the UUID of the grabbed body.
     */
    public static EnumMap<InteractionHand, UUID> grabbedBodies = new EnumMap<>(Map.of(
            InteractionHand.MAIN_HAND, UUID.randomUUID(),
            InteractionHand.OFF_HAND, UUID.randomUUID()
    ));

    /**
     * List containing indices of all bodies controlled by the player.
     */
    public static List<Integer> playerControlledBodies = new IntArrayList();

    /**
     * The current VR pose of the player, updated every frame.
     */
    public static VRPose currentPose;

    private ClientDataStore() {}
}
