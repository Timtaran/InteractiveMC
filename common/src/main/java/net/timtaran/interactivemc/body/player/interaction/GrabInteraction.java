/*
 * This file is part of InteractiveMC.
 * Licensed under LGPL 3.0.
 */
package net.timtaran.interactivemc.body.player.interaction;


import com.github.stephengold.joltjni.*;
import com.github.stephengold.joltjni.enumerate.EConstraintSpace;
import com.github.stephengold.joltjni.enumerate.EMotionType;
import com.github.stephengold.joltjni.operator.Op;
import com.github.stephengold.joltjni.readonly.ConstBodyLockInterface;
import com.github.stephengold.joltjni.readonly.RVec3Arg;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.timtaran.interactivemc.body.player.ContactListenerManager;
import net.timtaran.interactivemc.body.player.PlayerBodyManager;
import net.timtaran.interactivemc.body.player.PlayerBodyPart;
import net.timtaran.interactivemc.body.player.PlayerBodyPartData;
import net.timtaran.interactivemc.body.player.store.ClientPlayerBodyDataStore;
import net.timtaran.interactivemc.body.player.store.PlayerBodyDataStore;
import net.timtaran.interactivemc.init.InteractiveMC;
import net.timtaran.interactivemc.util.velthoric.VelthoricClientUtils;
import net.xmx.velthoric.core.body.VxBody;
import net.xmx.velthoric.core.constraint.VxConstraint;
import net.xmx.velthoric.core.constraint.manager.VxConstraintManager;
import net.xmx.velthoric.core.intersection.VxPhysicsIntersector;
import net.xmx.velthoric.core.intersection.raycast.VxHitResult;
import net.xmx.velthoric.core.intersection.raycast.VxRaycaster;
import net.xmx.velthoric.core.physics.VxJoltBridge;
import net.xmx.velthoric.core.physics.VxPhysicsLayers;
import net.xmx.velthoric.core.physics.world.VxPhysicsWorld;
import net.xmx.velthoric.math.VxConversions;
import net.xmx.velthoric.math.VxTransform;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;
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
 * </p>
 *
 * @author timtaran
 */
public class GrabInteraction {
    private static final float GRAB_RADIUS = 0.15f;
    private static final Vec3 GRAB_SHAPE_SCALE = new Vec3(1f, 1f, 1f);

    /**
     * How many ticks back the previous pose will be taken when calculating velocity changes for pulling body.
     */
    private static final int PULL_PREVIOUS_POSE_TICKS = 2;
    private static final double PULL_THRESHOLD = 0.1;

    /**
     * How long the pull should take to reach the target velocity (in seconds per meter).
     */
    private static final float PULL_TIME_MULTIPLIER = 0.1f;

    private static final float PULL_MAGNITUDE_MULTIPLIER = 1000f;

    private static final GrabResult EMPTY_GRAB_RESULT = new GrabResult(null, null);

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
    public record GrabResult(@Nullable VxBody grabbedBody, @Nullable VxConstraint grabConstraint) {
    }

