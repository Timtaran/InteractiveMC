/*
 * This file is part of InteractiveMC.
 * Licensed under LGPL 3.0.
 */
package net.timtaran.interactivemc.body.player.interaction;


import com.github.stephengold.joltjni.*;
import com.github.stephengold.joltjni.enumerate.EConstraintSpace;
import com.github.stephengold.joltjni.operator.Op;
import com.github.stephengold.joltjni.readonly.ConstBody;
import com.github.stephengold.joltjni.readonly.ConstBodyLockInterface;
import com.github.stephengold.joltjni.readonly.QuatArg;
import com.github.stephengold.joltjni.readonly.RVec3Arg;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.timtaran.interactivemc.body.player.PlayerBodyManager;
import net.timtaran.interactivemc.body.player.PlayerBodyPart;
import net.timtaran.interactivemc.body.player.PlayerBodyPartData;
import net.timtaran.interactivemc.body.player.store.ClientPlayerBodyDataStore;
import net.timtaran.interactivemc.body.player.store.PlayerBodyDataStore;
import net.timtaran.interactivemc.body.type.GrabPoint;
import net.timtaran.interactivemc.body.type.IGrabbable;
import net.timtaran.interactivemc.init.InteractiveMC;
import net.timtaran.interactivemc.util.PlayerBodyPartTransforms;
import net.timtaran.interactivemc.util.velthoric.VelthoricClientUtils;
import net.xmx.velthoric.core.body.VxBody;
import net.xmx.velthoric.core.constraint.VxConstraint;
import net.xmx.velthoric.core.constraint.manager.VxConstraintManager;
import net.xmx.velthoric.core.intersection.VxPhysicsIntersector;
import net.xmx.velthoric.core.intersection.raycast.VxHitResult;
import net.xmx.velthoric.core.intersection.raycast.VxRaycaster;
import net.xmx.velthoric.core.physics.VxPhysicsLayers;
import net.xmx.velthoric.core.physics.world.VxPhysicsWorld;
import net.xmx.velthoric.math.VxConversions;
import net.xmx.velthoric.math.VxTransform;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.vivecraft.api.VRAPI;
import org.vivecraft.api.data.VRBodyPart;
import org.vivecraft.api.data.VRBodyPartData;
import org.vivecraft.api.data.VRPose;
import org.vivecraft.api.data.VRPoseHistory;

import java.util.Comparator;
import java.util.List;

/**
 * Handles the logic for physical interactions between a player and objects in the world.
 * <p>
 * This class manages the detection of grabbable objects using sphere-casting,
 * the creation of physical constraints (joints) to "attach" objects to the player's
 * hands, and the subsequent release of those objects.
 * <p>
 * This class is only intended for use by {@link PlayerBodyManager}.
 *
 * @author timtaran
 * @see PlayerBodyManager
 */
public class GrabInteraction {
    private static final float GRAB_RADIUS = 0.15f;
    private static final Vec3 GRAB_SHAPE_SCALE = new Vec3(1f, 1f, 1f);

    /**
     * Ray distance to detect possible grabbed bodies.
     */
    private static final float REMOTE_GRAB_DISTANCE = 7.5f;

    /**
     * Distance from grabbed body where grabber can pull it.
     */
    private static final float REMOTE_GRAB_MAX_DISTANCE = 10f;

    /**
     * How many ticks back the previous pose will be taken when calculating velocity changes for pulling body.
     */
    private static final int PULL_PREVIOUS_POSE_TICKS = 3;
    private static final double PULL_THRESHOLD = 0.1;

    private static final float PULL_MAGNITUDE_STIFFNESS = 45f;
    private static final float PULL_MAGNITUDE_DAMPING = 12f;
    private static final float PULL_MIN_FORCE = 5f;
    private static final float PULL_MAX_FORCE = 3500f;

    private final VxPhysicsWorld world;
    private final PlayerBodyManager playerBodyManager;

