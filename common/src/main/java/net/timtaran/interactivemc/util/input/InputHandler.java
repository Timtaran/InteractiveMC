/*
 * This file is part of InteractiveMC.
 * Licensed under LGPL 3.0.
 */
package net.timtaran.interactivemc.util.input;

import net.minecraft.client.KeyMapping;
import net.timtaran.interactivemc.body.player.PlayerBodyPart;
import net.timtaran.interactivemc.body.player.interaction.TriggerState;
import net.timtaran.interactivemc.body.player.packet.C2SGrabPacket;
import net.timtaran.interactivemc.body.player.packet.C2SReleasePacket;
import net.timtaran.interactivemc.body.player.packet.C2STriggerStatePacket;
import net.timtaran.interactivemc.body.player.store.ClientPlayerBodyDataStore;
import net.timtaran.interactivemc.network.Networking;

public class InputHandler {
    /**
     * Sends packet to the server to grab an object using the specified playerBodyPart.
     *
     * @param playerBodyPart the playerBodyPart attempting to grab
     */
    public static void attemptGrab(PlayerBodyPart playerBodyPart) {
        Networking.sendToServer(new C2SGrabPacket(playerBodyPart));
    }

    /**
     * Sends a release packet to the server to remove the grab constraint.
     *
     * @param playerBodyPart the playerBodyPart to release
     */
    public static void attemptRelease(PlayerBodyPart playerBodyPart) {
        Networking.sendToServer(new C2SReleasePacket(playerBodyPart));
    }

    public static void updateTriggerState(PlayerBodyPart playerBodyPart, TriggerState triggerState) {
        TriggerState previousTriggerState = ClientPlayerBodyDataStore.triggerStates.put(playerBodyPart, triggerState);

        if (!triggerState.equals(previousTriggerState)) {
            Networking.sendToServer(new C2STriggerStatePacket(playerBodyPart, triggerState));
        }
    }

    public static TriggerState getTriggerState(PlayerBodyPart playerBodyPart) {
        return ClientPlayerBodyDataStore.triggerStates.get(playerBodyPart);
    }
}
