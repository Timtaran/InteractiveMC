package net.timtaran.interactivemc.mixin.bridge.vr.vivecraft;

import net.minecraft.client.KeyMapping;
import net.minecraft.world.InteractionHand;
import net.timtaran.interactivemc.body.player.interaction.GrabInteraction;
import net.timtaran.interactivemc.body.player.store.ClientPlayerBodyDataStore;
import net.timtaran.interactivemc.bridge.vr.vivecraft.VRInputActionExtension;
import net.timtaran.interactivemc.bridge.vr.vivecraft.VivecraftKeyMapRegistry;
import net.timtaran.interactivemc.util.vr.vivecraft.VivecraftConversions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.vivecraft.client_vr.provider.ControllerType;
import org.vivecraft.client_vr.provider.openvr_lwjgl.VRInputAction;

@Mixin(value = VRInputAction.class, remap = false)
public abstract class VRInputActionMixin implements VRInputActionExtension {
    @Shadow
    public abstract long getLastOrigin();

    @Unique
    private boolean interactivemc$interactivemcBinding;

    @Inject(
            method = "<init>",
            at = @At("TAIL")
    )
    public void saveActionForIMCBindings(CallbackInfo ci) {
        KeyMapping keyMapping = ((VRInputActionAccessor) this).getKeyBinding();

        interactivemc$interactivemcBinding =
                keyMapping == VivecraftKeyMapRegistry.MAIN_GRAB_KEYMAPPING
                        || keyMapping == VivecraftKeyMapRegistry.OFF_GRAB_KEYMAPPING
                        || keyMapping == VivecraftKeyMapRegistry.MAIN_TRIGGER_KEYMAPPING
                        || keyMapping == VivecraftKeyMapRegistry.OFF_TRIGGER_KEYMAPPING
                        || keyMapping == VivecraftKeyMapRegistry.MAIN_TRIGGER_TOUCH_KEYMAPPING
                        || keyMapping == VivecraftKeyMapRegistry.OFF_TRIGGER_TOUCH_KEYMAPPING;
    }

    @Override
    @Unique
    public boolean interactivemc$isInteractivemcBinding() {
        return interactivemc$interactivemcBinding;
    }

    @Inject(
            method = "isHanded",
            at = @At("HEAD"),
            cancellable = true
    )
    public void interactivemc$isHanded(CallbackInfoReturnable<Boolean> cir) {
        if (interactivemc$interactivemcBinding) {
            cir.setReturnValue(true);
        }
    }

    @Inject(
            method = "pressBinding",
            at = @At("HEAD"),
            cancellable = true
    )
    private void preventInputAction(ControllerType hand, CallbackInfo ci) {
        if (interactivemc$interactivemcBinding) {
            KeyMapping keyMapping = ((VRInputActionAccessor) this).getKeyBinding();

            InteractionHand interactionHand = VivecraftConversions.controllerTypeToInteractionHand(hand);
            System.out.println(((VRInputAction) (Object) this).name + interactionHand + hand);

            boolean isGrabbing = ClientPlayerBodyDataStore.isGrabbing(interactionHand);
            boolean canGrab = GrabInteraction.canGrabClient(interactionHand);

            boolean isGrabKey =
                    keyMapping == VivecraftKeyMapRegistry.MAIN_GRAB_KEYMAPPING
                            || keyMapping == VivecraftKeyMapRegistry.OFF_GRAB_KEYMAPPING;

            boolean isTriggerKey =
                    keyMapping == VivecraftKeyMapRegistry.MAIN_TRIGGER_KEYMAPPING
                            || keyMapping == VivecraftKeyMapRegistry.OFF_TRIGGER_KEYMAPPING
                            || keyMapping == VivecraftKeyMapRegistry.MAIN_TRIGGER_TOUCH_KEYMAPPING
                            || keyMapping == VivecraftKeyMapRegistry.OFF_TRIGGER_TOUCH_KEYMAPPING;

            if ((isGrabKey && (isGrabbing || canGrab)) || (isTriggerKey && isGrabbing)) {
                ClientPlayerBodyDataStore.cancelOrigins.add(getLastOrigin());
            }

            return;
        }

        if (ClientPlayerBodyDataStore.cancelOrigins.contains(getLastOrigin())) {
            ci.cancel();
        }
    }
}
