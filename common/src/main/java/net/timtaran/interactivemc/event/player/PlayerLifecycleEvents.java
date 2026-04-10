package net.timtaran.interactivemc.event.player;

import dev.architectury.event.events.common.PlayerEvent;
import net.minecraft.world.entity.player.Player;
import net.timtaran.interactivemc.body.player.PlayerBodyManager;
import net.xmx.velthoric.core.physics.world.VxPhysicsWorld;

/**
 * Handles player lifecycle events such as joining, respawning, and leaving the game.
 * <p>
 * This class listens to player lifecycle events and manages the creation and removal
 * of player bodies in the physics world accordingly.
 *
 * @author timtaran
 */
public class PlayerLifecycleEvents {
    private PlayerLifecycleEvents() {
    }

    /**
     * Initializes and registers all player lifecycle event listeners.
     */
    public static void init() {
        // todo handle dimension traveling
        PlayerEvent.PLAYER_JOIN.register(PlayerLifecycleEvents::spawnPlayer);

        PlayerEvent.PLAYER_RESPAWN.register(
                (player, _conqueredEnd, _removalReason) ->
                        spawnPlayer(player));

        PlayerEvent.PLAYER_QUIT.register(PlayerLifecycleEvents::removePlayer);
    }

    /**
     * Spawns player bodies when a player joins or respawns.
     *
     * @param player the player that joined or respawned
     */
    public static void spawnPlayer(Player player) {
        VxPhysicsWorld physicsWorld = VxPhysicsWorld.get(player.level().dimension());
        physicsWorld.execute(() ->
                PlayerBodyManager.get(physicsWorld).spawnPlayer(player));
    }

    /**
     * Removes player bodies when a player leaves the game.
     *
     * @param player the player that left
     */
    public static void removePlayer(Player player) {
        VxPhysicsWorld physicsWorld = VxPhysicsWorld.get(player.level().dimension());
        physicsWorld.execute(() ->
                PlayerBodyManager.get(physicsWorld).removePlayer(player));
    }
}