    public GrabInteraction(VxPhysicsWorld world, PlayerBodyManager playerBodyManager) {
        this.world = world;
        this.playerBodyManager = playerBodyManager;
    }

    /**
     * Result of a grab attempt.
     *
     * @param grabbedBody    the body that was grabbed
     * @param grabConstraint the grab constraint that was created
     * @see PlayerBodyPartData
     */
    public record GrabResult(@NotNull VxBody grabbedBody, @Nullable VxConstraint grabConstraint) {
    }

    /**
     * Attempts to grab an object using the specified player's body part.
     *
     * @param player          the player attempting to grab
     * @param grabberBody     the body that is grabbing
     * @param playerBodyPart  the player's body part
     * @param isRemoteAllowed whether remote grabbing (raycast-based) is allowed if no bodies are within grab radius
     * @return the body that was grabbed, or null if no body was grabbed
     */
    @Nullable
    public GrabResult grab(Player player, VxBody grabberBody, PlayerBodyPart playerBodyPart, boolean isRemoteAllowed) {
        PhysicsSystem physicsSystem = world.getPhysicsSystem();
        if (physicsSystem == null) return null;

        ConstBodyLockInterface lockInterface = physicsSystem.getBodyLockInterface();

        RVec3 worldGrabPoint;

        try (BodyLockRead lock = new BodyLockRead(lockInterface, grabberBody.getBodyId())) {
            ConstBody body = lock.getBody();
            if (body == null)
                return null;

            worldGrabPoint = getBodyPartGrabPointWorld(body, playerBodyPart);
        }

        System.out.println("worldGrabPoint_grab: " + worldGrabPoint);

        // Performing instant grab checks (body will be teleported and attached)
        GrabResult instantGrabResult = tryInstantGrab(lockInterface, player, grabberBody, worldGrabPoint, playerBodyPart);
        if (!isRemoteAllowed || instantGrabResult != null) {
            return instantGrabResult;
        }

        // No bodies available for grabbing at the point, performing raycast to find one to pull
        return tryRemoteGrab(lockInterface, player, grabberBody, worldGrabPoint, playerBodyPart);
    }

    /**
     * Attempts to instant grab an object using the specified player's hand.
     * <p>
     * This method performs a sphere cast from the grab point and tries to grab the closest
     * non-player body within the grab radius.
     */
    @Nullable
    private GrabResult tryInstantGrab(ConstBodyLockInterface lockInterface, Player player, VxBody grabberBody, RVec3 worldGrabPoint, PlayerBodyPart playerBodyPart) {
        List<VxPhysicsIntersector.IntersectShapeResult> intersections = findInstantGrabCandidates(player, grabberBody, worldGrabPoint);

        intersections.sort(Comparator.comparingDouble(result -> { // sort by closest intersection point to grab point.
            Vec3 p = result.bodyContactPoint();

            double dx = p.getX() - worldGrabPoint.x();
            double dy = p.getY() - worldGrabPoint.y();
            double dz = p.getZ() - worldGrabPoint.z();

            return dx * dx + dy * dy + dz * dz;
        }));

        int size = intersections.size();
        int[] bodyIds = new int[size + 1];

        for (int i = 0; i < size; i++) {
            bodyIds[i] = intersections.get(i).bodyId();
        }

        bodyIds[size] = grabberBody.getBodyId();

        try (BodyLockMultiWrite lock = new BodyLockMultiWrite(lockInterface, bodyIds)) {
            Body grabberJoltBody = lock.getBody(size);

            for (int i = 0; i < size; i++) {
                Body grabbedJoltBody = lock.getBody(i);

                if (!grabbedJoltBody.isInBroadPhase())
                    continue;

                VxPhysicsIntersector.IntersectShapeResult intersection = intersections.get(i);

                System.out.println(intersection);

                GrabResult grabResult = tryInstantGrabCandidate(player, grabberBody, grabberJoltBody, grabbedJoltBody, playerBodyPart, intersection, worldGrabPoint);

                if (grabResult != null) return grabResult;
            }
        }

        return null;
    }

