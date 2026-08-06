/*
 * This file is part of InteractiveMC.
 * Licensed under LGPL 3.0.
 */
package net.timtaran.interactivemc.util.vr.visor;

import net.timtaran.interactivemc.body.player.PlayerBodyPart;
import net.timtaran.interactivemc.util.vr.data.VRBodyPartData;
import net.timtaran.interactivemc.util.vr.data.VRBodyPartDataImpl;
import net.timtaran.interactivemc.util.vr.data.VRPoseImpl;
import org.joml.Quaternionf;
import org.vmstudio.visor.api.common.HandType;
import org.vmstudio.visor.api.common.player.VRPlayerPose;
import org.vmstudio.visor.api.common.player.VRPose;

public class VisorConversions {
    public static PlayerBodyPart handToBodyPart(HandType hand) {
        return hand == HandType.MAIN ? PlayerBodyPart.MAIN_HAND : PlayerBodyPart.OFF_HAND;
    }

    public static net.timtaran.interactivemc.util.vr.data.VRPose toInteractivemcType(VRPlayerPose pose, boolean isLeftHanded, float playerScale) {
        return new VRPoseImpl(
                toInteractivemcType(pose.getHmd()),
                toInteractivemcType(pose.getMainHand()),
                toInteractivemcType(pose.getOffhand()),
                isLeftHanded,
                playerScale
        );
    }

    public static VRBodyPartData toInteractivemcType(VRPose pose) {
        return new VRBodyPartDataImpl(pose.getPositionVec3(), pose.getDirectionVec3(), pose.getRotation().getNormalizedRotation(new Quaternionf()));
    }
}
