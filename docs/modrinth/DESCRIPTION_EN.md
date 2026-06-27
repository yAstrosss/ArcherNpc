# ArcherNpc

**Packet-based NPCs with a full on-screen image & GIF engine — 100% GUI-driven.**

Lightweight, lag-free lobby NPCs that aren't real entities (sent purely through packets), plus a built-in HUD engine that can display **static images, animated GIFs, and high-resolution pictures right on the player's screen** — perfect for shops, payment QR codes, warps, info panels, and announcements. Everything is configured by clicking through menus; commands are only needed for the basics. The plugin ships in **English *and* Brazilian Portuguese**.

> 🖼️ **[ IMAGEM AQUI — banner principal (hero), bem largo: nome do plugin "ArcherNpc" + uma fileira de NPCs e um exemplo de imagem aparecendo na tela do jogador. Coloque no topo da página. ]**

---

## ✨ Why ArcherNpc?

- ⚡ **Zero-lag, packet-based** — NPCs are never real entities, so the server never ticks them and they don't show up in `/kill @e`. Ideal for crowded hubs, lobbies, and spawns with many NPCs and players.
- 🧵 **Folia-ready & fully thread-safe** — built for modern, region-threaded servers (also runs on regular Paper).
- 🖱️ **GUI-first** — a rich click-driven editor. No fighting with config files.
- 🖼️ **HUD image engine** — the feature no other NPC plugin ships: draw images and GIFs on the player's screen.
- 🌍 **Bilingual out of the box** — full **English** and **Brazilian Portuguese** message files, switchable with one config line.
- 🎨 **ItemsAdder & Nexo support** *(BETA)* — use your custom models out of the box (no hard dependency).

---

## 🧍 NPC Types

Turn an NPC into anything:

- **Player** (with real skins)
- **Any living mob** — zombie, villager, skeleton, allay, iron golem, cow, and every other spawnable entity
- **Item display** — a floating item (vanilla, ItemsAdder, or Nexo)
- **Block display** — any block

> 🖼️ **[ IMAGEM AQUI — vários NPCs lado a lado: um player, um aldeão (villager), um diamante flutuando (item display) e um bloco (block display). ]**

---

## 🎨 Appearance

- **Skins** in three ways:
  - by **Mojang username** (downloaded, cached, applied asynchronously),
  - by **direct image URL** (resolved automatically through **MineSkin**),
  - or **Skin Mirror** — the NPC copies the skin of whoever is looking at it (per-viewer).
- New NPCs are created with **your** skin by default.
- **Multi-line nameplates** with full **MiniMessage** support — RGB hex `<#ff0000>`, `<gradient:gold:red>`, formatting — *and* classic `&` color codes. Use `|` to split lines.
- **PlaceholderAPI** support in nameplates, resolved **per viewer** (`%player_name%`, etc.) with smart throttling (re-sent only when the text actually changes — no flicker).
- **Glow** effect in **16 colors**.
- **Scale** from 0.1× to 10× (make NPCs tiny or giant).
- **Pose** — standing, sneaking, sleeping, swimming, sitting.
- **Equipment** — 6 slots: helmet, chestplate, leggings, boots, main hand, off hand (vanilla, ItemsAdder, or Nexo items).
- Adjustable **NPC height (Y)** and **nameplate height offset**.

> 🖼️ **[ IMAGEM AQUI — um NPC com nome em gradiente brilhando (glow), armadura customizada e uma diferença de escala visível ao lado de outro NPC. ]**

---

## 🕹️ Behavior & Interaction

