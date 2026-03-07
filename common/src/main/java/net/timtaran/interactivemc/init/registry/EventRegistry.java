package net.timtaran.interactivemc.init.registry;

import net.timtaran.interactivemc.event.player.PlayerLifecycleEvents;

public class EventRegistry {
    public static void register() {
        PlayerLifecycleEvents.init();
    }

    public static void registerClient() {

    }
}