    @NotNull
    private List<VxPhysicsIntersector.IntersectShapeResult> findInstantGrabCandidates(Player player, VxBody grabberBody, RVec3 worldGrabPoint) {
        try (ObjectLayerFilter olFilter = new ObjectLayerFilter() {
            @Override
            public boolean shouldCollide(int objectLayer) {
                return objectLayer != VxPhysicsLayers.NON_MOVING;
            }
        };
             BroadPhaseLayerFilter bplFilter = new BroadPhaseLayerFilter();
             BodyFilter bodyFilter = new BodyFilter(); // runtime checks works really strange so we will check body ids below
             SphereShape shape = new SphereShape(GRAB_RADIUS)) {

            RVec3Arg base = new RVec3(0.0f, 0.0f, 0.0f);

            VxTransform vxTransform = grabberBody.getTransform();
            RMat44 comTransform = new VxTransform(worldGrabPoint, vxTransform.getRotation()).toRMat44();

            List<VxPhysicsIntersector.IntersectShapeResult> intersections = VxPhysicsIntersector.narrowIntersectShape(world, shape, GRAB_SHAPE_SCALE, comTransform, base, bplFilter, olFilter, bodyFilter);

            intersections.removeIf(intersection ->
                    PlayerBodyDataStore.isPlayerControlledBody(player.getUUID(), intersection.bodyId())
            );

            return intersections;
        }
    }

    @Nullable
    private GrabResult tryInstantGrabCandidate(Player player, VxBody grabberBody, Body grabberJoltBody, Body grabbedJoltBody, PlayerBodyPart playerBodyPart, VxPhysicsIntersector.IntersectShapeResult intersection, RVec3Arg worldGrabPoint) {
        if (grabbedJoltBody.getObjectLayer() != VxPhysicsLayers.TERRAIN) { // Velthoric doesn't have VxBody for terrain, so we separate terrain and other bodies logic here
            VxBody grabbedBody = world.getBodyManager().getByJoltBodyId(intersection.bodyId());

            if (grabbedBody == null) {
                InteractiveMC.LOGGER.warn("grabbedBody is null for body ID: {}", intersection.bodyId());
                return null;
            }

            // calculating intersection point and body center offset
            RVec3 bodyContactPointOffset = Op.minus(intersection.bodyContactPoint().toRVec3(), grabbedJoltBody.getPosition()); // todo: move before physics layer check when terrain grabbing is implemented

            @Nullable GrabPoint grabPoint = null;
            boolean isGrabbable = grabbedBody instanceof IGrabbable;

            if (isGrabbable) {
                RVec3 bodyContactPointLocal = new RVec3(bodyContactPointOffset);
                bodyContactPointLocal.rotateInPlace(grabbedJoltBody.getRotation().conjugated());

                QuatArg rotationDifference = Op.star(grabberJoltBody.getRotation().conjugated(), grabbedJoltBody.getRotation());

                grabPoint = ((IGrabbable) grabbedBody).getGrabPoint(player, playerBodyPart, bodyContactPointLocal, rotationDifference);

                if (grabPoint == null)
                    return null;

                bodyContactPointOffset = new RVec3(grabPoint.position());
                bodyContactPointOffset.rotateInPlace(grabbedJoltBody.getRotation());
            }

            VxConstraint constraint;

            // Calculate a new world-space position for the body so that the local contact point
            // aligns exactly with the desired grab point in world space.
            if (grabbedJoltBody.isDynamic() && !PlayerBodyDataStore.isBodyGrabbed(grabberJoltBody.getId())) {
                // move body to grabber
                grabbedJoltBody.setPositionAndRotationInternal(
                        Op.minus(worldGrabPoint, bodyContactPointOffset),
                        isGrabbable ? Op.star(grabberJoltBody.getRotation(), grabPoint.rotation()) : grabbedJoltBody.getRotation()
                );
                constraint = attach(grabberBody, grabbedBody, worldGrabPoint, player, playerBodyPart);
                System.out.println("Attaching " + grabbedBody.getClass().getSimpleName() + " to " + grabberBody.getClass().getSimpleName() + " at " + worldGrabPoint);
            } else {
                // move grabber to body
                RVec3 bodyContactPoint = Op.plus(grabbedJoltBody.getPosition(), bodyContactPointOffset);

                grabberJoltBody.setPositionAndRotationInternal(
                        Op.minus(
                                bodyContactPoint,
                                PlayerBodyPartTransforms.getGrabPointRotatedLocal(grabberJoltBody.getRotation(), playerBodyPart)
                        ),
                        isGrabbable ? Op.star(grabbedJoltBody.getRotation(), grabPoint.rotation().conjugated()) : grabberJoltBody.getRotation()
                );
                constraint = attach(grabberBody, grabbedBody, bodyContactPoint, player, playerBodyPart);
                System.out.println("Attaching " + grabbedBody.getClass().getSimpleName() + " to " + grabberBody.getClass().getSimpleName() + " at " + bodyContactPoint);
            }

            world.getBodyPairIgnoreHandler().ignorePair(grabberBody.getBodyId(), grabbedBody.getBodyId());

            return new GrabResult(grabbedBody, constraint);

        } // todo add terrain grab after implementing client-side prediction

        return null;
    }

