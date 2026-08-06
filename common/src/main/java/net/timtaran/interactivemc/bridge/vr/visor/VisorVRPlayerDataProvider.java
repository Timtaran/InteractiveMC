/*
 * This file is part of InteractiveMC.
 * Licensed under LGPL 3.0.
 */
package net.timtaran.interactivemc.bridge.vr.visor;

import net.minecraft.world.entity.player.Player;
import net.timtaran.interactivemc.bridge.vr.VRPlayerDataProvider;
import net.timtaran.interactivemc.util.vr.VRPoseHistory;
import net.timtaran.interactivemc.util.vr.VRPoseHistoryImpl;
import net.timtaran.interactivemc.util.vr.data.VRPose;
import net.timtaran.interactivemc.util.vr.visor.VisorConversions;
import org.jetbrains.annotations.Nullable;
import org.vmstudio.visor.api.VisorAPI;
import org.vmstudio.visor.api.common.player.VRPlayer;
import org.vmstudio.visor.api.common.player.VisorPlayer;

public class VisorVRPlayerDataProvider implements VRPlayerDataProvider {
    private final VRPoseHistoryImpl poseHistory = new VRPoseHistoryImpl();

    public VisorVRPlayerDataProvider() {
        VisorAPI.registerAddon(new InteractiveMCVisorAddon());
    }

    @Override
    public @Nullable VRPose getVrPose(Player player) {
        VRPlayer vrPlayer = VisorAPI.getVRPlayer(player);

        if (vrPlayer == null)
            return null;

        return VisorConversions.toInteractivemcType(vrPlayer.getPoseData(), vrPlayer.isLeftHanded(), vrPlayer.getFullHeightScale());
    }

    @Override
    public boolean isVRPlayer(Player player) {
        VisorPlayer visorPlayer = VisorAPI.getVisorPlayer(player);
        return visorPlayer != null && visorPlayer.isVR();
    }

    @Override
    public VRPoseHistory getPoseHistory(Player player) {
        return this.poseHistory;
    }
}
