package net.timtaran.interactivemc.fabric;

import dev.architectury.platform.Platform;
import dev.architectury.utils.Env;
import net.fabricmc.api.ModInitializer;

import net.timtaran.interactivemc.init.InteractiveMC;

/**
 * Fabric mod entry point for InteractiveMC.
 * <p>
 * This class initializes the mod during the Fabric mod loading process.
 * </p>
 *
 * @author timtaran
 */
public final class InteractiveMCFabric implements ModInitializer {
    /**
     * Called during mod initialization.
     */
    @Override
    public void onInitialize() {
        InteractiveMC.onInit();
        if (Platform.getEnvironment() == Env.CLIENT) {
            InteractiveMC.onClientInit();
        }
    }
}
