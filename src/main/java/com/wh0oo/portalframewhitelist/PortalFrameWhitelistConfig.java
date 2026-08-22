package com.wh0oo.portalframewhitelist;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.state.BlockState;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public final class PortalFrameWhitelistConfig {
    private static final Path CONFIG_PATH =
        FabricLoader.getInstance().getConfigDir().resolve("portal_frame_whitelist.json");

    private static volatile Set<String> whitelist = Collections.emptySet();

    private PortalFrameWhitelistConfig() {
    }

    public static void load() {
        if (!Files.exists(CONFIG_PATH)) {
            writeDefaultConfig();
        }

        Set<String> loaded = new HashSet<>();

        try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
            JsonElement root = JsonParser.parseReader(reader);

            if (root != null && root.isJsonObject()) {
                JsonArray blocks = root.getAsJsonObject().getAsJsonArray("blocks");

                if (blocks != null) {
                    for (JsonElement element : blocks) {
                        if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()) {
                            String id = element.getAsString().trim().toLowerCase(Locale.ROOT);
                            if (!id.isEmpty()) {
                                loaded.add(id);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(
                "Failed to load Portal Frame Whitelist config: " + CONFIG_PATH,
                e
            );
        }

        whitelist = Collections.unmodifiableSet(loaded);
    }

    public static boolean isWhitelisted(BlockState state) {
        String id = BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString();
        return whitelist.contains(id);
    }

    private static void writeDefaultConfig() {
        JsonObject root = new JsonObject();
        root.add("blocks", new JsonArray());

        try {
            Files.createDirectories(CONFIG_PATH.getParent());

            try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
                new GsonBuilder()
                    .setPrettyPrinting()
                    .create()
                    .toJson(root, writer);
            }
        } catch (IOException e) {
            throw new RuntimeException(
                "Failed to create Portal Frame Whitelist config: " + CONFIG_PATH,
                e
            );
        }
    }
}
