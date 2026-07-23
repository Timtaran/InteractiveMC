/*
 * This file is part of InteractiveMC.
 * Licensed under LGPL 3.0.
 *
 * Original code from Vivecraft.
 */
package net.timtaran.interactivemc.util.vr;

import net.minecraft.world.phys.Vec3;
import net.timtaran.interactivemc.body.player.PlayerBodyPart;
import net.timtaran.interactivemc.body.player.data.PlayerBodyPartData;
import net.timtaran.interactivemc.util.vr.data.VRBodyPartData;
import net.timtaran.interactivemc.util.vr.data.VRPose;
import net.timtaran.interactivemc.util.vr.data.VRPoseImpl;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class VRPoseHistoryImpl implements VRPoseHistory {

    // Holds historical VRPose data. The index into here is simply the number of ticks back that data is, with index
    // 0 being 0 ticks back.
    private final LinkedList<PoseData> dataQueue = new LinkedList<>();

    public VRPoseHistoryImpl() {}

    public void addPose(VRPose pose, Vec3 playerPos) {
        this.dataQueue.addFirst(new PoseData(pose, playerPos));
        // + 1 here since index 0 is 0 ticks back.
        if (this.dataQueue.size() > MAX_TICKS_BACK + 1) {
            this.dataQueue.removeLast();
        }
    }

    public void clear() {
        this.dataQueue.clear();
    }

    public boolean isEmpty() {
        return this.dataQueue.isEmpty();
    }

    @Override
    public int ticksOfHistory() {
        return this.dataQueue.size() - 1;
    }

    @Override
    public List<VRPose> getAllHistoricalData() {
        return this.dataQueue.stream().map(PoseData::pose).toList();
    }

    @Override
    public VRPose getHistoricalData(int ticksBack, boolean playerPositionRelative) throws IllegalArgumentException {
        checkTicksBack(ticksBack);
        if (this.dataQueue.size() <= ticksBack) {
            return null;
        }
        return this.dataQueue.get(ticksBack).getPose(playerPositionRelative);
    }

    @Override
    public Vec3 netMovement(
            PlayerBodyPart bodyPart, int maxTicksBack, boolean playerPositionRelative) throws IllegalArgumentException
    {
        checkPartNonNull(bodyPart);
        checkTicksBack(maxTicksBack);
        if (this.dataQueue.size() <= 1) {
            return Vec3.ZERO;
        }
        Vec3 current = this.dataQueue.getFirst().getPos(bodyPart, playerPositionRelative);
        if (current == null) {
            return null;
        }
        Vec3 old = this.dataQueue.get(maxTicksBack).getPos(bodyPart, playerPositionRelative);
        if (old == null) {
            return null;
        }
        return current.subtract(old);
    }

    @Override
    public Vec3 averageVelocity(
            PlayerBodyPart bodyPart, int maxTicksBack, boolean playerPositionRelative) throws IllegalArgumentException
    {
        checkPartNonNull(bodyPart);
        checkTicksBack(maxTicksBack);
        if (this.dataQueue.size() <= 1) {
            return Vec3.ZERO;
        }
        maxTicksBack = getNumTicksBack(maxTicksBack);
        List<Vec3> diffs = new ArrayList<>(maxTicksBack);
        for (int i = 0; i < maxTicksBack; i++) {
            Vec3 newer = this.dataQueue.get(i).getPos(bodyPart, playerPositionRelative);
            Vec3 older = this.dataQueue.get(i + 1).getPos(bodyPart, playerPositionRelative);
            if (newer == null || older == null) {
                break;
            }
            diffs.add(newer.subtract(older));
        }
        if (diffs.isEmpty()) {
            // Return no change if the body part is available but no historical data or null if body part isn't
            // available.
            return this.dataQueue.getFirst().pose.getBodyPartData(bodyPart) != null ? Vec3.ZERO : null;
        }
        return new Vec3(
                diffs.stream().mapToDouble(vec -> vec.x).average().orElse(0),
                diffs.stream().mapToDouble(vec -> vec.y).average().orElse(0),
                diffs.stream().mapToDouble(vec -> vec.z).average().orElse(0)
        );
    }

    private void checkTicksBack(int ticksBack) {
        if (ticksBack < 0 || ticksBack > MAX_TICKS_BACK) {
            throw new IllegalArgumentException("Value must be between 0 and " + MAX_TICKS_BACK + ".");
        }
    }

    private void checkPartNonNull(PlayerBodyPart bodyPart) {
        if (bodyPart == null) {
            throw new IllegalArgumentException("Cannot get data for a null body part!");
        }
    }

    private int getNumTicksBack(int maxTicksBack) {
        if (this.dataQueue.size() <= maxTicksBack) {
            return this.dataQueue.size() - 1;
        } else {
            return maxTicksBack;
        }
    }

    private record PoseData(VRPose pose, Vec3 playerPosition) {
        @Nullable
        public Vec3 getPos(PlayerBodyPart bodyPart, boolean playerPositionRelative) {
            VRBodyPartData bodyPartData = this.pose.getBodyPartData(bodyPart);
            if (bodyPartData == null) {
                return null;
            } else if (playerPositionRelative) {
                return bodyPartData.getPos().subtract(this.playerPosition);
            }
            return bodyPartData.getPos();
        }

        public VRPose getPose(boolean playerPositionRelative) {
            if (playerPositionRelative) {
                return ((VRPoseImpl) this.pose).relativeToPosition(this.playerPosition);
            }
            return this.pose;
        }
    }
}