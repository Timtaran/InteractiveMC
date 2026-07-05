/*
 * This file is part of InteractiveMC.
 * Licensed under LGPL 3.0.
 */
package net.timtaran.interactivemc.init.registry;

import net.timtaran.interactivemc.mixin.bridge.vivecraft.KeyMappingHandlingMixin;
import net.timtaran.interactivemc.mixin.bridge.vivecraft.KeyMappingRegisterMixin;
import net.timtaran.interactivemc.mixin.bridge.vivecraft.VRInputActionMixin;
import net.timtaran.interactivemc.util.InteractiveMCIdentifier;
import org.vivecraft.client_vr.provider.HandedKeyBinding;

/**
 * Class containing all mod keymappings.
 * Registration happens inside {@link KeyMappingRegisterMixin}.
 * Handling happens inside {@link KeyMappingHandlingMixin}.
 * Others bindings prevention happens inside {@link VRInputActionMixin}
 *
 * @author timtaran
 */
public class KeyMapRegistry {
    // We are using `HandedKeyBinding` here, because vivecraft doesn't pass `ControllerType` to default KeyMapping's.

    /**
     * Main-Hand Trigger
     */
    public static final HandedKeyBinding MAIN_TRIGGER_KEYMAPPING = new HandedKeyBinding(
            InteractiveMCIdentifier.getTranslationKey("key", "mtrigger"),
            -1,
            HandedKeyBinding.CATEGORY_GAMEPLAY
    );

    public static final HandedKeyBinding MAIN_TRIGGER_TOUCH_KEYMAPPING = new HandedKeyBinding(
            InteractiveMCIdentifier.getTranslationKey("key", "mtouch"),
            -1,
            HandedKeyBinding.CATEGORY_GAMEPLAY
    );

    /**
     * Main-Hand Grab
     */
    public static final HandedKeyBinding MAIN_GRAB_KEYMAPPING = new HandedKeyBinding(
            InteractiveMCIdentifier.getTranslationKey("key", "mgrab"),
            -1,
            HandedKeyBinding.CATEGORY_GAMEPLAY
    );

    /**
     * Off-Hand Trigger
     */
    public static final HandedKeyBinding OFF_TRIGGER_KEYMAPPING = new HandedKeyBinding(
            InteractiveMCIdentifier.getTranslationKey("key", "otrigger"),
            -1,
            HandedKeyBinding.CATEGORY_GAMEPLAY
    );

    public static final HandedKeyBinding OFF_TRIGGER_TOUCH_KEYMAPPING = new HandedKeyBinding(
            InteractiveMCIdentifier.getTranslationKey("key", "otouch"),
            -1,
            HandedKeyBinding.CATEGORY_GAMEPLAY
    );

    /**
     * Off-Hand Grab
     */
    public static final HandedKeyBinding OFF_GRAB_KEYMAPPING = new HandedKeyBinding(
            InteractiveMCIdentifier.getTranslationKey("key", "ograb"),
            -1,
            HandedKeyBinding.CATEGORY_GAMEPLAY
    );
}