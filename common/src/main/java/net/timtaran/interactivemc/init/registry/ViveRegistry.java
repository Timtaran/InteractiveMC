/*
 * This file is part of InteractiveMC.
 * Licensed under LGPL 3.0.
 */
package net.timtaran.interactivemc.init.registry;

import net.timtaran.interactivemc.bridge.vivecraft.PlayerBodyTracker;
import org.vivecraft.client_vr.ClientDataHolderVR;

/**
 * Registry for Vivecraft related things.
 *
 * @author timtaran
 */
public class ViveRegistry {
    private static final PlayerBodyTracker PLAYER_BODY_TRACKER = new PlayerBodyTracker();

    /**
     * Initializes and registers Vivecraft VR trackers.
     */
    public static void registerClient() {
        ClientDataHolderVR.getInstance().registerTracker(PLAYER_BODY_TRACKER);
    }
}
