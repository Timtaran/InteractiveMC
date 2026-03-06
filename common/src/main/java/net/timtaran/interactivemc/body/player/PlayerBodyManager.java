/*
 * This file is part of InteractiveMC.
 * Licensed under LGPL 3.0.
 */
package net.timtaran.interactivemc.body.player;

import com.github.stephengold.joltjni.*;
import com.github.stephengold.joltjni.enumerate.EActivation;
import com.github.stephengold.joltjni.enumerate.EAxis;
import com.github.stephengold.joltjni.enumerate.EConstraintSpace;
import com.github.stephengold.joltjni.enumerate.EMotionType;
import com.github.stephengold.joltjni.operator.Op;
import com.github.stephengold.joltjni.readonly.RVec3Arg;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.timtaran.interactivemc.init.registry.BodyRegistry;
import net.timtaran.interactivemc.body.Grabber;
import net.timtaran.interactivemc.util.velthoric.GroupFilters;
import net.timtaran.interactivemc.init.InteractiveMC;
import net.xmx.velthoric.core.body.VxRemovalReason;
import net.xmx.velthoric.core.body.server.VxServerBodyManager;
import net.xmx.velthoric.core.body.type.VxBody;
import net.xmx.velthoric.core.constraint.VxConstraint;
import net.xmx.velthoric.core.constraint.manager.VxConstraintManager;
import net.xmx.velthoric.core.intersection.VxPhysicsIntersector;
import net.xmx.velthoric.core.physics.VxJoltBridge;
import net.xmx.velthoric.core.physics.VxPhysicsLayers;
import net.xmx.velthoric.core.physics.world.VxPhysicsWorld;
import net.xmx.velthoric.math.VxConversions;
import net.xmx.velthoric.math.VxTransform;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerBodyManager {
    public static final float GRAB_RADIUS = 0.3f;
    private static final Vec3 SHAPE_SCALE = new Vec3(1f, 1f, 1f);

    private static final HashMap<VxPhysicsWorld, PlayerBodyManager> managers = new HashMap<>();

    private record PlayerBodyPartData(UUID bodyPartId, UUID ghostBodyPartId, @Nullable UUID grabbedBodyId,
                                      @Nullable UUID grabConstraintId) {
    }

    /**
     * Contains all bodies associated with each player, indexed by their UUID.
     */
    private static final HashMap<UUID, EnumMap<PlayerBodyPart, PlayerBodyPartData>> playersBodies = new HashMap<>();

    /**
     * Contains the Jolt body IDs of all player bodies for quick lookup during interactions.
     */
    private static final ConcurrentHashMap<UUID, List<Integer>> playersJoltBodies = new ConcurrentHashMap<>();

    private final VxPhysicsWorld world;

    private PlayerBodyManager(VxPhysicsWorld world) {
        this.world = world;
    }

    public static PlayerBodyManager get(Level level) {
        return get(VxPhysicsWorld.get(level.dimension()));
    }

    public static PlayerBodyManager get(VxPhysicsWorld world) {
        return managers.computeIfAbsent(world, PlayerBodyManager::new);
    }

    /**
     * @return Data about the created body part, including the IDs of both the main and ghost bodies.
     */
    private PlayerBodyPartData createBodyPart(PlayerBodyPart partType, Player player) {
        Vec3 size = partType.getSize();
        Vec3 halfExtents = Op.star(0.5f, size);

        VxTransform transform = new VxTransform(
                VxConversions.toJolt(player.position().add(partType.getLocalPivot()).add(new net.minecraft.world.phys.Vec3(0, 2, 0))),
                new Quat()
        );

        PlayerBodyPartRigidBody bodyPart = world.getBodyManager().createRigidBody(
                BodyRegistry.PLAYER_BODY_PART,
                transform,
                EActivation.Activate,
                body -> {
                    body.setServerData(PlayerBodyPartRigidBody.DATA_HALF_EXTENTS, halfExtents);
                    body.setServerData(PlayerBodyPartRigidBody.DATA_PLAYER_ID, player.getUUID());
                    body.setServerData(PlayerBodyPartRigidBody.DATA_BODY_PART, partType);
                }
        );

        PlayerBodyPartGhostRigidBody bodyPartGhost = world.getBodyManager().createRigidBody(
                BodyRegistry.PLAYER_BODY_PART_GHOST,
                transform,
                EActivation.Activate,
                body -> {
                    body.setServerData(PlayerBodyPartRigidBody.DATA_HALF_EXTENTS, halfExtents);
                    body.setServerData(PlayerBodyPartRigidBody.DATA_PLAYER_ID, player.getUUID());
                    body.setServerData(PlayerBodyPartRigidBody.DATA_BODY_PART, partType);
                }
        );
        System.out.println(bodyPartGhost);
        VxJoltBridge.INSTANCE.getJoltBody(world, bodyPartGhost).setMotionType(EMotionType.Kinematic);
        // Workaround until https://github.com/xI-Mx-Ix/Velthoric/issues/31 will be resolved


        try (SixDofConstraintSettings settings = new SixDofConstraintSettings()) {
            settings.setSpace(EConstraintSpace.LocalToBodyCom);

            settings.setPosition1(new RVec3());
            settings.setPosition2(new RVec3());

            settings.setAxisX1(new Vec3(1f, 0f, 0f));
            settings.setAxisY1(new Vec3(0f, 1f, 0f));
            settings.setAxisX2(new Vec3(1f, 0f, 0f));
            settings.setAxisY2(new Vec3(0f, 1f, 0f));

            settings.setLimitedAxis(EAxis.TranslationX, 0f, 0f);
            settings.setLimitedAxis(EAxis.TranslationY, 0f, 0f);
            settings.setLimitedAxis(EAxis.TranslationZ, 0f, 0f);
            settings.setLimitedAxis(EAxis.RotationX, 0f, 0f);
            settings.setLimitedAxis(EAxis.RotationY, 0f, 0f);
            settings.setLimitedAxis(EAxis.RotationZ, 0f, 0f);

            MotorSettings linearMotor = new MotorSettings(8.0f, 1.0f, 3000.0f, 0f);
            settings.setMotorSettings(EAxis.TranslationX, linearMotor);
            settings.setMotorSettings(EAxis.TranslationY, linearMotor);
            settings.setMotorSettings(EAxis.TranslationZ, linearMotor);

            MotorSettings angularMotor = new MotorSettings(15.0f, 1.0f, 0f, 1600.0f);
            settings.setMotorSettings(EAxis.RotationX, angularMotor);
            settings.setMotorSettings(EAxis.RotationY, angularMotor);
            settings.setMotorSettings(EAxis.RotationZ, angularMotor);

            // Create the constraint. It will be activated automatically once both bodies are loaded.
            world.getConstraintManager().createConstraint(settings, bodyPartGhost.getPhysicsId(), bodyPart.getPhysicsId()).setPersistent(false);
        }

        return new PlayerBodyPartData(bodyPart.getPhysicsId(), bodyPartGhost.getPhysicsId(), null, null);
    }

    public void spawnPlayer(Player player) {
        if (playersBodies.containsKey(player.getUUID())) {
            removePlayer(player);
        }

        EnumMap<PlayerBodyPart, PlayerBodyPartData> playerBodies = new EnumMap<>(PlayerBodyPart.class);
        List<Integer> joltBodyIds = new ArrayList<>();

        VxServerBodyManager bodyManager = world.getBodyManager();

        for (PlayerBodyPart partType : PlayerBodyPart.values()) {
            PlayerBodyPartData bodyPartData = createBodyPart(partType, player);
            playerBodies.put(partType, bodyPartData);

            joltBodyIds.add(
                    bodyManager.getVxBody(bodyPartData.bodyPartId).getBodyId()
            );

            joltBodyIds.add(
                    bodyManager.getVxBody(bodyPartData.ghostBodyPartId).getBodyId()
            );
        }

        playersBodies.put(player.getUUID(), playerBodies);
        playersJoltBodies.put(player.getUUID(), joltBodyIds);
    }

    public void removePlayer(Player player) {
        EnumMap<PlayerBodyPart, PlayerBodyPartData> playerBodies = playersBodies.remove(player.getUUID());
        if (playerBodies == null) return;

        playersJoltBodies.remove(player.getUUID());

        for (PlayerBodyPartData bodyData : playerBodies.values()) {
            world.getBodyManager().removeBody(bodyData.bodyPartId, VxRemovalReason.DISCARD);
            world.getBodyManager().removeBody(bodyData.ghostBodyPartId, VxRemovalReason.DISCARD);
            // Constraints are being removed internally in removeBody, so we don't need to worry about them here.
        }
    }

    @Nullable
    public VxBody grab(Player player, InteractionHand interactionHand) {
        PlayerBodyPart playerBodyPart = PlayerBodyPart.fromInteractionHand(interactionHand);
        if (playerBodyPart == null)
            return null;

        EnumMap<PlayerBodyPart, PlayerBodyPartData> playerBodies = playersBodies.get(player.getUUID());
        if (playerBodies == null)
            return null;

        PlayerBodyPartData playerBodyPartData = playerBodies.get(playerBodyPart);
        // every player body part should be initialized if we have playerBodies
        if (playerBodyPartData == null) {
            throw new IllegalStateException(
                    "Missing body part " + playerBodyPart + " for player " + player.getUUID()
            );
        }

        if (playerBodyPartData.grabbedBodyId() != null)
            return null; // already grabbing something

        VxBody body = world.getBodyManager().getVxBody(playerBodyPartData.bodyPartId);
        if (body == null) {
            throw new IllegalStateException(
                    "Body not found for body part " + playerBodyPart + " of player " + player.getUUID()
            );
        }

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

            VxTransform vxTransform = body.getTransform();

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
                        !playersJoltBodies.get(player.getUUID()).contains(intersection.bodyId())

                ) {
                    Body grabbedJoltBody = VxJoltBridge.INSTANCE.getJoltBody(world, intersection.bodyId());

                    if (grabbedJoltBody.getObjectLayer() != VxPhysicsLayers.TERRAIN) {
                        VxBody grabbedBody = world.getBodyManager().getByJoltBodyId(intersection.bodyId());

                        if (grabbedBody == null) {
                            InteractiveMC.LOGGER.warn("vxBody1 is null for body ID: {}", intersection.bodyId());
                            continue;
                        }

                        CollisionGroup grabbedBodyCollisionGroup = grabbedJoltBody.getCollisionGroup();
                        boolean isNotGrabbed = (grabbedBodyCollisionGroup.getGroupFilter() == null || grabbedBodyCollisionGroup.getGroupId() == GroupFilters.PLAYER_BODY_GROUP_ID);

                        if (grabbedJoltBody.getMotionType() == EMotionType.Dynamic && isNotGrabbed) {
                            VxTransform grabbedBodyTransform = grabbedBody.getTransform();
                            // Calculate a new world-space position for the body so that the local contact point
                            // aligns exactly with the desired grab point in world space.
                            RVec3 worldGrabPointOnBody = new RVec3(
                                    worldGrabPoint.xx() - (intersection.bodyContactPoint().getX() - grabbedBodyTransform.getTranslation().xx()),
                                    worldGrabPoint.yy() - (intersection.bodyContactPoint().getY() - grabbedBodyTransform.getTranslation().yy()),
                                    worldGrabPoint.zz() - (intersection.bodyContactPoint().getZ() - grabbedBodyTransform.getTranslation().zz())
                            );

                            grabbedJoltBody.setPositionAndRotationInternal(worldGrabPointOnBody, grabbedBodyTransform.getRotation());
                        }
                        else {
                            // todo add move grabber body if grabbed body not meant to be moved by physics
                        }

                        try (FixedConstraintSettings settings = new FixedConstraintSettings()) {
                            settings.setSpace(EConstraintSpace.WorldSpace);
                            settings.setPoint1(worldGrabPoint);
                            settings.setPoint2(worldGrabPoint);

                            // todo rework
                            //if (body instanceof Grabber grabber)
                            //    grabbedJoltBody.setCollisionGroup(new CollisionGroup(GroupFilters.PLAYER_BODY_FILTER, GroupFilters.PLAYER_BODY_GROUP_ID, grabber.getSubGroupId()));

                            VxConstraint constraint = constraintManager.createConstraint(settings, body.getPhysicsId(), grabbedBody.getPhysicsId());
                            constraint.setPersistent(false);

                            playerBodies.put(playerBodyPart, new PlayerBodyPartData(playerBodyPartData.bodyPartId, playerBodyPartData.ghostBodyPartId, grabbedBody.getPhysicsId(), constraint.getConstraintId()));

                            return grabbedBody;
                        }
                    } // todo add terrain grab after implementing client-side prediction
                }
            }
        }

        return null;
    }

    public void release(Player player, InteractionHand interactionHand) {
        PlayerBodyPart playerBodyPart = PlayerBodyPart.fromInteractionHand(interactionHand);
        if (playerBodyPart == null)
            return;

        EnumMap<PlayerBodyPart, PlayerBodyPartData> playerBodies = playersBodies.get(player.getUUID());
        if (playerBodies == null)
            return;

        PlayerBodyPartData playerBodyPartData = playerBodies.get(playerBodyPart);
        if (playerBodyPartData == null) {
            throw new IllegalStateException(
                    "Missing body part " + playerBodyPart + " for player " + player.getUUID()
            );
        }

        if (playerBodyPartData.grabbedBodyId() == null || playerBodyPartData.grabConstraintId() == null) {
            return;
        }

        world.getConstraintManager().removeConstraint(playerBodyPartData.grabConstraintId);

        playerBodies.put(playerBodyPart, new PlayerBodyPartData(playerBodyPartData.bodyPartId, playerBodyPartData.ghostBodyPartId, null, null));
    }
}
