/*
 * This file is part of InteractiveMC.
 * Licensed under LGPL 3.0.
 */
package net.timtaran.interactivemc.bridge.vr.vivecraft;

import dev.architectury.platform.Platform;
import net.fabricmc.api.EnvType;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.timtaran.interactivemc.bridge.vr.VRPlayerDataProvider;
import net.timtaran.interactivemc.init.registry.ViveRegistry;
import net.timtaran.interactivemc.util.vr.VRPoseHistory;
import net.timtaran.interactivemc.util.vr.VRPoseHistoryImpl;
import net.timtaran.interactivemc.util.vr.data.VRPose;
import net.timtaran.interactivemc.util.vr.vivecraft.VivecraftConversions;
import org.jetbrains.annotations.Nullable;
import org.vivecraft.api.VRAPI;
import org.vivecraft.api.client.VRClientAPI;
import org.vivecraft.client.ClientVRPlayers;
import org.vivecraft.client.api_impl.VRClientAPIImpl;
import org.vivecraft.client_vr.settings.AutoCalibration;
import org.vivecraft.server.ServerVRPlayers;
import org.vivecraft.server.ServerVivePlayer;

public class VivecraftVRPlayerDataProvider implements VRPlayerDataProvider {
    private final VRPoseHistoryImpl poseHistory = new VRPoseHistoryImpl();

    public VivecraftVRPlayerDataProvider() {
        if (Platform.getEnv() == EnvType.CLIENT) {
            ViveRegistry.registerClient();
        }
    }

    /**
     * Retrieves the VR player data for the given player.
     * <p>
     * If the player is not a VR player, returns `null`.
     * <p>
     * If the player is a local player, retrieves data from the VRClientAPI instance.
     * <p>
     * If the player is a server player, retrieves data from the ServerVivePlayer instance.
     * <p>
     * If the player is a client player, retrieves data from the ClientVRPlayers instance.
     *
     * @param player the player to retrieve the VR player data for
     * @return the VR player data, or {@code null} if the player is not a Vivecraft VR player
     */
    @Override
    public @Nullable VRPose getVrPose(Player player) {
        if (!isVRPlayer(player))
            return null;

        org.vivecraft.api.data.VRPose vrPose = null;
        float playerScale = 1f;

        if (player instanceof ServerPlayer serverPlayer) {
            ServerVivePlayer vivePlayer = ServerVRPlayers.getVivePlayer(serverPlayer);
            vrPose = vivePlayer.asVRPose();
            playerScale = vivePlayer.heightScale * vivePlayer.worldScale;
        }

        if (Platform.getEnv() == EnvType.CLIENT) {
            if (player instanceof LocalPlayer) {
                vrPose = VRClientAPI.instance().getWorldRenderPose();
                playerScale = VRClientAPI.instance().getWorldScale() * AutoCalibration.getPlayerHeight() / 1.52F;
            } else {
                ClientVRPlayers.RotInfo clientRotations = ClientVRPlayers.getInstance().getRotationsForPlayer(player.getUUID());


                vrPose = clientRotations.asVRPose(player.position());
                playerScale = clientRotations.heightScale * clientRotations.worldScale;
            }
        }

        if (vrPose == null) {
            return null;
        }

        return VivecraftConversions.toInteractivemcType(vrPose, playerScale);
    }

    @Override
    public boolean isVRPlayer(Player player) {
        if (player instanceof LocalPlayer)
            return VRClientAPIImpl.INSTANCE.isVRActive();
        else
            return VRAPI.instance().isVRPlayer(player);
    }

    @Override
    public VRPoseHistory getPoseHistory(Player player) {
        return this.poseHistory;
    }
}
