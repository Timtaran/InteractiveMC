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
import net.xmx.velthoric.core.body.type.VxBody;
import net.xmx.velthoric.core.physics.world.VxPhysicsWorld;

/**
 * @author timtaran
 */
public class C2SGrabPacket extends HandInteractionPacket {
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
