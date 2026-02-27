/*
 * This file is part of InteractiveMC.
 * Licensed under LGPL 3.0.
 */
package net.timtaran.interactivemc.network.sync;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.timtaran.interactivemc.body.player.PlayerBodyPart;
import net.xmx.velthoric.core.network.synchronization.VxDataSerializer;
import net.xmx.velthoric.network.VxByteBuf;

/**
 * Registry for data serializers used in network synchronization.
 *
 * @author timtaran
 */
public class DataSerializers {
    private static final Int2ObjectMap<VxDataSerializer<?>> REGISTRY = new Int2ObjectOpenHashMap<>();
    private static int nextId = 0;

    public static final VxDataSerializer<PlayerBodyPart> BODY_PART = register(new VxDataSerializer<>() {
        @Override
        public void write(VxByteBuf buf, PlayerBodyPart value) {
            buf.writeEnum(value);
        }

        @Override
        public PlayerBodyPart read(VxByteBuf buf) {
            return buf.readEnum(PlayerBodyPart.class);
        }

        @Override
        public PlayerBodyPart copy(PlayerBodyPart value) {
            return value;
        }
    });

    private DataSerializers() {
    }

    private static <T> VxDataSerializer<T> register(VxDataSerializer<T> serializer) {
        REGISTRY.put(nextId++, serializer);
        return serializer;
    }

    public static VxDataSerializer<?> get(int id) {
        return REGISTRY.get(id);
    }
}
