/*
 * This file is part of InteractiveMC.
 * Licensed under LGPL 3.0.
 */
package net.timtaran.interactivemc.bridge.vr.visor;

import me.phoenixra.atumvr.api.input.profile.VRInteractionProfileType;
import me.phoenixra.atumvr.api.input.profile.types.OculusTouchProfile;
import me.phoenixra.atumvr.api.input.profile.types.ValveIndexProfile;
import net.timtaran.interactivemc.util.InteractiveMCIdentifier;
import net.timtaran.interactivemc.util.input.InputHandler;
import net.timtaran.interactivemc.util.vr.visor.VisorConversions;
import org.jetbrains.annotations.NotNull;
import org.vmstudio.visor.api.client.input.action.ActionBinding;
import org.vmstudio.visor.api.client.input.action.VRActionSet;
import org.vmstudio.visor.api.client.input.action.framework.VRActionKey;
import org.vmstudio.visor.api.common.HandType;

import java.util.Map;

public class GrabAction extends VRActionKey {
    public static final String MAIN_HAND_ID = "interactivemc_mgrab";
    public static final String OFF_HAND_ID = "interactivemc_ograb";
    private final HandType hand;

    public GrabAction(@NotNull VRActionSet actionSet, @NotNull HandType hand) {
        super(
                hand == HandType.MAIN ? MAIN_HAND_ID : OFF_HAND_ID,
                actionSet,
                '\ue000',
                hand == HandType.MAIN ? InteractiveMCIdentifier.getTranslationKey("key.mgrab") : InteractiveMCIdentifier.getTranslationKey("key.ograb")
        );
        this.hand = hand;
    }

    public @NotNull HandType getHand() {
        return this.hand;
    }

    @Override
    protected void onPress() {
        InputHandler.attemptGrab(VisorConversions.handToBodyPart(hand));
    }

    @Override
    protected void onRelease() {
        InputHandler.attemptRelease(VisorConversions.handToBodyPart(hand));
    }

    @Override
    public @NotNull Map<VRInteractionProfileType, ActionBinding> getDefaultBindings() {
        return Map.of(
                VRInteractionProfileType.OCULUS_TOUCH, hand == HandType.MAIN ? new ActionBinding(OculusTouchProfile.BUTTON_GRIP_RIGHT, OculusTouchProfile.BUTTON_GRIP_LEFT) : new ActionBinding(OculusTouchProfile.BUTTON_GRIP_LEFT, OculusTouchProfile.BUTTON_GRIP_RIGHT),
                VRInteractionProfileType.VALVE_INDEX, hand == HandType.MAIN ? new ActionBinding(ValveIndexProfile.BUTTON_GRIP_FORCE_RIGHT, ValveIndexProfile.BUTTON_GRIP_FORCE_LEFT) : new ActionBinding(ValveIndexProfile.BUTTON_GRIP_FORCE_LEFT, ValveIndexProfile.BUTTON_GRIP_FORCE_RIGHT)
        );
    }
}
