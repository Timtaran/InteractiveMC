package net.timtaran.interactivemc.body.type;

import com.github.stephengold.joltjni.readonly.QuatArg;
import com.github.stephengold.joltjni.readonly.RVec3Arg;

/**
 * Grab point for {@link net.timtaran.interactivemc.body.player.interaction.GrabInteraction}.
 * @param position position of grab point in body local space
 * @param rotation difference in rotation between grabber body and grab point after grab
 */
public record GrabPoint(RVec3Arg position, QuatArg rotation) {
}
