/*
 * This file is part of InteractiveMC.
 * Licensed under LGPL 3.0.
 */
package net.timtaran.interactivemc.init;

import com.mojang.logging.LogUtils;
import net.timtaran.interactivemc.init.registry.BodyRegistry;
import net.timtaran.interactivemc.init.registry.EventRegistry;
import net.timtaran.interactivemc.init.registry.ViveRegistry;
import net.timtaran.interactivemc.network.PacketRegistry;
import org.slf4j.Logger;

/**
 * The main class for the InteractiveMC mod.
 *
 * @author timtaran
 */
public class InteractiveMC {
    public static final String MOD_ID = "interactivemc";
    public static final Logger LOGGER = LogUtils.getLogger();

    /**
     * Called during the common initialization phase (Server and Client).
     * Initializes registries, packets, and loads native libraries.
     */
    public static void onInit() {
        LOGGER.info("Initializing InteractiveMC");

        EventRegistry.register();
        BodyRegistry.register();
        PacketRegistry.registerPackets();
    }

    /**
     * Called during the client-side initialization phase.
     * Initializes client-specific components.
     */
    public static void onClientInit() {
        LOGGER.info("Initializing InteractiveMC Client");

        EventRegistry.registerClient();
        ViveRegistry.registerClient();
        BodyRegistry.registerClient();
    }
}
