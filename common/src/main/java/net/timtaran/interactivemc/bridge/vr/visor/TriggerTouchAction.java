/*
 * This file is part of InteractiveMC.
 * Licensed under LGPL 3.0.
 */
package net.timtaran.interactivemc.bridge.vr.visor;

import me.phoenixra.atumvr.api.input.profile.VRInteractionProfileType;
import me.phoenixra.atumvr.api.input.profile.types.OculusTouchProfile;
import me.phoenixra.atumvr.api.input.profile.types.ValveIndexProfile;
import net.timtaran.interactivemc.body.player.interaction.TriggerState;
import net.timtaran.interactivemc.util.InteractiveMCIdentifier;
import net.timtaran.interactivemc.util.input.InputHandler;
import net.timtaran.interactivemc.util.vr.visor.VisorConversions;
import org.jetbrains.annotations.NotNull;
import org.vmstudio.visor.api.client.input.action.ActionBinding;
import org.vmstudio.visor.api.client.input.action.VRActionSet;
import org.vmstudio.visor.api.client.input.action.framework.VRActionKey;
import org.vmstudio.visor.api.common.HandType;

import java.util.Map;

public class TriggerTouchAction extends VRActionKey {
    public static final String MAINHAND_ID = "interactivemc_mtouch";
    public static final String OFFHAND_ID = "interactivemc_otouch";
    private final HandType hand;

    public TriggerTouchAction(@NotNull VRActionSet actionSet, @NotNull HandType hand) {
        super(
                hand == HandType.MAIN ? MAINHAND_ID : OFFHAND_ID,
                actionSet,
                '\ue000',
                hand == HandType.MAIN ? InteractiveMCIdentifier.getTranslationKey("key.mtouch") : InteractiveMCIdentifier.getTranslationKey("key.otouch")
        );
        this.hand = hand;
    }

    public @NotNull HandType getHand() {
        return this.hand;
    }

    @Override
    protected void onPress() {
        if (InputHandler.getTriggerState(VisorConversions.handToBodyPart(hand)) == TriggerState.RELEASE) {
            InputHandler.updateTriggerState(VisorConversions.handToBodyPart(hand), TriggerState.TOUCH);
        }
    }

    @Override
    protected void onRelease() {
        if (InputHandler.getTriggerState(VisorConversions.handToBodyPart(hand)) == TriggerState.TOUCH) {
            InputHandler.updateTriggerState(VisorConversions.handToBodyPart(hand), TriggerState.RELEASE);
        }
    }

    @Override
    public @NotNull Map<VRInteractionProfileType, ActionBinding> getDefaultBindings() {
        return Map.of(
                VRInteractionProfileType.OCULUS_TOUCH, hand == HandType.MAIN ? new ActionBinding(OculusTouchProfile.BUTTON_TRIGGER_TOUCH_RIGHT, OculusTouchProfile.BUTTON_TRIGGER_TOUCH_LEFT) : new ActionBinding(OculusTouchProfile.BUTTON_TRIGGER_TOUCH_LEFT, OculusTouchProfile.BUTTON_TRIGGER_TOUCH_RIGHT),
                VRInteractionProfileType.VALVE_INDEX, hand == HandType.MAIN ? new ActionBinding(ValveIndexProfile.BUTTON_TRIGGER_TOUCH_RIGHT, ValveIndexProfile.BUTTON_TRIGGER_TOUCH_LEFT) : new ActionBinding(ValveIndexProfile.BUTTON_TRIGGER_TOUCH_LEFT, ValveIndexProfile.BUTTON_TRIGGER_TOUCH_RIGHT)
        );
    }
}
