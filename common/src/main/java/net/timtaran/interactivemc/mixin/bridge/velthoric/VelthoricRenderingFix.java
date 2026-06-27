package net.timtaran.interactivemc.mixin.bridge.velthoric;

import com.github.stephengold.joltjni.RVec3;
import net.xmx.velthoric.core.body.client.VxClientBodyDataContainer;
import net.xmx.velthoric.core.body.client.VxClientBodyInterpolator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// todo: remove after https://github.com/velthoric/Velthoric/issues/66 fix
@Mixin(value = VxClientBodyInterpolator.class, remap = false)
public class VelthoricRenderingFix {
    @Inject(
            method = "interpolatePosition(Lnet/xmx/velthoric/core/body/client/VxClientBodyDataContainer;IFLcom/github/stephengold/joltjni/RVec3;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void interpolatePosition(
            VxClientBodyDataContainer c,
            int i,
            float partialTicks,
            RVec3 outPos,
            CallbackInfo ci
    ) {
        outPos.set(
                c.state1_posX.get(i),
                c.state1_posY.get(i),
                c.state1_posZ.get(i)
        );
        ci.cancel();
    }
}
