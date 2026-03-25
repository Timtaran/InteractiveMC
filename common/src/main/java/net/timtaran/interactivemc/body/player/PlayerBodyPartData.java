/*
 * This file is part of InteractiveMC.
 * Licensed under LGPL 3.0.
 */
package net.timtaran.interactivemc.body.player;

import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Record storing player body part data used by player physics systems.
 * <p>Grabbing states:
 * <ul>
 *     <li>{@code grabbedBodyId == null} — nothing is grabbed</li>
 *     <li>{@code grabbedBodyId != null && grabConstraintId == null} — body is being pulled</li>
 *     <li>{@code grabbedBodyId != null && grabConstraintId != null} — body is attached using a constraint</li>
 * </ul>
 *
 * @param bodyPartId       ID of dynamic body part
 * @param ghostBodyPartId  ID of the ghost body part
 * @param grabbedBodyId    ID of the grabbed/pulled body ({@code null} if body not grabbing anything)
 * @param grabConstraintId ID of the grab constraint ({@code null} if not attached)
 * @author timtaran
 */
public record PlayerBodyPartData(UUID bodyPartId, UUID ghostBodyPartId, @Nullable UUID grabbedBodyId,
                                 @Nullable UUID grabConstraintId) {
}