/*
 * This file is part of InteractiveMC.
 * Licensed under LGPL 3.0.
 *
 * Original code from Vivecraft.
 */
package net.timtaran.interactivemc.util.vr.data;

import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionfc;

public record VRBodyPartDataImpl(Vec3 pos, Vec3 dir, Quaternionfc rot) implements VRBodyPartData {

    @Override
    public Vec3 getPos() {
        return this.pos;
    }

    @Override
    public Vec3 getDir() {
        return this.dir;
    }

    @Override
    public Quaternionfc getRotation() {
        return this.rot;
    }

    @Override
    public String toString() {
        return """
            Position: %s
            Direction: %s
            Rotation: %s
            """.formatted(this.pos, this.dir, this.rot);
    }
}