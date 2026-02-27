package net.timtaran.interactivemc.body;

import com.github.stephengold.joltjni.GroupFilterTable;
import net.timtaran.interactivemc.body.player.PlayerBodyPart;

public class GroupFilters {
    public static final int PLAYER_BODY_GROUP = 0; // todo replace with better collision disabling system to avoid multiplayer issues (e.g. multiple players grabbing body)
    public static final GroupFilterTable PLAYER_BODY_FILTER = new GroupFilterTable(PlayerBodyPart.values().length);
}
