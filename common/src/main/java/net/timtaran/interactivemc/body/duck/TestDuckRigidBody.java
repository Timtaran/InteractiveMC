/*
 * This file is part of InteractiveMC.
 * Licensed under LGPL 3.0.
 */
package net.timtaran.interactivemc.body.duck;

import com.github.stephengold.joltjni.*;
import com.github.stephengold.joltjni.enumerate.EMotionType;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.timtaran.interactivemc.body.player.PlayerBodyPart;
import net.timtaran.interactivemc.body.player.interaction.TriggerState;
import net.timtaran.interactivemc.body.type.IGrabbable;
import net.xmx.velthoric.core.body.VxBody;
import net.xmx.velthoric.core.body.VxBodyType;
import net.xmx.velthoric.core.body.factory.VxRigidBodyFactory;
import net.xmx.velthoric.core.physics.VxPhysicsLayers;
import net.xmx.velthoric.core.physics.world.VxPhysicsWorld;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Test body for debugging grab and trigger interactions.
 */
public class TestDuckRigidBody extends VxBody implements IGrabbable {

    /**
     * Server-side constructor.
     *
     * @param type  the body type
     * @param physicsWorld the physics world
     * @param id    the unique identifier for this body
     */
    public TestDuckRigidBody(VxBodyType type, VxPhysicsWorld physicsWorld, UUID id) {
        super(type, physicsWorld, id);
    }

    /**
     * Client-side constructor.
     *
     * @param type the body type
     * @param id   the unique identifier for this body
     */
    @Environment(EnvType.CLIENT)
    public TestDuckRigidBody(VxBodyType type, UUID id) {
        super(type, id);
    }

    public static int createJoltBody(VxBody body, VxRigidBodyFactory factory) {
        try (
                ShapeSettings shapeSettings = new BoxShapeSettings(new Vec3(0.2f, 0.2f, 0.2f));
                BodyCreationSettings bcs = new BodyCreationSettings()
        ) {
            bcs.setMotionType(EMotionType.Dynamic);
            bcs.setObjectLayer(VxPhysicsLayers.MOVING);
            return factory.create(shapeSettings, bcs);
        }
    }

    @Override
    public @Nullable RVec3 getGrabPoint(RVec3 intersectionPoint) {
        return intersectionPoint;
    }

    @Override
    public @Nullable RVec3 getRemoteGrabPoint(RVec3 intersectionPoint) {
        return intersectionPoint;
    }

    @Override
    public void onTriggerStateUpdate(Player player, PlayerBodyPart bodyPart, TriggerState triggerState) {
        System.out.println("Trigger state update: " + triggerState);
    }

    @Override
    public void onGrab(Player player, PlayerBodyPart bodyPart, boolean isAttached) {
        System.out.println("on grab: " + isAttached);
    }

    @Override
    public void onPull(Player player, PlayerBodyPart bodyPart) {
        System.out.println("on pull");
    }

    @Override
    public void onRelease(Player player, PlayerBodyPart bodyPart, boolean isAttached) {
        System.out.println("on release");
    }

    @Override
    public boolean canRelease(Player player, PlayerBodyPart bodyPart) {
        System.out.println("can release");
        return IGrabbable.super.canRelease(player, bodyPart);
    }

    @Override
    public void onGrabClient(Player player, InteractionHand interactionHand, boolean isAttached) {
        System.out.println("on grab client");
    }

    @Override
    public void onReleaseClient(Player player, InteractionHand interactionHand, boolean isAttached) {
        System.out.println("on release client");
    }
}
