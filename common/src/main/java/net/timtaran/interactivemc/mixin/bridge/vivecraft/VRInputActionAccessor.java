package net.timtaran.interactivemc.mixin.bridge.vivecraft;

import net.minecraft.client.KeyMapping;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.vivecraft.client_vr.provider.openvr_lwjgl.VRInputAction;

@Mixin(value = VRInputAction.class, remap = false)
public interface VRInputActionAccessor {
    @Accessor
    KeyMapping getKeyBinding();
}
