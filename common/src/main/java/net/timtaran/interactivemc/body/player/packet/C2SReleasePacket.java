/*
 * This file is part of InteractiveMC.
 * Licensed under LGPL 3.0.
 */
package net.timtaran.interactivemc.body.player.packet;

import dev.architectury.networking.NetworkManager;
import net.minecraft.network.FriendlyByteBuf;
import net.timtaran.interactivemc.body.player.PlayerBodyManager;
import net.timtaran.interactivemc.body.player.PlayerBodyPart;
import net.xmx.velthoric.core.physics.world.VxPhysicsWorld;

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
     * @param playerBodyPart the body part releasing the grab
     */
    public C2SReleasePacket(PlayerBodyPart playerBodyPart) {
        super(playerBodyPart);
    }

    /**
     * Decodes a release packet from a network buffer.
     *
     * @param buf the buffer to read from
     * @return a new instance of the release packet
     */
    public static C2SReleasePacket decode(FriendlyByteBuf buf) {
        return new C2SReleasePacket(buf.readEnum(PlayerBodyPart.class));
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
            physicsWorld.execute(() -> PlayerBodyManager.get(physicsWorld).release(context.getPlayer(), getPlayerBodyPart()));
    }
}
