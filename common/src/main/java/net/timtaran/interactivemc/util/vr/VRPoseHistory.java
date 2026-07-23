/*
 * This file is part of InteractiveMC.
 * Licensed under LGPL 3.0.
 *
 * Original code from Vivecraft.
 */
package net.timtaran.interactivemc.util.vr;

import net.minecraft.world.phys.Vec3;
import net.timtaran.interactivemc.body.player.PlayerBodyPart;
import net.timtaran.interactivemc.util.vr.data.VRPose;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Represents the pose history of the VR player. See {@link VRPose} for individual Pose data.
 * In other words, it allows getting movement information of the VR player.
 * <p>
 * History is stored up to {@link #MAX_TICKS_BACK} ticks into the past. Attempting to retrieve history before
 * this far back will throw an {@link IllegalArgumentException}.
 * <p>
 * Many methods in this class accept the parameter {@code playerPositionRelative}. When this is {@code true}, all
 * calculations are done relative to the player, while if {@code false}, calculations are done in world space. For
 * example, the net movement of a player who moved from (0, 0, 0) to (3, 0, 0) but did NOT move their head, hands, etc.
 * would be (3, 0, 0) if {@code playerPositionRelative} is {@code false} and would be (0, 0, 0) if
 * {@code playerPositionRelative} is {@code true}.
 */
public interface VRPoseHistory {
    int MAX_TICKS_BACK = 60;

    /**
     * Gets the number of ticks, historical data is currently available for. The number returned by this method will
     * never be higher than {@link #MAX_TICKS_BACK}, the maximum number of ticks this implementation holds data for, however, it can be lower than {@link #MAX_TICKS_BACK}.
     *
     * @return The number of ticks, historical data is currently available for.
     */
    int ticksOfHistory();

    /**
     * Gets a raw list of {@link VRPose} instances, with index 0 representing the current tick's pose, 1 representing
     * last tick's pose, etc.
     *
     * @return The aforementioned list of {@link VRPose} instances.
     */
    List<VRPose> getAllHistoricalData();

    /**
     * Gets the pose from {@code ticksBack} ticks back, or {@code null} if such data isn't available.
     *
     * @param ticksBack              Ticks back to retrieve data from.
     * @param playerPositionRelative Whether to get the historical data with position relative to the player's position.
     * @return A {@link VRPose} instance from {@code ticksBack} ticks ago, or {@code null} if that data isn't available.
     * @throws IllegalArgumentException Thrown when {@code ticksBack} is outside the range [0,200].
     */
    VRPose getHistoricalData(int ticksBack, boolean playerPositionRelative) throws IllegalArgumentException;

    /**
     * Gets the net movement between the most recent VRPose in this instance and the oldest VRPose that can be
     * retrieved, going no farther back than {@code maxTicksBack}.
     *
     * @param bodyPart               The body part to get the net movement for.
     * @param maxTicksBack           The maximum number of ticks back to compare the most recent data with.
     * @param playerPositionRelative Whether net movement should be calculated relative to the player position.
     * @return The aforementioned net movement. Note that this will return zero change on all axes if only zero ticks
     * can be looked back. Will be {@code null} if the body part requested isn't available.
     * @throws IllegalArgumentException Thrown when {@code maxTicksBack} is outside the range [0,200] or an invalid
     *                                  {@code bodyPart} is supplied.
     */
    @Nullable
    Vec3 netMovement(
            PlayerBodyPart bodyPart, int maxTicksBack, boolean playerPositionRelative) throws IllegalArgumentException;

    /**
     * Gets the average velocity in blocks/tick between the most recent VRPose in this instance and the oldest VRPose
     * that can be retrieved, going no farther back than {@code maxTicksBack}.
     *
     * @param bodyPart               The body part to get the average velocity for.
     * @param maxTicksBack           The maximum number of ticks back to calculate velocity with.
     * @param playerPositionRelative Whether velocity should be calculated to the player position.
     * @return The aforementioned average velocity on each axis. Note that this will return zero velocity on all axes
     * if only zero ticks can be looked back. Will be {@code null} if the body part requested isn't available.
     * @throws IllegalArgumentException Thrown when {@code maxTicksBack} is outside the range [0,200] or an invalid
     *                                  {@code bodyPart} is supplied.
     */
    @Nullable
    Vec3 averageVelocity(
            PlayerBodyPart bodyPart, int maxTicksBack, boolean playerPositionRelative) throws IllegalArgumentException;
}