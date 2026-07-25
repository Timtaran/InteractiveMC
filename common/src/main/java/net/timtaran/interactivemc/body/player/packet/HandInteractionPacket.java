/*
 * This file is part of InteractiveMC.
 * Licensed under LGPL 3.0.
 */
package net.timtaran.interactivemc.body.player.packet;

import net.timtaran.interactivemc.body.player.PlayerBodyPart;
import net.xmx.velthoric.network.IVxNetPacket;
import net.xmx.velthoric.network.VxByteBuf;

/**
 * Base class for packets that involve player body parts interactions.
 * <p>
 * This abstract class provides common functionality for packets that need to
 * specify which body part is performing the action.
 *
 * @author timtaran
 */
public abstract class HandInteractionPacket implements IVxNetPacket {
    /**
     * The body part that is performing this interaction.
     */
    private final PlayerBodyPart playerBodyPart;

    /**
     * Constructs a new hand interaction packet.
     *
     * @param playerBodyPart the body part performing the interaction
     */
    protected HandInteractionPacket(PlayerBodyPart playerBodyPart) {
        this.playerBodyPart = playerBodyPart;
    }

    /**
     * Encodes the packet data into a network buffer.
     *
     * @param buf the buffer to write to
     */
    @Override
    public void encode(VxByteBuf buf) {
        buf.writeEnum(playerBodyPart);
    }

    /**
     * Gets the player body part for this packet.
     *
     * @return the player body part
     */
    public PlayerBodyPart getPlayerBodyPart() {
        return playerBodyPart;
    }
}