    @Nullable
    private GrabResult tryRemoteGrab(ConstBodyLockInterface lockInterface, Player player, VxBody grabberBody, RVec3 worldGrabPoint, PlayerBodyPart playerBodyPart) {  // todo replace with RVec3Arg
        try (ObjectLayerFilter olFilter = new ObjectLayerFilter() {
            @Override
            public boolean shouldCollide(int objectLayer) {
                return (objectLayer != VxPhysicsLayers.NON_MOVING && objectLayer != VxPhysicsLayers.TERRAIN);
            }
        }) {
            VRBodyPartData bodyPartData = PlayerBodyDataStore.vrPoses.get(player.getUUID()).getBodyPartData(playerBodyPart.toVRBodyPart());
            Vec3 direction = VxConversions.toJolt(bodyPartData.getDir()).toVec3().normalized();
            Quat bodyRotation = grabberBody.getTransform().getRotation().normalized(); // todo: remove
            System.out.println("VR Pose direction " + direction + "; Direction based on rotation: " + quatToForward(bodyRotation.getX(), bodyRotation.getY(), bodyRotation.getZ(), bodyRotation.getW()));
            List<VxHitResult> raycastResult = VxRaycaster.raycastAll(world, worldGrabPoint, direction, REMOTE_GRAB_DISTANCE, olFilter);

            System.out.println(worldGrabPoint + " " + direction); // todo remove and replace with visual indication

            System.out.println("111 " + System.currentTimeMillis());
            for (VxHitResult hit : raycastResult) {
                int grabbedBodyId = hit.getPhysicsHit().get().bodyId();

                if (hit.getPhysicsHit().isEmpty() || PlayerBodyDataStore.isPlayerControlledBody(player.getUUID(), grabbedBodyId))
                    continue;

                VxBody grabbedBody = world.getBodyManager().getByJoltBodyId(grabbedBodyId);

                if (grabbedBody == null)
                    continue;

                RVec3 hitPointOnBody;

                try (BodyLockWrite lock = new BodyLockWrite(lockInterface, grabbedBodyId)) {
                    if (!lock.succeededAndIsInBroadPhase())
                        continue;

                    Body grabbedJoltBody = lock.getBody();

                    if (!grabbedJoltBody.isDynamic())
                        continue;

                    hitPointOnBody = Op.minus(hit.getPhysicsHit().get().position(), grabbedJoltBody.getPosition());
                    hitPointOnBody.rotateInPlace(grabbedJoltBody.getRotation().conjugated());
                }

                final GrabPoint grabPoint;
                final boolean isGrabbable = grabbedBody instanceof IGrabbable;

                if (isGrabbable) {
                    grabPoint = ((IGrabbable) grabbedBody).getRemoteGrabPoint(player, playerBodyPart, hitPointOnBody);
                } else {
                    grabPoint = new GrabPoint(hitPointOnBody, new Quat());
                }

                if (grabPoint == null)
                    continue;

                return new GrabResult(grabbedBody, null);
            }
        }

        return null;
    }

