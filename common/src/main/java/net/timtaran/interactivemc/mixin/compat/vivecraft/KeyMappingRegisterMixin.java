/*
 * This file is part of InteractiveMC.
 * Licensed under LGPL 3.0.
 */
package net.timtaran.interactivemc.mixin.compat.vivecraft;

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
 * Injects into vivecraft keybindings setup to register hidden keymappings.
 *
 * @author timtaran
 */
@Mixin(value = VivecraftVRMod.class, remap = false)
public class KeyMappingRegisterMixin {
    @Shadow
    private Set<KeyMapping> hiddenKeyBindingSet;

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