    /**
     * Attempts to grab an object using the specified player's hand.
     * <p>
     * This method performs a sphere cast from the grab point and tries to grab the closest
     * non-player body within the grab radius.
     * </p>
     *
     * @param player         the player attempting to grab
     * @param grabberBody    the body that is grabbing
     * @param playerBodyPart the player's body part
     * @return the body that was grabbed, or null if no body was grabbed
     */
    public GrabResult grab(Player player, VxBody grabberBody, PlayerBodyPart playerBodyPart) {
        RVec3 worldGrabPoint = getBodyPartWorldGrabPoint(grabberBody, playerBodyPart);
        if (worldGrabPoint == null) return EMPTY_GRAB_RESULT;

        PhysicsSystem physicsSystem = world.getPhysicsSystem();
        if (physicsSystem == null) return EMPTY_GRAB_RESULT;

        ConstBodyLockInterface lockInterface = physicsSystem.getBodyLockInterface();

        // Performing instant grab checks (body will be teleported and attached
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

            System.out.println(worldGrabPoint);
            System.out.println(Op.minus(worldGrabPoint, new RVec3(VxConversions.toJolt(player.position()))));

            List<VxPhysicsIntersector.IntersectShapeResult> intersections = VxPhysicsIntersector.narrowIntersectShape(world, shape, GRAB_SHAPE_SCALE, comTransform, base, bplFilter, olFilter, bodyFilter);

            intersections.sort(Comparator.comparingDouble(result -> { // sort by closest intersection point to base.
                Vec3 p = result.bodyContactPoint();

                double dx = p.getX() - vxTransform.getTranslation().x();
                double dy = p.getY() - vxTransform.getTranslation().y();
                double dz = p.getZ() - vxTransform.getTranslation().z();

                return dx * dx + dy * dy + dz * dz;
            }));

            for (VxPhysicsIntersector.IntersectShapeResult intersection : intersections) {
                if (
                        !PlayerBodyDataStore.playersJoltBodies.get(player.getUUID()).contains(intersection.bodyId())

                ) {
                    // Lock the body for modification.
                    try (BodyLockWrite lock = new BodyLockWrite(lockInterface, intersection.bodyId())) {
                        if (!lock.succeededAndIsInBroadPhase())
                            continue;

                        Body grabbedJoltBody = lock.getBody();

                        if (grabbedJoltBody.getObjectLayer() != VxPhysicsLayers.TERRAIN) {
                            VxBody grabbedBody = world.getBodyManager().getByJoltBodyId(intersection.bodyId());

                            if (grabbedBody == null) {
                                InteractiveMC.LOGGER.warn("grabbedBody is null for body ID: {}", intersection.bodyId());
                                continue;
                            }

                            RVec3 worldGrabPointOnBody;

                            if (grabbedJoltBody.getMotionType() == EMotionType.Dynamic) {
                                VxTransform grabbedBodyTransform = grabbedBody.getTransform();
                                // Calculate a new world-space position for the body so that the local contact point
                                // aligns exactly with the desired grab point in world space.
                                worldGrabPointOnBody = new RVec3(
                                        worldGrabPoint.xx() - (intersection.bodyContactPoint().getX() - grabbedBodyTransform.getTranslation().xx()),
                                        worldGrabPoint.yy() - (intersection.bodyContactPoint().getY() - grabbedBodyTransform.getTranslation().yy()),
                                        worldGrabPoint.zz() - (intersection.bodyContactPoint().getZ() - grabbedBodyTransform.getTranslation().zz())
                                );

                                grabbedJoltBody.setPositionAndRotationInternal(worldGrabPointOnBody, grabbedBodyTransform.getRotation());
                            } else {
                                // todo move grabber body if grabbed body not meant to be moved by physics
                                continue;
                            }

                            VxConstraint constraint = attach(grabberBody, grabbedBody, worldGrabPoint);

                            ContactListenerManager.hashMap.add(new ContactListenerManager.Reject(grabberBody.getBodyId(), grabbedBody.getBodyId()));

                            return new GrabResult(grabbedBody, constraint);
                        } // todo add terrain grab after implementing client-side prediction
                    }
                }
            }
        }

