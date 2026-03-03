/*
 * This file is part of InteractiveMC.
 * Licensed under LGPL 3.0.
 */
package net.timtaran.interactivemc.init.registry;

import net.minecraft.client.KeyMapping;
import net.timtaran.interactivemc.util.InteractiveMCIdentifier;

/**
 * Class containing all mod keymappings.
 * Registration happens inside {@link net.timtaran.interactivemc.mixin.compat.vivecraft.KeyMappingRegisterMixin}.
 * Handling happens inside {@link net.timtaran.interactivemc.mixin.compat.vivecraft.KeyMappingHandlingMixin}.
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

    /**
     * Off-Hand Grab
     */
    public static final KeyMapping OFF_GRAB_KEYMAPPING = new KeyMapping(
            InteractiveMCIdentifier.getTranslationKey("key", "ograb"),
            -1,
            KeyMapping.CATEGORY_GAMEPLAY
    );

//    /**
//     * Fake keymapping that is being return to make Vivecraft think some key is not pressed
//     */
//    public static final KeyMapping FAKE_KEYMAPPING = new KeyMapping(
//            InteractiveMCIdentifier.getTranslationKey("key", "fakemapping"),
//            -1,
//            KeyMapping.CATEGORY_MISC
//    );

    /**
     * Registers the key mappings. Should be called during the client initialization phase.
     */
    public static void init() {
//        KeyMappingRegistry.register(MAIN_TRIGGER_KEYMAPPING);
//        KeyMappingRegistry.register(MAIN_GRAB_KEYMAPPING);
//        KeyMappingRegistry.register(OFF_GRAB_KEYMAPPING);
//        KeyMappingRegistry.register(OFF_TRIGGER_KEYMAPPING);
//
//        ClientTickEvent.CLIENT_POST.register(KeyMapRegistry::handleInputs);
    }
}