/*
 * This file is part of Velthoric.
 * Copyright (C) xI-Mx-Ix
 *
 * Modifications Copyright (C) 2026 timtaran
 *
 * Licensed under LGPL 3.0.
 */
package net.timtaran.interactivemc.network;

import dev.architectury.networking.NetworkManager;
import net.timtaran.interactivemc.body.player.packet.*;
import net.xmx.velthoric.network.IVxNetPacket;
import net.xmx.velthoric.network.VxByteBuf;

import java.util.function.Function;

/**
 * The central registry that lists all network packets used by InteractiveMC.
 * <p>
 * This class is responsible for initializing the networking subsystem and
 * assigning unique byte IDs to each packet class per network side.
 * </p>
 *
 * @author xI-Mx-Ix
 */
public class PacketRegistry {

    private static int c2sId = 0;
    private static int s2cId = 0;

    /**
     * Initializes the {@link Networking} system and registers all packet types.
     * <p>
     * This method iterates through all available packet classes and registers them
     * with a side-specific unique ID and their decoder reference.
     * </p>
     * <p>
     * <b>Note:</b> This method must be called during the common initialization phase
     * of the mod (e.g., inside <code>onInitialize</code> or the common setup event).
     * </p>
     */

    public static void registerPackets() {
        Networking.init();

        // --- Client to Server Packets (C2S) ---
        // These packets are sent by the client and handled on the server.

        registerC2S(
                C2SFrameVRPosePacket.class,
                C2SFrameVRPosePacket::decode
        );

        registerC2S(
                C2SGrabPacket.class,
                C2SGrabPacket::decode
        );

        registerC2S(
                C2SReleasePacket.class,
                C2SReleasePacket::decode
        );

        registerC2S(
                C2STriggerStatePacket.class,
                C2STriggerStatePacket::decode
        );

        // --- Server to Client Packets (S2C) ---
        // These packets are sent by the server and handled on the client.

        registerS2C(
                S2CGrabResultPacket.class,
                S2CGrabResultPacket::decode
        );
    }

    /**
     * Internal helper to register a Client-to-Server packet with an automated ID.
     */
    private static <T extends IVxNetPacket> void registerC2S(Class<T> clazz, Function<VxByteBuf, T> decoder) {
        Networking.register(NetworkManager.Side.C2S, c2sId++, clazz, decoder);
    }

    /**
     * Internal helper to register a Server-to-Client packet with an automated ID.
     */
    private static <T extends IVxNetPacket> void registerS2C(Class<T> clazz, Function<VxByteBuf, T> decoder) {
        Networking.register(NetworkManager.Side.S2C, s2cId++, clazz, decoder);
    }
}
