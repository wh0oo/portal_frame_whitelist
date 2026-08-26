# AnyBlock Portals

AnyBlock Portals lets you use additional blocks as Nether portal frames.

You choose which blocks are allowed by adding them to the whitelist config. Vanilla obsidian portals continue to work normally.

## Features

* Add custom blocks to Nether portal frames
* Mix whitelisted blocks with obsidian
* Use multiple different whitelisted blocks in the same frame
* Change the whitelist without restarting the server
* Invalid block IDs are ignored and logged instead of breaking the mod

## Config

The config file is:

```text
config/portal_frame_whitelist.json
```

Example:

```json
{
  "blocks": [
    "minecraft:pink_wool",
    "minecraft:purple_wool",
    "minecraft:amethyst_block",
    "minecraft:end_portal_frame"
  ]
}
```

Use full block IDs, including the namespace.

For vanilla blocks, that means names like:

```text
minecraft:pink_wool
minecraft:amethyst_block
minecraft:crying_obsidian
```

## Commands

The following commands are available to server operators:

```text
/anyblockportals add <block_id>
/anyblockportals remove <block_id>
/anyblockportals list
/anyblockportals reload
```

### Add a block

```text
/anyblockportals add minecraft:pink_wool
```

Adds the block to the whitelist, saves the config, and makes the change active immediately.

### Remove a block

```text
/anyblockportals remove minecraft:pink_wool
```

Removes the block from the whitelist and makes the change active immediately.

### List blocks

```text
/anyblockportals list
```

Shows the blocks currently in the whitelist.

### Reload the config

```text
/anyblockportals reload
```

Reloads the whitelist from the config file without restarting the server.

## Portal Behavior

The whitelist is checked when a portal is created.

Existing portals continue to work after they have been created. If a portal is broken and needs to be lit again, its frame must meet the current whitelist rules.

Any block can be added to the whitelist, but some blocks may look unusual or behave differently when used as part of a portal frame. Use woerd blocks at your own risk
