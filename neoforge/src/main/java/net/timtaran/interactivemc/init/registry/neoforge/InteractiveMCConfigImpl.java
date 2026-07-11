package net.timtaran.interactivemc.init.registry.neoforge;

import net.neoforged.fml.loading.FMLPaths;

import java.nio.file.Path;

public class InteractiveMCConfigImpl {
    public static Path getConfigDirectory() {
        return FMLPaths.CONFIGDIR.get();
    }
}
