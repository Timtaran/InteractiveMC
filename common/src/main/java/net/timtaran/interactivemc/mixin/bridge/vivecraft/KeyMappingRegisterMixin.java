/*
 * This file is part of InteractiveMC.
 * Licensed under LGPL 3.0.
 */
package net.timtaran.interactivemc.mixin.bridge.vivecraft;

import net.minecraft.client.KeyMapping;
import net.timtaran.interactivemc.init.registry.KeyMapRegistry;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.client.VivecraftVRMod;

import java.util.Set;

/**
 * Mixin that injects into Vivecraft's keybinding setup to register hidden keymappings.
 * <p>
 * This mixin registers the grab and trigger keymappings as hidden keybindings
 * so that Vivecraft doesn't interfere with them.
 *
 * @author timtaran
 * @see KeyMapRegistry
 */
@Mixin(value = VivecraftVRMod.class, remap = false)
public class KeyMappingRegisterMixin {
    /**
     * The set of hidden keybindings maintained by Vivecraft.
     */
    @Shadow
    private Set<KeyMapping> hiddenKeyBindingSet;

    /**
     * Registers InteractiveMC keymappings as hidden in Vivecraft.
     * <p>
     * This method is called after Vivecraft initializes its hidden keybinding set,
     * allowing us to add our custom grab and trigger keymappings to it.
     * </p>
     *
     * @param ci the callback info for the mixin injection
     */
    @Inject(
            method = "setupKeybindingSets",
            at = @At(value = "FIELD", target = "Lorg/vivecraft/client/VivecraftVRMod;hiddenKeyBindingSet:Ljava/util/Set;", opcode = Opcodes.PUTFIELD, shift = At.Shift.AFTER)

    )
    public void interactivemc$registerKeymappings(CallbackInfo ci) {
        hiddenKeyBindingSet.add(KeyMapRegistry.MAIN_TRIGGER_KEYMAPPING);
        hiddenKeyBindingSet.add(KeyMapRegistry.OFF_TRIGGER_KEYMAPPING);
        hiddenKeyBindingSet.add(KeyMapRegistry.MAIN_GRAB_KEYMAPPING);
        hiddenKeyBindingSet.add(KeyMapRegistry.OFF_GRAB_KEYMAPPING);
    }
}