        // No body found around grab point, performing raycast to add body to pull
        try (ObjectLayerFilter olFilter = new ObjectLayerFilter() {
            @Override
            public boolean shouldCollide(int objectLayer) {
                return (objectLayer != VxPhysicsLayers.NON_MOVING && objectLayer != VxPhysicsLayers.TERRAIN);
            }
        }) {
            VRBodyPartData bodyPartData = PlayerBodyDataStore.vrPoses.get(player.getUUID()).getBodyPartData(playerBodyPart.toVRBodyPart());
            Vec3 direction = VxConversions.toJolt(bodyPartData.getDir()).toVec3().normalized();
            List<VxHitResult> raycastResult = VxRaycaster.raycastAll(world, worldGrabPoint, direction, 5);

            System.out.println(worldGrabPoint + " " + direction); // todo remove and replace with visual indication

            System.out.println("111 " + System.currentTimeMillis());
            for (VxHitResult hit : raycastResult) {
                int grabbedBodyId = hit.getPhysicsHit().get().bodyId();

                if (hit.getPhysicsHit().isEmpty() || PlayerBodyDataStore.playersJoltBodies.get(player.getUUID()).contains(grabbedBodyId))
                    continue;

                VxBody grabbedBody = world.getBodyManager().getByJoltBodyId(grabbedBodyId);

                if (grabbedBody == null)
                    continue;

                if (!VxJoltBridge.INSTANCE.getJoltBody(world, grabbedBodyId).isDynamic())
                    continue;

                RVec3 grabPointOnBody;

                try (BodyLockWrite lock = new BodyLockWrite(lockInterface, grabbedBodyId)) {
                    if (!lock.succeededAndIsInBroadPhase())
                        continue;

                    Body grabbedJoltBody = lock.getBody();

                    grabPointOnBody = Op.minus(hit.getPhysicsHit().get().position(), grabbedJoltBody.getCenterOfMassPosition());
                }

                ContactListenerManager.notifyList.add(
                        new ContactListenerManager.Notify(
                                grabberBody.getBodyId(),
                                hit.getPhysicsHit().get().bodyId(),
                                (body1, body2) ->
                                        world.execute(() -> {
                                            System.out.println(ContactListenerManager.notifyList.removeIf((reject -> reject.body1() == body1 && reject.body2() == body2)));

                                            RVec3 worldRemoteGrabPoint = getBodyPartWorldGrabPoint(grabberBody, playerBodyPart);

                                            try (BodyLockWrite lock = new BodyLockWrite(lockInterface, body2)) {
                                                if (!lock.succeededAndIsInBroadPhase())
                                                    return;

                                                System.out.println("getting body to attach");
                                                Body body = lock.getBody();
                                                System.out.println("body: " + body);

                                                // Calculate a new world-space position for the body so that the local contact point
                                                // aligns exactly with the desired grab point in world space.
                                                RVec3 worldGrabPointOnBody = new RVec3(
                                                        worldRemoteGrabPoint.xx() - grabPointOnBody.xx(),
                                                        worldRemoteGrabPoint.yy() - grabPointOnBody.yy(),
                                                        worldRemoteGrabPoint.zz() - grabPointOnBody.zz()
                                                );
                                                System.out.println("world grab point on body: " + worldGrabPointOnBody);

                                                body.setPositionAndRotationInternal(worldGrabPointOnBody, body.getRotation());
                                            }

                                            // todo possibly replace with hand calculations instead of physics because bug
                                            VxConstraint constraint = attach(grabberBody, world.getBodyManager().getByJoltBodyId(body2), worldRemoteGrabPoint);
                                            InteractionHand interactionHand = playerBodyPart.toInteractionHand();
                                            if (interactionHand == null)
                                                return;

                                            world.getLevel().getServer().execute(() -> playerBodyManager.processGrabResult(player, playerBodyPart, new GrabResult(grabbedBody, constraint)));
                                        })
                        )
                );

                System.out.println(grabbedBody.getClass().getSimpleName());
                return new GrabResult(grabbedBody, null);
            }
        }

