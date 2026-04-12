/*
 * This file is part of InteractiveMC.
 * Licensed under LGPL 3.0.
 */
package net.timtaran.interactivemc.body.player.interaction;

import net.minecraft.world.entity.player.Player;
import net.timtaran.interactivemc.body.player.PlayerBodyPart;
import net.timtaran.interactivemc.body.type.IGrabbable;
import net.xmx.velthoric.core.body.VxBody;

/**
 * Handles the logic for trigger interactions.
 *
 * @author timtaran
 */
public class TriggerInteraction {
    public TriggerInteraction() {
    }

    public void updateGrabState(Player player, VxBody grabbedBody, PlayerBodyPart playerBodyPart, TriggerState triggerState) {
        if (grabbedBody instanceof IGrabbable grabbable) {
            grabbable.onTriggerStateUpdate(player, playerBodyPart, triggerState);
        }
    }
}
