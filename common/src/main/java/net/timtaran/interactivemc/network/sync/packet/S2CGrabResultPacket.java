package net.timtaran.interactivemc.network.sync.packet;

import dev.architectury.networking.NetworkManager;
import net.minecraft.world.InteractionHand;
import net.timtaran.interactivemc.data.ClientDataStore;
import net.xmx.velthoric.network.VxByteBuf;

public class S2CGrabResultPacket extends HandInteractionPacket {
    private final boolean isGrabSuccessful;

    public S2CGrabResultPacket(InteractionHand interactionHand, boolean isGrabSuccessful) {
        super(interactionHand);
        this.isGrabSuccessful = isGrabSuccessful;
    }

    @Override
    public void encode(VxByteBuf buf) {
        super.encode(buf);
        buf.writeBoolean(isGrabSuccessful);
    }

    public static S2CGrabResultPacket decode(VxByteBuf buf) {
        return new S2CGrabResultPacket(buf.readEnum(InteractionHand.class), buf.readBoolean());
    }

    @Override
    public void handle(NetworkManager.PacketContext context) {
        ClientDataStore.isGrabbing = this.isGrabSuccessful;
    }
}
