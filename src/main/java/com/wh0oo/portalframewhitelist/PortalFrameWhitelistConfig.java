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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public final class PortalFrameWhitelistConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger("AnyBlock Portals");

    private static final Path CONFIG_PATH =
        FabricLoader.getInstance().getConfigDir().resolve("portal_frame_whitelist.json");

    private static volatile Set<String> whitelistIds = Collections.emptySet();
    private static volatile Set<Block> whitelistedBlocks = Collections.emptySet();

    private PortalFrameWhitelistConfig() {
    }

    public static void load() {
        if (!Files.exists(CONFIG_PATH)) {
            writeDefaultConfig();
        }

        Map<String, Block> blockRegistry = getBlockRegistryMap();
        Set<String> loadedIds = new HashSet<>();
        Set<Block> loadedBlocks = new HashSet<>();

        try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
            JsonElement root = JsonParser.parseReader(reader);

            if (root != null && root.isJsonObject()) {
                JsonArray blocks = root.getAsJsonObject().getAsJsonArray("blocks");

                if (blocks != null) {
                    for (JsonElement element : blocks) {
                        if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()) {
                            String id = normalizeBlockId(element.getAsString());

                            if (!id.isEmpty()) {
                                Block block = blockRegistry.get(id);

                                if (block != null) {
                                    loadedIds.add(id);
                                    loadedBlocks.add(block);
                                } else {
                                    LOGGER.warn("[AnyBlock Portals] Unknown block ID in config, ignoring: {}", id);
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("[AnyBlock Portals] Failed to load portal_frame_whitelist.json", e);
            throw new RuntimeException(
                "Failed to load Portal Frame Whitelist config: " + CONFIG_PATH,
                e
            );
        }

        whitelistIds = Collections.unmodifiableSet(loadedIds);
        whitelistedBlocks = Collections.unmodifiableSet(loadedBlocks);
    }

    public static boolean isWhitelisted(BlockState state) {
        return whitelistedBlocks.contains(state.getBlock());
    }

    public static Set<String> getWhitelist() {
        return whitelistIds;
    }

    public static String normalizeBlockId(String id) {
        return id.trim().toLowerCase(Locale.ROOT);
    }

    public static boolean isKnownBlockId(String id) {
        return getBlockRegistryMap().containsKey(normalizeBlockId(id));
    }

    public static boolean addBlock(String id) {
        String normalized = normalizeBlockId(id);
        Map<String, Block> blockRegistry = getBlockRegistryMap();
        Block block = blockRegistry.get(normalized);

        if (block == null || whitelistIds.contains(normalized)) {
            return false;
        }

        Set<String> updatedIds = new HashSet<>(whitelistIds);
        Set<Block> updatedBlocks = new HashSet<>(whitelistedBlocks);

        updatedIds.add(normalized);
        updatedBlocks.add(block);

        writeConfig(updatedIds);

        whitelistIds = Collections.unmodifiableSet(updatedIds);
        whitelistedBlocks = Collections.unmodifiableSet(updatedBlocks);
        return true;
    }

    public static boolean removeBlock(String id) {
        String normalized = normalizeBlockId(id);

        if (!whitelistIds.contains(normalized)) {
            return false;
        }

        Map<String, Block> blockRegistry = getBlockRegistryMap();
        Block block = blockRegistry.get(normalized);

        Set<String> updatedIds = new HashSet<>(whitelistIds);
        Set<Block> updatedBlocks = new HashSet<>(whitelistedBlocks);

        updatedIds.remove(normalized);

        if (block != null) {
            updatedBlocks.remove(block);
        }

        writeConfig(updatedIds);

        whitelistIds = Collections.unmodifiableSet(updatedIds);
        whitelistedBlocks = Collections.unmodifiableSet(updatedBlocks);
        return true;
    }

    private static Map<String, Block> getBlockRegistryMap() {
        Map<String, Block> blocks = new HashMap<>();

        for (Block block : BuiltInRegistries.BLOCK) {
            blocks.put(BuiltInRegistries.BLOCK.getKey(block).toString(), block);
        }

        return blocks;
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
            LOGGER.error("[AnyBlock Portals] Failed to write portal_frame_whitelist.json", e);
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
