/*
 * This file is part of InteractiveMC.
 * Licensed under LGPL 3.0.
 */
package net.timtaran.interactivemc.init.registry;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.timtaran.interactivemc.body.duck.TestDuckRigidBody;
import net.timtaran.interactivemc.body.duck.TestDuckRigidBodyRenderer;
import net.timtaran.interactivemc.body.player.physics.PlayerBodyPartGhostRigidBody;
import net.timtaran.interactivemc.body.player.physics.PlayerBodyPartRigidBody;
import net.timtaran.interactivemc.body.player.renderer.PlayerBodyPartGhostRenderer;
import net.timtaran.interactivemc.body.player.renderer.PlayerBodyPartRenderer;
import net.timtaran.interactivemc.util.InteractiveMCIdentifier;
import net.xmx.velthoric.core.behavior.impl.VxSummonableBehavior;
import net.xmx.velthoric.core.behavior.impl.VxTickBehavior;
import net.xmx.velthoric.core.body.VxBodyType;
import net.xmx.velthoric.core.body.persistence.behavior.VxPersistenceBehavior;
import net.xmx.velthoric.core.body.registry.VxBodyRegistry;
import net.xmx.velthoric.core.network.internal.behavior.VxNetSyncBehavior;
import net.xmx.velthoric.core.network.synchronization.behavior.VxSynchronizedDataBehavior;

/**
 * Registry for physics body types used in the mod.
 * <p>
 * This class registers body types and their associated client-side factories and renderers.
 *
 * @author timtaran
 */
public class BodyRegistry {
    /**
     * The dynamic rigid body type for player body parts (head, hands, etc.).
     */
    public static final VxBodyType<PlayerBodyPartRigidBody> PLAYER_BODY_PART = VxBodyType.Builder
            .<PlayerBodyPartRigidBody>create(PlayerBodyPartRigidBody::new)
            .rigidProvider(PlayerBodyPartRigidBody::createJoltBody)
            .behaviors(builder -> builder
                    .add(new VxNetSyncBehavior())
                    .add(new VxSynchronizedDataBehavior(PlayerBodyPartRigidBody::defineSyncData))
            )
            .build(InteractiveMCIdentifier.get("player_body_part"));

    /**
     * The ghost (kinematic) rigid body type for player body parts, used to track VR controller positions.
     */
    public static final VxBodyType<PlayerBodyPartGhostRigidBody> PLAYER_BODY_PART_GHOST = VxBodyType.Builder
            .<PlayerBodyPartGhostRigidBody>create(PlayerBodyPartGhostRigidBody::new)
            .rigidProvider(PlayerBodyPartGhostRigidBody::createJoltBody)
            .behaviors(builder -> builder
                    .add(new VxTickBehavior())
                    .add(new VxNetSyncBehavior())
                    .add(new VxSynchronizedDataBehavior(PlayerBodyPartGhostRigidBody::defineSyncData))
            )
            .build(InteractiveMCIdentifier.get("player_ghost_body_part"));

    public static final VxBodyType<TestDuckRigidBody> TEST_DUCK = VxBodyType.Builder
            .<TestDuckRigidBody>create(TestDuckRigidBody::new)
            .rigidProvider(TestDuckRigidBody::createJoltBody)
            .behaviors(builder -> builder
                    .add(new VxPersistenceBehavior())
                    .add(new VxNetSyncBehavior())
                    .add(new VxSummonableBehavior())
            )
            .build(InteractiveMCIdentifier.get("test_duck"));

    /**
     * Registers all body types on the server side.
     */
    public static void register() {
        VxBodyRegistry.getInstance().register(PLAYER_BODY_PART);
        VxBodyRegistry.getInstance().register(PLAYER_BODY_PART_GHOST);
        VxBodyRegistry.getInstance().register(TEST_DUCK);
    }

    /**
     * Registers client-side factories and renderers for body types.
     */
    @Environment(EnvType.CLIENT)
    public static void registerClient() {
        var registry = VxBodyRegistry.getInstance();

        // Client-side factory registration
        registry.registerClientFactory(PLAYER_BODY_PART.getTypeId(), PlayerBodyPartRigidBody::new);
        registry.registerClientFactory(PLAYER_BODY_PART_GHOST.getTypeId(), PlayerBodyPartGhostRigidBody::new);
        registry.registerClientFactory(TEST_DUCK.getTypeId(), TestDuckRigidBody::new);

        // Client-side renderer registration
        registry.registerClientRenderer(PLAYER_BODY_PART.getTypeId(), new PlayerBodyPartRenderer());
        registry.registerClientRenderer(PLAYER_BODY_PART_GHOST.getTypeId(), new PlayerBodyPartGhostRenderer());
        registry.registerClientRenderer(TEST_DUCK.getTypeId(), new TestDuckRigidBodyRenderer());
    }
}
