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
import com.github.stephengold.joltjni.readonly.Vec3Arg;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.timtaran.interactivemc.body.player.interaction.GrabInteraction;
import net.timtaran.interactivemc.body.player.interaction.TriggerInteraction;
import net.timtaran.interactivemc.body.player.interaction.TriggerState;
import net.timtaran.interactivemc.body.player.packet.S2CGrabResultPacket;
import net.timtaran.interactivemc.body.player.physics.PlayerBodyPartGhostRigidBody;
import net.timtaran.interactivemc.body.player.physics.PlayerBodyPartRigidBody;
import net.timtaran.interactivemc.body.player.store.PlayerBodyDataStore;
import net.timtaran.interactivemc.init.InteractiveMC;
import net.timtaran.interactivemc.init.registry.BodyRegistry;
import net.timtaran.interactivemc.network.Networking;
import net.xmx.velthoric.core.body.VxBody;
import net.xmx.velthoric.core.body.VxRemovalReason;
import net.xmx.velthoric.core.body.server.VxServerBodyManager;
import net.xmx.velthoric.core.physics.VxJoltBridge;
import net.xmx.velthoric.core.physics.world.VxPhysicsWorld;
import net.xmx.velthoric.math.VxConversions;
import net.xmx.velthoric.math.VxTransform;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.UUID;

/**
 * Manages the creation, tracking, and interaction of player bodies in the physics world.
 * <p>
 * Each player has multiple body parts (head, hands, etc.) represented as both
 * dynamic and ghost (kinematic) rigid bodies.
 * <p>
 * The manager is responsible for:
 * <ul>
 *     <li>Creating and removing player bodies</li>
 *     <li>Managing grab interactions between bodies</li>
 *     <li>Maintaining constraints between bodies</li>
 * </ul>
 *
 * @author timtaran
 */
public class PlayerBodyManager {
    private static final HashMap<VxPhysicsWorld, PlayerBodyManager> managers = new HashMap<>();

    private final VxPhysicsWorld world;
    private final GrabInteraction grabInteraction;
    private final TriggerInteraction triggerInteraction;

    private PlayerBodyManager(VxPhysicsWorld world) {
        this.world = world;
        this.grabInteraction = new GrabInteraction(world, this);
        this.triggerInteraction = new TriggerInteraction();
    }

    /**
     * Gets the PlayerBodyManager for the given level.
     *
     * @param level the level
     * @return the manager for that level's physics world
     */
    public static PlayerBodyManager get(Level level) {
        return get(VxPhysicsWorld.get(level.dimension()));
    }

    /**
     * Gets the PlayerBodyManager for the given physics world.
     *
     * @param world the physics world
     * @return the manager for that world
     */
    public static PlayerBodyManager get(VxPhysicsWorld world) {
        return managers.computeIfAbsent(world, PlayerBodyManager::new);
    }

    /**
     * Creates a body part for the given player and body part type.
     *
     * @param partType the type of body part (head, hands, etc.)
     * @param player   the player who owns this body part
     * @return data about the created body part, including the IDs of both the main and ghost bodies
     */
    private PlayerBodyPartData createBodyPart(PlayerBodyPart partType, Player player) {
        Vec3Arg size = partType.getSize();
        Vec3 halfExtents = Op.star(0.5f, size);

        VxTransform transform = new VxTransform(
                VxConversions.toJolt(player.position().add(partType.getLocalPivot()).add(new net.minecraft.world.phys.Vec3(0, 2, 0))),
                new Quat()
        );

        PlayerBodyPartRigidBody bodyPart = (PlayerBodyPartRigidBody) world.getBodyManager().createBody(
                BodyRegistry.PLAYER_BODY_PART,
                transform,
                EActivation.Activate,
                body -> {
                    body.setServerData(PlayerBodyPartRigidBody.DATA_HALF_EXTENTS, halfExtents);
                    body.setServerData(PlayerBodyPartRigidBody.DATA_PLAYER_ID, player.getUUID());
                    body.setServerData(PlayerBodyPartRigidBody.DATA_BODY_PART, partType);
                }
        );

        PlayerBodyPartGhostRigidBody bodyPartGhost = (PlayerBodyPartGhostRigidBody) world.getBodyManager().createBody(
                BodyRegistry.PLAYER_BODY_PART_GHOST,
                transform,
                EActivation.Activate,
                body -> {
                    body.setServerData(PlayerBodyPartGhostRigidBody.DATA_HALF_EXTENTS, halfExtents);
                    body.setServerData(PlayerBodyPartGhostRigidBody.DATA_PLAYER_ID, player.getUUID());
                    body.setServerData(PlayerBodyPartGhostRigidBody.DATA_BODY_PART, partType);
                }
        );
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

        return new PlayerBodyPartData(bodyPart.getPhysicsId(), bodyPartGhost.getPhysicsId(), TriggerState.RELEASE, null);
    }

