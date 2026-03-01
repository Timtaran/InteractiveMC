package net.timtaran.interactivemc.bridge.vivecraft;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.Vec3;
import net.timtaran.interactivemc.body.player.PlayerBodyManager;
import net.timtaran.interactivemc.body.player.PlayerBodyPart;
import net.timtaran.interactivemc.data.ClientDataStore;
import net.timtaran.interactivemc.network.Networking;
import net.timtaran.interactivemc.network.sync.packet.C2SGrabPacket;
import net.timtaran.interactivemc.network.sync.packet.C2SReleasePacket;
import net.timtaran.interactivemc.util.InteractiveMCIdentifier;
import net.timtaran.interactivemc.util.velthoric.VelthoricClientUtils;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.vivecraft.api.client.HeldInteractModule;
import org.vivecraft.api.data.VRBodyPart;
import org.vivecraft.api.data.VRBodyPartData;

public class BodyGrabModule implements HeldInteractModule {
    private final ResourceLocation id;
    private final InteractionHand hand;
    private final Vector3f localGrabOffset;

    public BodyGrabModule(InteractionHand hand) {
        this.id = InteractiveMCIdentifier.get(hand.name().toLowerCase() + "_body_grab");
        this.hand = hand;

        localGrabOffset = PlayerBodyPart.fromInteractionHand(hand).getLocalGrabPointVec3f();
    }

    @Override
    public boolean isActive(LocalPlayer localPlayer, InteractionHand interactionHand, Vec3 vec3) {
        return interactionHand.equals(hand);
    }

    @Override
    public boolean onPress(LocalPlayer localPlayer, InteractionHand interactionHand) {
        System.out.println("on_press");

        return false;
    }


    @Override
    public boolean onHoldTick(LocalPlayer player, InteractionHand interactionHand) {
        if (ClientDataStore.isGrabbing) {
            return true;
        }
        Networking.sendToServer(new C2SGrabPacket(interactionHand));

        VRBodyPartData bodyPartData = ClientDataStore.currentPose.getBodyPartData(VRBodyPart.fromInteractionHand(interactionHand));
        Quaternionf targetRot = new Quaternionf(bodyPartData.getRotation());

        Vector3f controllerPos = bodyPartData.getPos().toVector3f();
        Vector3f offset = new Vector3f(localGrabOffset);
        targetRot.transform(offset);

        Vector3f targetPos = controllerPos.add(offset);

        for (Integer bodyIndex : VelthoricClientUtils.bodiesAround(targetPos.x, targetPos.y, targetPos.z, PlayerBodyManager.GRAB_RADIUS)) {
            if (!ClientDataStore.playerControlledBodies.contains(bodyIndex)) {
                ClientDataStore.isGrabbing = true;
                return true;
            }
        }

        return false;
        // todo return value based on if player is actually grabbing something
    }

    @Override
    public void onRelease(LocalPlayer localPlayer, InteractionHand interactionHand) {
        Networking.sendToServer(new C2SReleasePacket(interactionHand));
        ClientDataStore.isGrabbing = false;
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public int getPriority() {
        // before everything
        return -1;
    }

    @Override
    public boolean swingsArm() {
        return false;
    }
}
