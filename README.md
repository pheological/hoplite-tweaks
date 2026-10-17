# Hoplite Tweaks

An all-in-one Hoplite mod that adds all the QOL and utility features you need for Hoplite. Includes Team View, Cooldown Viewing, Kill Tracker, Supply Beams, and much more. Supports versions 1.21.11–26.2.

**Use the P key to open the GUI.**

## Main Features

### Team View

This setting allows you to see all teammates regardless of distance in Civilization or any team-based game modes.

### Cooldown Viewer

This setting shows cooldowns for all legendary items. Typically, the action bar only allows you to see the cooldown of one item at a time, but this setting allows you to see all of them.

### Supply Beams

This setting adds easily spotted supply beams where loot drops are announced in chat. After visiting the supply drop, the marker disappears.

### Kill Tracker

This setting allows you to show kills in the tab list and/or player nametags. It can be configured to show only kills after the mining phase or total kills.

## Additional Features

- **Anti Slur**: Prevents you from using slurs, which helps stop you from being muted automatically.
- **Team Glow**: Adds a glowing effect to teammates in competitive duels.
- **Quickcraft most recent**: An unbound keybind that activates the latest clickable “you have all the items to craft” chat prompt.
- **Mention Ping**: Plays a ping sound when your name is mentioned or someone sends a message in party chat.
- **Auto Party Chat**: Automatically puts you in party chat after joining a party.
- **Auto Pet**: Automatically pets the dragon during dragon scatter games.
- **Auto Apply Skins**: Automatically applies weapon skins after you obtain a legendary item.
- **Slot Lock**: Prevents dropping items from locked slots. Press L while hovering over a player inventory slot to lock or unlock it. Locks are remembered between sessions; moving items is still allowed.
- **Ping Header**: Shows ping in or above player nametags.
- **No Lava Fog**: Allows you to see under lava.

## Dependencies

This mod requires both [YACL](https://modrinth.com/mod/yacl) and [Mod Menu](https://modrinth.com/mod/modmenu) to run. You probably already have these installed if you have installed other mods before.

**Officially approved but not affiliated with Hoplite.**

**Wonder why the icon changed? Due to copyright restrictions, I am unable to use the Hoplite logo.**

Looking for the download? [Download Hoplite Tweaks on Modrinth](https://modrinth.com/mod/hoplite-tweaks).

## EvergreenHUD Direction integration

`dev.pheological.hoplite_tweaks.api.TeamTrackerApi.teammates()` provides an immutable
snapshot for EvergreenHUD's Direction HUD. Call it each frame on the client thread.
The API uses only Java types in its public signatures, so EvergreenHUD can optionally
load it through reflection after checking Fabric Loader for `hoplite_tweaks`.

Each `Teammate` exposes `playerId()` (UUID), `displayName()`, `color()` (server marker
color), `x()`, `y()`, `z()`, `bearing()` and `distance()`. Bearing is clockwise from
north (0° north, 90° east), matching EvergreenHUD's compass convention. Positions
use interpolated loaded players or the tracker's smoothed server positions.

Resolve each UUID through Minecraft's player info to obtain its skin, then render
its face and hat using EvergreenHUD's player-head renderer. Use the UUID's default
skin when player info is unavailable. Wrap `teammate.bearing() - cameraBearing`
to ±180° and place the head at `width / 2 + delta * width / span`; clip heads outside
the compass span and apply the Direction HUD's existing edge fade.

The snapshot is empty when Hoplite or Team Tracker is disabled, or no world is
loaded. It excludes the local player, dead teammates, disconnected locations, and
other dimensions. No new settings or EvergreenHUD dependency are introduced.
EvergreenHUD must consume this interface to display the heads in its Direction HUD.

## Building runnable JARs

Run `./gradlew buildAndCollect` to build all supported Minecraft versions. All runnable JARs are collected together in `build/libs/<mod-version>/` (currently `build/libs/1.3.1/`). Individual runnable-JAR tasks also update this shared folder automatically. This command does not generate sources or Javadoc JARs.

Run `./gradlew test` separately to run the test suites.