        return EMPTY_GRAB_RESULT;
    }

    public VxConstraint attach(VxBody grabberBody, VxBody grabbedBody, RVec3Arg worldGrabPoint) {
        try (FixedConstraintSettings settings = new FixedConstraintSettings()) {
            settings.setSpace(EConstraintSpace.WorldSpace);
            settings.setPoint1(worldGrabPoint);
            settings.setPoint2(worldGrabPoint); // todo replace with 6dof constraint and move all constraints to separate class

            // todo rework

            VxConstraintManager constraintManager = world.getConstraintManager();

            VxConstraint constraint = constraintManager.createConstraint(settings, grabberBody.getPhysicsId(), grabbedBody.getPhysicsId());
            if (constraint != null)
                constraint.setPersistent(false);

            return constraint;
        }
    }

    public void pull(Player player, VxBody grabberBody, VxBody grabbedBody, PlayerBodyPart playerBodyPart) {
        VRPose currentPose = VRAPI.instance().getVRPose(player);
        if (currentPose == null)
            return;

        VRPoseHistory historicalPoses = VRAPI.instance().getHistoricalVRPoses(player);
        if (historicalPoses == null)
            return;

        VRPose previousPose = historicalPoses.getHistoricalData(PULL_PREVIOUS_POSE_TICKS);
        if (previousPose == null)
            return;

        // todo possibly replace with VRPoseHistory#netMovement or VRPoseHistory#averagePosition relative
        VRBodyPartData currentBodyPartData = currentPose.getBodyPartData(playerBodyPart.toVRBodyPart());
        VRBodyPartData previousBodyPartData = previousPose.getBodyPartData(playerBodyPart.toVRBodyPart());
        VRBodyPartData headData = currentPose.getHead();

        if (currentBodyPartData == null || previousBodyPartData == null || headData == null)
            return;

        net.minecraft.world.phys.Vec3 handMovement = currentBodyPartData.getPos().subtract(previousBodyPartData.getPos());
        net.minecraft.world.phys.Vec3 handToBody = headData.getPos().subtract(currentBodyPartData.getPos()).normalize();
        double pullAmount = handMovement.dot(handToBody); // todo remove pulling relative to body

        if (pullAmount > PULL_THRESHOLD) {
            PhysicsSystem physicsSystem = world.getPhysicsSystem();
            if (physicsSystem == null) return;

            BodyInterface bodyInterface = physicsSystem.getBodyInterface();
            ConstBodyLockInterface lockInterface = physicsSystem.getBodyLockInterface();

            System.out.println("1221");
            System.out.println(grabbedBody.getClass().getSimpleName());
            bodyInterface.activateBody(grabbedBody.getBodyId());
            System.out.println(VxJoltBridge.INSTANCE.getJoltBody(world, grabbedBody.getBodyId()).getPosition());
            System.out.println(world.getBodyManager().getByJoltBodyId(grabbedBody.getBodyId()).getClass().getSimpleName());

            // Lock the body for modification.
            try (BodyLockWrite lock = new BodyLockWrite(lockInterface, grabbedBody.getBodyId())) {
                // Only apply forces to valid, dynamic (simulated) objects.
                if (lock.succeededAndIsInBroadPhase() && lock.getBody().isDynamic()) {
                    RVec3 worldGrabPoint1 = getBodyPartWorldGrabPoint(grabberBody, playerBodyPart);
                    Vector3f worldGrabPointMinecraft = currentBodyPartData.getPos().toVector3f();
                    Vector3f offset = playerBodyPart.getLocalGrabPointVec3f();

                    Quaternionf targetRot = new Quaternionf(currentBodyPartData.getRotation());
                    targetRot.transform(offset);

                    Vector3f targetPos = worldGrabPointMinecraft.add(offset);

                    RVec3 worldGrabPoint = VxConversions.toJolt(targetPos).toRVec3();
                    // RVec3 worldGrabPoint = worldGrabPoint1;

                    System.out.println("Difference between transform and controller calculation is: " + Op.minus(worldGrabPoint, worldGrabPoint1));

                    System.out.println("world grab point: " + worldGrabPoint);

//                    worldGrabPoint = grabbedBody.getTransform().getTranslation();
//                    System.out.println(worldGrabPoint);
//                    System.out.println(Op.minus(worldGrabPoint, new RVec3(VxConversions.toJolt(player.position()))));


                    Body body = lock.getBody();
                    RVec3 bodyPos = body.getCenterOfMassPosition();
                    RVec3 vectorToTarget = Op.minus(worldGrabPoint, bodyPos);

                    double distance = vectorToTarget.length();
                    System.out.println(distance);
                    System.out.println(Op.minus(bodyPos, worldGrabPoint).length());
                    if (distance > 1e-6) {
                        float time = (float) distance * PULL_TIME_MULTIPLIER;
                        float MIN_TIME = 0.05f;
                        float MAX_TIME = 2.0f;
                        time = Math.max(MIN_TIME, Math.min(MAX_TIME, time));

                        Vec3 direction = vectorToTarget.toVec3().normalized();
                        //direction.rotateInPlace(body.getRotation().conjugated());
                        System.out.println(direction);
                        float magnitude = (1.0f / time) * (float) distance * PULL_MAGNITUDE_MULTIPLIER;

                        Vec3 impulse = Op.star(magnitude, direction);
                        System.out.println(impulse);
                        body.addImpulse(impulse);
                    }
                }
            }
        }
    }

    /**
     * Releases any object being grabbed by the specified player's hand.
     * <p>
     * This removes the grab constraint, allowing the grabbed body to move freely again.
     * </p>
     *
     * @param playerBodyPartData the player's body part data
     */
    public void release(PlayerBodyPartData playerBodyPartData) {
//        ContactListenerManager.notifyOnRemovalList.add(new ContactListenerManager.Notify(
//                world.getBodyManager().getVxBody(playerBodyPartData.bodyPartId()).getBodyId(),
//                world.getBodyManager().getVxBody(playerBodyPartData.grabbedBodyId()).getBodyId(),
//                (bodyid1, bodyid2) -> {
//                    System.out.println("notify" + bodyid1 + bodyid2);
//                    ContactListenerManager.hashMap.removeIf((reject ->
//                            reject.body2() == world.getBodyManager().getVxBody(playerBodyPartData.grabbedBodyId()).getBodyId() &&
//                                    reject.body1() == world.getBodyManager().getVxBody(playerBodyPartData.bodyPartId()).getBodyId()
//                    ));
//                }
//        ));

        ContactListenerManager.notifyList.removeIf((reject ->
                reject.body2() == world.getBodyManager().getVxBody(playerBodyPartData.grabbedBodyId()).getBodyId() &&
                        reject.body1() == world.getBodyManager().getVxBody(playerBodyPartData.bodyPartId()).getBodyId()
        ));

        ContactListenerManager.hashMap.removeIf((reject ->
                reject.body2() == world.getBodyManager().getVxBody(playerBodyPartData.grabbedBodyId()).getBodyId() &&
                        reject.body1() == world.getBodyManager().getVxBody(playerBodyPartData.bodyPartId()).getBodyId()
        ));

        if (playerBodyPartData.grabConstraintId() != null)
            world.getConstraintManager().removeConstraint(playerBodyPartData.grabConstraintId());
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

        Vector3f localGrabOffset = PlayerBodyPart.fromInteractionHand(interactionHand).getLocalGrabPointVec3f();
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

    private RVec3 getBodyPartWorldGrabPoint(VxBody body, PlayerBodyPart playerBodyPart) {
        VxTransform vxTransform = body.getTransform();
        // todo make static after tests

        PhysicsSystem physicsSystem = world.getPhysicsSystem();
        if (physicsSystem == null) return null;

//        BodyInterface bodyInterface = physicsSystem.getBodyInterface();
//        ConstBodyLockInterface lockInterface = physicsSystem.getBodyLockInterface();
//
//        RVec3 joltBodyPos = new RVec3();
//        Quat joltBodyRot = new Quat();
//
//        try (BodyLockWrite lock = new BodyLockWrite(lockInterface, body.getBodyId())) {
//            Body joltBody = lock.getBody();
//            joltBodyPos = joltBody.getCenterOfMassPosition();
//            joltBodyRot = joltBody.getRotation();
//        }

        RVec3 worldGrabPoint = vxTransform.getTranslation();
        //System.out.println("Center of mass: " + joltBodyPos);
        //System.out.println("Center of mass and transform difference: " + Op.minus(joltBodyPos, worldGrabPoint));
        RVec3 localGrabPoint = playerBodyPart.getLocalGrabPoint();

        // Rotate the local grab point by the body's rotation to get the correct world offset.
        RVec3 localGrabPointRotated = new RVec3(localGrabPoint);
        localGrabPointRotated.rotateInPlace(getRotation(vxTransform));
        System.out.println("vxBody rotation: " + vxTransform.getRotation());
        //System.out.println("body rotation: " + joltBodyRot);
        System.out.println("rotated grab point: " + localGrabPointRotated);

        // Add the rotated local grab point to the body's position to get the final grab point in world space.
        worldGrabPoint.addInPlace(localGrabPointRotated.xx(), localGrabPointRotated.yy(), localGrabPointRotated.zz());

        System.out.println("grab point: " + worldGrabPoint);

        return worldGrabPoint;
    }

    private static Quat getRotation(VxTransform vxTransform) {
        return vxTransform.getRotation();
    }
}
