package net.timtaran.interactivemc.neoforge;

import net.neoforged.fml.common.Mod;

import net.timtaran.interactivemc.ExampleMod;

@Mod(ExampleMod.MOD_ID)
public final class ExampleModNeoForge {
    public ExampleModNeoForge() {
        // Run our common setup.
        ExampleMod.init();
    }
}