- **Look-at-player** — the NPC smoothly turns to face nearby players (with an angle gate so it doesn't spin).
- **Per-action click triggers** — assign each action to **left-click**, **right-click**, or **any** click.
- **Per-NPC cooldown** to prevent click spam (anti double-click built in).
- **Collision** toggle (let players walk through the NPC or not).
- **Visibility modes** — everyone, permission-based, or **manual** (hand-pick which players can see it, saved by UUID so it survives restarts and reconnects).
- **Show in tab** toggle (player type).
- **Configurable view distance** — NPCs only render within a radius you set.

> 🖼️ **[ IMAGEM AQUI — um NPC virado para o jogador (look-at) com a etiqueta de nome visível acima da cabeça. ]**

---

## ⚡ The Action Engine

Chain any number of actions per NPC — **reorderable** in the GUI, each with its own click trigger. This is far more than a simple "run command on click":

**Basic actions**
- Run a command **as the player**
- Run a command **as console** (`%player%` → the clicker's name)
- Run a command **as the player with temporary OP** (`comando-op`) — OP is granted only for that single command and **always** removed afterward, even if the command fails. Great for warps/crates that need OP; admin-only by design.
- Send a **message**
- **Teleport** (with optional yaw/pitch)
- Play a **sound**
- **Open a menu** (integrates with menu plugins)
- **Connect** to another server (proxy)
- **Show a HUD image / GIF** — and run the next actions **when it finishes** (see below)

**Advanced flow control**
- 🖼️ **Image as a gate** — the `image` action shows the picture and **defers the rest of the list until it ends**. Everything after an `image` action is effectively the "on-finish" sequence.
- ⏳ **Wait** (`esperar`) — pause the sequence by seconds or ticks (`5`, `5s`, `20t`) before continuing.
- 🎲 **Random** (`aleatorio`) — picks **one** of the following actions at random and runs only that. Perfect for random rewards.
- 🔒 **Lock** (`travar`) — a barrier that locks the NPC for that player until the sequence finishes, blocking spam during timed/`wait` sequences.

> 🎞️ **[ GIF AQUI — clicar no NPC → um QR code aparece na tela → some → mensagem de confirmação (demonstrando o encadeamento "imagem → ação ao terminar"). ]**

Example — a timed, spam-proof reward sequence:

```
0. lock
1. message  &aPreparing your reward...
2. wait     3s
3. op-command  crate open vip
```

Example — a random reward:

```
0. random
1. op-command  give-diamond %player%
2. op-command  give-iron %player%
3. message     &cNo luck this time!
```

---

## 🖼️ The HUD Image Engine *(the headline feature)*

Display images **on the player's screen** — not stuck on a block or a map. Drop a file into a folder and the plugin turns it into font glyphs inside an auto-generated resource pack.

- **Static images** (PNG) — shop banners, **payment QR codes**, logos.
- **Animated GIFs** — frame-accurate playback at the GIF's own speed.
- **High-resolution mode** — large images dropped into the `hd/` subfolder are automatically **sliced into a seamless tile grid** for crisp detail.
- **Auto-generated resource pack** — drop a `.png` or `.gif` into `hud_imagens/` (the file name becomes the image id) and the plugin builds, hosts, and serves the pack for you.
- **Display target** — show the image as a `title` (centered), on the `actionbar`, or in `chat`.
- **In-game tuning GUI** (`/npc hud`) — adjust glyph height, vertical position (ascent), and HD tile size live, then **Apply**.
- **Plays nicely with other packs** — stacks alongside ItemsAdder / Oraxen resource packs instead of replacing them.

### Flexible hosting — four modes, with honest trade-offs

- **`upload`** *(default, recommended)* — auto-uploads to mc-packs.net. **Zero config**, works behind NAT/firewall, **no open port needed** — ideal for shared/limited hosting. (Depends on a free third-party service staying online.)
- **`auto`** — tries `upload`, falls back to your `hud-url`, then to `local`. Best reliability without losing convenience.
- **`manual`** *(most reliable long-term)* — you host the generated `hud_pack.zip` yourself and paste the direct link. Full control; recommended for serious servers (e.g. **GitHub Releases + jsDelivr** CDN, free and stable). You re-upload when the pack changes.
- **`local`** — the plugin runs its own HTTP server on a configurable port, served on **all interfaces**, using your public IP/domain. For VPS/dedicated hosts with an open port.

### Customize the pack itself

All without touching code — set in `config.yml`:

- **Prompt text** shown on the "Download resource pack?" screen.
- **Pack name** and **description** in the client's pack list.
- A custom **logo/icon** (drop a square PNG; non-square images are centered, not distorted).
- **Force-accept** toggle (kick players who decline, or keep it optional).

> 🎞️ **[ GIF AQUI — um GIF animado tocando centralizado na tela do jogador (HUD). ]**
>
> 🖼️ **[ IMAGEM AQUI — uma imagem em alta resolução exibida na tela para mostrar a qualidade do fatiamento em grade (HD tiling). ]**

---

## 🧩 ItemsAdder & Nexo Integration *(BETA)*

> ⚠️ **BETA** — this integration is still being tested. It works, but expect rough edges.

Use your existing custom content with no extra setup:

- Set an NPC's item/block display to any **ItemsAdder** (`itemsadder:id`) or **Nexo** (`nexo:id`) model.
- Equip NPCs with custom items.
- Detected automatically by reflection — **no hard dependency**, and nothing breaks if they're absent (it falls back to the vanilla item silently).

> 🖼️ **[ IMAGEM AQUI — um NPC exibindo um modelo customizado do ItemsAdder/Nexo. ]**

---

## 🌐 Cross-Server & Networking *(BungeeCord / Velocity)*

- The `connect` action sends the player to another backend over the `BungeeCord` channel (Velocity speaks it too).
- **Built-in transfer queue** — instead of slamming the target backend when a crowd clicks at once, transfers are **rate-limited and queued per target**, with live position feedback on the action bar. No thundering herd on big lobbies.
- Cleanly degrades on a single server: with the proxy disabled, `connect` just logs and does nothing (no errors).

---

## 🌍 Languages

- Full message files for **English** (`messages_en.yml`) and **Brazilian Portuguese** (`messages_pt-br.yml`).
- Switch with a single config line: `lang: en` or `lang: pt-br`.
- Every file is editable, so you can fully retranslate or reword to match your server's tone.

---

## 🖱️ The Editor GUI

Run `/npc editar <id>` and configure **everything** by clicking through a 54-slot menu:

- Skin · Name/nameplate · Look-at · Actions (add/remove/reorder/change trigger) · Glow & color · Scale · Show in tab · Type (player/mob/item/block) · Pose · Equipment
- **Type picker** and **item/block picker** menus (paginated)
- Move here · Center · Face me · Copy · Collision · Skin-mirror · Visibility
- Attach a HUD image · Adjust NPC height & nameplate offset · Delete

Text fields (skin name, custom model IDs, commands, copy id) are typed in chat after the menu closes — type `cancelar` to abort. No config editing required.

> 🖼️ **[ IMAGEM AQUI — o menu principal do editor com todos os ícones de opções. ]**
>
> 🖼️ **[ IMAGEM AQUI — o menu de seleção de item/bloco (picker paginado). ]**

---

## ⌨️ Commands

All commands have **tab-completion** for NPC IDs. Permission: `archernpc.admin`.

| Command | Description |
|---|---|
| `/npc criar <id>` | Create an NPC where you stand (with your skin) |
| `/npc editar <id>` | Open the editor GUI |
| `/npc deletar <id>` | Delete an NPC |
| `/npc listar` | List all NPCs |
| `/npc copiar <id> <new>` | Duplicate an NPC |
| `/npc mover · centralizar · rotacionar <id>` | Position the NPC (move here / center on block / face you) |
| `/npc nome <id> <text>` | Set the nameplate (`|` splits lines) |
| `/npc skin <id> <username\|url>` | Set the skin (auto-detects username vs URL) |
| `/npc acao adicionar · listar · remover <id> ...` | Manage click actions |
| `/npc colidir · espelho · visibilidade <id>` | Toggle collision / skin-mirror / cycle visibility |
| `/npc permissao <id> <perm>` · `/npc ver <id> <player>` | Permission node / manual per-player visibility |
| `/npc hud` | Open the HUD settings menu |
| `/npc hud recarregar` | Rebuild & re-serve the pack after changing images/config |
| `/npc hud <image> [seconds]` | Test a HUD image on yourself |

---

## 🔧 Config Highlights

- `lang` — `en` or `pt-br`.
- `tracking-ativo` — master kill-switch for all NPC spawning (emergency brake).
- `distancia-visao` — render distance in blocks.
- `intervalo-tracking` — how often (in ticks) viewers are recalculated.
- `spawn-grace-ms` — delay after login before spawning NPCs (avoids login stalls).
- `cooldown-clique` — minimum ms between clicks on the same NPC.
- `rede-proxy` — enable the cross-server `connect` action and queue.
- Full HUD block (`hud-modo`, `hud-url`, `hud-porta`, `hud-host-publico`, `hud-exibir`, `hud-altura`, `hud-ascent`, `hud-hd-tile`, `hud-prompt`, `hud-pack-nome`, `hud-pack-descricao`, `hud-logo`, `hud-forcar`).

> Missing keys always fall back to safe defaults — updating the plugin never breaks your existing `config.yml`.

---

## ✅ Requirements

- **Paper / Folia 1.21+** (Java 21)
- **PacketEvents** (NPC rendering — required)
- *Optional:* PlaceholderAPI, ItemsAdder, Nexo

---

## 📝 Notes

- Ships in **English and Brazilian Portuguese** — set `lang` in `config.yml`.
- Storage is simple **YAML per NPC** — no database required.

> 🖼️ **[ IMAGEM AQUI — rodapé opcional: créditos / banner do Discord. ]**
