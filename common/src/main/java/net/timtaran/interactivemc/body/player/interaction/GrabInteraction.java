/*
 * This file is part of InteractiveMC.
 * Licensed under LGPL 3.0.
 */
package net.timtaran.interactivemc.body.player.interaction;


import com.github.stephengold.joltjni.*;
import com.github.stephengold.joltjni.enumerate.EConstraintSpace;
import com.github.stephengold.joltjni.operator.Op;
import com.github.stephengold.joltjni.readonly.*;
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
    private static final int PULL_PREVIOUS_POSE_TICKS = 3;
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
        PhysicsSystem physicsSystem = world.getPhysicsSystem();
        if (physicsSystem == null) return EMPTY_GRAB_RESULT;

        ConstBodyLockInterface lockInterface = physicsSystem.getBodyLockInterface();

        RVec3 worldGrabPoint;

        try (BodyLockRead lock = new BodyLockRead(lockInterface, grabberBody.getBodyId())) {
            ConstBody body = lock.getBody();
            if (body == null)
                return EMPTY_GRAB_RESULT;

            worldGrabPoint = getBodyPartGrabPointWorld(body, playerBodyPart);
        }

        System.out.println("worldGrabPoint_grab: " + worldGrabPoint);

        // Performing instant grab checks (body will be teleported and attached)
        GrabResult instantGrabResult = tryInstantGrab(lockInterface, player, grabberBody, worldGrabPoint, playerBodyPart);
        if (instantGrabResult != null)
            return instantGrabResult;

        // No bodies available for grabbing at the point, performing raycast to find one to pull
        return tryRemoteGrab(lockInterface, player, grabberBody, worldGrabPoint, playerBodyPart);
    }

    @Nullable
    private GrabResult tryInstantGrab(ConstBodyLockInterface lockInterface, Player player, VxBody grabberBody, RVec3 worldGrabPoint, PlayerBodyPart bodyPart) {
        try (ObjectLayerFilter olFilter = new ObjectLayerFilter() {
            @Override
            public boolean shouldCollide(int objectLayer) {
                return objectLayer != VxPhysicsLayers.NON_MOVING;
            }
        };
             BroadPhaseLayerFilter bplFilter = new BroadPhaseLayerFilter();
             BodyFilter bodyFilter = new BodyFilter(); // runtime checks works really strange so we will check body ids below
             SphereShape shape = new SphereShape(GRAB_RADIUS)) {

            System.out.println("instant grab");

            RVec3Arg base = new RVec3(0.0f, 0.0f, 0.0f);

            VxTransform vxTransform = grabberBody.getTransform();
            RMat44 comTransform = new VxTransform(worldGrabPoint, vxTransform.getRotation()).toRMat44();

            List<VxPhysicsIntersector.IntersectShapeResult> intersections = VxPhysicsIntersector.narrowIntersectShape(world, shape, GRAB_SHAPE_SCALE, comTransform, base, bplFilter, olFilter, bodyFilter);

            intersections.removeIf(intersection ->
                    PlayerBodyDataStore.isPlayerControlledBody(player.getUUID(), intersection.bodyId())
            );
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

                    if (grabbedJoltBody.getObjectLayer() != VxPhysicsLayers.TERRAIN) { // Velthoric doesn't have VxBody for terrain, so we separate terrain and other bodies logic here
                        VxBody grabbedBody = world.getBodyManager().getByJoltBodyId(intersection.bodyId());

                        if (grabbedBody == null) {
                            InteractiveMC.LOGGER.warn("grabbedBody is null for body ID: {}", intersection.bodyId());
                            continue;
                        }

                        // calculating intersection point and body center offset
                        RVec3 bodyContactPointOffset = Op.minus(intersection.bodyContactPoint().toRVec3(), grabbedJoltBody.getPosition()); // todo: move before physics layer check when terrain grabbing is implemented
                        if (grabbedBody instanceof IGrabbable grabbableBody) {
                            RVec3 bodyContactPointLocal = new RVec3(bodyContactPointOffset);
                            bodyContactPointLocal.rotateInPlace(grabbedJoltBody.getRotation().conjugated());

                            bodyContactPointLocal = grabbableBody.getGrabPoint(bodyContactPointLocal);

                            if (bodyContactPointLocal == null)
                                continue;

                            bodyContactPointOffset = new RVec3(bodyContactPointLocal);
                            bodyContactPointOffset.rotateInPlace(grabbedJoltBody.getRotation());
                        }

                        VxConstraint constraint;

                        // Calculate a new world-space position for the body so that the local contact point
                        // aligns exactly with the desired grab point in world space.
                        if (grabbedJoltBody.isDynamic() && !PlayerBodyDataStore.isBodyGrabbed(grabberJoltBody.getId())) {
                            // move body to grabber
                            grabbedJoltBody.setPositionAndRotationInternal(
                                    Op.minus(worldGrabPoint, bodyContactPointOffset),
                                    grabbedJoltBody.getRotation()
                            );
                            constraint = attach(grabberBody, grabbedBody, worldGrabPoint);
                            System.out.println("Attaching " + grabbedBody.getClass().getSimpleName() + " to " + grabberBody.getClass().getSimpleName() + " at " + worldGrabPoint);
                        } else {
                            // move grabber to body
                            RVec3 bodyContactPoint = Op.plus(grabbedJoltBody.getPosition(), bodyContactPointOffset);

                            grabberJoltBody.setPositionAndRotationInternal(
                                    Op.minus(
                                            bodyContactPoint,
                                            PlayerBodyPartTransforms.getGrabPointRotatedLocal(grabberJoltBody.getRotation(), bodyPart)
                                    ),
                                    grabberJoltBody.getRotation()
                            );
                            constraint = attach(grabberBody, grabbedBody, bodyContactPoint);
                            System.out.println("Attaching " + grabbedBody.getClass().getSimpleName() + " to " + grabberBody.getClass().getSimpleName() + " at " + bodyContactPoint);
                        }

                        ContactListenerManager.hashMap.add(new ContactListenerManager.Reject(grabberBody.getBodyId(), grabbedBody.getBodyId()));

                        return new GrabResult(grabbedBody, constraint);

                    } // todo add terrain grab after implementing client-side prediction
                }
            }
        }

        return null;
    }

    private GrabResult tryRemoteGrab(ConstBodyLockInterface lockInterface, Player player, VxBody grabberBody, RVec3 worldGrabPoint, PlayerBodyPart playerBodyPart) {  // todo replace with RVec3Arg
        try (ObjectLayerFilter olFilter = new ObjectLayerFilter() {
            @Override
            public boolean shouldCollide(int objectLayer) {
                return (objectLayer != VxPhysicsLayers.NON_MOVING && objectLayer != VxPhysicsLayers.TERRAIN);
            }
        }) {
            VRBodyPartData bodyPartData = PlayerBodyDataStore.vrPoses.get(player.getUUID()).getBodyPartData(playerBodyPart.toVRBodyPart());
            Vec3 direction = VxConversions.toJolt(bodyPartData.getDir()).toVec3().normalized();
            Quat bodyRotation = grabberBody.getTransform().getRotation().normalized();
            System.out.println("VR Pose direction " + direction + "; Direction based on rotation: " + quatToForward(bodyRotation.getX(), bodyRotation.getY(), bodyRotation.getZ(), bodyRotation.getW()));
            List<VxHitResult> raycastResult = VxRaycaster.raycastAll(world, worldGrabPoint, direction, 5, olFilter);

            System.out.println(worldGrabPoint + " " + direction); // todo remove and replace with visual indication

            System.out.println("111 " + System.currentTimeMillis());
            for (VxHitResult hit : raycastResult) {
                int grabbedBodyId = hit.getPhysicsHit().get().bodyId();

                if (hit.getPhysicsHit().isEmpty() || PlayerBodyDataStore.isPlayerControlledBody(player.getUUID(), grabbedBodyId))
                    continue;

                VxBody grabbedBody = world.getBodyManager().getByJoltBodyId(grabbedBodyId);

                if (grabbedBody == null)
                    continue;

                RVec3 grabPointOnBody;

                try (BodyLockWrite lock = new BodyLockWrite(lockInterface, grabbedBodyId)) {
                    if (!lock.succeededAndIsInBroadPhase())
                        continue;

                    Body grabbedJoltBody = lock.getBody();

                    if (!grabbedJoltBody.isDynamic())
                        continue;

                    grabPointOnBody = Op.minus(hit.getPhysicsHit().get().position(), grabbedJoltBody.getCenterOfMassPosition());
                }

                ContactListenerManager.notifyList.add(
                        new ContactListenerManager.Notify(
                                grabberBody.getBodyId(),
                                hit.getPhysicsHit().get().bodyId(),
                                (body1, body2) ->
                                        // Modifying bodies during physics update is not allowed
                                        world.execute(() -> {
                                            // Using non-locking body interface because
                                            ContactListenerManager.notifyList.removeIf((reject -> reject.body1() == body1 && reject.body2() == body2));

                                            // todo: possibly replace with hand calculations instead of physics because bug
                                            RVec3 worldRemoteGrabPoint = getBodyPartGrabPointWorld(lockInterface, grabbedBodyId, playerBodyPart);
                                            if (worldRemoteGrabPoint == null)
                                                return;

                                            try (BodyLockWrite lock = new BodyLockWrite(lockInterface, body2)) {
                                                if (!lock.succeededAndIsInBroadPhase())
                                                    return;

                                                System.out.println("getting body to attach");
                                                Body body = lock.getBody();
                                                System.out.println("body: " + body);

                                                // Calculate a new world-space position for the body so that the local contact point
                                                // aligns exactly with the desired grab point in world space.
                                                body.setPositionAndRotationInternal(Op.minus(worldRemoteGrabPoint, grabPointOnBody), body.getRotation());
                                            }

                                            VxConstraint constraint = attach(grabberBody, world.getBodyManager().getByJoltBodyId(body2), worldRemoteGrabPoint);

                                            world.getLevel().getServer().execute(() -> playerBodyManager.processGrabResult(player, playerBodyPart, new GrabResult(grabbedBody, constraint)));
                                        })
                        ));

                System.out.println(grabbedBody.getClass().getSimpleName());
                return new GrabResult(grabbedBody, null);
            }
        }

        return new GrabResult(null, null);
    }

    @Nullable
    public VxConstraint attach(VxBody grabberBody, VxBody grabbedBody, RVec3Arg worldGrabPoint) {
        if (worldGrabPoint == null)
            return null;
        try (FixedConstraintSettings settings = new FixedConstraintSettings()) {
            settings.setSpace(EConstraintSpace.WorldSpace);
            settings.setPoint1(worldGrabPoint);
            settings.setPoint2(worldGrabPoint); // todo replace with 6dof constraint and move all constraints to separate class

            VxConstraintManager constraintManager = world.getConstraintManager();

            VxConstraint constraint = constraintManager.createConstraint(settings, grabberBody.getPhysicsId(), grabbedBody.getPhysicsId());
            if (constraint != null)
                constraint.setPersistent(false);

            return constraint;
        }
    }

    public void pull(Player player, VxBody grabberBody, VxBody grabbedBody, PlayerBodyPart playerBodyPart) {
        VRPoseHistory historicalPoses = VRAPI.instance().getHistoricalVRPoses(player);
        if (historicalPoses == null)
            return;

        net.minecraft.world.phys.Vec3 lookDirection = historicalPoses.getHistoricalData(0).getHead().getDir();
        net.minecraft.world.phys.Vec3 handMovement = historicalPoses.netMovement(playerBodyPart.toVRBodyPart(), PULL_PREVIOUS_POSE_TICKS, true);

        if (handMovement == null) {
            return;
        }

        // todo: remove after fixing getBodyPartWorldGrabPoint method
        // not doing null check because body part data can't be null because handMovement is not null
        net.minecraft.world.phys.Vec3 bodyPartPosition = historicalPoses.getHistoricalData(0).getBodyPartData(playerBodyPart.toVRBodyPart()).getPos();

        double pullAmount = handMovement.dot(lookDirection);

        if (pullAmount > PULL_THRESHOLD) {
            PhysicsSystem physicsSystem = world.getPhysicsSystem();
            if (physicsSystem == null) return;

            BodyInterface bodyInterface = physicsSystem.getBodyInterface();
            ConstBodyLockInterface lockInterface = physicsSystem.getBodyLockInterface();

            System.out.println("1221");
            System.out.println(grabbedBody.getClass().getSimpleName());
            bodyInterface.activateBody(grabbedBody.getBodyId());
            System.out.println(VxJoltBridge.INSTANCE.getJoltBody(world, grabbedBody.getBodyId()).getPosition());
            System.out.println(grabbedBody.getClass().getSimpleName());

            // Lock the body for modification.
            try (BodyLockMultiWrite lock = new BodyLockMultiWrite(lockInterface, grabberBody.getBodyId(), grabbedBody.getBodyId())) {
                Body grabberJoltBody = lock.getBody(0);
                Body grabbedJoltBody = lock.getBody(1);

                // Only apply forces to valid, dynamic (simulated) objects.
                if (grabbedJoltBody.isInBroadPhase() && grabbedJoltBody.isDynamic()) {
                    RVec3 worldGrabPoint1 = getBodyPartGrabPointWorld(grabberJoltBody, playerBodyPart);

                    RVec3 worldGrabPoint = VxConversions.toJolt(bodyPartPosition.toVector3f()).toRVec3();
                    System.out.println("Difference between transform and controller calculation is: " + Op.minus(worldGrabPoint, worldGrabPoint1));

                    // worldGrabPoint = worldGrabPoint1;

                    System.out.println("world grab point: " + worldGrabPoint);

                    RVec3 bodyPos = grabbedJoltBody.getCenterOfMassPosition();
                    RVec3 vectorToTarget = Op.minus(worldGrabPoint, bodyPos);

                    // todo: replace with adding forces every physics tick to correct direction if body collided with other body during pulling
                    double distance = vectorToTarget.length();
                    System.out.println(distance);
                    System.out.println(Op.minus(bodyPos, worldGrabPoint).length());
                    if (distance > 1e-6) {
                        float time = (float) distance * PULL_TIME_MULTIPLIER;
                        float MIN_TIME = 0.05f;
                        float MAX_TIME = 2.0f;
                        time = Math.max(MIN_TIME, Math.min(MAX_TIME, time));

                        Vec3 direction = vectorToTarget.toVec3().normalized();
                        System.out.println(direction);
                        float magnitude = (1.0f / time) * (float) distance * PULL_MAGNITUDE_MULTIPLIER;

                        Vec3 impulse = Op.star(magnitude, direction);
                        System.out.println(impulse);
                        grabbedJoltBody.addImpulse(impulse);
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
        VxBody grabberBody = world.getBodyManager().getVxBody(playerBodyPartData.bodyPartId());
        VxBody grabbedBody = world.getBodyManager().getVxBody(playerBodyPartData.grabbedBodyId());

        if (grabberBody == null || grabbedBody == null)
            return;

        ContactListenerManager.notifyList.removeIf((reject ->
                reject.body1() == grabberBody.getBodyId() &&
                        reject.body2() == grabbedBody.getBodyId()
        ));

        ContactListenerManager.hashMap.removeIf((reject ->
                reject.body1() == grabberBody.getBodyId() &&
                        reject.body2() == grabbedBody.getBodyId()
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

        Vector3f localGrabOffset = PlayerBodyPart.fromInteractionHand(interactionHand).getGrabPointVec3f();
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

    private RVec3 getBodyPartGrabPointWorld(ConstBodyLockInterface lockInterface, int bodyId, PlayerBodyPart playerBodyPart) {
        try (BodyLockRead lock = new BodyLockRead(lockInterface, bodyId)) {
            ConstBody body = lock.getBody();
            if (body == null)
                return null;

            return getBodyPartGrabPointWorld(body, playerBodyPart);
        }
    }

    private RVec3 getBodyPartGrabPointWorld(ConstBody body, PlayerBodyPart playerBodyPart) {
        return PlayerBodyPartTransforms.getGrabPointRotatedWorld(body.getPosition(), body.getRotation(), playerBodyPart);
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
