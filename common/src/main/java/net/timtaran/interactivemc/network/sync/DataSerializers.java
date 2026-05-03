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
 * Registry for custom data serializers used in network synchronization.
 * <p>
 * This class manages all custom data types that need to be serialized and
 * transmitted over the network. Each serializer is assigned a unique ID for lookup.
 *
 * @author timtaran
 */
public class DataSerializers {
    /**
     * Registry mapping serializer IDs to their corresponding serializer instances.
     */
    private static final Int2ObjectMap<VxDataSerializer<?>> REGISTRY = new Int2ObjectOpenHashMap<>();
    /**
     * Counter for assigning unique IDs to new serializers.
     */
    private static int nextId = 0;

    /**
     * Serializer for {@link PlayerBodyPart} enum.
     * <p>
     * Serializes and deserializes player body part types over the network.
     * </p>
     */
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

    /**
     * Registers a new data serializer and assigns it a unique ID.
     *
     * @param serializer the serializer to register
     * @param <T>        the type of data this serializer handles
     * @return the registered serializer
     */
    private static <T> VxDataSerializer<T> register(VxDataSerializer<T> serializer) {
        REGISTRY.put(nextId++, serializer);
        return serializer;
    }

    /**
     * Retrieves a registered serializer by its ID.
     *
     * @param id the serializer ID
     * @return the serializer, or null if not found
     */
    public static VxDataSerializer<?> get(int id) {
        return REGISTRY.get(id);
    }
}
