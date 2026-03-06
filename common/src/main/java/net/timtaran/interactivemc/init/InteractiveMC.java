/*
 * This file is part of InteractiveMC.
 * Licensed under LGPL 3.0.
 */
package net.timtaran.interactivemc.init;

import com.mojang.logging.LogUtils;
import dev.architectury.event.events.common.PlayerEvent;
import net.timtaran.interactivemc.init.registry.BodyRegistry;
import net.timtaran.interactivemc.body.player.PlayerBodyManager;
import net.timtaran.interactivemc.init.registry.KeyMapRegistry;
import net.timtaran.interactivemc.init.registry.ViveRegistry;
import net.timtaran.interactivemc.network.PacketRegistry;
import net.xmx.velthoric.core.physics.world.VxPhysicsWorld;
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

        BodyRegistry.register();
        PacketRegistry.registerPackets();

        // todo refactor

        PlayerEvent.PLAYER_JOIN.register(player -> {
            VxPhysicsWorld physicsWorld = VxPhysicsWorld.get(player.level().dimension());
            physicsWorld.execute(() ->
                    PlayerBodyManager.get(physicsWorld).spawnPlayer(player));
        });

        PlayerEvent.PLAYER_RESPAWN.register(
                (player, _conqueredEnd, _removalReason) -> {
                    VxPhysicsWorld physicsWorld = VxPhysicsWorld.get(player.level().dimension());
                    physicsWorld.execute(() ->
                            PlayerBodyManager.get(physicsWorld).spawnPlayer(player));
                });

        PlayerEvent.PLAYER_QUIT.register(player ->
                {
                    VxPhysicsWorld physicsWorld = VxPhysicsWorld.get(player.level().dimension());
                    physicsWorld.execute(() ->
                            PlayerBodyManager.get(physicsWorld).removePlayer(player));
                }
        );
    }

    /**
     * Called during the client-side initialization phase.
     * Initializes client-specific components.
     */
    public static void onClientInit() {
        LOGGER.info("Initializing InteractiveMC Client");

        ViveRegistry.init();
        BodyRegistry.registerClient();
        KeyMapRegistry.init();
    }
}
