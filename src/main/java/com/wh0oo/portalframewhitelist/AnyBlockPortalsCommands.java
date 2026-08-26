package com.wh0oo.portalframewhitelist;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.permissions.Permissions;

import java.util.Set;
import java.util.TreeSet;

public final class AnyBlockPortalsCommands {
    private AnyBlockPortalsCommands() {
    }

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, buildContext, selection) -> {
            dispatcher.register(
                Commands.literal("anyblockportals")
                    .requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_MODERATOR))

                    .then(Commands.literal("add")
                        .then(Commands.argument("block_id", StringArgumentType.greedyString())
                            .executes(context -> {
                                String entered = StringArgumentType.getString(context, "block_id");
                                String id = PortalFrameWhitelistConfig.normalizeBlockId(entered);

                                if (!PortalFrameWhitelistConfig.isKnownBlockId(id)) {
                                    context.getSource().sendFailure(
                                        Component.literal(
                                            "[AnyBlock Portals] Unknown block ID: " + id
                                                + ". Nothing was added. Use a full namespaced ID such as minecraft:pink_wool."
                                        )
                                    );
                                    return 0;
                                }

                                try {
                                    boolean added = PortalFrameWhitelistConfig.addBlock(id);

                                    if (!added) {
                                        context.getSource().sendSuccess(
                                            () -> Component.literal(
                                                "[AnyBlock Portals] " + id
                                                    + " is already in the portal frame whitelist. No changes were made."
                                            ),
                                            false
                                        );
                                        return 1;
                                    }

                                    context.getSource().sendSuccess(
                                        () -> Component.literal(
                                            "[AnyBlock Portals] Added " + id
                                                + " to the portal frame whitelist. The config was saved and the change is active immediately."
                                        ),
                                        true
                                    );
                                    return 1;
                                } catch (RuntimeException e) {
                                    context.getSource().sendFailure(
                                        Component.literal(
                                            "[AnyBlock Portals] Failed to add " + id
                                                + " because the config file could not be written. No in-memory change was made."
                                        )
                                    );
                                    return 0;
                                }
                            })))

                    .then(Commands.literal("remove")
                        .then(Commands.argument("block_id", StringArgumentType.greedyString())
                            .executes(context -> {
                                String entered = StringArgumentType.getString(context, "block_id");
                                String id = PortalFrameWhitelistConfig.normalizeBlockId(entered);

                                try {
                                    boolean removed = PortalFrameWhitelistConfig.removeBlock(id);

                                    if (!removed) {
                                        context.getSource().sendSuccess(
                                            () -> Component.literal(
                                                "[AnyBlock Portals] " + id
                                                    + " is not currently in the portal frame whitelist. No changes were made."
                                            ),
                                            false
                                        );
                                        return 1;
                                    }

                                    context.getSource().sendSuccess(
                                        () -> Component.literal(
                                            "[AnyBlock Portals] Removed " + id
                                                + " from the portal frame whitelist. The config was saved and the change is active immediately."
                                        ),
                                        true
                                    );
                                    return 1;
                                } catch (RuntimeException e) {
                                    context.getSource().sendFailure(
                                        Component.literal(
                                            "[AnyBlock Portals] Failed to remove " + id
                                                + " because the config file could not be written. No in-memory change was made."
                                        )
                                    );
                                    return 0;
                                }
                            })))

                    .then(Commands.literal("list")
                        .executes(context -> {
                            Set<String> blocks = new TreeSet<>(PortalFrameWhitelistConfig.getWhitelist());

                            if (blocks.isEmpty()) {
                                context.getSource().sendSuccess(
                                    () -> Component.literal(
                                        "[AnyBlock Portals] The portal frame whitelist is empty. Only vanilla portal frame blocks are currently allowed."
                                    ),
                                    false
                                );
                                return 1;
                            }

                            context.getSource().sendSuccess(
                                () -> Component.literal(
                                    "[AnyBlock Portals] Portal frame whitelist contains "
                                        + blocks.size() + " block"
                                        + (blocks.size() == 1 ? "" : "s") + ": "
                                        + String.join(", ", blocks)
                                ),
                                false
                            );
                            return 1;
                        }))

                    .then(Commands.literal("reload")
                        .executes(context -> {
                            Set<String> previous = PortalFrameWhitelistConfig.getWhitelist();

                            try {
                                PortalFrameWhitelistConfig.load();
                                int count = PortalFrameWhitelistConfig.getWhitelist().size();

                                context.getSource().sendSuccess(
                                    () -> Component.literal(
                                        "[AnyBlock Portals] Reloaded portal_frame_whitelist.json successfully. "
                                            + count + " configured block"
                                            + (count == 1 ? " is" : "s are")
                                            + " now active."
                                    ),
                                    true
                                );
                                return 1;
                            } catch (RuntimeException e) {
                                int count = previous.size();

                                context.getSource().sendFailure(
                                    Component.literal(
                                        "[AnyBlock Portals] Reload failed. The config could not be parsed or read. "
                                            + "The last-known-good whitelist remains active with "
                                            + count + " block" + (count == 1 ? "" : "s") + "."
                                    )
                                );
                                return 0;
                            }
                        }))
            );
        });
    }
}
