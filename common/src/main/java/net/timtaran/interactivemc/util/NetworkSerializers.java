package net.timtaran.interactivemc.util;

import net.minecraft.network.FriendlyByteBuf;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;

public class NetworkSerializers {
    public static void serialize(FriendlyByteBuf buffer, Quaternionfc quat) {
        buffer.writeFloat(quat.w());
        buffer.writeFloat(quat.x());
        buffer.writeFloat(quat.y());
        buffer.writeFloat(quat.z());
    }

    public static Quaternionf deserialize(FriendlyByteBuf buffer) {
        float w = buffer.readFloat();
        return new Quaternionf(buffer.readFloat(), buffer.readFloat(), buffer.readFloat(), w);
    }

}
