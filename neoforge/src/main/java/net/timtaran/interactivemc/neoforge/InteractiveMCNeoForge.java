package net.timtaran.interactivemc.neoforge;

import dev.architectury.platform.Platform;
import dev.architectury.utils.Env;
import net.neoforged.fml.common.Mod;

import net.timtaran.interactivemc.init.InteractiveMC;

/**
 * NeoForge mod entry point for InteractiveMC.
 * <p>
 * This class initializes the mod during the NeoForge mod loading process.
 * </p>
 *
 * @author timtaran
 */
@Mod(InteractiveMC.MOD_ID)
public final class InteractiveMCNeoForge {
    /**
     * Constructor called during NeoForge mod initialization.
     */
    public InteractiveMCNeoForge() {
        InteractiveMC.onInit();
        if (Platform.getEnvironment() == Env.CLIENT) {
            InteractiveMC.onClientInit();
        }
    }
}
