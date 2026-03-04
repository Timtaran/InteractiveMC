/*
 * This file is part of InteractiveMC.
 * Licensed under LGPL 3.0.
 */
package net.timtaran.interactivemc.util.velthoric;

import com.github.stephengold.joltjni.GroupFilterTable;
import net.timtaran.interactivemc.body.player.PlayerBodyPart;
import net.xmx.velthoric.core.physics.VxCollisionRegistry;

/**
 * Utility class that defines collision filter groups used for player body physics.
 * <p>
 * This class contains predefined group identifiers and a configured
 * {@link GroupFilterTable} instance for handling collisions between
 * {@link PlayerBodyPart} elements and their grabbed counterparts.
 * <p>
 * The filter table is initialized statically and configured once at class load time.
 *
 * @author timtaran
 */
public class GroupFilters {
    public static final int PLAYER_BODY_GROUP_ID = VxCollisionRegistry.claimGroupId();

    /**
     * Collision filter table for player body parts.
     * <p>
     * The table contains {@code PlayerBodyPart.BODY_PARTS_AMOUNT * 2} subgroups:
     * <ul>
     *     <li>
     *         The first {@code PlayerBodyPart.BODY_PARTS_AMOUNT} subgroups
     *         correspond directly to {@link PlayerBodyPart} values.
     *     </li>
     *     <li>
     *         The second {@code PlayerBodyPart.BODY_PARTS_AMOUNT} subgroups
     *         correspond to grabbed bodies associated with each
     *         {@link PlayerBodyPart}.
     *     </li>
     * </ul>
     * <p>
     * For each body part with index {@code i}:
     * <ul>
     *     <li>Collision with itself ({@code i, i}) is enabled.</li>
     *     <li>
     *         Collision between the body part subgroup {@code i} and its
     *         grabbed counterpart subgroup {@code i + PlayerBodyPart.BODY_PARTS_AMOUNT}
     *         is disabled.
     *     </li>
     * </ul>
     */
    public static final GroupFilterTable PLAYER_BODY_FILTER = new GroupFilterTable(PlayerBodyPart.BODY_PARTS_AMOUNT * 2);

    static {
        setupPlayerBodyCollisions();
    }

    /**
     * Configures collision rules for player body parts and their grabbed counterparts.
     * <p>
     * Enables self-collision for each body part subgroup and disables
     * collisions between a body part and its corresponding grabbed subgroup.
     */
    private static void setupPlayerBodyCollisions() {
        for (int i = 0; i < PlayerBodyPart.BODY_PARTS_AMOUNT; i++) {
            //PLAYER_BODY_FILTER.enableCollision(i, i);
            //ShouldCollidePLAYER_BODY_FILTER.disableCollision(i, i + PlayerBodyPart.BODY_PARTS_AMOUNT);
        }
    }

    /**
     * Private constructor to prevent instantiation.
     */
    private GroupFilters() {
    }
}
