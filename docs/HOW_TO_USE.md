# ArcherNpc: Complete user manual

> Packet-based lobby NPCs (PacketEvents) for **Paper 1.21.11** servers. No server-side entity, no
> real-mob lag. The plugin is **bilingual** (en / pt-br); this is the **English** manual. It includes
> a GUI editor and a **HUD** system (on-screen images/logos/GIFs via an automatically generated
> resource pack).

Contents:

1. [What the plugin does](#1-what-the-plugin-does)
2. [Installation and dependencies](#2-installation-and-dependencies)
3. [First steps (5 minutes)](#3-first-steps-5-minutes)
4. [Permission](#4-permission)
5. [All commands](#5-all-commands)
6. [GUI editor (`/npc editar`)](#6-gui-editor-npc-editar)
7. [Appearance: player, mob, item, block](#7-appearance-player-mob-item-block)
8. [Skin](#8-skin)
9. [Name / label (nameplate)](#9-name--label-nameplate)
10. [Click actions](#10-click-actions)
11. [Glow, scale, pose, equipment](#11-glow-scale-pose-equipment)
12. [Visibility and collision](#12-visibility-and-collision)
13. [HUD: on-screen images/logos/GIFs](#13-hud-on-screen-imageslogosgifs)
14. [Hosting the pack (upload / auto / manual / local)](#14-hosting-the-pack)
15. [Customizing the pack: text, logo, name, description](#15-customizing-the-pack-text-logo-name-description)
16. [Network / proxy (BungeeCord / Velocity)](#16-network--proxy)
17. [Full `config.yml` reference](#17-full-configyml-reference)
18. [Where the data is stored](#18-where-the-data-is-stored)
19. [Troubleshooting](#19-troubleshooting)
20. [Project status: what's done and what's missing](#20-project-status)

---

## 1. What the plugin does

- **100% packet-based NPCs**, each NPC is drawn directly on each player's client. There is no
  server-side entity, so it doesn't weigh on the tick and doesn't show up in `/kill @e`.
- **Appearances**: player skin, any **mob**, or **item/block** on display (includes models from
  **ItemsAdder/Nexo**).
- **Click actions**: command, message, teleport, sound, connect to another server on the network, open
  a menu, show an **on-screen image/GIF**.
- **Nameplates** (name above the head) with multiple lines and **PlaceholderAPI**.
- **Look at the player**, colored glow, scale, pose, equipment in all 6 slots.
- **Visibility** by everyone / permission / manual, and **mirror-skin** (the NPC copies the skin of
  whoever is looking).
- Complete **GUI editor**, you can do almost everything without memorizing a command.
- **HUD**: drop a `.png`/`.gif` into a folder, the plugin generates a resource pack and pushes it to
  players; you show the image on screen as an NPC click action.

---

## 2. Installation and dependencies

| Plugin | Required? | What for |
|---|---|---|
| **PacketEvents** | **Yes** | The base for all packets (spawn, skin, interaction). Without it the plugin won't start. |
| **PlaceholderAPI** | Optional | Placeholders in nameplates (`%player_name%` etc). |
| **ItemsAdder** or **Nexo** | Optional | Custom models on item/block NPCs or in equipment. |

Steps:

1. **Paper 1.21.11** server (or compatible), **Java 21**.
2. Put **PacketEvents** in `plugins/`.
3. Put `ArcherNpc-v1.0-beta.jar` in `plugins/`.
4. Start the server. The console should show:
   `ArcherNpc enabled (Paper-native, packet NPCs).`
5. Confirm with `/plugins`, ArcherNpc appears under **Paper Plugins**.

> The plugin auto-detects whether PacketEvents is already installed as a separate plugin and **does
> not** reinitialize over it (that would break everyone's packets). If you don't have the standalone
> PacketEvents plugin, ArcherNpc starts its own instance.

---

## 3. First steps (5 minutes)

```
/npc criar shop # creates an NPC with YOUR skin, facing where you're looking
                           # -> it shows up in front of you in moments
/npc nome shop &b&lSHOP|&7Click to open
/npc acao adicionar shop mensagem &aWelcome to the shop!
                           # -> right-click the NPC: the message appears
/npc editar shop # opens the visual editor to set the rest of the click
```

That's it, you already have a clickable NPC with a name and an action. The rest of this manual is
"everything else you can do".

---

## 4. Permission

Every `/npc` command (and the `/npcs` alias) requires:

```
archernpc.admin
```

Give it to staff only. There are no separate per-subcommand permissions, anyone with
`archernpc.admin` can do everything.

> The permission used for **permission-based visibility** of NPCs is **a different thing**: it's any
> node you choose per NPC (see [Visibility](#12-visibility-and-collision)).

---

## 5. All commands

Notation: `<required>` and `[optional]`. `<id>` has **tab-completion** (TAB) with the existing NPCs.

### Basics

| Command | What it does |
|---|---|
| `/npc criar <id>` | Creates an NPC at your position, with your skin. `id`: a-z, 0-9, `_`, up to 16 characters. |
| `/npc deletar <id>` | Removes the NPC and deletes its file. |
| `/npc listar` | Lists all created NPCs. |
| `/npc editar <id>` | Opens the **GUI editor** (recommended for everything). |
| `/npc copiar <id> <new>` | Duplicates NPC `<id>` into a new id, at your position. |

### Position

| Command | What it does |
|---|---|
| `/npc mover <id>` | Brings the NPC to where you are (position + facing). |
| `/npc centralizar <id>` | Centers the NPC in the middle of the block (x.5 / z.5). |
| `/npc rotacionar <id>` | Makes the NPC **face you**. |

### Appearance and text

| Command | What it does |
|---|---|
| `/npc nome <id> <text>` | Sets the nameplate. Use `|` to separate **lines**. Empty text removes it. Colors with `&`. |
| `/npc skin <id> <value>` | Sets the skin. `<value>` = a **nick** (Mojang) or an **http(s) link** (via MineSkin), auto-detected. |

> Glow, scale, pose, equipment, type (mob/item/block) and HUD image are done through the
> **editor** (`/npc editar`). See sections 6-11.

### Click actions

| Command | What it does |
|---|---|
| `/npc acao adicionar <id> <type> <value>` | Adds an action. Types in [section 10](#10-click-actions). |
| `/npc acao listar <id>` | Lists the NPC's actions with their index. |
| `/npc acao remover <id> <index>` | Removes the action by index (see `acao listar`). |

### Behavior

| Command | What it does |
|---|---|
| `/npc colidir <id>` | Toggles collision (whether the player passes through the NPC or not). |
| `/npc espelho <id>` | Toggles **mirror-skin** (the NPC shows the skin of whoever is looking). |
| `/npc visibilidade <id>` | Cycles **todos -> permissao -> manual**. |
| `/npc permissao <id> <perm>` | Sets the permission node used when visibility is `permissao`. |
| `/npc ver <id> <player>` | Toggles whether an **online** player sees the NPC when visibility is `manual`. Saved by UUID. |

### HUD (on-screen images)

| Command | What it does |
|---|---|
| `/npc hud` | Opens the HUD config GUI (scale/position/where to display/reload). |
| `/npc hud recarregar` | Regenerates the pack (after adding/swapping images or editing `config.yml`). |
| `/npc hud <image> [seconds]` | **Tests** an image on you for N seconds (default 10). |

---

## 6. GUI editor (`/npc editar`)

`/npc editar <id>` opens a 54-slot menu with **everything**. The items are read-only (you can't take
them out). Button summary:

| Button | Function |
|---|---|
| Skin | Type a nick (in chat) to change the skin. |
| Name / Nameplate | Type the text (use `|` between lines). |
| Look at player | Toggles the NPC tracking you with its head. |
| Actions | Opens the actions submenu (add/remove/reorder/change trigger). |
| Glow | Toggles the glowing outline. |
| Glow color | Cycles through 16 colors. |
| Scale | Left +0.1 / Right −0.1 (from 0.1x to 10x). |
| Show in tab | Toggles whether the NPC appears in the player list (TAB). |
| Appearance | Choose player / mob / item / block (with paginated pickers). |
| Pose | normal / agachado / dormindo / nadando / sentado. |
| Equipment | Type `slot ref` (e.g. `capacete minecraft:DIAMOND_HELMET`). |
| Name height | Raises/lowers the nameplate (offset). |
| NPC height (Y) | Fine height adjustment for the NPC. |
| On-screen image (HUD) | Attaches an image/gif to the NPC click (becomes an `imagem` action). |
| Move / Center / Turn | Quick positioning. |
| Copy NPC | Type a new id to duplicate. |
| Collision / Mirror-skin / Visibility | Toggle behavior. |
| Delete | Removes the NPC. |

> The in-game button labels follow the configured language (`messages_en.yml` / `messages_pt-br.yml`);
> the literal action types stored on the NPC stay in Portuguese.

**Chat input:** when the editor asks for text (skin, name, equipment, copy...), it closes the menu and
waits for your next chat message. Type `cancelar` to abort. It works even if you start with `/`.

---

## 7. Appearance: player, mob, item, block

In the editor -> **Appearance**. Four categories:

- **Player (skin)**, the NPC is a fake player (uses the defined skin; see section 8).
- **Entity / Mob**, any living, spawnable Minecraft entity (zombie, villager, allay, cow...).
  Paginated picker, or "Type manually" (e.g. `cow`, `zombie`, `allay`).
- **Item (display)**, any item, shown as a display. Includes `minecraft:ID`, **`nexo:id`**, **`itemsadder:id`**.
- **Block (display)**, any block, shown as a display.

> **ItemsAdder/Nexo** models are resolved via reflection: it works if the plugin is installed, and
> simply falls back to the default item if it isn't (no error).

---

## 8. Skin

- **By nick**: `/npc skin <id> <nick>` or editor -> Skin -> type a player's name (their Mojang account
  skin is downloaded, cached, and applied). The download is async, the skin "arrives" in moments.
- **By URL** (image link): `/npc skin <id> https://.../skin.png`, if the value starts with `http(s)`,
  the plugin resolves the skin via **MineSkin** automatically. It works with no setup, but it's
  rate-limited (it can fail under heavy use). Depends on MineSkin being up.
- **Mirror-skin** (`/npc espelho <id>` or the editor button): the NPC shows the **skin of whoever is
  looking**. Great for "your character" in a lobby.
- When you **create** an NPC, it's born with **your** skin.

---

## 9. Name / label (nameplate)

```
/npc nome <id> line1|line2|line3
```

- Each `|` becomes a **line** floating above the head.
- Colors and formatting with `&` (e.g. `&b&lSHOP`, `&7click`).
- With **PlaceholderAPI** installed, placeholders are resolved **per player** (e.g.
  `&aHello %player_name%`) and update on their own (no flicker, only resent when the text changes).
- Empty text removes the nameplate.
- The nameplate height is adjusted in the editor (**Name height / offset**).

---

## 10. Click actions

Add them with `/npc acao adicionar <id> <type> <value>` or via the editor (**Actions** submenu).
The actions run **in the order** of the list when the NPC is clicked.

| Type | Value | What it does |
|---|---|---|
| `mensagem` | `&aHello!` | Sends a message (colors with `&`) to the player. |
| `comando` | `spawn` | Runs the command **as the player** (without the `/`). |
| `console` | `give %player% diamond 1` | Runs it **in the console**. `%player%` becomes the name of whoever clicked. |
| `teleporte` | `world,100,64,200[,yaw,pitch]` | Teleports the player. |
| `som` | `minecraft:ui.button.click` or `ENTITY_VILLAGER_YES` | Plays a sound. |
| `menu` | `shop` | Shortcut for `menu <value>` (integrates with menu plugins). |
| `conectar` | `lobby2` | Sends the player to another server on the network (**requires `rede-proxy: true`**, section 16). |
| `comando-op` | `warp vip` | Runs the command **as the player with temporary OP** (always removed afterward). `%player%` becomes the name. powerful, staff configures it only. |
| `imagem` | `qr` or `qr;5` | Shows the **HUD image/GIF** `qr` for N seconds (default 5). See below. |
| `esperar` | `5` / `5s` / `20t` | **Pauses** and defers the rest of the list: number/`s` = seconds, `t` = ticks. |
| `aleatorio` | (no value) | **Picks ONE** of the actions listed **after** it and runs only that one (random). |
| `travar` | (no value) | **Barrier**: locks the NPC for that player until the sequence ends (blocks spam in sequences with `esperar`). |

**Per-action trigger (button):** each action has a trigger, `direito` (right, the default), `esquerdo`
(left) or `qualquer` (any). Change it in the editor (Actions submenu -> left-click an item to cycle the
trigger).

**Order and "on finish":** the `imagem` action is special, it shows the image and **defers** the rest
of the list until the image ends. In other words, **everything after an `imagem` in the list is the
"on-finish action"**. Example:

```
0. imagem promo;5
1. mensagem &aThanks for watching!
2. comando shop
```
 -> shows `promo` for 5s; when it ends, sends the message and opens the shop.

**Timed sequences and randomization:** `esperar` and `imagem` defer the rest of the list; `aleatorio`
branches. Examples:

```
# Timed sequence (locks spam while it runs):
0. travar
1. mensagem &aPreparing your prize...
2. esperar 3s
3. comando-op cratebox abrir vip

# Random reward (picks 1 of the 3 following actions):
0. aleatorio
1. comando-op givdiamond %player%
2. comando-op giviron %player%
3. mensagem &cYou won nothing this time!
```

> `comando-op` grants **temporary** OP to the player only during that command and **always** removes
> it afterward (even if the command fails). Use it for commands that require OP; configure it
> carefully.

---

## 11. Glow, scale, pose, equipment

All in the editor (`/npc editar`):

- **Glow** + **color**, glowing outline; 16 colors (white, red, gold, aqua, blue, ...).
- **Scale**, from 0.1x to 10x (step 0.1).
- **Pose**, normal / agachado (crouching) / dormindo (sleeping) / nadando (swimming) / sentado
  (sitting).
- **Equipment**, 6 slots, format `slot ref`:
  - slots: `capacete` (helmet), `peito` (chestplate), `calca` (leggings), `bota` (boots), `mao`
    (main hand), `secundaria` (off hand)
  - ref: `minecraft:DIAMOND_HELMET`, `nexo:<id>`, `itemsadder:<id>`; `remover` clears the slot.
  - e.g.: `capacete minecraft:NETHERITE_HELMET`

---

## 12. Visibility and collision

- **Collision** (`/npc colidir <id>`): whether the player passes through the NPC or bumps into it.
- **Visibility** (`/npc visibilidade <id>` cycles):
  - `todos`, everyone sees it (default).
  - `permissao`, only those with the node set via `/npc permissao <id> <perm>` see it.
  - `manual`, hidden by default; only those you grant with `/npc ver <id> <player>` see it.

**Manual visibility step by step:**
1. `/npc visibilidade <id>` until it reads `manual`.
2. `/npc ver <id> <player>` (player must be **online**), grants access; run it again to revoke (toggle).
3. The grant is saved by **UUID**, so it survives a restart and applies when the player reconnects.

Distance at which the NPC appears: `distancia-visao` in `config.yml`.

---

## 13. HUD: on-screen images/logos/GIFs

The HUD turns `.png`/`.gif` into **font glyphs** inside a resource pack generated on the fly, and shows
them on the player's screen (title/actionbar/chat). Good for logos, QR codes, banners, "watch and
earn", etc.

### How to add images

1. The folder is created automatically: `plugins/ArcherNpc/hud_imagens/`
   - Drop `.png` or `.gif` here. The **file name** (without extension, lowercase) becomes the image **id**.
   - Subfolder `hud_imagens/hd/` -> large images are **sliced into a grid** (high resolution on screen).
2. `/npc hud recarregar` (or restart). The console shows which images were loaded.
3. `/npc hud <id> [seconds]` to test it on yourself.

### How to show it to the player

- **On an NPC click**: editor -> **On-screen image (HUD)** -> choose the image (left=5s, right=10s,
  shift=30s). This creates the action `imagem <id>;<seconds>`.
- Or directly: `/npc acao adicionar <id> imagem <imgId>;<seconds>`.

### Size/position adjustments

GUI `/npc hud`, or in `config.yml`:
- `hud-exibir`: `title` (center) / `actionbar` / `chat`.
- `hud-altura` (glyph height) and `hud-ascent` (vertical position; **keep ascent ≤ altura**).
- `hud-hd-tile`: on-screen size of each tile of the HD images.
- After changing it: **APPLY** in the GUI or `/npc hud recarregar`.

---

## 14. Hosting the pack

For players to see the images, the client needs to **download the pack**. `hud-modo` decides how it's
published. An honest look at each option:

### `upload` (default, recommended): mc-packs.net
- **Zero configuration**, works behind NAT/firewall, **no port to open**.
- Ideal for **shared/limited** hosting (only uses outbound HTTP).
- Depends on a **free third-party** service (mc-packs.net) staying up. If it goes down, the HUD
  stops until it's back. The pack becomes public on their CDN.
- **Use when:** you don't control the server (shared hosting) and you want it to "just work".

### `auto` (upload with a safety net)
- Tries `upload`; if that fails, uses the `hud-url` you set; if there's none, falls back to `local`.
- Better reliability without losing the convenience of upload.
- **Use when:** you want upload, but with an automatic plan B.

### `manual` (you host it): **most reliable long-term**
- You upload `hud_pack.zip` (generated in `plugins/ArcherNpc/`) somewhere **of your own** and paste the link into `hud-url`.
- Full control, permanent, fast. **Recommended for serious servers.**
- You re-upload the zip and update the link **every time the pack changes** (added an image, changed the scale...).
- **Recommended free and stable hosts:**
  - **GitHub Releases** + **jsDelivr** (free CDN): upload `hud_pack.zip` to a release and use
    `https://cdn.jsdelivr.net/gh/<user>/<repo>@<tag>/hud_pack.zip`.
  - Any web server/CDN of your own, Cloudflare R2, etc. **It has to be a direct link to the `.zip`.**

### `local` (the plugin's built-in HTTP server)
- The plugin starts an HTTP server on `hud-porta` serving the zip, on **all interfaces**.
- It only works if players can **reach your machine's IP:port**: it requires the **port open/
  forwarded** and the **public IP/domain** in `hud-host-publico`.
- On **shared** hosting it usually **won't** work (no extra port).
- **Use when:** VPS/dedicated with an open port and you don't want to depend on an external service.

> **Important fix in this version:** previously the `local` mode was stuck on `127.0.0.1` (it only
> worked on the same PC). Now it serves on all interfaces and builds the link from `hud-host-publico`
> (or from `server-ip`). Set `hud-host-publico` to your public IP/domain.

### Quick recommendation

| Your situation | Use |
|---|---|
| Shared hosting / no control over ports | `upload` (or `auto`) |
| Want maximum reliability and you control a repo/CDN | `manual` (GitHub Releases + jsDelivr) |
| VPS/dedicated with an open port | `local` (with `hud-host-publico`) or `auto` |

---

## 15. Customizing the pack: text, logo, name, description

All in `config.yml`, no code changes. It accepts `&` colors and `\n` for line breaks.

| Key | What it controls | Where the player sees it |
|---|---|---|
| `hud-prompt` | The **"Download resource pack?"** screen message | In the popup when joining/receiving the pack. |
| `hud-pack-nome` | The pack **name** (line 1 of the description) | In the client's pack list. |
| `hud-pack-descricao` | The pack **description** (line 2) | Below the name, in the pack list. |
| `hud-logo` | File name of the **icon/logo** PNG | As the pack icon on the resource screen. |
| `hud-forcar` | Force-accept (`true`) or not (`false`) | If `true`, anyone who declines is disconnected. |

### How to set the logo

1. Save a **square** PNG (128×128 or 256×256 works great).
2. Put it in `plugins/ArcherNpc/` with the name set in `hud-logo` (default `hud_logo.png`).
3. `/npc hud recarregar`. Done, the icon ships with the pack (it becomes `pack.png`).

If the image isn't square, the plugin centers it on a transparent square (without distorting it). Set
`hud-logo: ''` for no icon.

### Example

```yaml
hud-prompt: '&b&lMY SERVER &r&7» &fDownload the pack to see the &alogos&f and &aimages&f!'
hud-pack-nome: '&bMY SERVER &fHUD'
hud-pack-descricao: '&7Official logos, banners and GIFs'
hud-logo: 'hud_logo.png'
hud-forcar: false
```

> After editing these keys, run `/npc hud recarregar` (or restart) to regenerate the pack and resend it
> to online players.

---

## 16. Network / proxy

The `conectar <server>` action (sending the player to another backend) uses the `BungeeCord` channel
(Velocity speaks this channel too).

- `config.yml` -> `rede-proxy: true`.
- Restart. The plugin registers the outgoing channel.
- Use `/npc acao adicionar hub conectar lobby2` -> clicking sends to `lobby2`.

With `rede-proxy: false` (a single server), the `conectar` action just logs a notice and does nothing, 
no error.

---

## 17. Full `config.yml` reference

> **Important:** every key has a default value in the code. If you update the plugin and your
> `config.yml` doesn't have a new key, the plugin uses the default without breaking. To get the new
> comments, delete `config.yml` (it gets recreated) or add the key by hand.

### General

| Key | Default | Meaning |
|---|---|---|
| `lang` | `pt-br` | Language of the command/log/HUD messages: `en` or `pt-br`. Loads `messages_<lang>.yml`. (The config keys themselves stay in pt-BR.) |
| `tracking-ativo` | `true` | Toggles ALL NPC spawning (an emergency brake, see troubleshooting). |
| `distancia-visao` | `48` | Distance (blocks) at which the player starts seeing the NPC. |
| `intervalo-tracking` | `10` | How many ticks between recalculating who sees each NPC (20 ticks = 1s). |
| `spawn-grace-ms` | `1000` | Grace period (ms) after login before spawning NPCs (avoids stalling the login). `0` = immediate. |
| `cooldown-clique` | `500` | Minimum time (ms) between two clicks on the same NPC by the same player (effective min. 200). |
| `rede-proxy` | `false` | `true` enables the `conectar` action (proxied network). |

### HUD: general

| Key | Default | Meaning |
|---|---|---|
| `hud-ativo` | `true` | Toggles the HUD system. |
| `hud-exibir` | `title` | Where the image appears: `title` / `actionbar` / `chat`. |
| `hud-altura` | `128` | Height (px) of the image glyph. |
| `hud-ascent` | `80` | Vertical position of the glyph. **Keep ≤ `hud-altura`.** |
| `hud-hd-tile` | `16` | On-screen size of each tile of the images in `hud_imagens/hd/`. |

### HUD: hosting

| Key | Default | Meaning |
|---|---|---|
| `hud-modo` | `upload` | `upload` / `auto` / `manual` / `local` (see section 14). |
| `hud-url` | `''` | Direct link to the `.zip` (used by `manual`, and as the `auto` fallback). |
| `hud-porta` | `8123` | Port of the built-in HTTP server (`local` mode). |
| `hud-host-publico` | `''` | Public IP/domain the players use (`local` mode). Empty = tries `server-ip`, otherwise 127.0.0.1. |

### HUD: pack text and visuals

| Key | Default | Meaning |
|---|---|---|
| `hud-prompt` | (example text) | The "Download pack?" screen message. `&` colors, `\n` line break. Empty = client default. |
| `hud-forcar` | `false` | Forces accepting the pack (`true`), anyone who declines is disconnected. |
| `hud-pack-nome` | `&bSEU SERVER &fHUD` | Pack name (line 1 of the description). |
| `hud-pack-descricao` | (example text) | Pack description (line 2). |
| `hud-logo` | `hud_logo.png` | Pack icon PNG (in `plugins/ArcherNpc/`). Empty = no icon. |

---

## 18. Where the data is stored

```
plugins/ArcherNpc/
├── config.yml # configuration (this manual, section 17)
├── messages_pt-br.yml # pt-BR messages (editable), used if lang: pt-br
├── messages_en.yml # English messages (editable), used if lang: en
├── npcs/<id>.yml # 1 file per NPC (position, skin, actions, appearance...)
├── hud_imagens/ # drop .png/.gif here (id = file name)
│ └── hd/ # large images (sliced into a high-resolution grid)
├── hud_logo.png # (optional) pack icon, name configurable in hud-logo
└── hud_pack.zip # the generated resource pack (use this one in 'manual' mode)
```

Each NPC is a YAML with Portuguese keys (`mundo-uuid`, `x/y/z`, `skin.*`, `acoes`, `tipo`, `brilho`,
`escala`, `pose`, `equipamento.*`, `visibilidade`, etc). You can edit it by hand and use
`/npc hud recarregar` / restart, but the **editor** is the safe path.

---

## 19. Troubleshooting

**Players get dropped with "network protocol error" on join**
- Set `tracking-ativo: false`, restart, join, fix the problematic NPC, switch it back to `true`.
- Increase `spawn-grace-ms` (e.g. `2000`) to give the client time to load before the packets.

**The NPC doesn't appear**
- Are you within `distancia-visao`? In the **same world**?
- Is `tracking-ativo` set to `true`?
- Is the **visibility** not `manual` (hides it from everyone) or `permissao` without you having the node?

**The skin won't load / stays Steve**
- The download is async, wait a few seconds.
- Does the nick exist on Mojang? Skin by URL isn't supported (use a nick or mirror).

**The pack won't download / stays "red" (failed)**
- The console logs each player's status: `[HUD] <player> resource pack: <STATUS>`.
- `upload`: the mc-packs.net service may be down, try `auto` or `manual`.
- `local`: is `hud-porta` **open/forwarded**? Is `hud-host-publico` reachable from outside?
- `manual`: is `hud-url` a **direct link** to the `.zip` (not an HTML page)?

**"port already in use" in local mode**
- Change `hud-porta` to a free port and `/npc hud recarregar`.

**The image appears shifted/cropped**
- Adjust `hud-altura`/`hud-ascent` (remember: `ascent ≤ altura`) and `hud-hd-tile`; then **APPLY**.

---

## 20. Project status

### Done and working
- Paper-native boot + PacketEvents (with a guard so it doesn't break other plugins' PacketEvents).
- Packet NPCs: create/delete/list/copy, per-file persistence, visibility by distance and by viewer,
  session cleanup on quit.
- Skins by **nick** and by **URL/MineSkin** (async, cache, coalescing) and **mirror-skin**.
- Click actions (mensagem/comando/console/**comando-op**/teleporte/som/menu/conectar/**imagem**),
  and **advanced actions** (`esperar`, `aleatorio`, `travar`), cooldown, anti-double-click,
  per-button trigger (direito/esquerdo/qualquer).
- Network connector (BungeeCord/Velocity proxy) with clean degradation on a single server.
- Multi-line nameplates with PlaceholderAPI (throttle + diff).
- Look at player (with an angle gate), glow+color, scale, pose, 6-slot equipment.
- Types: player, **any mob**, item/block on display, including **ItemsAdder/Nexo** (via reflection).
- Visibility `todos`/`permissao`/**`manual`** (with `/npc ver`), collision, show in tab.
- Complete **GUI editor** (localized, en / pt-br) + chat input.
- **HUD system**: png/gif/HD -> generated resource pack, `upload`/`auto`/`manual`/`local` hosting,
  and **pack customization** (prompt, logo, name, description, force).

### Notes
- **Skin by URL** depends on **MineSkin** being up; without an API key there's a rate limit
  (set `skin-mineskin-key`).
- **`comando-op`** grants temporary OP to the player during the command (always removed afterward).
  Configure it carefully, it's an admin feature.
- Pending internal/cosmetic refactors (extract `LookMath`, aim test, split out `GlowService`,
  `EquipmentData` as a record), they **don't affect usage**, only code organization.

> Summary: the plugin is **complete for a lobby**, creating NPCs, appearances, simple and advanced
> actions, nameplate, manual/permission visibility, skins by nick/URL/mirror, and a HUD with a custom
> logo.
