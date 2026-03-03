package net.timtaran.interactivemc.network.sync.packet;

import dev.architectury.networking.NetworkManager;
import net.minecraft.world.InteractionHand;
import net.timtaran.interactivemc.data.ClientDataStore;
import net.xmx.velthoric.network.VxByteBuf;

import java.util.UUID;

public class S2CGrabResultPacket extends HandInteractionPacket {
    private final UUID grabbedBodyUUID;

    public S2CGrabResultPacket(InteractionHand interactionHand, UUID grabbedBodyUUID) {
        super(interactionHand);
        this.grabbedBodyUUID = grabbedBodyUUID;
    }

    @Override
    public void encode(VxByteBuf buf) {
        super.encode(buf);
        buf.writeUUID(grabbedBodyUUID);
    }

    public static S2CGrabResultPacket decode(VxByteBuf buf) {
        return new S2CGrabResultPacket(buf.readEnum(InteractionHand.class), buf.readUUID());
    }

    @Override
    public void handle(NetworkManager.PacketContext context) {
        ClientDataStore.grabbedBodies.put(getInteractionHand(), grabbedBodyUUID);
    }
}
