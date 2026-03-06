/*
 * This file is part of InteractiveMC.
 * Licensed under LGPL 3.0.
 */
package net.timtaran.interactivemc.mixin.compat.vivecraft;

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
 * Processes all keymappings right before vivecraft right before vivecraft would perform own checks.
 *
 * @author timtaran
 * @see KeyMapRegistry
 */
@Mixin(value = MCVR.class, remap = false)
public class KeyMappingHandlingMixin {
    @Inject(
            method = "processBindings",
            at = @At("HEAD")
    )
    private void interactivemc$processKeymappings(CallbackInfo ci) {
        interactivemc$updateGrabState(InteractionHand.MAIN_HAND, KeyMapRegistry.MAIN_GRAB_KEYMAPPING);
        interactivemc$updateGrabState(InteractionHand.OFF_HAND, KeyMapRegistry.OFF_GRAB_KEYMAPPING);
    }

    @Unique
    private static void interactivemc$updateGrabState(InteractionHand hand, KeyMapping keyMapping) {
        if (keyMapping.consumeClick()) {
            interactivemc$grab(hand);
        } else if (!keyMapping.isDown() && ClientDataStore.grabbedBodies.get(hand) != null) {
            interactivemc$release(hand);
        }
    }

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
