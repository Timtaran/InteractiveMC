package net.timtaran.interactivemc.init.registry;

import net.timtaran.interactivemc.event.player.PlayerLifecycleEvents;

/**
 * Registry for mod events.
 * <p>
 * This class is responsible for registering and initializing all event listeners
 * used by the InteractiveMC mod.
 *
 * @author timtaran
 */
public class EventRegistry {
    /**
     * Registers common events that apply to both client and server.
     */
    public static void register() {
        PlayerLifecycleEvents.init();
    }

    /**
     * Registers client-side specific events.
     */
    public static void registerClient() {

    }
}
