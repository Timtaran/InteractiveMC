/*
 * This file is part of InteractiveMC.
 * Licensed under LGPL 3.0.
 */
package net.timtaran.interactivemc.bridge.vr.vivecraft;

import net.timtaran.interactivemc.bridge.MixinCompatPlugin;

public class MixinVivecraftPlugin extends MixinCompatPlugin {
    @Override
    public String getModId() {
        return "vivecraft";
    }
}
