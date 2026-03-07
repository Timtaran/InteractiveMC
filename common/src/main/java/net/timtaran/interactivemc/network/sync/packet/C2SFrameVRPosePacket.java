/*
 * This file is part of InteractiveMC.
 * Licensed under LGPL 3.0.
 */
package net.timtaran.interactivemc.network.sync.packet;

import dev.architectury.networking.NetworkManager;
import net.minecraft.network.FriendlyByteBuf;
import net.timtaran.interactivemc.data.PlayerDataStore;
import net.xmx.velthoric.network.IVxNetPacket;
import net.xmx.velthoric.network.VxByteBuf;
import org.jetbrains.annotations.Nullable;
import org.vivecraft.api.data.FBTMode;
import org.vivecraft.api.data.VRBodyPart;
import org.vivecraft.api.data.VRBodyPartData;
import org.vivecraft.api.data.VRPose;
import org.vivecraft.common.api_impl.data.VRBodyPartDataImpl;
import org.vivecraft.common.api_impl.data.VRPoseImpl;
import org.vivecraft.common.network.CommonNetworkHelper;

import java.util.EnumMap;
import java.util.Map;

/**
 * Client-to-Server packet containing the player's current VR pose.
 * <p>
 * This packet is sent every frame from the client to transmit the current position
 * and rotation data for all VR body parts. The server stores this data for physics
 * simulation and network replication to other players.
 *
 * @author timtaran
 */
public class C2SFrameVRPosePacket implements IVxNetPacket {
    /** The current VR pose data containing all body part positions and rotations. */
    private final VRPose pose;

    /**
     * Constructs a new VR pose packet.
     *
     * @param pose the VR pose containing current body part data
     */
    public C2SFrameVRPosePacket(VRPose pose) {
        this.pose = pose;
    }

    /**
     * Writes VR body part data to the network buffer.
     * <p>
     * If the data is null, a boolean flag is written instead. Otherwise, the position,
     * direction, and rotation are serialized.
     *
     * @param buf the buffer to write to
     * @param data the body part data to serialize, or null if not present
     */
    private static void writeBodyPartData(FriendlyByteBuf buf, @Nullable VRBodyPartData data) {
        buf.writeBoolean(data != null);
        if (data == null) return;

        buf.writeVec3(data.getPos());
        buf.writeVec3(data.getDir());
        CommonNetworkHelper.serialize(buf, data.getRotation());
    }

    /**
     * Reads VR body part data from the network buffer.
     *
     * @param buf the buffer to read from
     * @return the deserialized body part data, or null if not present
     */
    private static VRBodyPartData readBodyPartData(FriendlyByteBuf buf) {
        boolean present = buf.readBoolean();
        if (!present) return null;

        return new VRBodyPartDataImpl(buf.readVec3(), buf.readVec3(), CommonNetworkHelper.deserializeVivecraftQuaternion(buf));
    }

    /**
     * Encodes the packet's VR pose data into a network buffer.
     * <p>
     * Serializes all body parts, seated state, handedness, and FBT mode.
     *
     * @param buf the buffer to write to
     */
    @Override
    public void encode(VxByteBuf buf) {
        buf.writeBoolean(pose.isSeated());
        buf.writeBoolean(pose.isLeftHanded());

        buf.writeEnum(pose.getFBTMode());

        for (VRBodyPart part : VRBodyPart.values()) {
            writeBodyPartData(buf, pose.getBodyPartData(part));
        }
    }

    /**
     * Decodes the packet from a network buffer.
     * <p>
     * Reconstructs the VR pose from the serialized data by reading all body parts
     * and player state flags.
     *
     * @param buf the buffer to read from
     * @return a new instance of the VR pose packet with decoded data
     */
    public static C2SFrameVRPosePacket decode(VxByteBuf buf) {
        boolean isSeated = buf.readBoolean();
        boolean isLeftHanded = buf.readBoolean();
        FBTMode fbtMode = buf.readEnum(FBTMode.class);

        // Read all body parts into a map (stored by enum key)
        Map<VRBodyPart, VRBodyPartData> map = new EnumMap<>(VRBodyPart.class);
        for (VRBodyPart part : VRBodyPart.values()) {
            map.put(part, readBodyPartData(buf));
        }

        return new C2SFrameVRPosePacket(
                new VRPoseImpl(
                    map.get(VRBodyPart.HEAD),
                    map.get(VRBodyPart.MAIN_HAND),
                    map.get(VRBodyPart.OFF_HAND),
                    map.get(VRBodyPart.RIGHT_FOOT),
                    map.get(VRBodyPart.LEFT_FOOT),
                    map.get(VRBodyPart.WAIST),
                    map.get(VRBodyPart.RIGHT_KNEE),
                    map.get(VRBodyPart.LEFT_KNEE),
                    map.get(VRBodyPart.RIGHT_ELBOW),
                    map.get(VRBodyPart.LEFT_ELBOW),
                    isSeated,
                    isLeftHanded,
                    fbtMode
            )
        );
    }

    /**
     * Handles the VR pose packet on the server side.
     * <p>
     * Updates the server-side VR pose data store with the player's current pose,
     * which is then used for physics simulation and replication to other players.
     *
     * @param context the packet context containing the player and network info
     */
    @Override
    public void handle(NetworkManager.PacketContext context) {
        PlayerDataStore.vrPoses.put(context.getPlayer().getUUID(), pose);
    }
}
