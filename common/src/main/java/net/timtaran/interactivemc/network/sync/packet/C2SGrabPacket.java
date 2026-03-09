/*
 * This file is part of InteractiveMC.
 * Licensed under LGPL 3.0.
 */
package net.timtaran.interactivemc.network.sync.packet;

import dev.architectury.networking.NetworkManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.timtaran.interactivemc.body.player.PlayerBodyManager;
import net.timtaran.interactivemc.network.Networking;
import net.xmx.velthoric.core.body.VxBody;
import net.xmx.velthoric.core.physics.world.VxPhysicsWorld;

/**
 * Client-to-Server packet requesting a grab action.
 * <p>
 * This packet is sent from the client when a player attempts to grab an object
 * using their specified hand. The server processes this request and sends back
 * a {@link S2CGrabResultPacket} with the result.
 *
 * @author timtaran
 */
public class C2SGrabPacket extends HandInteractionPacket {
    /**
     * Constructs a new grab request packet.
     *
     * @param interactionHand the hand attempting to grab
     */
    public C2SGrabPacket(InteractionHand interactionHand) {
        super(interactionHand);
    }

    /**
     * Decodes a grab packet from a network buffer.
     *
     * @param buf the buffer to read from
     * @return a new instance of the grab packet
     */
    public static C2SGrabPacket decode(FriendlyByteBuf buf) {
        return new C2SGrabPacket(buf.readEnum(InteractionHand.class));
    }

    /**
     * Handles the grab request on the server side.
     * <p>
     * This method executes on the physics world thread to ensure thread safety,
     * then sends the result back to the client.
     * </p>
     *
     * @param context the packet context containing the player and network info
     */
    @Override
    public void handle(NetworkManager.PacketContext context) {
        VxPhysicsWorld physicsWorld = VxPhysicsWorld.get(context.getPlayer().level().dimension());
        if (physicsWorld != null)
            physicsWorld.execute(() -> {
                        VxBody grabbedBody = PlayerBodyManager.get(physicsWorld).grab(context.getPlayer(), getInteractionHand());

                        if (context.getPlayer() instanceof ServerPlayer serverPlayer) {
                            context.getPlayer().getServer().execute(() ->
                                    Networking.sendToPlayer(
                                            serverPlayer,
                                            new S2CGrabResultPacket(
                                                    getInteractionHand(),
                                                    grabbedBody == null ? null : grabbedBody.getPhysicsId()
                                            )
                                    )
                            );
                        }
                    }
            );
    }
}
