package net.timtaran.interactivemc.util.vivecraft;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.InteractionHand;
import org.vivecraft.api.client.VRClientAPI;
import org.vivecraft.client_vr.provider.ControllerType;

public class VivecraftConversions {
    private VivecraftConversions() {}

    @Environment(EnvType.CLIENT)
    public static InteractionHand controllerTypeToInteractionHand(ControllerType hand) {
        return switch (hand) {
            case RIGHT -> VRClientAPI.instance().isLeftHanded() ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
            case LEFT -> VRClientAPI.instance().isLeftHanded() ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
        };
    }
}
