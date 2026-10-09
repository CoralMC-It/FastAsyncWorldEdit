# FastAsyncWorldEdit
[![Join us on Discord](https://img.shields.io/discord/268444645527126017.svg?label=&logo=discord&logoColor=ffffff&color=7389D8&labelColor=6A7EC2)](https://discord.gg/intellectualsites)
[![bStats Servers](https://img.shields.io/bstats/servers/1403)](https://bstats.org/plugin/bukkit/FastAsyncWorldEdit/1403)
[![Crowdin](https://badges.crowdin.net/e/4a5819fae3fd88234a8ea13bfbb072bb/localized.svg)](https://intellectualsites.crowdin.com/fastasyncworldedit)

## What is FAWE and why should I use it?

FAWE is designed for efficient world editing.
* Simple to set up and use
* Extremely configurable
* Uses minimal CPU/Memory
* Safe for many players to use
* Insanely fast when using the slowest mode

FastAsyncWorldEdit is a fork of WorldEdit that has huge speed and memory improvements and considerably more features.  
If you use other plugins that depend on WorldEdit, simply having FAWE installed will boost their performance.

### Coral SMP Dev 26.2 trim lock-order correction

The `tongue` branch ports the SMP Dev correction from installed revision
`31e0175d7b39b3b866de3d03117057c4bb4b6e18` onto the pulled upstream source.
The 26.2 adapter's aggressive `trim` held its global section lock while calling
`CharGetBlocks.trim`, which waits on per-section monitors. Region-owned applies
hold a per-section monitor before acquiring the global lock: the JVM detected
two cycles involving both lock owners on SMP Dev.

The corrected `trim` releases the metadata locks before invalidating ordinal
sections. Non-aggressive trimming snapshots section metadata before taking any
per-layer monitor. The current upstream `GlobalPalette` handling is retained;
world writes, region ownership and other adapters are unchanged.

`overlay/exact31e0175-trim.patch` preserves the original adapter-source correction.
The standalone `overlay` build assembles only `PaperweightGetBlocks.trim(Z)Z`
onto the exact installed jar, SHA-256
`5555bd629b8bf96e513bea704c5f8659858ad83f8722c1137eae4b141c18f09d`.
It retains the 4,125 other installed entries byte-for-byte and refuses another
baseline. This version-locked overlay is not a full replacement runtime built
from the newer upstream source or different NMS mappings.

Build the overlay separately with Java 25:
`gradlew.bat -p overlay -PfaweBaseline=<installed-fawe.jar> -PcanvasRuntime=<canvas-26.2-runtime.jar> patchedJar`.
The matching locally published `io.canvasmc.canvas:canvas-api:26.2.local-SNAPSHOT`
is required; the baseline, runtime jar and generated artifacts are not committed.

The exact-revision correction was verified on SMP Dev only: both JVM deadlock
cycles disappeared, all 27 duel maps became ready after actual paste completion,
3,375 clipboard/block samples matched, and a subsequent reset completed with
region heartbeats responding. The normal watchdog remains enabled. This source
publication did not rebuild, rerun those checks or deploy to production.

## Support

If you have any questions or need help with something, please join our Discord server:
[![](https://discordapp.com/api/guilds/268444645527126017/widget.png?style=banner2)](https://discord.gg/intellectualsites)

## Downloads

Releases are available either on Modrinth or on CurseForge.
- [Modrinth](https://modrinth.com/plugin/fastasyncworldedit/)
- [CurseForge](https://dev.bukkit.org/projects/fawe)

### Experimental Builds
- [Jenkins](https://ci.athion.net/job/FastAsyncWorldEdit/)

## Features

* Over 200 Commands
* Style and translate messages and commands
* (No setup required) Clipboard web integration (Clipboard)
* Unlimited `//undo`, per-world history, instant lookups/rollback, and cross-server clipboards
* Advanced per-player limits (entity, tiles, memory, changes, iterations, regions, inventory)
* Visualization, targeting modes/masks, and scroll actions
* Adds lots of powerful new //brushes and //tools.
* Adds a lot more mask functionality. (new mask syntax, patterns, expressions, source masks)
* Adds a lot more pattern functionality. (a lot of new pattern syntax and patterns)
* Adds edit transforms (apply transforms to a source, e.g., on //paste)
* Adds support for new formats (e.g. Structure Blocks)
* Instant copying of arbitrary size with `//lazycopy`
* Auto repair partially corrupt schematic files
* Biome mixing, in-game world painting, dynamic view distance, vanilla CUI, off-axis rotation, image importing, cave generation,
  multi-clipboards, interactive messages, schematic visualization, lag prevention, persistent brushes, and A LOT MORE

### Performance

There are several placement modes, each supporting higher throughput than the previous. All editing is processed
asynchronously, with
certain tasks being broken up on the main thread. The default mode is chunk placement.
* Blocks (Bukkit-API) - Only used if chunk placement isn't supported. Still faster than any other plugin on Spigot.
* Chunks (NMS) - Places entire chunk sections
* World (CFI) - Used to generate new worlds/regions

### Protection Plugins

The following plugins are supported with Bukkit:
* [WorldGuard](https://dev.bukkit.org/projects/worldguard)
* [PlotSquared](https://www.spigotmc.org/resources/77506/)

### Logging and Rollback

By default you can use `//inspect` and `//history rollback` to search and restore changes. To reduce disk usage, increase the
compression level and buffer size. To bypass logging, use `//fast`.

### Developer API

FAWE maintains API compatibility with WorldEdit, so you can use the normal WorldEdit API asynchronously.
FAWE also has some asynchronous wrappers for the Bukkit API.
The wiki has examples for various things like reading NBT, modifying world files, pasting schematics, splitting up tasks, lighting, etc.
If you need help with anything, hop on [Discord](https://discord.gg/intellectualsites).

## Documentation

* [Wiki](https://intellectualsites.github.io/fastasyncworldedit-documentation/)
* [Javadocs](https://intellectualsites.github.io/fastasyncworldedit-javadocs/)

## Contributing

Want to add new features to FastAsyncWorldEdit or fix bugs yourself? You can get the game running, with FastAsyncWorldEdit, from the code here:

For additional information about compiling FastAsyncWorldEdit, read the [compiling documentation](https://github.com/IntellectualSites/FastAsyncWorldEdit/blob/main/COMPILING.adoc).

## Special thanks


[![JetBrains logo.](https://resources.jetbrains.com/storage/products/company/brand/logos/jetbrains.svg)](https://jb.gg/OpenSource)
<br>
The creators of IntelliJ IDEA support us with their Open Source Licenses.

<a href="https://yourkit.com/"><img src="https://www.yourkit.com/images/yklogo.png" width="200">
</a>

Thank you to YourKit for supporting our product by providing us with their innovative and intelligent tools
for monitoring and profiling Java and .NET applications.
YourKit is the creator of [YourKit Java Profiler](https://www.yourkit.com/java/profiler/), [YourKit .NET Profiler](https://www.yourkit.com/.net/profiler/), and [YourKit YouMonitor](https://www.yourkit.com/youmonitor/).
