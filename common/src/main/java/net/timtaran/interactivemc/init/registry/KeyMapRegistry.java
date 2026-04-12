/*
 * This file is part of InteractiveMC.
 * Licensed under LGPL 3.0.
 */
package net.timtaran.interactivemc.init.registry;

import net.minecraft.client.KeyMapping;
import net.timtaran.interactivemc.mixin.bridge.vivecraft.KeyMappingHandlingMixin;
import net.timtaran.interactivemc.mixin.bridge.vivecraft.KeyMappingRegisterMixin;
import net.timtaran.interactivemc.util.InteractiveMCIdentifier;

/**
 * Class containing all mod keymappings.
 * Registration happens inside {@link KeyMappingRegisterMixin}.
 * Handling happens inside {@link KeyMappingHandlingMixin}.
 *
 * @author timtaran
 */
public class KeyMapRegistry {
    // todo intercept vivecraft grip and trigger binds (vivecraft handles internal keymappings before everything and we don't want to mappings like hotbar change or breaking blocks to be called while interacting)
    // this could be done at MCVR#processBindings or VRInputAction#pressBinding or other classes
    // (or just consumeClick before vivecraft could process it)

    /**
     * Main-Hand Trigger
     */
    public static final KeyMapping MAIN_TRIGGER_KEYMAPPING = new KeyMapping(
            InteractiveMCIdentifier.getTranslationKey("key", "mtrigger"),
            -1,
            KeyMapping.CATEGORY_GAMEPLAY
    );

    public static final KeyMapping MAIN_TRIGGER_TOUCH_KEYMAPPING = new KeyMapping(
            InteractiveMCIdentifier.getTranslationKey("key", "mtouch"),
            -1,
            KeyMapping.CATEGORY_GAMEPLAY
    );

    /**
     * Main-Hand Grab
     */
    public static final KeyMapping MAIN_GRAB_KEYMAPPING = new KeyMapping(
            InteractiveMCIdentifier.getTranslationKey("key", "mgrab"),
            -1,
            KeyMapping.CATEGORY_GAMEPLAY
    );

    /**
     * Off-Hand Trigger
     */
    public static final KeyMapping OFF_TRIGGER_KEYMAPPING = new KeyMapping(
            InteractiveMCIdentifier.getTranslationKey("key", "otrigger"),
            -1,
            KeyMapping.CATEGORY_GAMEPLAY
    );

    public static final KeyMapping OFF_TRIGGER_TOUCH_KEYMAPPING = new KeyMapping(
            InteractiveMCIdentifier.getTranslationKey("key", "otouch"),
            -1,
            KeyMapping.CATEGORY_GAMEPLAY
    );

    /**
     * Off-Hand Grab
     */
    public static final KeyMapping OFF_GRAB_KEYMAPPING = new KeyMapping(
            InteractiveMCIdentifier.getTranslationKey("key", "ograb"),
            -1,
            KeyMapping.CATEGORY_GAMEPLAY
    );
}