    @Nullable
    public VxConstraint attach(VxBody grabberBody, VxBody grabbedBody, RVec3Arg worldGrabPoint, Player player, PlayerBodyPart playerBodyPart) {
        try (FixedConstraintSettings settings = new FixedConstraintSettings()) {
            settings.setSpace(EConstraintSpace.WorldSpace);
            settings.setPoint1(worldGrabPoint);
            settings.setPoint2(worldGrabPoint); // todo replace with 6dof constraint and move all constraints to separate class

            VxConstraintManager constraintManager = world.getConstraintManager();

            VxConstraint constraint = constraintManager.createConstraint(settings, grabberBody.getPhysicsId(), grabbedBody.getPhysicsId());
            if (constraint != null)
                constraint.setPersistent(false);

            if (grabbedBody instanceof IGrabbable grabbableBody) {
                grabbableBody.onGrab(player, playerBodyPart, true);
            }

            return constraint;
        }
    }

    public GrabResult attachIfWithinReach(Player player, VxBody grabberBody, VxBody grabbedBody, PlayerBodyPart playerBodyPart) {
        PhysicsSystem physicsSystem = world.getPhysicsSystem();
        if (physicsSystem == null) {
            return null;
        }

        ConstBodyLockInterface lockInterface = physicsSystem.getBodyLockInterface();

        int grabberBodyId = grabberBody.getBodyId();
        int grabbedBodyId = grabbedBody.getBodyId();
        RVec3 worldGrabPoint = getBodyPartGrabPointWorld(grabberBody, playerBodyPart);

        List<VxPhysicsIntersector.IntersectShapeResult> intersections = findInstantGrabCandidates(player, grabberBody, worldGrabPoint);

        for (VxPhysicsIntersector.IntersectShapeResult intersection : intersections) {
            if (intersection.bodyId() != grabbedBodyId) {
                continue;
            }

            int[] bodyIds = new int[]{grabberBodyId, grabbedBodyId};
            try (BodyLockMultiWrite lock = new BodyLockMultiWrite(lockInterface, bodyIds)) {
                Body grabberJoltBody = lock.getBody(0);
                Body grabbedJoltBody = lock.getBody(1);

                if (!grabbedJoltBody.isInBroadPhase())
                    continue;

                return tryInstantGrabCandidate(player, grabberBody, grabberJoltBody, grabbedJoltBody, playerBodyPart, intersection, worldGrabPoint);
            }
        }

        return null;
    }

    public void applyPullForce(Player player, VxBody grabberBody, VxBody grabbedBody, PlayerBodyPart playerBodyPart) {
        PhysicsSystem physicsSystem = world.getPhysicsSystem();
        if (physicsSystem == null) return;

        BodyInterface bodyInterface = physicsSystem.getBodyInterfaceNoLock();
        ConstBodyLockInterface lockInterface = physicsSystem.getBodyLockInterface();

        // Lock the body for modification.
        try (BodyLockMultiWrite lock = new BodyLockMultiWrite(lockInterface, grabberBody.getBodyId(), grabbedBody.getBodyId())) {
            Body grabberJoltBody = lock.getBody(0);
            Body grabbedJoltBody = lock.getBody(1);

            bodyInterface.activateBody(grabbedBody.getBodyId());

            // Only apply forces to valid, dynamic (simulated) objects.
            if (grabbedJoltBody.isInBroadPhase() && grabbedJoltBody.isDynamic()) {
                RVec3 worldGrabPoint = getBodyPartGrabPointWorld(grabberJoltBody, playerBodyPart);

                RVec3 bodyPos = grabbedJoltBody.getCenterOfMassPosition();
                RVec3 toTarget = Op.minus(worldGrabPoint, bodyPos);

                double distance = toTarget.length();

                if (distance > REMOTE_GRAB_MAX_DISTANCE) {
                    playerBodyManager.release(player, playerBodyPart.toInteractionHand());
                    return;
                }

                if (distance < 1e-4d) {
                    return;
                }

                Vec3 direction = toTarget.toVec3().normalized();

                Vec3 velocity = grabbedJoltBody.getLinearVelocity();

                float velocityAlongDirection = velocity.dot(direction);

                float forceMagnitude =
                        (float) distance * PULL_MAGNITUDE_STIFFNESS
                                - velocityAlongDirection * PULL_MAGNITUDE_DAMPING;
                float inverseMass = grabbedJoltBody.getMotionProperties().getInverseMass();

                if (inverseMass <= 0.0f) {
                    return;
                }

                forceMagnitude /= inverseMass;
                forceMagnitude = Math.max(PULL_MIN_FORCE, Math.min(PULL_MAX_FORCE, forceMagnitude));

                Vec3 force = Op.star(forceMagnitude, direction);

                grabbedJoltBody.addForce(force);
            }
        }
    }

