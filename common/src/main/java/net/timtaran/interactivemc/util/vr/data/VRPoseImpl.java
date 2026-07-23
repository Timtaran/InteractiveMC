/*
 * This file is part of InteractiveMC.
 * Licensed under LGPL 3.0.
 */
package net.timtaran.interactivemc.util.vr.data;

import net.minecraft.world.phys.Vec3;
import net.timtaran.interactivemc.body.player.PlayerBodyPart;

public record VRPoseImpl(VRBodyPartData head, VRBodyPartData mainHand, VRBodyPartData offHand, boolean isLeftHanded, float playerScale) implements VRPose {
    @Override
    public VRBodyPartData getBodyPartData(PlayerBodyPart bodyPart) {
        return switch (bodyPart) {
            case HEAD -> head;
            case MAIN_HAND -> mainHand;
            case OFF_HAND -> offHand;
        };
    }

    @Override
    public boolean isLeftHanded() {
        return isLeftHanded;
    }

    @Override
    public float getPlayerScale() {
        return playerScale;
    }

    @Override
    public VRPoseImpl relativeToPosition(Vec3 position) {
        return new VRPoseImpl(
                relativeToPosition(this.head, position),
                relativeToPosition(this.mainHand, position),
                relativeToPosition(this.offHand, position),
                this.isLeftHanded, this.playerScale
        );
    }

    private VRBodyPartData relativeToPosition(VRBodyPartData vrBodyPartData, Vec3 position) {
        if (vrBodyPartData == null) {
            return null;
        }
        return new VRBodyPartDataImpl(vrBodyPartData.getPos().subtract(position), vrBodyPartData.getDir(),
                vrBodyPartData.getRotation());
    }
}
