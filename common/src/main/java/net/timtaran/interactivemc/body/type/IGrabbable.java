/*
 * This file is part of InteractiveMC.
 * Licensed under LGPL 3.0.
 */
package net.timtaran.interactivemc.body.type;

import com.github.stephengold.joltjni.RVec3;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.timtaran.interactivemc.body.player.PlayerBodyPart;
import net.timtaran.interactivemc.body.player.interaction.TriggerState;
import org.jetbrains.annotations.Nullable;

/**
 * An interface for bodies that can be grabbed by other bodies.
 * <p>
 * Implementations of this interface provide specific points for grabbing,
 * defining where and how the body will be held when interacted with.
 *
 * @author timtaran
 */
public interface IGrabbable {
    /**
     * Gets the grab point position in the local space.
     * <p>
     * Defines where on the body a grabber attaches.
     *
     * @param intersectionPoint the intersection point between the grabber and the body,
     *                          relative to the body position (not center of mass).
     *                          Implementations may use this to determine the closest
     *                          valid grab point.
     *                          If the grab point is arbitrary, return
     *                          {@code intersectionPoint}.
     * @return the local position of the grab point, or {@code null} to prevent grabbing.
     */
    @Nullable
    RVec3 getGrabPoint(RVec3 intersectionPoint);

    /**
     * Gets the grab point position in the local space.
     * <p>
     * Defines where a grabber attaches to the body during a remote grab.
     *
     * @param intersectionPoint the intersection point between the grabber and the body.
     * @return the local position of the grab point, or {@code null} to prevent grabbing.
     * @see IGrabbable#getGrabPoint(RVec3)
     */
    @Nullable
    RVec3 getRemoteGrabPoint(RVec3 intersectionPoint);

    /**
     * Method called when body is being interacted.
     *
     * @param player       the player interacting with the body
     * @param bodyPart     player body part interacting with the body
     * @param triggerState the trigger state of body part
     */
    default void onTriggerStateUpdate(Player player, PlayerBodyPart bodyPart, TriggerState triggerState) {
    }

    /**
     * Method called when the body is grabbed.
     * <p>
     * If {@code isAttached} is {@code false}, the body is marked to pulled.
     *
     * @param player     the player grabbing the body
     * @param bodyPart   player body part grabbing the body
     * @param isAttached {@code true} if the body is attached to the player body part, {@code false} otherwise
     */
    default void onGrab(Player player, PlayerBodyPart bodyPart, boolean isAttached) {
    }

    /**
     * Method called when the client receives successful grab result from the server.
     * <p>
     * If {@code isAttached} is {@code false}, the body is marked to pulled.
     *
     * @param player          the player grabbing the body
     * @param interactionHand interaction hand grabbing the body
     */
    @Environment(EnvType.CLIENT)
    default void onGrabClient(Player player, InteractionHand interactionHand, boolean isAttached) {
    }

    /**
     * Method called when the client receives unsuccessful grab result from the server.
     *
     * @param player          the player grabbing the body
     * @param interactionHand interaction hand releasing the body
     */
    @Environment(EnvType.CLIENT)
    default void onReleaseClient(Player player, InteractionHand interactionHand, boolean isAttached) {
    }

    /**
     * Method called when the body is being pulled.
     * <p>
     * If {@code isAttached} is {@code false}, the body was marked to pulled.
     *
     * @param player   the player pulling the body
     * @param bodyPart player body part pulling the body
     */
    default void onPull(Player player, PlayerBodyPart bodyPart) {
    }

    /**
     * Method called when the body is released.
     *
     * @param player   the player releasing the body
     * @param bodyPart player body part releasing the body
     * @param isAttached {@code true} if the body was attached to the player body part, {@code false} otherwise
     */
    default void onRelease(Player player, PlayerBodyPart bodyPart, boolean isAttached) {
    }

    /**
     * Method called before the body is released.
     *
     * @param player   the player releasing the body
     * @param bodyPart player body part releasing the body
     * @return {@code true} to allow releasing body, {@code false} otherwise
     */
    default boolean canRelease(Player player, PlayerBodyPart bodyPart) {
        return true;
    }
}