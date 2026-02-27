/*
 * This file is part of InteractiveMC.
 * Licensed under LGPL 3.0.
 */
package net.timtaran.interactivemc.init.registry;

import net.minecraft.world.InteractionHand;
import net.timtaran.interactivemc.bridge.vivecraft.BodyGrabModule;
import net.timtaran.interactivemc.bridge.vivecraft.PlayerBodyTracker;
import org.vivecraft.client_vr.ClientDataHolderVR;

/**
 * Registry for ViveCraft related things.
 *
 * @author timtaran
 */
public class ViveRegistry {
    private static final PlayerBodyTracker PLAYER_BODY_TRACKER = new PlayerBodyTracker();
    private static final BodyGrabModule MAIN_HAND_GRAB_MODULE = new BodyGrabModule(InteractionHand.MAIN_HAND);
    private static final BodyGrabModule OFF_HAND_GRAB_MODULE = new BodyGrabModule(InteractionHand.OFF_HAND);

    /**
     * Initializes and registers ViveCraft VR trackers.
     */
    public static void init() {
        ClientDataHolderVR.getInstance().registerTracker(PLAYER_BODY_TRACKER);
        ClientDataHolderVR.getInstance().interactTracker.registerModules(
                MAIN_HAND_GRAB_MODULE, OFF_HAND_GRAB_MODULE
        );
    }
}