    /**
     * Spawns all body parts for a player when they join or respawn.
     * <p>
     * If the player already has bodies, they will be removed first.
     * </p>
     *
     * @param player the player to spawn bodies for
     */
    public void spawnPlayer(Player player) {
        removePlayer(player);

        EnumMap<PlayerBodyPart, PlayerBodyPartData> playerBodies = new EnumMap<>(PlayerBodyPart.class);
        IntSet joltBodyIds = new IntOpenHashSet();

        VxServerBodyManager bodyManager = world.getBodyManager();

        for (PlayerBodyPart partType : PlayerBodyPart.values()) {
            PlayerBodyPartData bodyPartData = createBodyPart(partType, player);
            playerBodies.put(partType, bodyPartData);

            joltBodyIds.add(
                    bodyManager.getVxBody(bodyPartData.bodyPartId()).getBodyId()
            );

            joltBodyIds.add(
                    bodyManager.getVxBody(bodyPartData.ghostBodyPartId()).getBodyId()
            );
        }

        PlayerBodyDataStore.playersBodies.put(player.getUUID(), playerBodies);
        PlayerBodyDataStore.playersJoltBodies.put(player.getUUID(), joltBodyIds);
    }

    /**
     * Removes all body parts for a player when they leave the game.
     *
     * @param player the player to remove bodies for
     */
    public void removePlayer(Player player) {
        EnumMap<PlayerBodyPart, PlayerBodyPartData> playerBodies = PlayerBodyDataStore.playersBodies.remove(player.getUUID());
        if (playerBodies == null) return;

        PlayerBodyDataStore.playersJoltBodies.remove(player.getUUID());

        for (PlayerBodyPartData bodyData : playerBodies.values()) {
            world.getBodyManager().removeBody(bodyData.bodyPartId(), VxRemovalReason.DISCARD);
            world.getBodyManager().removeBody(bodyData.ghostBodyPartId(), VxRemovalReason.DISCARD);
            // Constraints are being removed internally in removeBody, so we don't need to worry about them here.
        }
    }

    public void onPrePhysicsTick() {
        PlayerBodyDataStore.playersBodies.forEach((playerId, bodyParts) -> {
            Player player = world.getLevel().getPlayerByUUID(playerId);
            if (player == null) return;

            bodyParts.forEach((bodyPart, bodyPartData) -> {
                if (bodyPartData.grabData() == null || !bodyPartData.grabData().isAttached()) {
                    return;
                }

                VxBody grabberBody = world.getBodyManager().getVxBody(bodyPartData.bodyPartId());
                VxBody grabbedBody = world.getBodyManager().getVxBody(bodyPartData.grabData().grabbedBodyId());

                GrabInteraction.GrabResult grabResult = grabInteraction.attachIfWithinReach(player, grabberBody, grabbedBody, bodyPart);

                if (grabResult != null) {
                    processGrabResult(player, bodyPart, grabResult);
                }
            });
        });

    }

    public void onPhysicsTick() {
        PlayerBodyDataStore.playersBodies.forEach((playerId, bodyParts) -> {
            Player player = world.getLevel().getPlayerByUUID(playerId);
            if (player == null) return;

            bodyParts.forEach((bodyPart, bodyPartData) -> {
                if (bodyPartData.grabData() == null || !bodyPartData.grabData().isAttached() || !bodyPartData.grabData().retracting()) {
                    return;
                }

                VxBody grabberBody = world.getBodyManager().getVxBody(bodyPartData.bodyPartId());
                VxBody grabbedBody = world.getBodyManager().getVxBody(bodyPartData.grabData().grabbedBodyId());

                grabInteraction.applyPullForce(player, grabberBody, grabbedBody, bodyPart, bodyPartData);
            });
        });
    }

