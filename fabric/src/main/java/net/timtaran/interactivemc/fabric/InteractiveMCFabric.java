package net.timtaran.interactivemc.fabric;

import dev.architectury.platform.Platform;
import dev.architectury.utils.Env;
import net.fabricmc.api.ModInitializer;

import net.timtaran.interactivemc.init.InteractiveMC;

public final class InteractiveMCFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        InteractiveMC.onInit();
        if (Platform.getEnvironment() == Env.CLIENT) {
            InteractiveMC.onClientInit();
        }
    }
}
