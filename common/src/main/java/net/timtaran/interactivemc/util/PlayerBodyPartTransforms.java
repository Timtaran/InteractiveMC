package net.timtaran.interactivemc.util;

import com.github.stephengold.joltjni.Quat;
import com.github.stephengold.joltjni.RVec3;
import com.github.stephengold.joltjni.Vec3;
import com.github.stephengold.joltjni.operator.Op;
import net.timtaran.interactivemc.body.player.PlayerBodyPart;

public class PlayerBodyPartTransforms {
    public static Vec3 getGrabPointRotatedLocal(Quat rotation, PlayerBodyPart bodyPart, float scale) {
        // Rotate the local grab point by the body's rotation to get the correct world offset.
        return Op.star(Op.star(rotation, bodyPart.getGrabPoint()), scale);
    }

    public static RVec3 getGrabPointRotatedWorld(RVec3 position, Quat rotation, PlayerBodyPart bodyPart, float scale) {
        return Op.plus(position, getGrabPointRotatedLocal(rotation, bodyPart, scale));
    }

    public static Vec3 getTrackingOffsetLocal(Quat rotation, PlayerBodyPart bodyPart, float scale) {
        return Op.star(Op.star(rotation, bodyPart.getTrackingOffset()), scale);
    }

    public static RVec3 getTrackingOffsetWorld(RVec3 position, Quat rotation, PlayerBodyPart bodyPart, float scale) {
        return Op.plus(position, getTrackingOffsetLocal(rotation, bodyPart, scale));
    }
}
