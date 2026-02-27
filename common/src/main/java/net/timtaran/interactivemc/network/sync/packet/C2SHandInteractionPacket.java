/*
 * This file is part of InteractiveMC.
 * Licensed under LGPL 3.0.
 */
package net.timtaran.interactivemc.network.sync.packet;

import net.minecraft.world.InteractionHand;
import net.xmx.velthoric.network.IVxNetPacket;
import net.xmx.velthoric.network.VxByteBuf;

/**
 * @author timtaran
 */
public abstract class C2SHandInteractionPacket implements IVxNetPacket {
    private final InteractionHand interactionHand;

    public C2SHandInteractionPacket(InteractionHand interactionHand) {
        this.interactionHand = interactionHand;
    }


    @Override
    public void encode(VxByteBuf buf) {
        buf.writeEnum(interactionHand);
    }

    public InteractionHand getInteractionHand() {
        return interactionHand;
    }
}
