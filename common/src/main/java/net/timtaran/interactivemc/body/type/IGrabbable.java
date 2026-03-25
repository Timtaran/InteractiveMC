/*
 * This file is part of InteractiveMC.
 * Licensed under LGPL 3.0.
 */
package net.timtaran.interactivemc.body.type;

import com.github.stephengold.joltjni.Vec3;
import net.minecraft.world.entity.player.Player;
import net.timtaran.interactivemc.body.player.PlayerBodyPart;
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
     * @param intersectionPoint the intersection point between the grabber and the body.
     *                          Implementations may use this to determine the closest
     *                          valid grab point.
     *                          If the grab point is arbitrary, return
     *                          {@code intersectionPoint}.
     * @return the local position of the grab point, or {@code null} to prevent grabbing.
     */
    @Nullable
    Vec3 getGrabPoint(Vec3 intersectionPoint);

    /**
     * Gets the grab point position in the local space.
     * <p>
     * Defines where a grabber attaches to the body during a remote grab.
     *
     * @return the local position of the grab point, or {@code null} to prevent grabbing.
     */
    @Nullable
    Vec3 getRemoteGrabPoint();

    /**
     * Method called when body is being interacted.
     *
     * @param player   the player interacting with the body
     * @param bodyPart player body part interacting with the body
     */
    default void onInteract(Player player, PlayerBodyPart bodyPart) {
    }

    /**
     * Method called when the body is grabbed.
     *
     * @param player    the player grabbing the body
     * @param bodyPart  player body part grabbing the body
     * @param isAttached {@code true} if the body is attached to the player body part, {@code false} otherwise
     */
    default void onGrab(Player player, PlayerBodyPart bodyPart, boolean isAttached) {
    }

    /**
     * Method called when the body is being pulled.
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
     */
    default void onRelease(Player player, PlayerBodyPart bodyPart) {
    }
}