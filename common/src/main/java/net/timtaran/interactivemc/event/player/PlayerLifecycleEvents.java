package net.timtaran.interactivemc.event.player;

import dev.architectury.event.events.common.PlayerEvent;
import net.minecraft.world.entity.player.Player;
import net.timtaran.interactivemc.body.player.PlayerBodyManager;
import net.xmx.velthoric.core.physics.world.VxPhysicsWorld;

public class PlayerLifecycleEvents {
    private PlayerLifecycleEvents() {
    }

    public static void init() {
        PlayerEvent.PLAYER_JOIN.register(PlayerLifecycleEvents::spawnPlayer);

        PlayerEvent.PLAYER_RESPAWN.register(
                (player, _conqueredEnd, _removalReason) ->
                        spawnPlayer(player));

        PlayerEvent.PLAYER_QUIT.register(PlayerLifecycleEvents::removePlayer);
    }

    public static void spawnPlayer(Player player) {
        VxPhysicsWorld physicsWorld = VxPhysicsWorld.get(player.level().dimension());
        physicsWorld.execute(() ->
                PlayerBodyManager.get(physicsWorld).spawnPlayer(player));
    }

    public static void removePlayer(Player player) {
        VxPhysicsWorld physicsWorld = VxPhysicsWorld.get(player.level().dimension());
        physicsWorld.execute(() ->
                PlayerBodyManager.get(physicsWorld).removePlayer(player));
    }
}
