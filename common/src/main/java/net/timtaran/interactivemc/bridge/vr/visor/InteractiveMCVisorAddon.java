/*
 * This file is part of InteractiveMC.
 * Licensed under LGPL 3.0.
 */
package net.timtaran.interactivemc.bridge.vr.visor;

import dev.architectury.platform.Platform;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.timtaran.interactivemc.init.InteractiveMC;
import net.timtaran.interactivemc.init.registry.InteractiveMCConfig;
import net.timtaran.interactivemc.util.InteractiveMCIdentifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.vmstudio.visor.api.VisorAPI;
import org.vmstudio.visor.api.client.input.action.VRActionSet;
import org.vmstudio.visor.api.common.HandType;
import org.vmstudio.visor.api.common.addon.ComponentRegistries;
import org.vmstudio.visor.api.common.addon.VisorAddon;

public class InteractiveMCVisorAddon implements VisorAddon {
    @Override
    public void onAddonLoad() {
        if (Platform.getEnv() == EnvType.CLIENT) {
            clientInit();
        }
    }

    @Environment(EnvType.CLIENT)
    private void clientInit() {
        ComponentRegistries registries = VisorAPI.addonManager().getRegistries();
        VRActionSet gameActions = registries.actionSets().getComponent("game");
        if (gameActions == null) {
            InteractiveMC.LOGGER.error("Could not register grab controls: Visor game action set is missing");
        } else {
            for (HandType hand : HandType.values()) {
                GrabAction grabAction = new GrabAction(gameActions, hand);
                TriggerTouchAction triggerTouchAction = new TriggerTouchAction(gameActions, hand);
                TriggerPressAction triggerPressAction = new TriggerPressAction(gameActions, hand, triggerTouchAction);
                gameActions.addKeyAction(grabAction);
                gameActions.addKeyAction(triggerTouchAction);
                gameActions.addKeyAction(triggerPressAction);
            }
            PoseUpdateTask poseUpdateTask = new PoseUpdateTask(this);
            registries.tasks().registerComponent(poseUpdateTask);
        }
    }

    @Override
    public @NotNull String getAddonId() {
        return "interactivemc";
    }

    @Override
    public @NotNull Component getAddonName() {
        return InteractiveMCIdentifier.getTranslation("visor.addon.name");
    }

    @Override
    public String getModId() {
        return "interactivemc";
    }

    @Override
    public @Nullable Screen createAddonSettingsScreen(@NotNull Screen backScreen) {
        return InteractiveMCConfig.getInstance().getConfigScreen(backScreen);
    }
}
