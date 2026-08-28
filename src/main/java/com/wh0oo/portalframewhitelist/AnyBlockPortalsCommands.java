package com.wh0oo.portalframewhitelist;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceArgument;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.world.level.block.Block;

import java.util.Locale;
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
                        .then(Commands.argument("block_id", ResourceArgument.resource(buildContext, Registries.BLOCK))
                            .suggests((context, builder) -> {
                                String remaining = builder.getRemaining().toLowerCase(Locale.ROOT);

                                for (Block block : BuiltInRegistries.BLOCK) {
                                    String id = BuiltInRegistries.BLOCK.getKey(block).toString();

                                    if (id.startsWith(remaining)) {
                                        builder.suggest(id);
                                    }
                                }

                                return builder.buildFuture();
                            })
                            .executes(context -> {
                                Block block = ResourceArgument
                                    .getResource(context, "block_id", Registries.BLOCK)
                                    .value();

                                String id = BuiltInRegistries.BLOCK.getKey(block).toString();

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
                        .then(Commands.argument("block_id", ResourceArgument.resource(buildContext, Registries.BLOCK))
                            .suggests((context, builder) -> {
                                String remaining = builder.getRemaining().toLowerCase(Locale.ROOT);

                                for (String id : new TreeSet<>(PortalFrameWhitelistConfig.getWhitelist())) {
                                    if (id.startsWith(remaining)) {
                                        builder.suggest(id);
                                    }
                                }

                                return builder.buildFuture();
                            })
                            .executes(context -> {
                                Block block = ResourceArgument
                                    .getResource(context, "block_id", Registries.BLOCK)
                                    .value();

                                String id = BuiltInRegistries.BLOCK.getKey(block).toString();

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
