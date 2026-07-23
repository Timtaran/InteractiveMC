/*
 * This file is part of InteractiveMC.
 * Licensed under LGPL 3.0.
 */
package net.timtaran.interactivemc.body.player.packet;

import dev.architectury.networking.NetworkManager;
import net.minecraft.network.FriendlyByteBuf;
import net.timtaran.interactivemc.body.player.PlayerBodyPart;
import net.timtaran.interactivemc.body.player.store.PlayerBodyDataStore;
import net.timtaran.interactivemc.util.NetworkSerializers;
import net.timtaran.interactivemc.util.vr.data.VRBodyPartDataImpl;
import net.timtaran.interactivemc.util.vr.data.VRPose;
import net.timtaran.interactivemc.util.vr.data.VRPoseImpl;
import net.xmx.velthoric.network.IVxNetPacket;
import net.xmx.velthoric.network.VxByteBuf;
import org.jetbrains.annotations.Nullable;
import net.timtaran.interactivemc.util.vr.data.VRBodyPartData;

import java.util.EnumMap;
import java.util.Map;

/**
 * Client-to-Server packet for updating the player's VR pose.
 * <p>
 * This packet is sent every frame from the client to transmit the current position
 * and rotation data for all VR body parts. The server stores this data for physics
 * simulation and network replication to other players.
 *
 * @author timtaran
 */
public class C2SFrameVRPosePacket implements IVxNetPacket {
    /**
     * The current VR pose data containing all body part positions and rotations.
     */
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
     * @param buf  the buffer to write to
     * @param data the body part data to serialize, or null if not present
     */
    private static void writeBodyPartData(FriendlyByteBuf buf, @Nullable VRBodyPartData data) {
        buf.writeBoolean(data != null);
        if (data == null) return;

        buf.writeVec3(data.getPos());
        buf.writeVec3(data.getDir());
        NetworkSerializers.serialize(buf, data.getRotation());
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

        return new VRBodyPartDataImpl(buf.readVec3(), buf.readVec3(), NetworkSerializers.deserialize(buf));
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
        // Read all body parts into a map (stored by enum key)
        Map<PlayerBodyPart, VRBodyPartData> map = new EnumMap<>(PlayerBodyPart.class);
        for (PlayerBodyPart part : PlayerBodyPart.values()) {
            map.put(part, readBodyPartData(buf));
        }

        return new C2SFrameVRPosePacket(
                new VRPoseImpl(
                        map.get(PlayerBodyPart.HEAD),
                        map.get(PlayerBodyPart.MAIN_HAND),
                        map.get(PlayerBodyPart.OFF_HAND),
                        buf.readBoolean(),
                        buf.readFloat()
                )
        );
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
        for (PlayerBodyPart part : PlayerBodyPart.values()) {
            writeBodyPartData(buf, pose.getBodyPartData(part));
        }

        buf.writeBoolean(pose.isLeftHanded());
        buf.writeFloat(pose.getPlayerScale());
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
        PlayerBodyDataStore.playerData.get(context.getPlayer().getUUID()).updateVrPose(pose);
    }
}
