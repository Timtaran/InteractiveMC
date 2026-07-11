package net.timtaran.interactivemc.mixin.bridge.vivecraft;

import net.timtaran.interactivemc.body.player.store.ClientPlayerBodyDataStore;
import net.timtaran.interactivemc.bridge.vivecraft.VRInputActionExtension;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.vivecraft.client_vr.provider.openvr_lwjgl.MCOpenVR;
import org.vivecraft.client_vr.provider.openvr_lwjgl.VRInputAction;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Mixin(value = MCOpenVR.class, remap = false)
public class VRInputActionProcessReorderMixin {
    @Redirect(
            method = "processInputs",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/Map;values()Ljava/util/Collection;"
            )
    )
    private Collection<VRInputAction> reorderInputActions(Map<String, VRInputAction> map) {
        ClientPlayerBodyDataStore.cancelOrigins.clear();
        List<VRInputAction> first = new ArrayList<>();
        List<VRInputAction> rest = new ArrayList<>();

        for (VRInputAction action : map.values()) {
            if (((VRInputActionExtension) action).interactivemc$isInteractivemcBinding()) {
                first.add(action);
            } else {
                rest.add(action);
            }
        }

        first.addAll(rest);
        return first;
    }
}
