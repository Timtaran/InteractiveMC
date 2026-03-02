/*
 * This file is part of InteractiveMC.
 * Licensed under LGPL 3.0.
 */
package net.timtaran.interactivemc.bridge.vivecraft;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.player.LocalPlayer;
import net.timtaran.interactivemc.network.Networking;
import net.timtaran.interactivemc.network.sync.packet.C2SFrameVRPosePacket;
import net.timtaran.interactivemc.data.ClientDataStore;
import org.vivecraft.api.client.Tracker;
import org.vivecraft.api.client.VRClientAPI;
import org.vivecraft.api.data.VRPose;

/**
 * Tracker for monitoring the player's body movements in ViveCraft VR.
 *
 * @author timtaran
 */
@Environment(EnvType.CLIENT)
public class PlayerBodyTracker implements Tracker {
    @Override
    public ProcessType processType() {
        return ProcessType.PER_FRAME;
    }

    @Override
    public boolean isActive(LocalPlayer localPlayer) {
        return localPlayer != null;
    }

    @Override
    public void activeProcess(LocalPlayer localPlayer) {
        // todo optimize?
        VRPose renderPose = VRClientAPI.instance().getWorldRenderPose();
        ClientDataStore.currentPose = renderPose;
        Networking.sendToServer(new C2SFrameVRPosePacket(renderPose));
    }
}