    /**
     * Attempts to grab an object using the specified player's hand.
     *
     * @param player          the player attempting to grab
     * @param interactionHand the hand to use for grabbing (main or off-hand)
     * @param isRemoteAllowed whether remote grabbing (raycast-based) is allowed if no bodies are within grab radius
     * @return the body that was grabbed, or null if no body was grabbed
     * @see GrabInteraction#grab(Player, VxBody, PlayerBodyPart, boolean)
     */
    @Nullable
    public VxBody grab(Player player, InteractionHand interactionHand, boolean isRemoteAllowed) {
        PlayerBodyPart playerBodyPart = PlayerBodyPart.fromInteractionHand(interactionHand);
        if (playerBodyPart == null)
            return null;

        EnumMap<PlayerBodyPart, PlayerBodyPartData> playerBodies = PlayerBodyDataStore.playersBodies.get(player.getUUID());
        if (playerBodies == null)
            return null;

        PlayerBodyPartData playerBodyPartData = playerBodies.get(playerBodyPart);
        // every player body part should be initialized if we have playerBodies
        if (playerBodyPartData == null) {
            throw new IllegalStateException(
                    "Missing body part " + playerBodyPart + " for player " + player.getUUID()
            );
        }

        System.out.println("grab data: " + playerBodyPartData);

        if (playerBodyPartData.grabData() != null)
            return null; // already grabbing something

        VxBody body = world.getBodyManager().getVxBody(playerBodyPartData.bodyPartId());
        if (body == null) {
            throw new IllegalStateException(
                    "Body not found for body part " + playerBodyPart + " of player " + player.getUUID()
            );
        }

        GrabInteraction.GrabResult grabResult = grabInteraction.grab(player, body, playerBodyPart, isRemoteAllowed);

        processGrabResult(player, playerBodyPart, grabResult);

        return grabResult.grabbedBody();
    }

    /**
     * Updates pull behavior for a grabbed body part.
     *
     * @param player the player attempting to pull
     */
    public void updatePullState(Player player, InteractionHand interactionHand) {
        try {
            PlayerBodyPart playerBodyPart = PlayerBodyPart.fromInteractionHand(interactionHand);
            if (playerBodyPart == null)
                return;

            EnumMap<PlayerBodyPart, PlayerBodyPartData> playerBodies = PlayerBodyDataStore.playersBodies.get(player.getUUID());
            if (playerBodies == null)
                return;

            PlayerBodyPartData playerBodyPartData = playerBodies.get(playerBodyPart);
            // every player body part should be initialized if we have playerBodies
            if (playerBodyPartData == null)
                return;

            if (
                    playerBodyPartData.grabData() == null ||
                            !playerBodyPartData.grabData().isAttached() ||
                            !playerBodyPartData.grabData().retracting()
            )
                return;

            VxBody grabberBody = world.getBodyManager().getVxBody(playerBodyPartData.bodyPartId());
            if (grabberBody == null) {
                return;
            }

            VxBody grabbedBody = world.getBodyManager().getVxBody(playerBodyPartData.grabData().grabbedBodyId());

            if (grabInteraction.updatePullState(player, grabberBody, grabbedBody, playerBodyPart, playerBodyPartData)) {
                playerBodies.put(playerBodyPart, playerBodyPartData.withGrabData(playerBodyPartData.grabData().withRetracting(true)));
            }
        } catch (Exception e) {
            InteractiveMC.LOGGER.error("Error while pulling grabbed body", e);
        }
    }

