/*
 * This file is part of InteractiveMC.
 * Licensed under LGPL 3.0.
 */
package net.timtaran.interactivemc.bridge.vr.vivecraft;

import net.minecraft.client.KeyMapping;
import net.timtaran.interactivemc.mixin.bridge.vr.vivecraft.KeyMappingHandlingMixin;
import net.timtaran.interactivemc.mixin.bridge.vr.vivecraft.KeyMappingRegisterMixin;
import net.timtaran.interactivemc.mixin.bridge.vr.vivecraft.VRInputActionMixin;
import net.timtaran.interactivemc.util.InteractiveMCIdentifier;

/**
 * Class containing all mod keymappings.
 * Registration happens inside {@link KeyMappingRegisterMixin}.
 * Handling happens inside {@link KeyMappingHandlingMixin}.
 * Others bindings prevention happens inside {@link VRInputActionMixin}
 *
 * @author timtaran
 */
public class VivecraftKeyMapRegistry {
    /**
     * Main-Hand Trigger
     */
    public static final KeyMapping MAIN_TRIGGER_KEYMAPPING = new KeyMapping(
            InteractiveMCIdentifier.getTranslationKey("key.mtrigger"),
            -1,
            KeyMapping.CATEGORY_GAMEPLAY
    );

    public static final KeyMapping MAIN_TRIGGER_TOUCH_KEYMAPPING = new KeyMapping(
            InteractiveMCIdentifier.getTranslationKey("key.mtouch"),
            -1,
            KeyMapping.CATEGORY_GAMEPLAY
    );

    /**
     * Main-Hand Grab
     */
    public static final KeyMapping MAIN_GRAB_KEYMAPPING = new KeyMapping(
            InteractiveMCIdentifier.getTranslationKey("key.mgrab"),
            -1,
            KeyMapping.CATEGORY_GAMEPLAY
    );

    /**
     * Off-Hand Trigger
     */
    public static final KeyMapping OFF_TRIGGER_KEYMAPPING = new KeyMapping(
            InteractiveMCIdentifier.getTranslationKey("key.otrigger"),
            -1,
            KeyMapping.CATEGORY_GAMEPLAY
    );

    public static final KeyMapping OFF_TRIGGER_TOUCH_KEYMAPPING = new KeyMapping(
            InteractiveMCIdentifier.getTranslationKey("key.otouch"),
            -1,
            KeyMapping.CATEGORY_GAMEPLAY
    );

    /**
     * Off-Hand Grab
     */
    public static final KeyMapping OFF_GRAB_KEYMAPPING = new KeyMapping(
            InteractiveMCIdentifier.getTranslationKey("key.ograb"),
            -1,
            KeyMapping.CATEGORY_GAMEPLAY
    );
}