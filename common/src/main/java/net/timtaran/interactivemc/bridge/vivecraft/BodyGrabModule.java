package net.timtaran.interactivemc.bridge.vivecraft;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.Vec3;
import net.timtaran.interactivemc.network.Networking;
import net.timtaran.interactivemc.network.sync.packet.C2SGrabPacket;
import net.timtaran.interactivemc.network.sync.packet.C2SReleasePacket;
import net.timtaran.interactivemc.util.InteractiveMCIdentifier;
import org.vivecraft.api.client.HeldInteractModule;

public class BodyGrabModule implements HeldInteractModule {
    private final ResourceLocation id;
    private final InteractionHand hand;

    public BodyGrabModule(InteractionHand hand) {
        this.id = InteractiveMCIdentifier.get(hand.name().toLowerCase() + "_body_grab");
        this.hand = hand;
    }

    @Override
    public boolean isActive(LocalPlayer localPlayer, InteractionHand interactionHand, Vec3 vec3) {
        return interactionHand.equals(hand);
    }

    @Override
    public boolean onHoldTick(LocalPlayer player, InteractionHand interactionHand) {
        Networking.sendToServer(new C2SGrabPacket(interactionHand));
        return HeldInteractModule.super.onHoldTick(player, interactionHand);
        // todo return value based on if player is actually grabbing something
    }

    @Override
    public void onRelease(LocalPlayer localPlayer, InteractionHand interactionHand) {
        Networking.sendToServer(new C2SReleasePacket(interactionHand));
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public int getPriority() {
        // before everything
        return 0;
    }

    @Override
    public boolean onPress(LocalPlayer localPlayer, InteractionHand interactionHand) {
        return false;
    }

    /**
     * @return
     */
    @Override
    public boolean swingsArm() {
        return false;
    }
}
