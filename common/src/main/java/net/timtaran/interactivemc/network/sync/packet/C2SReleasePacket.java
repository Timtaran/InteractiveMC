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
 * @author timtaran
 */
public class C2SReleasePacket extends C2SHandInteractionPacket {
    public C2SReleasePacket(InteractionHand interactionHand) {
        super(interactionHand);
    }

    public static C2SReleasePacket decode(VxByteBuf buf) {
        return new C2SReleasePacket(buf.readEnum(InteractionHand.class));
    }

    @Override
    public void handle(NetworkManager.PacketContext context) {
        VxPhysicsWorld physicsWorld = VxPhysicsWorld.get(context.getPlayer().level().dimension());
        if (physicsWorld != null)
            physicsWorld.execute(() -> PlayerBodyManager.get(physicsWorld).release(context.getPlayer(), getInteractionHand()));
    }
}
