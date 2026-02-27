package net.timtaran.interactivemc.util.vivecraft;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;
import org.vivecraft.api.VRAPI;
import org.vivecraft.api.client.VRClientAPI;
import org.vivecraft.api.data.VRPose;
import org.vivecraft.client.ClientVRPlayers;
import org.vivecraft.client.api_impl.VRClientAPIImpl;
import org.vivecraft.client_vr.settings.AutoCalibration;
import org.vivecraft.server.ServerVRPlayers;
import org.vivecraft.server.ServerVivePlayer;

/**
 * Utilities for interacting with ViveCraft without using the API, as it lacks the necessary functions.
 *
 * @author timtaran
 */
public class ViveCraftUtils {

    /**
     * Checks if the given player is a ViveCraft VR player.
     *
     * @param player the player to check
     * @return `true` if the player is a ViveCraft VR player, `false` otherwise
     */
    public static boolean isVRPlayer(Player player) {
        if (player instanceof LocalPlayer)
            return VRClientAPIImpl.INSTANCE.isVRActive();
        else
            return VRAPI.instance().isVRPlayer(player);
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
     * @return the VR player data, or null if the player is not a ViveCraft VR player
     */

    @Nullable
    public static VRPlayerData getVRPlayerData(Player player) {
        if (!isVRPlayer(player)) {
            return null;
        } else if (player instanceof LocalPlayer) {
            VRPose renderPose = VRClientAPI.instance().getWorldRenderPose();
            return new VRPlayerData(
                    VRClientAPI.instance().getWorldScale(),
                    AutoCalibration.getPlayerHeight() / 1.52F,
                    renderPose
            );
        } else if (player instanceof ServerPlayer serverPlayer) {
            ServerVivePlayer vivePlayer = ServerVRPlayers.getVivePlayer(serverPlayer);
            return new VRPlayerData(
                    vivePlayer.worldScale,
                    vivePlayer.heightScale,
                    vivePlayer.asVRPose()
            );
        } else {
            ClientVRPlayers.RotInfo clientRotations = ClientVRPlayers.getInstance().getRotationsForPlayer(player.getUUID());

            return new VRPlayerData(
                    clientRotations.worldScale,
                    clientRotations.heightScale,
                    clientRotations.asVRPose(player.position())
            );
        }
    }
}
