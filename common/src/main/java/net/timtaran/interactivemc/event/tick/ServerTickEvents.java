package net.timtaran.interactivemc.event.tick;

import dev.architectury.event.events.common.TickEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.timtaran.interactivemc.body.player.PlayerBodyManager;
import net.xmx.velthoric.core.physics.world.VxPhysicsWorld;

public class ServerTickEvents {
    private ServerTickEvents() {
    }

    /**
     * Initializes and registers all server tick event listeners.
     */
    public static void init() {
        TickEvent.Server.SERVER_LEVEL_POST.register(ServerTickEvents::bodyPullHandler);
    }

    /**
     * Handles hand pull to pull not attached grabbed body.
     *
     * @param level the server level
     */
    private static void bodyPullHandler(ServerLevel level) {
        VxPhysicsWorld physicsWorld = VxPhysicsWorld.get(level.dimension());
        if (physicsWorld == null) return;

        physicsWorld.execute(() -> {
            for (ServerPlayer player : level.players()) {
                for (InteractionHand interactionHand : InteractionHand.values()) {
                    PlayerBodyManager.get(physicsWorld).pull(player, interactionHand);
                }
            }
        });
    }
}
