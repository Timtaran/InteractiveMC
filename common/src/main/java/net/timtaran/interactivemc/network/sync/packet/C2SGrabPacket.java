/*
 * This file is part of InteractiveMC.
 * Licensed under LGPL 3.0.
 */
package net.timtaran.interactivemc.network.sync.packet;

import dev.architectury.networking.NetworkManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.timtaran.interactivemc.body.player.PlayerBodyManager;
import net.xmx.velthoric.core.physics.world.VxPhysicsWorld;

/**
 * @author timtaran
 */
public class C2SGrabPacket extends C2SHandInteractionPacket {
    public C2SGrabPacket(InteractionHand interactionHand) {
        super(interactionHand);
    }

    public static C2SGrabPacket decode(FriendlyByteBuf buf) {
        return new C2SGrabPacket(buf.readEnum(InteractionHand.class));
    }

    @Override
    public void handle(NetworkManager.PacketContext context) {
        VxPhysicsWorld physicsWorld = VxPhysicsWorld.get(context.getPlayer().level().dimension());
        if (physicsWorld != null)
            physicsWorld.execute(() -> PlayerBodyManager.get(physicsWorld).grab(context.getPlayer(), getInteractionHand()));
    }
}
