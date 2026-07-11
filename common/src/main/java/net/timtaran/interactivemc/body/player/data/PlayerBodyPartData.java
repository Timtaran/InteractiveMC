/*
 * This file is part of InteractiveMC.
 * Licensed under LGPL 3.0.
 */
package net.timtaran.interactivemc.body.player.data;

import net.timtaran.interactivemc.body.player.interaction.TriggerState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Record storing player body part data used by player physics systems.
 *
 * @param bodyPartId      ID of dynamic body part
 * @param ghostBodyPartId ID of the ghost body part
 * @param triggerState    Current trigger state
 * @param grabData        Current grab state ({@code null} if nothing is grabbed)
 * @author timtaran
 */
public record PlayerBodyPartData(
        UUID bodyPartId,
        UUID ghostBodyPartId,
        TriggerState triggerState,
        @Nullable GrabData grabData
) {
    /**
     * Returns a new instance of {@code PlayerBodyPartData} with the updated trigger state.
     */
    public PlayerBodyPartData withTriggerState(@Nullable TriggerState newTriggerState) {
        return new PlayerBodyPartData(bodyPartId, ghostBodyPartId, newTriggerState, grabData);
    }

    /**
     * Returns a new instance of {@code PlayerBodyPartData} with the updated grab data.
     */
    public PlayerBodyPartData withGrabData(@Nullable GrabData newGrabData) {
        return new PlayerBodyPartData(bodyPartId, ghostBodyPartId, triggerState, newGrabData);
    }

    /**
     * Grab interaction state.
     * <p>
     * Constraint states:
     * <ul>
     *     <li>{@code constraintId == null} — body is freely grabbed/pulled</li>
     *     <li>{@code constraintId != null} — body is attached using a constraint</li>
     * </ul>
     *
     * <p>
     * Retraction states:
     * <ul>
     *     <li>{@code retracting == false} — grabbed body remains at current distance</li>
     *     <li>{@code retracting == true} — grabbed body is pulled toward the body part every tick</li>
     * </ul>
     *
     * @param grabbedBodyId ID of grabbed body
     * @param constraintId  ID of attachment constraint ({@code null} if not attached)
     * @param retracting    Whether grabbed body is being pulled toward the body part
     */
    public record GrabData(
            @NotNull UUID grabbedBodyId,
            @Nullable UUID constraintId,
            boolean retracting
    ) {
        public boolean isAttached() {
            return constraintId != null;
        }

        public GrabData withRetracting(boolean newRetracting) {
            return new GrabData(grabbedBodyId, constraintId, newRetracting);
        }
    }
}