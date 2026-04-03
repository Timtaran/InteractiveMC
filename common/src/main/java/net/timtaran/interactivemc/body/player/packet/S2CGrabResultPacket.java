/*
 * This file is part of InteractiveMC.
 * Licensed under LGPL 3.0.
 */
package net.timtaran.interactivemc.body.player.packet;

import dev.architectury.networking.NetworkManager;
import net.minecraft.world.InteractionHand;
import net.timtaran.interactivemc.body.player.store.ClientPlayerBodyDataStore;
import net.xmx.velthoric.network.VxByteBuf;

import java.util.UUID;

/**
 * Server-to-Client packet containing the result of a grab action.
 * <p>
 * This packet is sent from the server in response to a {@link C2SGrabPacket},
 * containing the UUID of the successfully grabbed body or null if the grab failed.
 *
 * @author timtaran
 */
public class S2CGrabResultPacket extends HandInteractionPacket {
    /**
     * The UUID of the grabbed body, or null if the grab was unsuccessful.
     */
    private final UUID grabbedBodyUUID;

    /**
     * Constructs a new grab result packet.
     *
     * @param interactionHand the hand that performed the grab
     * @param grabbedBodyUUID the UUID of the grabbed body, or null if grab failed
     */
    public S2CGrabResultPacket(InteractionHand interactionHand, UUID grabbedBodyUUID) {
        super(interactionHand);
        this.grabbedBodyUUID = grabbedBodyUUID;
    }

    /**
     * Decodes a grab result packet from a network buffer.
     *
     * @param buf the buffer to read from
     * @return a new instance of the grab result packet
     */
    public static S2CGrabResultPacket decode(VxByteBuf buf) {
        return new S2CGrabResultPacket(buf.readEnum(InteractionHand.class), buf.readUUID());
    }

    /**
     * Encodes the packet data into a network buffer.
     *
     * @param buf the buffer to write to
     */
    @Override
    public void encode(VxByteBuf buf) {
        super.encode(buf);
        buf.writeUUID(grabbedBodyUUID);
    }

    /**
     * Handles the grab result on the client side.
     * <p>
     * Updates the client-side data store with the UUID of the grabbed body.
     * </p>
     *
     * @param context the packet context containing the player and network info
     */
    @Override
    public void handle(NetworkManager.PacketContext context) {
        ClientPlayerBodyDataStore.grabbedBodies.put(getInteractionHand(), grabbedBodyUUID);
    }
}
