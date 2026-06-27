# Building ArcherNpc

How to build the plugin from source and reproduce the released jar.

## Prerequisites

- **JDK 21** (the build enforces Java 21 via the Gradle toolchain).
- Internet access on the first build. Gradle downloads the wrapper and the declared
  dependencies from Maven Central, the PaperMC repo, CodeMC (PacketEvents), and
  extendedclip (PlaceholderAPI).
- No global Gradle install required; use the included wrapper (`./gradlew`).

## Dependencies

All dependencies are `compileOnly` (compile-time only). **None are shaded or bundled** into
the jar. They are provided at runtime by the server or by other plugins.

| Dependency | Version | Scope | Provided at runtime by |
|---|---|---|---|
| `io.papermc.paper:paper-api` | `1.21.11-R0.1-SNAPSHOT` | compileOnly | the server |
| `com.github.retrooper:packetevents-spigot` | `2.12.2` | compileOnly | the PacketEvents plugin (required) |
| `me.clip:placeholderapi` | `2.11.6` | compileOnly | the PlaceholderAPI plugin (optional) |

Test scope uses JUnit 5 (`junit-bom:6.0.0`).

## Build

```
./gradlew build
```

- Compiles `src/main/java` (Java 21, UTF-8).
- Runs the unit tests in `src/test/java`.
- Produces the plugin jar at `build/libs/ArcherNpc-v<version>.jar`
  (`<version>` is `1.0-beta`, set in `build.gradle`). This is the released artifact.

## Local test server

```
./gradlew runServer
```

Spins up a local Paper 1.21.11 test server via the `run-paper` Gradle plugin and downloads
PacketEvents automatically.

## Reproducing the released jar

Check out the released tag, then with JDK 21:

```
./gradlew clean build
```

The resulting `build/libs/ArcherNpc-v<version>.jar` is the distributed artifact.
