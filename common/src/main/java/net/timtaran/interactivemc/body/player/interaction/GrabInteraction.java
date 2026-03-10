/*
 * This file is part of InteractiveMC.
 * Licensed under LGPL 3.0.
 */
package net.timtaran.interactivemc.body.player.interaction;


import com.github.stephengold.joltjni.*;
import com.github.stephengold.joltjni.enumerate.EConstraintSpace;
import com.github.stephengold.joltjni.enumerate.EMotionType;
import com.github.stephengold.joltjni.readonly.RVec3Arg;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
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
import net.xmx.velthoric.core.physics.VxJoltBridge;
import net.xmx.velthoric.core.physics.VxPhysicsLayers;
import net.xmx.velthoric.core.physics.world.VxPhysicsWorld;
import net.xmx.velthoric.math.VxTransform;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.vivecraft.api.data.VRBodyPart;
import org.vivecraft.api.data.VRBodyPartData;

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
    private static final float GRAB_RADIUS = 0.3f;
    private static final Vec3 SHAPE_SCALE = new Vec3(1f, 1f, 1f);

    private final VxPhysicsWorld world;

    public GrabInteraction(VxPhysicsWorld world) {
        this.world = world;
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

            RVec3 worldGrabPoint = vxTransform.getTranslation();
            RVec3 localGrabPoint = playerBodyPart.getLocalGrabPoint();

            // Rotate the local grab point by the body's rotation to get the correct world offset.
            RVec3 localGrabPointRotated = new RVec3(localGrabPoint);
            localGrabPointRotated.rotateInPlace(vxTransform.getRotation());

            // Add the rotated local grab point to the body's position to get the final grab point in world space.
            worldGrabPoint.addInPlace(localGrabPointRotated.xx(), localGrabPointRotated.yy(), localGrabPointRotated.zz());
            RMat44 comTransform = new VxTransform(worldGrabPoint, vxTransform.getRotation()).toRMat44();

            List<VxPhysicsIntersector.IntersectShapeResult> intersections = VxPhysicsIntersector.narrowIntersectShape(world, shape, SHAPE_SCALE, comTransform, base, bplFilter, olFilter, bodyFilter);

            intersections.sort(Comparator.comparingDouble(result -> { // sort by closest intersection point to base.
                Vec3 p = result.bodyContactPoint();

                double dx = p.getX() - vxTransform.getTranslation().x();
                double dy = p.getY() - vxTransform.getTranslation().y();
                double dz = p.getZ() - vxTransform.getTranslation().z();

                return dx * dx + dy * dy + dz * dz;
            }));

            VxConstraintManager constraintManager = world.getConstraintManager();

            for (VxPhysicsIntersector.IntersectShapeResult intersection : intersections) {
                if (
                        !PlayerBodyDataStore.playersJoltBodies.get(player.getUUID()).contains(intersection.bodyId())

                ) {
                    Body grabbedJoltBody = VxJoltBridge.INSTANCE.getJoltBody(world, intersection.bodyId());

                    if (grabbedJoltBody.getObjectLayer() != VxPhysicsLayers.TERRAIN) {
                        VxBody grabbedBody = world.getBodyManager().getByJoltBodyId(intersection.bodyId());

                        if (grabbedBody == null) {
                            InteractiveMC.LOGGER.warn("grabbedBody is null for body ID: {}", intersection.bodyId());
                            continue;
                        }

                        if (grabbedJoltBody.getMotionType() == EMotionType.Dynamic) {
                            VxTransform grabbedBodyTransform = grabbedBody.getTransform();
                            // Calculate a new world-space position for the body so that the local contact point
                            // aligns exactly with the desired grab point in world space.
                            RVec3 worldGrabPointOnBody = new RVec3(
                                    worldGrabPoint.xx() - (intersection.bodyContactPoint().getX() - grabbedBodyTransform.getTranslation().xx()),
                                    worldGrabPoint.yy() - (intersection.bodyContactPoint().getY() - grabbedBodyTransform.getTranslation().yy()),
                                    worldGrabPoint.zz() - (intersection.bodyContactPoint().getZ() - grabbedBodyTransform.getTranslation().zz())
                            );

                            grabbedJoltBody.setPositionAndRotationInternal(worldGrabPointOnBody, grabbedBodyTransform.getRotation());
                        } else {
                            // todo move grabber body if grabbed body not meant to be moved by physics
                        }

                        try (FixedConstraintSettings settings = new FixedConstraintSettings()) {
                            settings.setSpace(EConstraintSpace.WorldSpace);
                            settings.setPoint1(worldGrabPoint);
                            settings.setPoint2(worldGrabPoint); // todo replace with 6dof constraint and move all constraints to separate class

                            // todo rework
                            //if (body instanceof Grabber grabber)
                            //    grabbedJoltBody.setCollisionGroup(new CollisionGroup(GroupFilters.PLAYER_BODY_FILTER, GroupFilters.PLAYER_BODY_GROUP_ID, grabber.getSubGroupId()));

                            VxConstraint constraint = constraintManager.createConstraint(settings, grabberBody.getPhysicsId(), grabbedBody.getPhysicsId());
                            constraint.setPersistent(false);

                            return new GrabResult(grabbedBody, constraint);
                        }
                    } // todo add terrain grab after implementing client-side prediction
                }
            }
        }
        return new GrabResult(null, null);
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
        if (ClientPlayerBodyDataStore.currentPose != null)
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
}
