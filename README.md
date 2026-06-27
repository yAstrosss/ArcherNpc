# ArcherNpc

Packet-based lobby NPCs for **Paper / Folia 1.21+**, with a built-in on-screen image
and GIF engine. NPCs are sent purely through packets and are never real server
entities, so they don't tick, don't show up in `/kill @e`, and stay cheap on crowded hubs.

Almost everything is configured through an in-game GUI editor. The plugin ships with full
**English** and **Brazilian Portuguese** message files.

> Status: `1.0-beta`. Paper-native (`paper-plugin.yml` + bootstrap), Folia-aware.

---

## Features

**NPCs**
- Packet NPCs: create / edit / delete / list / copy, persisted as one YAML file per NPC.
- Appearances: player skin, any spawnable mob, or item/block display.
- Skins by Mojang username, by image URL (resolved through MineSkin), or *skin mirror*
  (the NPC shows the skin of whoever looks at it, per viewer).
- Multi-line nameplates with MiniMessage and legacy `&` colors, plus per-viewer
  PlaceholderAPI resolution with change-only re-send (no flicker).
- Look-at-player (with an angle gate), glow in 16 colors, scale 0.1x to 10x, poses, and a
  6-slot equipment set.
- Visibility modes: everyone / permission-based / manual (hand-picked per player, stored
  by UUID so it survives restarts and reconnects). Collision and tab-list toggles.

**Action engine**
- Per-action click triggers (left / right / any) and a per-NPC cooldown.
- Actions: message, run-as-player, run-as-console, temporary-OP command, teleport, sound,
  open menu, connect to another server (proxy), and show a HUD image/GIF.
- Flow control: `wait`, `random` (run one of the following actions), and `lock`
  (block re-trigger until a timed sequence finishes). A HUD image action defers the rest
  of the list until it ends, so the actions after it act as an "on-finish" sequence.

**HUD image engine**
- Renders PNG / GIF as font glyphs inside an auto-generated resource pack and shows them
  on the player's screen (title / actionbar / chat).
- Static images, frame-accurate GIFs, and an HD mode that slices large images into a tile
  grid. In-game tuning GUI for glyph height, vertical position, and tile size.
- Four hosting modes with documented trade-offs: `upload` (mc-packs.net, zero-config),
  `auto` (upload with fallback), `manual` (host the zip yourself), and `local` (built-in
  HTTP server). Pack prompt, name, description, icon, and force-accept are configurable.

**Integrations**
- PacketEvents (required) for all packet work.
- PlaceholderAPI (optional) for nameplate placeholders.
- ItemsAdder / Nexo (optional, beta) custom models, detected by reflection with a silent
  vanilla fallback when absent.
- Cross-server `connect` action over the BungeeCord channel (Velocity-compatible), with a
  per-target rate-limited transfer queue.

---

## Requirements

- Paper or Folia **1.21+**, Java **21**.
- **PacketEvents** (required).
- Optional: PlaceholderAPI, ItemsAdder, Nexo.

---

## Installation

1. Install PacketEvents into `plugins/`.
2. Drop the `ArcherNpc-v<version>.jar` into `plugins/`.
3. Start the server. On boot you should see
   `ArcherNpc ativado (Paper nativo, NPCs por pacotes).`
4. `/plugins` lists ArcherNpc under **Paper Plugins**.

---

## Commands

All `/npc` subcommands require the `archernpc.admin` permission and tab-complete NPC IDs.

| Command | Description |
|---|---|
| `/npc criar <id>` | Create an NPC where you stand, using your skin. |
| `/npc editar <id>` | Open the GUI editor. |
| `/npc deletar <id>` | Delete an NPC. |
| `/npc listar` | List all NPCs. |
| `/npc copiar <id> <new>` | Duplicate an NPC. |
| `/npc mover \| centralizar \| rotacionar <id>` | Position the NPC. |
| `/npc nome <id> <text>` | Set the nameplate (`\|` splits lines). |
| `/npc skin <id> <username\|url>` | Set the skin (auto-detects username vs URL). |
| `/npc acao adicionar \| listar \| remover <id> ...` | Manage click actions. |
| `/npc colidir \| espelho \| visibilidade <id>` | Toggle collision / skin-mirror / visibility. |
| `/npc permissao <id> <perm>` · `/npc ver <id> <player>` | Visibility node / manual per-player visibility. |
| `/npc hud [recarregar \| <image> [seconds]]` | Open the HUD GUI, rebuild the pack, or test an image. |

Permissions:
- `archernpc.admin`: full access to `/npc`.
- `archernpc.action.privileged`: allows triggering privileged (console / temp-OP) actions.

---

## Building from source

```
./gradlew build        # compile, run tests, produce the plugin jar
```

- Output jar: `build/libs/ArcherNpc-v<version>.jar`.
- `./gradlew runServer` spins up a local Paper 1.21.11 test server (downloads PacketEvents
  automatically) via the `run-paper` plugin.

---

## Project layout

```
src/main/java/com/yastro/npc/
  action/      click actions and the action engine
  command/     /npc command + tab-complete
  config/      config, messages, YAML NPC storage
  display/     item/block display models
  gui/         the GUI editor
  hud/         image/GIF -> resource pack engine and hosting
  interaction/ click handling and cooldown
  model/       NPC data model
  nameplate/   multi-line nameplates and placeholders
  npc/         packet NPC, tracking, sessions, look controller
  server/      proxy connector and transfer queue
  skin/        Mojang/MineSkin resolution
  util/        helpers
src/main/resources/   config.yml, messages_en.yml, messages_pt-br.yml, paper-plugin.yml
src/test/java/        unit tests
```

---

## Documentation

- **Full user manual (English):** [`docs/HOW_TO_USE.md`](docs/HOW_TO_USE.md)
- **Full user manual (Portuguese):** [`docs/COMO_USAR.md`](docs/COMO_USAR.md)
- **Store descriptions:** [`docs/modrinth/DESCRIPTION_EN.md`](docs/modrinth/DESCRIPTION_EN.md) · [`docs/modrinth/DESCRIPTION_PT.md`](docs/modrinth/DESCRIPTION_PT.md)

---

## License

Licensed under the **GNU General Public License v3.0**. See [`LICENSE`](LICENSE).
