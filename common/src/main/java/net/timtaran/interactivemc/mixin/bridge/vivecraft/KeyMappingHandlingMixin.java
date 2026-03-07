/*
 * This file is part of InteractiveMC.
 * Licensed under LGPL 3.0.
 */
package net.timtaran.interactivemc.mixin.bridge.vivecraft;

import net.minecraft.client.KeyMapping;
import net.minecraft.world.InteractionHand;
import net.timtaran.interactivemc.body.player.PlayerBodyManager;
import net.timtaran.interactivemc.body.player.PlayerBodyPart;
import net.timtaran.interactivemc.data.ClientDataStore;
import net.timtaran.interactivemc.init.registry.KeyMapRegistry;
import net.timtaran.interactivemc.network.Networking;
import net.timtaran.interactivemc.network.sync.packet.C2SGrabPacket;
import net.timtaran.interactivemc.network.sync.packet.C2SReleasePacket;
import net.timtaran.interactivemc.util.velthoric.VelthoricClientUtils;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.api.data.VRBodyPart;
import org.vivecraft.api.data.VRBodyPartData;
import org.vivecraft.client_vr.provider.MCVR;

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
public class KeyMappingHandlingMixin {
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
    }

    /**
     * Updates the grab/release state for a single hand based on key state.
     * <p>
     * If the grab key is pressed, attempts to grab an object. If the key is released
     * and an object was being held, releases it.
     * </p>
     *
     * @param hand the interaction hand (main or off-hand)
     * @param keyMapping the key mapping to check
     */
    @Unique
    private static void interactivemc$updateGrabState(InteractionHand hand, KeyMapping keyMapping) {
        if (keyMapping.consumeClick()) {
            interactivemc$grab(hand);
        } else if (!keyMapping.isDown() && ClientDataStore.grabbedBodies.get(hand) != null) {
            interactivemc$release(hand);
        }
    }

    /**
     * Attempts to grab an object using the specified hand.
     * <p>
     * Performs a sphere cast from the controller position to find nearby bodies
     * that can be grabbed. Sends a grab packet to the server.
     * </p>
     *
     * @param interactionHand the hand attempting to grab (main or off-hand)
     * @return true if a grabbable object was found nearby, false otherwise
     */
    @Unique
    private static boolean interactivemc$grab(InteractionHand interactionHand) {
        boolean isGrabbing = false;

        if (ClientDataStore.currentPose != null) {
            Vector3f localGrabOffset = PlayerBodyPart.fromInteractionHand(interactionHand).getLocalGrabPointVec3f();
            VRBodyPartData bodyPartData = ClientDataStore.currentPose.getBodyPartData(VRBodyPart.fromInteractionHand(interactionHand));
            if (bodyPartData != null) {
                Quaternionf targetRot = new Quaternionf(bodyPartData.getRotation());

                Vector3f controllerPos = bodyPartData.getPos().toVector3f();
                Vector3f offset = new Vector3f(localGrabOffset);
                targetRot.transform(offset);

                Vector3f targetPos = controllerPos.add(offset);

                for (Integer bodyIndex : VelthoricClientUtils.bodiesAround(targetPos.x, targetPos.y, targetPos.z, PlayerBodyManager.GRAB_RADIUS)) {
                    if (!ClientDataStore.playerControlledBodies.contains(bodyIndex)) {
                        isGrabbing = true;
                        break;
                    }
                }
            }
        }
        System.out.println("sendpacket" + isGrabbing);
        Networking.sendToServer(new C2SGrabPacket(interactionHand));
        return isGrabbing;
    }

    /**
     * Releases the currently grabbed object using the specified hand.
     * <p>
     * Sends a release packet to the server to remove the grab constraint.
     * </p>
     *
     * @param interactionHand the hand to release (main or off-hand)
     */
    @Unique
    private static void interactivemc$release(InteractionHand interactionHand) {
        Networking.sendToServer(new C2SReleasePacket(interactionHand));
    }

//    @ModifyExpressionValue(
//            method = "processBindings",
//            at = @At(
//                    value = "FIELD",
//                    target = "Lorg/vivecraft/client/VivecraftVRMod;keyHotbarNext:Lnet/minecraft/client/KeyMapping;",
//                    opcode = Opcodes.GETFIELD
//            )
//    )
//    private KeyMapping intercept(KeyMapping keyMapping) {
//        return keyMapping;
//    }
}
