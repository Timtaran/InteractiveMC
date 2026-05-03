/*
 * This file is part of InteractiveMC.
 * Licensed under LGPL 3.0.
 */
package net.timtaran.interactivemc.body.player.packet;

import dev.architectury.networking.NetworkManager;
import net.minecraft.world.InteractionHand;
import net.timtaran.interactivemc.body.player.store.ClientPlayerBodyDataStore;
import net.timtaran.interactivemc.body.type.IGrabbable;
import net.xmx.velthoric.core.body.VxBody;
import net.xmx.velthoric.core.body.client.VxClientBodyManager;
import net.xmx.velthoric.network.VxByteBuf;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
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
     * Whether the grabbed body is attached to the player or not.
     */
    private final boolean isAttached;

    /**
     * Constructs a new grab result packet.
     *
     * @param interactionHand the hand that performed the grab
     * @param grabbedBodyUUID the UUID of the grabbed body, or null if grab failed
     */
    public S2CGrabResultPacket(InteractionHand interactionHand, @Nullable UUID grabbedBodyUUID, boolean isAttached) {
        super(interactionHand);

        if (grabbedBodyUUID == null) {
            isBodyPresent = false;
            this.grabbedBodyUUID = NULL_UUID;
        }
        else {
            isBodyPresent = true;
            this.grabbedBodyUUID = grabbedBodyUUID;
        }

        this.isAttached = isAttached;
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

        if (isBodyPresent) {
            return new S2CGrabResultPacket(interactionHand, buf.readUUID(), buf.readBoolean());
        }
        else {
            return new S2CGrabResultPacket(interactionHand, null, buf.readBoolean());
        }
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

        if (isBodyPresent) {
            buf.writeUUID(grabbedBodyUUID);
        }

        buf.writeBoolean(isAttached);
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
        InteractionHand hand = getInteractionHand();
        UUID currentId = getGrabbedBodyUUID();

        UUID previousId = ClientPlayerBodyDataStore.grabbedBodies.put(hand, currentId);

        if (Objects.equals(previousId, currentId)) {
            return;
        }

        VxBody grabbedBody = VxClientBodyManager.getInstance().getVxBody(Objects.requireNonNullElse(currentId, previousId));
        if (!(grabbedBody instanceof IGrabbable grabbable)) {
            return;
        }

        if (currentId != null) {
            grabbable.onGrabClient(context.getPlayer(), hand, isAttached);
        } else {
            grabbable.onReleaseClient(context.getPlayer(), hand, isAttached);
        }
    }
}
