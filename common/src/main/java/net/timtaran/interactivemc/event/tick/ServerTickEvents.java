/*
 * This file is part of InteractiveMC.
 * Licensed under LGPL 3.0.
 */
package net.timtaran.interactivemc.event.tick;

import dev.architectury.event.events.common.TickEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.timtaran.interactivemc.body.player.PlayerBodyManager;
import net.timtaran.interactivemc.body.player.store.PlayerBodyDataStore;
import net.timtaran.interactivemc.init.registry.ProviderRegistry;
import net.timtaran.interactivemc.util.vr.VRPoseHistoryImpl;
import net.timtaran.interactivemc.util.vr.data.VRPose;
import net.xmx.velthoric.core.physics.world.VxPhysicsWorld;

public class ServerTickEvents {
    private ServerTickEvents() {
    }

    /**
     * Initializes and registers all server tick event listeners.
     */
    public static void init() {
        TickEvent.Server.SERVER_LEVEL_POST.register(ServerTickEvents::vrPoseHistorian);
        TickEvent.Server.SERVER_LEVEL_POST.register(ServerTickEvents::bodyPullHandler);
        TickEvent.Server.SERVER_LEVEL_POST.register(ServerTickEvents::playerScaleHandler);
    }

    private static void vrPoseHistorian(ServerLevel level) {
        for (ServerPlayer player : level.players()) {
            VRPoseHistoryImpl poseHistory = (VRPoseHistoryImpl) ProviderRegistry.getVrPlayerDataProvider().getPoseHistory(player);

            poseHistory.addPose(ProviderRegistry.getVrPlayerDataProvider().getVrPose(player), player.position());
        }
    }

    /**
     * Checks if pull gesture triggered.
     *
     * @param level the server level
     */
    private static void bodyPullHandler(ServerLevel level) {
        VxPhysicsWorld physicsWorld = VxPhysicsWorld.get(level.dimension());
        if (physicsWorld == null) return;

        physicsWorld.execute(() -> {
            for (ServerPlayer player : level.players()) {
                for (InteractionHand interactionHand : InteractionHand.values()) {
                    PlayerBodyManager.get(physicsWorld).updatePullState(player, interactionHand);
                }
            }
        });
    }

    /**
     * Checks if player scale changed and body respawn required.
     *
     * @param level the server level
     */
    private static void playerScaleHandler(ServerLevel level) {
        for (ServerPlayer player : level.players()) {
            VRPose vrPlayerData = ProviderRegistry.getVrPlayerDataProvider().getVrPose(player);

            if (vrPlayerData == null) return;

            if (PlayerBodyDataStore.playerData.get(player.getUUID()).isScaleUpdateRequired(vrPlayerData.getPlayerScale())) {
                VxPhysicsWorld physicsWorld = VxPhysicsWorld.get(player.level().dimension());
                physicsWorld.execute(() ->
                        PlayerBodyManager.get(physicsWorld).spawnPlayer(player));
            }
        }
    }
}
