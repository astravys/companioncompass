# Companion Compass

Companion Compass is a client-side NeoForge 1.21.1 HUD mod for finding other players in the same dimension without a minimap, coordinates, labels, or server-side support.

## Behavior

- Replaces the vanilla experience bar with a compact horizontal companion compass when another player is present in the client world.
- Holding Shift hides the compass and shows the standard Minecraft experience bar again.
- Ignores the local player and players outside the current dimension.
- Draws deterministic colored pixel markers based on player UUIDs.
- Marker position follows the relative viewing angle, marker size follows distance, brightness reflects meaningful height differences, and alpha fades players behind the viewer.
- Draws everything programmatically through Minecraft GUI rendering. No textures, Mixins, Access Transformers, server logic, or third-party libraries are used.

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

If Java is not in `PATH`, set `JAVA_HOME` to a Java 21 installation before running Gradle.
