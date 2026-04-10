package net.timtaran.interactivemc.util;

import com.github.stephengold.joltjni.Quat;
import com.github.stephengold.joltjni.RVec3;
import com.github.stephengold.joltjni.operator.Op;
import net.timtaran.interactivemc.body.player.PlayerBodyPart;

public class PlayerBodyPartTransforms {
    public static RVec3 getGrabPointRotatedLocal(Quat rotation, PlayerBodyPart bodyPart) {
        // Rotate the local grab point by the body's rotation to get the correct world offset.
        return rotateVectorInPlace(bodyPart.getGrabPoint(), rotation);
    }

    public static RVec3 getGrabPointRotatedWorld(RVec3 position, Quat rotation, PlayerBodyPart bodyPart) {
        return Op.plus(position, getGrabPointRotatedLocal(rotation, bodyPart));
    }

    public static RVec3 getTrackingOffsetLocal(Quat rotation, PlayerBodyPart bodyPart) {
        return rotateVectorInPlace(bodyPart.getTrackingOffset(), rotation);
    }

    public static RVec3 getTrackingOffsetWorld(RVec3 position, Quat rotation, PlayerBodyPart bodyPart) {
        return Op.plus(position, getTrackingOffsetLocal(rotation, bodyPart));
    }

    private static RVec3 rotateVectorInPlace(RVec3 vector, Quat rotation) {
        vector.rotateInPlace(rotation);
        return vector;
    }
}
