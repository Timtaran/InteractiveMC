/*
 * This file is part of InteractiveMC.
 * Licensed under LGPL 3.0.
 */
package net.timtaran.interactivemc.body.type;

import com.github.stephengold.joltjni.RVec3;
import com.github.stephengold.joltjni.Vec3;
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
     *
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
     *
     *
     * @param player     the player grabbing the body
     * @param bodyPart   player body part grabbing the body
     * @param isAttached {@code true} if the body is attached to the player body part, {@code false} otherwise
     */
    default void onGrab(Player player, PlayerBodyPart bodyPart, boolean isAttached) {
        // todo: mention double call when remote grabbing body (on raycast and mount), implement
    }

    /**
     * Method called when the body is being pulled.
     *
     * @param player   the player pulling the body
     * @param bodyPart player body part pulling the body
     */
    default void onPull(Player player, PlayerBodyPart bodyPart) {
    }

    // todo implement
    /**
     * Method called when the body is released.
     *
     * @param player   the player releasing the body
     * @param bodyPart player body part releasing the body
     * @return {@code true} to allow releasing body, {@code false} otherwise
     */
    default boolean onRelease(Player player, PlayerBodyPart bodyPart) {
        return true;
    }
}