    public boolean updatePullState(Player player, VxBody grabberBody, VxBody grabbedBody, PlayerBodyPart playerBodyPart) {
        VRPoseHistory historicalPoses = VRAPI.instance().getHistoricalVRPoses(player);
        if (historicalPoses == null)
            return false;

        net.minecraft.world.phys.Vec3 handMovement =
                historicalPoses.netMovement(
                        playerBodyPart.toVRBodyPart(),
                        PULL_PREVIOUS_POSE_TICKS,
                        true
                );

        if (handMovement == null || handMovement.lengthSqr() < 1e-3d) {
            return false;
        }

        // current hand/controller position
        RVec3 handPosition = grabberBody.getTransform().getTranslation();

        // direction from grabbed object to hand
        RVec3 pullDirection = Op.minus(handPosition, grabbedBody.getTransform().getTranslation()).normalized();

        double pullAmount = handMovement.normalize().dot(VxConversions.toMinecraft(pullDirection));

        if (pullAmount > PULL_THRESHOLD) {
            if (grabbedBody instanceof IGrabbable grabbableBody) {
                return grabbableBody.onPull(player, playerBodyPart);
            }

            return true;
        }

        return false;
    }

    /**
     * Releases any object being grabbed by the specified player's hand.
     * <p>
     * This removes the grab constraint, allowing the grabbed body to move freely again.
     * </p>
     *
     * @param playerBodyPartData the player's body part data
     * @return {@code true} if the body was successfully released, {@code false} otherwise
     */
    public boolean release(Player player, VxBody grabberBody, VxBody grabbedBody, PlayerBodyPart playerBodyPart, PlayerBodyPartData playerBodyPartData) {
        if (grabberBody == null || grabbedBody == null) {
            return true; // bodies no longer exists, returning true, so PlayerBodyManager remove grab data from datastore
        }

        if (grabbedBody instanceof IGrabbable grabbableBody) {
            boolean isReleaseAllowed = grabbableBody.canRelease(player, playerBodyPart);
            if (!isReleaseAllowed) {
                return false;
            }
        }

        if (world.getBodyPairIgnoreHandler().isPairIgnored(grabberBody.getBodyId(), grabbedBody.getBodyId())) {
            world.getBodyPairIgnoreHandler().removeIgnorePair(grabberBody.getBodyId(), grabbedBody.getBodyId()); // todo: remove after distance from bodies are big enough to not collide
        }

        boolean isGrabConstraint = playerBodyPartData.grabData().constraintId() != null;

        if (isGrabConstraint)
            world.getConstraintManager().removeConstraint(playerBodyPartData.grabData().constraintId());

        if (grabbedBody instanceof IGrabbable grabbableBody) {
            grabbableBody.onRelease(player, playerBodyPart, isGrabConstraint);
        }

        return true;
    }

