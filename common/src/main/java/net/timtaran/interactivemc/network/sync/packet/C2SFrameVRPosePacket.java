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
 * A packet that contains VR pose.
 *
 * @author timtaran
 */
public class C2SFrameVRPosePacket implements IVxNetPacket {
    private final VRPose pose;

    /**
     * Constructs a new C2S packet.
     *
     * @param pose The VR pose to send.
     */
    public C2SFrameVRPosePacket(VRPose pose) {
        this.pose = pose;
    }

    /**
     * Writes the VR body part data to the buffer.
     * @param buf  The buffer to read from.
     * @param data VR body part data.
     */
    private static void writeBodyPartData(FriendlyByteBuf buf, @Nullable VRBodyPartData data) {
        buf.writeBoolean(data != null);
        if (data == null) return;

        buf.writeVec3(data.getPos());
        buf.writeVec3(data.getDir());
        CommonNetworkHelper.serialize(buf, data.getRotation());
    }

    /**
     * Constructs a new C2S packet.
     * @param buf The buffer to read from.
     * @return    A new instance of the packet.
     */
    private static VRBodyPartData readBodyPartData(FriendlyByteBuf buf) {
        boolean present = buf.readBoolean();
        if (!present) return null;

        return new VRBodyPartDataImpl(buf.readVec3(), buf.readVec3(), CommonNetworkHelper.deserializeVivecraftQuaternion(buf));
    }


    /**
     * Encodes the packet's data into a network buffer.
     *
     * @param buf The buffer to write to.
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
     * Decompresses the data before reconstructing the map.
     *
     * @param buf The buffer to read from.
     * @return A new instance of the packet.
     */
    public static C2SFrameVRPosePacket decode(VxByteBuf buf) {
        boolean isSeated = buf.readBoolean();
        boolean isLeftHanded = buf.readBoolean();
        FBTMode fbtMode = buf.readEnum(FBTMode.class);

        // прочитаем все части в Map (сохраняем по enum-ключу)
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

    @Override
    public void handle(NetworkManager.PacketContext context) {
        PlayerDataStore.vrPoses.put(context.getPlayer().getUUID(), pose);
    }
}
