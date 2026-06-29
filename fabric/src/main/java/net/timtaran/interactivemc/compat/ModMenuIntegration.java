package net.timtaran.interactivemc.compat;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.timtaran.interactivemc.init.registry.InteractiveMCConfig;

public class ModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parentScreen -> InteractiveMCConfig.getInstance().getConfigScreen(parentScreen);
    }
}