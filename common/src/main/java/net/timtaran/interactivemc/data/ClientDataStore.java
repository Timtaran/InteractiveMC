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

public final class ClientDataStore {
    public static EnumMap<InteractionHand, UUID> grabbedBodies = new EnumMap<>(Map.of(
            InteractionHand.MAIN_HAND, UUID.randomUUID(),
            InteractionHand.OFF_HAND, UUID.randomUUID()
    ));

    /**
     * List containing indices of all bodies controlled by player.
     */
    public static List<Integer> playerControlledBodies = new IntArrayList();

    public static VRPose currentPose;

    private ClientDataStore() {}
}
