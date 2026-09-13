# Hoplite Tweaks

Hoplite Tweaks is a mod that adds QOL modifications to the minecraft server Hoplite.

## Included features

- Hoplite teammate support
  - Receives Hoplite's authoritative `hoplite-addons:update_teammates` and reset packets.
  - Renders a camera-facing chevron above each teammate in the world.
  - Displays tab-list health and live distance beneath the teammate name.
  - Uses live player positions nearby and exact server-provided positions at distance.
  - Can hide nearby marker shapes and distance while retaining health.
  - Uses Hoplite role colors: kings are yellow, party members are blue, and
    regular teammates are green.
  - Supports marker scale, height, visibility range, name, and distance controls.
- Duel teammate glow
  - Outlines teammates in duel/competitive modes.
  - Prefers Hoplite packet team membership and falls back to the vanilla scoreboard team.
- Cooldown HUD
  - Receives Apollo display, remove, and reset cooldown messages.
  - Shows compact cards with readable timers and color-changing progress bars.
  - Can show purely visual vanilla-style cooldown sweeps on matching hotbar items.
  - Can render optional client-side top bars using matching hotbar item models.
- Mod Menu configuration
  - Separate General, Team View, Cooldowns, and Supply Beams tabs.
  - Feature toggles, sliders, role-color information, and reset-per-tab.
  - Cooldown HUD position, scale, and compact-mode controls.
  - Persists to `config/hoplite-tweaks.json`.
- Hoplite utilities
  - Session kill tracking in tab, scoreboard, and configurable player nametags.
  - Customizable multiplayer ping labels beside or above player nametags on every server.
  - Party-message and mention pings.
  - Automatic party-chat switching after joining a party.
  - Weekly crate reminders and automatic pet selection.
  - Optional automatic activation of Hoplite's clickable skin-application prompt.
  - Optional double-tap protection against accidentally dropping a hotbar sword.
  - Separate double-tap protection for recognized named legendary items.
  - Outgoing anti-slur protection backed by an automatically updated HTTPS text list and offline cache.
  - Optional three-second chat queue for unranked players, detected from gray sender names in chat.
  - Per-player chat-name colors and bold styling backed by an automatically updated player list.

### Supply crate beams

Supply Beams marks each Hoplite supply-drop announcement with a customizable beam, including
locations outside loaded terrain. Beams use the announced X/Z coordinates and start at Y=64 until the
destination chunk first loads. They then remember the actual surface height even when the chunk
unloads, updating it if the terrain loads again. Beams have a bright core and soft outer glow, with
a consistent apparent width at any distance. Color, screen width, height, and opacity are adjustable.
Each beam expires after five minutes or clears permanently when you come within 25 horizontal
blocks by default. Set the arrival radius to zero to disable arrival clearing.

Assign **Toggle Supply Crate Beams** under **Controls → Key Binds → Hoplite Tweaks**; it is
unassigned by default. Hiding beams does not stop tracking or expiration. The Supply Beams tab
also provides a Controls shortcut and a button to clear tracked drops.

### Network disclosure

The anti-slur module makes a read-only HTTPS GET request to the
[`blocked-words.txt`](./blocked-words.txt) file in this repository when the mod starts and when
the player joins a server. This request only downloads moderation rules. Hoplite Tweaks does not
upload chat messages, player identifiers, server information, telemetry, or other user data.

The chat-name highlighter similarly downloads [`highlighted-players.txt`](./highlighted-players.txt).
Both files are bundled into releases as immediate offline fallbacks and refreshed from this
repository without uploading any player or chat data.

Each highlighted player line uses
`USERNAME #CHAT_HEX chat_weight #NAMETAG_HEX nametag_weight`. Each weight is either `bold` or
`normal`. Chat styling only applies to a configured sender name followed by a colon, so ordinary
mentions are left unchanged.

The feature implementation is original. The project contains a small,
dependency-free protobuf wire reader for Apollo cooldown compatibility and an
independent codec for Hoplite's teammate payload schema; it does not bundle
Teamviewer or Coolite's implementation.

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
- `apollo/` owns cooldown decoding and shared short-lived marker state.
- `TeammateMarkerRenderer` renders billboarded world-space teammate markers.
- `HopliteHud` renders cooldown data.
- `DuelGlow` contains duel detection and team decisions.
- Highlighted world nametags are restricted to active Hoplite connections.
- `config/` owns persistence and the Mod Menu screen.
- Stonecutter comments isolate API differences introduced in Minecraft 26.x.

New Hoplite features should check `HopliteSession.isActive()` before reading or
mutating gameplay state.
