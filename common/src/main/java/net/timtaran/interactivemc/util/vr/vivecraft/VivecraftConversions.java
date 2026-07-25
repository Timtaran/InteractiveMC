package net.timtaran.interactivemc.util.vr.vivecraft;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.timtaran.interactivemc.body.player.PlayerBodyPart;
import net.timtaran.interactivemc.util.vr.data.VRBodyPartDataImpl;
import net.timtaran.interactivemc.util.vr.data.VRPoseImpl;
import org.vivecraft.api.client.VRClientAPI;
import org.vivecraft.api.data.VRBodyPartData;
import org.vivecraft.api.data.VRPose;
import org.vivecraft.client_vr.provider.ControllerType;

public class VivecraftConversions {
    private VivecraftConversions() {}

    @Environment(EnvType.CLIENT)
    public static PlayerBodyPart controllerTypeToPlayerBodyPart(ControllerType hand) {
        return switch (hand) {
            case RIGHT -> VRClientAPI.instance().isLeftHanded() ? PlayerBodyPart.OFF_HAND : PlayerBodyPart.MAIN_HAND;
            case LEFT -> VRClientAPI.instance().isLeftHanded() ? PlayerBodyPart.MAIN_HAND : PlayerBodyPart.OFF_HAND;
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
