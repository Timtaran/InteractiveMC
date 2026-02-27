package net.timtaran.interactivemc.neoforge;

import dev.architectury.platform.Platform;
import dev.architectury.utils.Env;
import net.neoforged.fml.common.Mod;

import net.timtaran.interactivemc.init.InteractiveMC;

@Mod(InteractiveMC.MOD_ID)
public final class InteractiveMCNeoForge {
    public InteractiveMCNeoForge() {
        InteractiveMC.onInit();
        if (Platform.getEnvironment() == Env.CLIENT) {
            InteractiveMC.onClientInit();
        }
    }
}
