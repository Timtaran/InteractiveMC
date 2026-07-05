/*
 * This file is part of InteractiveMC.
 * Licensed under LGPL 3.0.
 */
package net.timtaran.interactivemc.mixin.bridge.vivecraft;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.KeyMapping;
import net.minecraft.world.InteractionHand;
import net.timtaran.interactivemc.body.player.interaction.TriggerState;
import net.timtaran.interactivemc.body.player.packet.C2SGrabPacket;
import net.timtaran.interactivemc.body.player.packet.C2SReleasePacket;
import net.timtaran.interactivemc.body.player.packet.C2STriggerStatePacket;
import net.timtaran.interactivemc.body.player.store.ClientPlayerBodyDataStore;
import net.timtaran.interactivemc.init.registry.KeyMapRegistry;
import net.timtaran.interactivemc.network.Networking;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.client_vr.provider.MCVR;
import org.vivecraft.client_vr.provider.openvr_lwjgl.VRInputAction;

/**
 * Mixin that processes keymappings right before Vivecraft would perform its own checks.
 * <p>
 * This mixin intercepts the key binding processing to handle grab and release
 * actions before Vivecraft's default handling takes place.
 *
 * @author timtaran
 * @see KeyMapRegistry
 */
@Mixin(value = MCVR.class, remap = false)
public abstract class KeyMappingHandlingMixin {
    @Shadow
    public abstract VRInputAction getInputAction(KeyMapping keyMapping);


    /**
     * Injects into the processBindings method to handle grab/release keymappings.
     * <p>
     * This method is called at the very beginning of processBindings, before
     * Vivecraft's default key handling.
     * </p>
     *
     * @param ci the callback info for the mixin injection
     */
    @Inject(
            method = "processBindings",
            at = @At("HEAD")
    )
    private void interactivemc$processKeymappings(CallbackInfo ci) {
        interactivemc$updateGrabState(InteractionHand.MAIN_HAND, KeyMapRegistry.MAIN_GRAB_KEYMAPPING);
        interactivemc$updateGrabState(InteractionHand.OFF_HAND, KeyMapRegistry.OFF_GRAB_KEYMAPPING);
        interactivemc$updateTriggerState(InteractionHand.MAIN_HAND, KeyMapRegistry.MAIN_TRIGGER_TOUCH_KEYMAPPING, KeyMapRegistry.MAIN_TRIGGER_KEYMAPPING);
        interactivemc$updateTriggerState(InteractionHand.OFF_HAND, KeyMapRegistry.OFF_TRIGGER_TOUCH_KEYMAPPING, KeyMapRegistry.OFF_TRIGGER_KEYMAPPING);
    }

    /**
     * Updates the grab/release state for a single hand based on key state.
     * <p>
     * If the grab key is pressed, attempts to grab an object. If the key is released
     * and an object was being held, releases it.
     * </p>
     *
     * @param hand       the interaction hand (main or off-hand)
     * @param keyMapping the key mapping to check
     */
    @Unique
    private void interactivemc$updateGrabState(InteractionHand hand, KeyMapping keyMapping) {
        if (keyMapping.consumeClick()) {
            interactivemc$grab(hand);
        } else if (!keyMapping.isDown() && ClientPlayerBodyDataStore.isGrabbing(hand)) {
            interactivemc$release(hand);
        }
    }

    /**
     * Sends packet to the server to grab an object using the specified hand.
     *
     * @param interactionHand the hand attempting to grab (main or off-hand)
     */
    @Unique
    private static void interactivemc$grab(InteractionHand interactionHand) {
        Networking.sendToServer(new C2SGrabPacket(interactionHand));
    }

    /**
     * Sends a release packet to the server to remove the grab constraint.
     *
     * @param interactionHand the hand to release (main or off-hand)
     */
    @Unique
    private static void interactivemc$release(InteractionHand interactionHand) {
        Networking.sendToServer(new C2SReleasePacket(interactionHand));
    }

    @Unique
    private void interactivemc$updateTriggerState(InteractionHand hand, KeyMapping touchKeyMapping, KeyMapping pressKeyMapping) {
        TriggerState triggerState;

        if (pressKeyMapping.consumeClick() || pressKeyMapping.isDown()) {
            triggerState = TriggerState.PRESS;
        } else if (touchKeyMapping.consumeClick() || touchKeyMapping.isDown()) {
            triggerState = TriggerState.TOUCH;
        } else {
            triggerState = TriggerState.RELEASE;
        }

        TriggerState previousTriggerState = ClientPlayerBodyDataStore.triggerStates.put(hand, triggerState);

        if (!triggerState.equals(previousTriggerState)) {
            Networking.sendToServer(new C2STriggerStatePacket(hand, triggerState));
        }
    }

    /**
     * Intercepts KeyMapping.consumeClick to optionally suppress input events
     * originating from previously handled grab actions.
     * <p>
     * This is used to prevent double-processing of input events when grab
     * actions have already been consumed by InteractiveMC logic.
     *
     * @param key      key mapping being checked
     * @param original original consumeClick operation
     * @return false if the event should be suppressed, otherwise original result
     */
    @WrapOperation(
            method = "processBindings",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/KeyMapping;consumeClick()Z"
            )
    )
    private boolean wrapConsumeClick(KeyMapping key, Operation<Boolean> original) {
        boolean callResult = original.call(key);

        if (callResult) {
            if (ClientPlayerBodyDataStore.cancelOrigins.contains(getInputAction(key).getLastOrigin())) {
                return false;
            }
        }

        return callResult;
    }
}
