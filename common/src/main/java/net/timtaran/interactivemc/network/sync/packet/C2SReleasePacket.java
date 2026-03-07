/*
 * This file is part of InteractiveMC.
 * Licensed under LGPL 3.0.
 */
package net.timtaran.interactivemc.network.sync.packet;

import dev.architectury.networking.NetworkManager;
import net.minecraft.world.InteractionHand;
import net.timtaran.interactivemc.body.player.PlayerBodyManager;
import net.xmx.velthoric.core.physics.world.VxPhysicsWorld;
import net.xmx.velthoric.network.VxByteBuf;

/**
 * Client-to-Server packet requesting a release action.
 * <p>
 * This packet is sent from the client when a player releases a grabbed object.
 * The server processes this request and removes the grab constraint.
 *
 * @author timtaran
 */
public class C2SReleasePacket extends HandInteractionPacket {
    /**
     * Constructs a new release request packet.
     *
     * @param interactionHand the hand releasing the grab
     */
    public C2SReleasePacket(InteractionHand interactionHand) {
        super(interactionHand);
    }

    /**
     * Decodes a release packet from a network buffer.
     *
     * @param buf the buffer to read from
     * @return a new instance of the release packet
     */
    public static C2SReleasePacket decode(VxByteBuf buf) {
        return new C2SReleasePacket(buf.readEnum(InteractionHand.class));
    }

    /**
     * Handles the release request on the server side.
     * <p>
     * This method executes on the physics world thread to ensure thread safety
     * and removes the grab constraint from the grabbed object.
     * </p>
     *
     * @param context the packet context containing the player and network info
     */
    @Override
    public void handle(NetworkManager.PacketContext context) {
        VxPhysicsWorld physicsWorld = VxPhysicsWorld.get(context.getPlayer().level().dimension());
        if (physicsWorld != null)
            physicsWorld.execute(() -> PlayerBodyManager.get(physicsWorld).release(context.getPlayer(), getInteractionHand()));
    }
}
