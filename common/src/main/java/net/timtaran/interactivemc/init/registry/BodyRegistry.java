/*
 * This file is part of Velthoric.
 * Licensed under LGPL 3.0.
 */
package net.timtaran.interactivemc.init.registry;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.timtaran.interactivemc.body.player.PlayerBodyPartGhostRenderer;
import net.timtaran.interactivemc.body.player.PlayerBodyPartGhostRigidBody;
import net.timtaran.interactivemc.body.player.PlayerBodyPartRenderer;
import net.timtaran.interactivemc.body.player.PlayerBodyPartRigidBody;
import net.xmx.velthoric.core.body.registry.VxBodyRegistry;
import net.xmx.velthoric.core.body.registry.VxBodyType;
import net.timtaran.interactivemc.util.InteractiveMCIdentifier;

/**
 * Registry for physics body types used in the mod.
 * <p>
 * This class registers body types and their associated client-side factories and renderers.
 *
 * @author timtaran
 */
public class BodyRegistry {
    /** The dynamic rigid body type for player body parts (head, hands, etc.). */
    public static final VxBodyType<PlayerBodyPartRigidBody> PLAYER_BODY_PART = VxBodyType.Builder
            .<PlayerBodyPartRigidBody>create(PlayerBodyPartRigidBody::new)
            .noSummon()
            .build(InteractiveMCIdentifier.get("player_body_part"));

    /** The ghost (kinematic) rigid body type for player body parts, used to track VR controller positions. */
    public static final VxBodyType<PlayerBodyPartGhostRigidBody> PLAYER_BODY_PART_GHOST = VxBodyType.Builder
            .<PlayerBodyPartGhostRigidBody>create(PlayerBodyPartGhostRigidBody::new)
            .noSummon()
            .build(InteractiveMCIdentifier.get("player_ghost_body_part"));

    /**
     * Registers all body types on the server side.
     */
    public static void register() {
        VxBodyRegistry.getInstance().register(PLAYER_BODY_PART);
        VxBodyRegistry.getInstance().register(PLAYER_BODY_PART_GHOST);
    }

    /**
     * Registers client-side factories and renderers for body types.
     */
    @Environment(EnvType.CLIENT)
    public static void registerClient() {
        var registry = VxBodyRegistry.getInstance();

        // Client-side factory registration
        registry.registerClientFactory(PLAYER_BODY_PART.getTypeId(), (type, id) -> new PlayerBodyPartRigidBody((VxBodyType<PlayerBodyPartRigidBody>) type, id));
        registry.registerClientFactory(PLAYER_BODY_PART_GHOST.getTypeId(), (type, id) -> new PlayerBodyPartGhostRigidBody((VxBodyType<PlayerBodyPartGhostRigidBody>) type, id));

        // Client-side renderer registration
        registry.registerClientRenderer(PLAYER_BODY_PART.getTypeId(), new PlayerBodyPartRenderer());
        registry.registerClientRenderer(PLAYER_BODY_PART_GHOST.getTypeId(), new PlayerBodyPartGhostRenderer());
    }
}
