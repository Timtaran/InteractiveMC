package net.timtaran.interactivemc.mixin.bridge.velthoric;

import net.timtaran.interactivemc.body.player.PlayerBodyManager;
import net.xmx.velthoric.core.physics.world.VxPhysicsWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = VxPhysicsWorld.class, remap = false)
public class PhysicsTickEvent {
    @Inject(method = "onPrePhysicsTick", at = @At("TAIL"))
    private void onPrePhysicsTick(CallbackInfo ci) {
        PlayerBodyManager.get((VxPhysicsWorld) (Object) this).onPrePhysicsTick();
    }

    @Inject(method = "onPhysicsTick", at = @At("TAIL"))
    private void onPhysicsTick(CallbackInfo ci) {
        PlayerBodyManager.get((VxPhysicsWorld) (Object) this).onPhysicsTick();
    }
}
