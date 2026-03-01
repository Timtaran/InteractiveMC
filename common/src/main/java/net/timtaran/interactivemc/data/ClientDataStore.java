package net.timtaran.interactivemc.data;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import org.vivecraft.api.data.VRPose;

import java.util.List;

public final class ClientDataStore {
    public static boolean isGrabbing = false;

    /**
     * List containing indices of all bodies controlled by player.
     */
    public static List<Integer> playerControlledBodies = new IntArrayList();

    public static VRPose currentPose;

    private ClientDataStore() {}
}
