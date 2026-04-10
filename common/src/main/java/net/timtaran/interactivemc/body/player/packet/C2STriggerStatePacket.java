/*
 * This file is part of InteractiveMC.
 * Licensed under LGPL 3.0.
 */
package net.timtaran.interactivemc.body.player.packet;


import dev.architectury.networking.NetworkManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.timtaran.interactivemc.body.player.PlayerBodyManager;
import net.timtaran.interactivemc.body.player.interaction.TriggerState;
import net.xmx.velthoric.core.physics.world.VxPhysicsWorld;
import net.xmx.velthoric.network.VxByteBuf;

/**
 * Client-to-Server packet for updating the player's trigger state.
 *
 * @author timtaran
 */
public class C2STriggerStatePacket extends HandInteractionPacket {
    /**
     * The new trigger state to set.
     */
    private final TriggerState triggerState;

    /**
     * Constructs a new grab request packet.
     *
     * @param interactionHand the hand attempting to grab
     * @param triggerState    the trigger state to set
     */
    public C2STriggerStatePacket(InteractionHand interactionHand, TriggerState triggerState) {
        super(interactionHand);
        this.triggerState = triggerState;
    }

    public void encode(VxByteBuf buf) {
        super.encode(buf);
        buf.writeEnum(triggerState);
    }

    /**
     * Decodes a grab packet from a network buffer.
     *
     * @param buf the buffer to read from
     * @return a new instance of the grab packet
     */
    public static C2STriggerStatePacket decode(FriendlyByteBuf buf) {
        return new C2STriggerStatePacket(buf.readEnum(InteractionHand.class), buf.readEnum(TriggerState.class));
    }

    /**
     * Handles the trigger state update packet on the server side.
     * <p>
     * This method executes on the physics world thread and notifies grabbed body (if any) of the new trigger state.
     * </p>
     *
     * @param context the packet context containing the player and network info
     */
    @Override
    public void handle(NetworkManager.PacketContext context) {
        VxPhysicsWorld physicsWorld = VxPhysicsWorld.get(context.getPlayer().level().dimension());
        if (physicsWorld != null)
            physicsWorld.execute(() -> PlayerBodyManager.get(physicsWorld).updateTriggerState(context.getPlayer(), getInteractionHand(), triggerState));

    }
}
