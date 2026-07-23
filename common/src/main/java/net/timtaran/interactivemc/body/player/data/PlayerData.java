package net.timtaran.interactivemc.body.player.data;

import net.timtaran.interactivemc.util.vr.data.VRPose;

public class PlayerData {
    private VRPose currentVrPose;

    /**
     * The VR poses of the players by UUID from previous packet.
     * Used to predict player movements on data loss.
     */
    private VRPose previousVrPose;

    private boolean poseUsed;

    /**
     * Last pose update time in ns.
     */
    private long poseUpdate;

    private float playerScale;

    public void updateVrPose(VRPose vrPose) {
        this.previousVrPose = this.currentVrPose;
        this.currentVrPose = vrPose;
        this.poseUsed = false;
        this.poseUpdate = System.nanoTime();
    }

    public boolean isPoseUsed() {
        return this.poseUsed;
    }

    public void markPoseAsUsed() {
        this.poseUsed = true;
    }

    public boolean isScaleUpdateRequired(float playerScale) {
        if (this.playerScale == playerScale) {
            return false;
        }

        this.playerScale = playerScale;
        return true;
    }

    public VRPose getCurrentVrPose() {
        return currentVrPose;
    }

    public VRPose getPreviousVrPose() {
        return previousVrPose;
    }

    public float getPlayerScale() {
        return playerScale;
    }

    public long getPoseUpdate() {
        return poseUpdate;
    }

    public PlayerData(VRPose vrPose, float playerScale) {
        this.currentVrPose = vrPose;
        this.previousVrPose = vrPose;
        this.poseUpdate = System.nanoTime();
        this.playerScale = playerScale;
    }
}
