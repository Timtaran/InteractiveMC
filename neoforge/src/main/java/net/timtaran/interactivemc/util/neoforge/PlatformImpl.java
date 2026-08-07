package net.timtaran.interactivemc.util.neoforge;

import net.neoforged.fml.loading.LoadingModList;

public class PlatformImpl {
    public static boolean isModLoaded(String modId) {
        // This utility method is being used to check is mod present while loading mixins, so using ModList will result a crush, since it is not initialized yet.
        return LoadingModList.get().getMods().stream().anyMatch(mod -> mod.getModId().equals(modId));
    }
}
