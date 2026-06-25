# Companion Compass

Companion Compass is a NeoForge 1.21.1 HUD mod that replaces the vanilla experience bar with a compact compass for other players in the same dimension. It does not show names, coordinates, health, cardinal directions, routes, or a minimap.

## Behavior

- The server sends periodic position snapshots for online players in the same dimension.
- The client uses those snapshots to draw colored markers over the experience bar area.
- Holding Shift hides the compass and shows the standard Minecraft experience bar.
- Marker color is deterministic from the player's UUID.
- Marker position, size, brightness, and transparency are calculated on the client.
- No chunks are force-loaded for the compass.
- No Mixins, Access Transformers, custom textures, commands, or third-party libraries are used.

If the server does not have Companion Compass installed, the client falls back to a limited mode using only players currently loaded in the client world. In that mode, long-distance tracking is limited by normal Minecraft entity visibility.

## Installation

### Client

Place the Companion Compass jar in the `mods` folder of a NeoForge 1.21.1 client instance.

### Dedicated Server

Place the same Companion Compass jar in the dedicated server `mods` folder. Every client that wants the compass HUD should also install the jar locally.

### LAN World

The LAN host needs the jar in their client instance so the integrated server can send position snapshots. Other LAN players also need the jar in their own client instances to receive snapshots and render the HUD.

## Requirements

- Minecraft 1.21.1
- NeoForge 21.1.x
- Java 21

## Development

Build:

```powershell
.\gradlew.bat build
```

Run the client:

```powershell
.\gradlew.bat runClient
```

Run a dedicated server:

```powershell
.\gradlew.bat runServer
```

If Java is not in `PATH`, set `JAVA_HOME` to a Java 21 installation before running Gradle.
