/*
 * This file is part of Disable Elytra Outside The End.
 * Licensed under LGPL 3.0.
 *
 * Copyright (c) 2025 timtaran
 */
package net.timtaran.interactivemc.init.registry;

import com.google.gson.GsonBuilder;
import dev.architectury.injectables.annotations.ExpectPlatform;
import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.StringControllerBuilder;
import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.timtaran.interactivemc.init.InteractiveMC;
import net.timtaran.interactivemc.util.InteractiveMCIdentifier;

import java.nio.file.Path;

/**
 * The main config class for the mod.
 *
 * @author timtaran
 */
public class InteractiveMCConfig {
    private static final String DEFAULT_TEST_VALUE = "halo";

    public static ConfigClassHandler<InteractiveMCConfig> HANDLER = ConfigClassHandler.createBuilder(InteractiveMCConfig.class)
            .id(InteractiveMCIdentifier.get("config"))
            .serializer(config -> GsonConfigSerializerBuilder.create(config)
                    .setPath(getConfigDirectory().resolve(InteractiveMC.MOD_ID + ".json5"))
                    .appendGsonBuilder(GsonBuilder::setPrettyPrinting)
                    .setJson5(true)
                    .build())
            .build();

    @SerialEntry(comment = "Test Value")
    public String testValue = DEFAULT_TEST_VALUE;

    public InteractiveMCConfig(
            String testValue
    ) {
        this.testValue = testValue;
    }

    public InteractiveMCConfig() {
    }

    @ExpectPlatform
    public static Path getConfigDirectory() {
        throw new AssertionError();
    }

    /**
     * Gets the singleton instance of the config.
     *
     * @return the config instance
     */
    public static InteractiveMCConfig getInstance() {
        return HANDLER.instance();
    }

    /**
     * Saves the config and updates the global storage.
     */
    public static void save() {
        HANDLER.save();
    }

    /**
     * Loads the config and updates the global storage.
     */
    public static void load() {
        HANDLER.load();
    }

    /**
     * Generates the config screen using YetAnotherConfigLib.
     *
     * @param parentScreen the parent screen
     * @return the config screen
     */
    public Screen getConfigScreen(Screen parentScreen) {
        return YetAnotherConfigLib.createBuilder()
                .title(Component.literal("Disable Elytra Outside The End"))
                .category(ConfigCategory.createBuilder()
                        .name(InteractiveMCIdentifier.getTranslation("config.categories.client"))
                        .group(OptionGroup.createBuilder()
                                .name(InteractiveMCIdentifier.getTranslation("config.groups.base"))
                                .option(
                                        Option.<String>createBuilder()
                                                .name(InteractiveMCIdentifier.getTranslation("config.options.testvalue"))
                                                .binding(
                                                        DEFAULT_TEST_VALUE,
                                                        () -> this.testValue,
                                                        newVal -> this.testValue = newVal
                                                )
                                                .controller(StringControllerBuilder::create)
                                                .build()
                                )
                                .build()
                        )
                        .build())
                .save(InteractiveMCConfig::save)
                .build().generateScreen(parentScreen);
    }
}