/*
 * This file is part of InteractiveMC.
 * Licensed under LGPL 3.0.
 */
package net.timtaran.interactivemc.body.player.store;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.timtaran.interactivemc.body.player.PlayerBodyPart;
import net.timtaran.interactivemc.body.player.interaction.TriggerState;
import net.timtaran.interactivemc.bridge.vr.vivecraft.PlayerBodyTracker;
import net.timtaran.interactivemc.mixin.bridge.vr.vivecraft.VRInputActionMixin;
import net.timtaran.interactivemc.mixin.bridge.vr.vivecraft.VRInputActionProcessReorderMixin;
import net.timtaran.interactivemc.util.vr.data.VRPose;

import java.util.*;

/**
 * Client-side data store containing VR-related information.
 * <p>
 * This class stores data that is specific to the client side, such as grabbed bodies
 * and the current VR pose information.
 *
 * @author timtaran
 */
public final class ClientPlayerBodyDataStore {
    /**
     * Maps from {@link PlayerBodyPart} to the UUID of the grabbed body.
     */
    public static EnumMap<PlayerBodyPart, UUID> grabbedBodies = new EnumMap<>(PlayerBodyPart.class);

    public static EnumMap<PlayerBodyPart, TriggerState> triggerStates = new EnumMap<>(Map.of(
            PlayerBodyPart.HEAD, TriggerState.RELEASE,
            PlayerBodyPart.MAIN_HAND, TriggerState.RELEASE,
            PlayerBodyPart.OFF_HAND, TriggerState.RELEASE
    ));

    /**
     * List containing indices of all bodies controlled by the player.
     * Stores the Jolt physics body IDs associated with each player.
     * <p>
     * Used for fast lookups in {@link net.timtaran.interactivemc.body.player.interaction.GrabInteraction#canGrabClient(PlayerBodyPart)}
     */
    public static List<Integer> playerControlledBodies = new IntArrayList();

    /**
     * The current VR pose of the player, updated every frame.
     * @see PlayerBodyTracker
     */
    public static VRPose currentPose;

    /**
     * Origins scheduled to be ignored by other actions sharing the same VR origin.
     * @see VRInputActionMixin
     * @see VRInputActionProcessReorderMixin
     */
    public static final Set<Long> cancelOrigins = new HashSet<>();

    public static boolean isGrabbing(PlayerBodyPart bodyPart) {
        return ClientPlayerBodyDataStore.grabbedBodies.get(bodyPart) != null;
    }

    private ClientPlayerBodyDataStore() {
    }
}
