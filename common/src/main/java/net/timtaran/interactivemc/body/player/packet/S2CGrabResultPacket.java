/*
 * This file is part of InteractiveMC.
 * Licensed under LGPL 3.0.
 */
package net.timtaran.interactivemc.body.player.packet;

import dev.architectury.networking.NetworkManager;
import net.minecraft.world.InteractionHand;
import net.timtaran.interactivemc.body.player.store.ClientPlayerBodyDataStore;
import net.xmx.velthoric.network.VxByteBuf;
import org.jetbrains.annotations.Nullable;

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
    private static final UUID NULL_UUID = UUID.fromString("00000000-0000-0000-0000-000000000000");

    /**
     * Whether the grab was successful or not.
     */
    private final boolean isBodyPresent;

    /**
     * The UUID of the grabbed body.
     */
    private final UUID grabbedBodyUUID;

    /**
     * Constructs a new grab result packet.
     *
     * @param interactionHand the hand that performed the grab
     * @param grabbedBodyUUID the UUID of the grabbed body, or null if grab failed
     */
    public S2CGrabResultPacket(InteractionHand interactionHand, @Nullable UUID grabbedBodyUUID) {
        super(interactionHand);

        if (grabbedBodyUUID == null) {
            isBodyPresent = false;
            this.grabbedBodyUUID = NULL_UUID;
        }
        else {
            isBodyPresent = true;
            this.grabbedBodyUUID = grabbedBodyUUID;
        }
    }

    /**
     * Decodes a grab result packet from a network buffer.
     *
     * @param buf the buffer to read from
     * @return a new instance of the grab result packet
     */
    public static S2CGrabResultPacket decode(VxByteBuf buf) {
        InteractionHand interactionHand = buf.readEnum(InteractionHand.class);

        boolean isBodyPresent = buf.readBoolean();

        if (isBodyPresent)
            return new S2CGrabResultPacket(interactionHand, buf.readUUID());
        else
            return new S2CGrabResultPacket(interactionHand, null);
    }

    /**
     * Encodes the packet data into a network buffer.
     *
     * @param buf the buffer to write to
     */
    @Override
    public void encode(VxByteBuf buf) {
        super.encode(buf);

        buf.writeBoolean(isBodyPresent);

        if (isBodyPresent)
            buf.writeUUID(grabbedBodyUUID);
    }


    @Nullable
    public UUID getGrabbedBodyUUID() {
        if (isBodyPresent)
            return grabbedBodyUUID;
        else
            return null;
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
        ClientPlayerBodyDataStore.grabbedBodies.put(getInteractionHand(), getGrabbedBodyUUID());
    }
}