    /**
     * Checks is any body around player can be grabbed.
     * Used to predict if player can grab body in {@link net.timtaran.interactivemc.mixin.bridge.vivecraft.KeyMappingHandlingMixin} to prevent other grip binds to be called.
     *
     * @return is any body around player can be grabbed.
     */
    @Environment(EnvType.CLIENT)
    public static boolean canGrabClient(InteractionHand interactionHand) {
        if (ClientPlayerBodyDataStore.currentPose == null)
            return false;

        Vector3fc localGrabOffset = PlayerBodyPart.fromInteractionHand(interactionHand).getGrabPointVec3f();
        VRBodyPartData bodyPartData = ClientPlayerBodyDataStore.currentPose.getBodyPartData(VRBodyPart.fromInteractionHand(interactionHand));
        if (bodyPartData != null) {
            Quaternionf targetRot = new Quaternionf(bodyPartData.getRotation());

            Vector3f controllerPos = bodyPartData.getPos().toVector3f();
            Vector3f offset = new Vector3f(localGrabOffset);
            targetRot.transform(offset);

            Vector3f targetPos = controllerPos.add(offset);

            for (Integer bodyIndex : VelthoricClientUtils.bodiesAround(targetPos.x, targetPos.y, targetPos.z, GRAB_RADIUS)) {
                if (!ClientPlayerBodyDataStore.playerControlledBodies.contains(bodyIndex)) {
                    return true;
                }
            }
        }

        return false;
    }

    @Nullable
    private RVec3 getBodyPartGrabPointWorld(ConstBodyLockInterface lockInterface, int bodyId, PlayerBodyPart playerBodyPart) {
        try (BodyLockRead lock = new BodyLockRead(lockInterface, bodyId)) {
            ConstBody body = lock.getBody();
            if (body == null)
                return null;

            return getBodyPartGrabPointWorld(body, playerBodyPart);
        }
    }

    private RVec3 getBodyPartGrabPointWorld(VxBody body, PlayerBodyPart playerBodyPart) {
        return getBodyPartGrabPointWorld(body.getTransform(), playerBodyPart);
    }

    private RVec3 getBodyPartGrabPointWorld(VxTransform transform, PlayerBodyPart playerBodyPart) {
        return PlayerBodyPartTransforms.getGrabPointRotatedWorld(transform.getTranslation(), transform.getRotation(), playerBodyPart);
    }

    private RVec3 getBodyPartGrabPointWorld(ConstBody body, PlayerBodyPart playerBodyPart) {
        return PlayerBodyPartTransforms.getGrabPointRotatedWorld(body.getPosition(), body.getRotation(), playerBodyPart);
    }

    @Nullable
    private RVec3 getBodyPartGrabPointWorld(VRPose pose, PlayerBodyPart playerBodyPart) {
        VRBodyPartData bodyPartData = pose.getBodyPartData(playerBodyPart.toVRBodyPart());
        if (bodyPartData == null)
            return null;

        return getBodyPartGrabPointWorld(bodyPartData, playerBodyPart);
    }

    private RVec3 getBodyPartGrabPointWorld(VRBodyPartData bodyPartData, PlayerBodyPart playerBodyPart) {
        RVec3 position = VxConversions.toJolt(bodyPartData.getPos());
        Quat rotation = VxConversions.toJolt(bodyPartData.getRotation());
        return Op.plus(
                PlayerBodyPartTransforms.getTrackingOffsetLocal(rotation, playerBodyPart),
                PlayerBodyPartTransforms.getGrabPointRotatedWorld(position, rotation, playerBodyPart)
        );
    }

    private static Vec3 quatToForward(float x, float y, float z, float w) {
        // todo put in separate class
        // forward vector (0, 0, 1) rotated by quaternion
        float dx = 2 * (x * z + w * y);
        float dy = 2 * (y * z - w * x);
        float dz = 1 - 2 * (x * x + y * y);

        // normalize to be safe
        float len = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
        return new Vec3(dx / len, dy / len, dz / len);
    }
}
