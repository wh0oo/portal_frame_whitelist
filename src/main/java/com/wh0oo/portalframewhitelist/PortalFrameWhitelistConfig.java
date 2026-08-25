package com.wh0oo.portalframewhitelist;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

public final class PortalFrameWhitelistConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger("AnyBlock Portals");

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
        Set<String> validBlockIds = getValidBlockIds();

        try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
            JsonElement root = JsonParser.parseReader(reader);

            if (root != null && root.isJsonObject()) {
                JsonArray blocks = root.getAsJsonObject().getAsJsonArray("blocks");

                if (blocks != null) {
                    for (JsonElement element : blocks) {
                        if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()) {
                            String id = normalizeBlockId(element.getAsString());

                            if (!id.isEmpty()) {
                                if (validBlockIds.contains(id)) {
                                    loaded.add(id);
                                } else {
                                    LOGGER.warn("[AnyBlock Portals] Unknown block ID in config, ignoring: {}", id);
                                }
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

    public static Set<String> getWhitelist() {
        return whitelist;
    }

    public static String normalizeBlockId(String id) {
        return id.trim().toLowerCase(Locale.ROOT);
    }

    public static boolean isKnownBlockId(String id) {
        return getValidBlockIds().contains(normalizeBlockId(id));
    }

    public static boolean addBlock(String id) {
        String normalized = normalizeBlockId(id);

        if (!isKnownBlockId(normalized) || whitelist.contains(normalized)) {
            return false;
        }

        Set<String> updated = new HashSet<>(whitelist);
        updated.add(normalized);
        writeConfig(updated);
        whitelist = Collections.unmodifiableSet(updated);
        return true;
    }

    public static boolean removeBlock(String id) {
        String normalized = normalizeBlockId(id);

        if (!whitelist.contains(normalized)) {
            return false;
        }

        Set<String> updated = new HashSet<>(whitelist);
        updated.remove(normalized);
        writeConfig(updated);
        whitelist = Collections.unmodifiableSet(updated);
        return true;
    }

    private static Set<String> getValidBlockIds() {
        Set<String> validBlockIds = new HashSet<>();

        for (Block block : BuiltInRegistries.BLOCK) {
            validBlockIds.add(BuiltInRegistries.BLOCK.getKey(block).toString());
        }

        return validBlockIds;
    }

    private static void writeConfig(Set<String> blocks) {
        JsonObject root = new JsonObject();
        JsonArray blockArray = new JsonArray();

        for (String id : new TreeSet<>(blocks)) {
            blockArray.add(id);
        }

        root.add("blocks", blockArray);

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
                "Failed to write Portal Frame Whitelist config: " + CONFIG_PATH,
                e
            );
        }
    }

    private static void writeDefaultConfig() {
        writeConfig(Collections.emptySet());
    }
}
