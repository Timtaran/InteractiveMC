package net.timtaran.interactivemc.util.vr.vivecraft;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.InteractionHand;
import net.timtaran.interactivemc.util.vr.data.VRBodyPartDataImpl;
import net.timtaran.interactivemc.util.vr.data.VRPoseImpl;
import org.vivecraft.api.client.VRClientAPI;
import org.vivecraft.api.data.VRBodyPartData;
import org.vivecraft.api.data.VRPose;
import org.vivecraft.client_vr.provider.ControllerType;

public class VivecraftConversions {
    private VivecraftConversions() {}

    @Environment(EnvType.CLIENT)
    public static InteractionHand controllerTypeToInteractionHand(ControllerType hand) {
        return switch (hand) {
            case RIGHT -> VRClientAPI.instance().isLeftHanded() ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
            case LEFT -> VRClientAPI.instance().isLeftHanded() ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
        };
    }

    public static net.timtaran.interactivemc.util.vr.data.VRPose toInteractivemcType(VRPose pose, float playerScale) {
        return new VRPoseImpl(
                toInteractivemcType(pose.getHead()),
                toInteractivemcType(pose.getMainHand()),
                toInteractivemcType(pose.getOffHand()),
                pose.isLeftHanded(),
                playerScale
        );
    }

    public static net.timtaran.interactivemc.util.vr.data.VRBodyPartData toInteractivemcType(VRBodyPartData bodyPartData) {
        return new VRBodyPartDataImpl(bodyPartData.getPos(), bodyPartData.getDir(), bodyPartData.getRotation());
    }
}
