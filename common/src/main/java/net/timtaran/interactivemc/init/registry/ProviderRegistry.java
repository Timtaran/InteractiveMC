/*
 * This file is part of InteractiveMC.
 * Licensed under LGPL 3.0.
 */
package net.timtaran.interactivemc.init.registry;

import dev.architectury.platform.Platform;
import net.timtaran.interactivemc.bridge.vr.VRPlayerDataProvider;
import net.timtaran.interactivemc.bridge.vr.vivecraft.VivecraftVRPlayerDataProvider;

public class ProviderRegistry {
    private ProviderRegistry() {}

    private static VRPlayerDataProvider vrPlayerDataProvider;

    public static VRPlayerDataProvider getVrPlayerDataProvider() {
        return vrPlayerDataProvider;
    }

    public static void loadVRProviders() {
        if (Platform.isModLoaded("visor")) {
            // todo: add visor support
        }

        if (Platform.isModLoaded("vivecraft")) {
            vrPlayerDataProvider = new VivecraftVRPlayerDataProvider();
            return;
        }

        throw new IllegalStateException("No supported VR provider found");
    }
}
