/*
 * This file is part of InteractiveMC.
 * Licensed under LGPL 3.0.
 */
package net.timtaran.interactivemc.body.player.packet;

import net.minecraft.world.InteractionHand;
import net.xmx.velthoric.network.IVxNetPacket;
import net.xmx.velthoric.network.VxByteBuf;

/**
 * Base class for packets that involve hand interactions.
 * <p>
 * This abstract class provides common functionality for packets that need to
 * specify which hand (main or off-hand) is performing the action.
 *
 * @author timtaran
 */
public abstract class HandInteractionPacket implements IVxNetPacket {
    /**
     * The hand that is performing this interaction.
     */
    private final InteractionHand interactionHand; // todo replace with player body part

    /**
     * Constructs a new hand interaction packet.
     *
     * @param interactionHand the hand performing the interaction
     */
    protected HandInteractionPacket(InteractionHand interactionHand) {
        this.interactionHand = interactionHand;
    }

    /**
     * Encodes the packet data into a network buffer.
     *
     * @param buf the buffer to write to
     */
    @Override
    public void encode(VxByteBuf buf) {
        buf.writeEnum(interactionHand);
    }

    /**
     * Gets the interaction hand for this packet.
     *
     * @return the interaction hand (main or off-hand)
     */
    public InteractionHand getInteractionHand() {
        return interactionHand;
    }
}
