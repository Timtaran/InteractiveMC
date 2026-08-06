package net.timtaran.interactivemc.bridge.vr.visor;

import net.minecraft.client.player.LocalPlayer;
import net.timtaran.interactivemc.body.player.packet.C2SFrameVRPosePacket;
import net.timtaran.interactivemc.body.player.store.ClientPlayerBodyDataStore;
import net.timtaran.interactivemc.network.Networking;
import net.timtaran.interactivemc.util.vr.data.VRPose;
import net.timtaran.interactivemc.util.vr.visor.VisorConversions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.vmstudio.visor.api.VisorAPI;
import org.vmstudio.visor.api.client.player.VRLocalPlayer;
import org.vmstudio.visor.api.client.player.pose.PlayerPoseType;
import org.vmstudio.visor.api.client.tasks.TaskType;
import org.vmstudio.visor.api.client.tasks.VisorTask;
import org.vmstudio.visor.api.common.addon.VisorAddon;

public class PoseUpdateTask extends VisorTask {
    public PoseUpdateTask(VisorAddon owner) {
        super(owner);
    }

    @Override
    protected void onRun(@Nullable LocalPlayer player) {
        VRLocalPlayer localPlayer = VisorAPI.client().getVRLocalPlayer();
        VRPose vrPose = VisorConversions.toInteractivemcType(localPlayer.getPoseData(PlayerPoseType.RENDER), localPlayer.isLeftHanded(), localPlayer.getFullHeightScale());
        ClientPlayerBodyDataStore.currentPose = vrPose;
        Networking.sendToServer(new C2SFrameVRPosePacket(vrPose));
    }

    @Override
    protected void onClear(@Nullable LocalPlayer player) {

    }

    @Override
    public boolean isActive(@Nullable LocalPlayer player) {
        return VisorAPI.clientState().sceneType().isWorld();
    }

    @Override
    public @NotNull String getId() {
        return "interactivemc_pose_update";
    }

    @Override
    public @NotNull TaskType getType() {
        return TaskType.VR_PRE_RENDER;
    }
}
