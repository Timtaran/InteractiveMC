/*
 * This file is part of Velthoric.
 * Copyright (C) xI-Mx-Ix
 *
 * Modifications Copyright (C) 2026 timtaran
 *
 * Licensed under LGPL 3.0.
 */
package net.timtaran.interactivemc.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.timtaran.interactivemc.util.InteractiveMCIdentifier;

/**
 * A raw container payload that transports direct Netty ByteBufs through the Minecraft networking system.
 * <p>
 * This bypasses the object-heavy serialization of standard payloads by treating the
 * entire packet content as a single opaque blob of bytes.
 * </p>
 *
 * @param data The raw Netty buffer containing the packet ID and payload.
 * @author xI-Mx-Ix
 * @author timtaran
 */
public record C2SRawPayload(ByteBuf data) implements CustomPacketPayload {

    public static final Type<C2SRawPayload> TYPE = new Type<>(InteractiveMCIdentifier.get("network_c2s"));

    /**
     * A zero-copy StreamCodec that slices the buffer instead of copying it.
     */
    public static final StreamCodec<RegistryFriendlyByteBuf, C2SRawPayload> CODEC = StreamCodec.of(
            (buf, payload) -> {
                // Write the raw Netty buffer directly into the output
                buf.writeBytes(payload.data);
                // Release the buffer as ownership is transferred to the network stack
                payload.data.release();
            },
            (buf) -> {
                // Create a slice of the remaining readable bytes.
                // This does NOT copy the memory, making it extremely efficient.
                int readable = buf.readableBytes();
                return new C2SRawPayload(buf.readBytes(readable));
            }
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}