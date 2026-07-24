# Hoplite Tweaks

Hoplite Tweaks is a mod that adds QOL modifications to the minecraft server Hoplite.

## Included features

- Apollo teammate support
  - Receives Hoplite's `lunar:apollo` team packets.
  - Renders a camera-facing chevron above each teammate in the world.
  - Displays the teammate name and live distance above the marker.
  - Uses Hoplite role colors: kings are yellow, party members are blue, and
    regular teammates are green.
  - Supports marker scale, height, visibility range, name, and distance controls.
- Duel teammate glow
  - Outlines teammates in duel/competitive modes.
  - Prefers Apollo team membership and falls back to the vanilla scoreboard team.
- Cooldown HUD
  - Receives Apollo display, remove, and reset cooldown messages.
  - Shows compact cards with readable timers and color-changing progress bars.
- Mod Menu configuration
  - Separate General, Team, and Cooldowns tabs.
  - Independent feature toggles, sliders, role-color information, and reset-per-tab.
  - Cooldown HUD position, scale, and compact-mode controls.
  - Persists to `config/hoplite-tweaks.json`.
- Hoplite utilities
  - Party-message and mention pings.
  - Automatic party-chat switching after joining a party.
  - Weekly crate reminders and automatic pet selection.
  - Royale nick detection with continuous player checks and rate-limited profile lookups.
  - Outgoing anti-slur protection backed by an automatically updated HTTPS text list and offline cache.
  - Per-player chat-name colors and bold styling backed by an automatically updated player list.

### Network disclosure

The anti-slur module makes a read-only HTTPS GET request to the
[`blocked-words.txt`](./blocked-words.txt) file in this repository when the mod starts and when
the player joins a server. This request only downloads moderation rules. Hoplite Tweaks does not
upload chat messages, player identifiers, server information, telemetry, or other user data.

The chat-name highlighter similarly downloads [`highlighted-players.txt`](./highlighted-players.txt).
Both files are bundled into releases as immediate offline fallbacks and refreshed from this
repository without uploading any player or chat data.

Each highlighted player line uses `USERNAME #RRGGBB bold`. Replace `bold` with `normal` for
regular text weight.

The feature implementation is original. The project contains a small,
dependency-free protobuf wire reader for compatibility with the public Apollo
packet format; it does not bundle Teamviewer or Coolite's implementation.

## Supported versions

The source is shared through Stonecutter and currently produces Fabric builds
for:

- Minecraft 1.21.11
- Minecraft 26.1.2 (metadata also marks 26.1 and 26.1.1)
- Minecraft 26.2

Java 21 is required for 1.21.11. Java 25 or newer is required for 26.x.

## Build

```bash
./gradlew build
```

Per-version jars are written to each version project's `build/libs` directory.
To collect distributable jars under the root `build/libs` directory:

```bash
./gradlew buildAndCollect
```

## Architecture

- `HopliteSession` is the mandatory server-address gate.
- `apollo/` owns protocol decoding and short-lived session state.
- `TeammateMarkerRenderer` renders billboarded world-space teammate markers.
- `HopliteHud` renders cooldown data.
- `DuelGlow` contains duel detection and team decisions.
- `config/` owns persistence and the Mod Menu screen.
- Stonecutter comments isolate API differences introduced in Minecraft 26.x.

New Hoplite features should check `HopliteSession.isActive()` before reading or
mutating gameplay state.