    /**
     * Releases any object being grabbed by the specified player's hand.
     *
     * @param player          the player releasing the grab
     * @param interactionHand the hand to release (main or off-hand)
     * @see GrabInteraction#release(Player, VxBody, VxBody, PlayerBodyPart, PlayerBodyPartData)
     */
    public void release(Player player, InteractionHand interactionHand) {
        PlayerBodyPart playerBodyPart = PlayerBodyPart.fromInteractionHand(interactionHand);
        System.out.println("release playerbodypart: " + playerBodyPart);
        if (playerBodyPart == null)
            return;

        EnumMap<PlayerBodyPart, PlayerBodyPartData> playerBodies = PlayerBodyDataStore.playersBodies.get(player.getUUID());
        System.out.println("release playerbodies: " + playerBodies);
        if (playerBodies == null)
            return;

        PlayerBodyPartData playerBodyPartData = playerBodies.get(playerBodyPart);
        System.out.println("release playerbodypartdata: " + playerBodyPartData);
        if (playerBodyPartData == null) {
            return;
        }

        System.out.println("release triggered with: " + playerBodyPartData);

        if (playerBodyPartData.grabData() == null) {
            return;
        }

        VxBody grabberBody = world.getBodyManager().getVxBody(playerBodyPartData.bodyPartId());
        VxBody grabbedBody = world.getBodyManager().getVxBody(playerBodyPartData.grabData().grabbedBodyId());

        boolean isReleaseSuccessful = grabInteraction.release(player, grabberBody, grabbedBody, playerBodyPart, playerBodyPartData);

        if (!isReleaseSuccessful) {
            return;
        }

        // remove only first found body because multiple bodies can grab single body
        PlayerBodyDataStore.grabbedBodies.rem(grabbedBody.getBodyId());

        boolean isGrabConstraint = playerBodyPartData.grabData().constraintId() != null;

        playerBodies.put(playerBodyPart, playerBodyPartData.withGrabData(null));
        if (player instanceof ServerPlayer serverPlayer) {
            player.getServer().execute(() ->
                    Networking.sendToPlayer(
                            serverPlayer,
                            new S2CGrabResultPacket(
                                    interactionHand,
                                    null,
                                    isGrabConstraint
                            )
                    )
            );
        }
    }

    public void updateTriggerState(Player player, InteractionHand interactionHand, TriggerState triggerState) {
        PlayerBodyPart playerBodyPart = PlayerBodyPart.fromInteractionHand(interactionHand);
        if (playerBodyPart == null) return;

        EnumMap<PlayerBodyPart, PlayerBodyPartData> playerBodies = PlayerBodyDataStore.playersBodies.get(player.getUUID());
        if (playerBodies == null) return;

        PlayerBodyPartData playerBodyPartData = playerBodies.get(playerBodyPart);
        if (playerBodyPartData == null) return;

        if (playerBodyPartData.triggerState().equals(triggerState)) return;

        playerBodies.put(playerBodyPart, playerBodyPartData.withTriggerState(triggerState));

        if (playerBodyPartData.grabData() == null || playerBodyPartData.grabData().constraintId() == null) return;

        VxBody grabbedBody = world.getBodyManager().getVxBody(playerBodyPartData.grabData().grabbedBodyId());

        triggerInteraction.updateGrabState(player, grabbedBody, playerBodyPart, triggerState);
    }

    public void processGrabResult(Player player, PlayerBodyPart playerBodyPart, GrabInteraction.GrabResult grabResult) {
        System.out.println(grabResult);
        EnumMap<PlayerBodyPart, PlayerBodyPartData> playerBodies = PlayerBodyDataStore.playersBodies.get(player.getUUID());
        if (playerBodies == null)
            return;

        PlayerBodyPartData playerBodyPartData = playerBodies.get(playerBodyPart);
        if (playerBodyPartData == null) {
            return;
        }
        System.out.println(playerBodyPartData);

        UUID grabbedBodyId = grabResult.grabbedBody() != null ? grabResult.grabbedBody().getPhysicsId() : null;
        UUID constraintId = grabResult.grabConstraint() != null ? grabResult.grabConstraint().getConstraintId() : null;

        playerBodies.put(playerBodyPart, playerBodyPartData.withGrabData(new PlayerBodyPartData.GrabData(grabbedBodyId, constraintId, false)));

        if (grabResult.grabbedBody() != null) {
            PlayerBodyDataStore.grabbedBodies.add(grabResult.grabbedBody().getBodyId());
        }

        InteractionHand interactionHand = playerBodyPart.toInteractionHand();
        if (interactionHand == null)
            return;

        if (player instanceof ServerPlayer serverPlayer) {
            player.getServer().execute(() ->
                    Networking.sendToPlayer(
                            serverPlayer,
                            new S2CGrabResultPacket(
                                    interactionHand,
                                    grabbedBodyId,
                                    constraintId != null
                            )
                    )
            );
        }
    }
}
