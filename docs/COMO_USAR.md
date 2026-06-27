# ArcherNpc — Manual completo de uso

> NPCs de lobby por **pacotes** (PacketEvents) para servidores **Paper 1.21.11**. Sem entidade no
> servidor, sem lag de mob real. Tudo em **português**, com editor por GUI e sistema de **HUD**
> (imagens/logos/GIFs na tela via resource pack gerado automaticamente).

Índice:

1. [O que o plugin faz](#1-o-que-o-plugin-faz)
2. [Instalação e dependências](#2-instalação-e-dependências)
3. [Primeiros passos (5 minutos)](#3-primeiros-passos-5-minutos)
4. [Permissão](#4-permissão)
5. [Todos os comandos](#5-todos-os-comandos)
6. [Editor por GUI (`/npc editar`)](#6-editor-por-gui-npc-editar)
7. [Aparência: player, mob, item, bloco](#7-aparência-player-mob-item-bloco)
8. [Skin](#8-skin)
9. [Nome / etiqueta (nameplate)](#9-nome--etiqueta-nameplate)
10. [Ações de clique](#10-ações-de-clique)
11. [Brilho, escala, pose, equipamento](#11-brilho-escala-pose-equipamento)
12. [Visibilidade e colisão](#12-visibilidade-e-colisão)
13. [HUD — imagens/logos/GIFs na tela](#13-hud--imagenslogosgifs-na-tela)
14. [Hospedagem do pack (upload / auto / manual / local)](#14-hospedagem-do-pack)
15. [Customizar o pack: texto, logo, nome, descrição](#15-customizar-o-pack-texto-logo-nome-descrição)
16. [Rede / proxy (BungeeCord / Velocity)](#16-rede--proxy)
17. [Referência completa do `config.yml`](#17-referência-completa-do-configyml)
18. [Onde os dados ficam salvos](#18-onde-os-dados-ficam-salvos)
19. [Solução de problemas](#19-solução-de-problemas)
20. [Status do projeto: o que está pronto e o que falta](#20-status-do-projeto)

---

## 1. O que o plugin faz

- **NPCs 100% por pacotes** — cada NPC é desenhado direto no cliente de cada jogador. Não existe
  entidade no servidor, então não pesa no tick e não aparece em `/kill @e`.
- **Aparências**: skin de jogador, qualquer **mob**, ou **item/bloco** em display (inclui modelos do
  **ItemsAdder/Nexo**).
- **Ações ao clicar**: comando, mensagem, teleporte, som, conectar a outro servidor da rede, abrir
  menu, mostrar **imagem/GIF na tela**.
- **Etiquetas** (nome acima da cabeça) com múltiplas linhas e **PlaceholderAPI**.
- **Olhar o jogador**, brilho colorido, escala, pose, equipamento nas 6 slots.
- **Visibilidade** por todos / permissão / manual, e **skin-espelho** (NPC copia a skin de quem olha).
- **Editor por GUI** completo em português — dá pra fazer quase tudo sem decorar comando.
- **HUD**: solte `.png`/`.gif` numa pasta, o plugin gera um resource pack e empurra pros jogadores;
  você mostra a imagem na tela como ação de clique do NPC.

---

## 2. Instalação e dependências

| Plugin | Obrigatório? | Para quê |
|---|---|---|
| **PacketEvents** | ✅ **Sim** | Base de todos os pacotes (spawn, skin, interação). Sem ele o plugin não liga. |
| **PlaceholderAPI** | Opcional | Placeholders nas etiquetas (`%player_name%` etc). |
| **ItemsAdder** ou **Nexo** | Opcional | Modelos customizados em NPCs do tipo item/bloco ou no equipamento. |

Passos:

1. Servidor **Paper 1.21.11** (ou compatível), **Java 21**.
2. Coloque o **PacketEvents** em `plugins/`.
3. Coloque o `ArcherNpc-v1.0-beta.jar` em `plugins/`.
4. Inicie o servidor. No console deve aparecer:
   `ArcherNpc ativado (Paper nativo, NPCs por pacotes).`
5. Confirme com `/plugins` — o ArcherNpc aparece na seção **Paper Plugins**.

> O plugin detecta sozinho se o PacketEvents já está instalado como plugin separado e **não**
> reinicializa por cima (isso quebraria os pacotes de todo mundo). Se você não tiver o plugin
> PacketEvents separado, o ArcherNpc sobe a própria instância.

---

## 3. Primeiros passos (5 minutos)

```
/npc criar loja            # cria um NPC com a SUA skin, virado pra onde você olha
                           # → ele aparece na sua frente em instantes
/npc nome loja &b&lLOJA|&7Clique para abrir
/npc acao adicionar loja mensagem &aBem-vindo à loja!
                           # → clique no NPC com botão direito: a mensagem aparece
/npc editar loja           # abre o editor visual pra ajustar o resto no clique
```

Pronto — você já tem um NPC clicável com nome e ação. O resto deste manual é "tudo que dá pra fazer".

---

## 4. Permissão

Todo o comando `/npc` (e o alias `/npcs`) exige:

```
archernpc.admin
```

Dê só pra staff. Não há permissões separadas por subcomando — quem tem `archernpc.admin` pode tudo.

> A permissão usada na **visibilidade por permissão** dos NPCs é **outra coisa**: é um nó qualquer que
> você escolhe por NPC (veja [Visibilidade](#12-visibilidade-e-colisão)).

---

## 5. Todos os comandos

Use `<obrigatório>` e `[opcional]`. O `<id>` tem **autocompletar** (TAB) com os NPCs existentes.

### Básicos

| Comando | O que faz |
|---|---|
| `/npc criar <id>` | Cria um NPC na sua posição, com a sua skin. `id`: a-z, 0-9, `_`, até 16 caracteres. |
| `/npc deletar <id>` | Remove o NPC e apaga o arquivo dele. |
| `/npc listar` | Lista todos os NPCs criados. |
| `/npc editar <id>` | Abre o **editor por GUI** (recomendado pra tudo). |
| `/npc copiar <id> <novo>` | Duplica o NPC `<id>` num novo id, na sua posição. |

### Posição

| Comando | O que faz |
|---|---|
| `/npc mover <id>` | Traz o NPC pra onde você está (posição + direção). |
| `/npc centralizar <id>` | Centraliza o NPC no meio do bloco (x.5 / z.5). |
| `/npc rotacionar <id>` | Faz o NPC **virar pra você**. |

### Aparência e texto

| Comando | O que faz |
|---|---|
| `/npc nome <id> <texto>` | Define a etiqueta. Use `|` pra separar **linhas**. Texto vazio remove. Cores com `&`. |
| `/npc skin <id> <valor>` | Define a skin. `<valor>` = **nick** (Mojang) ou **link http(s)** (via MineSkin) — autodetecta. |

> Brilho, escala, pose, equipamento, tipo (mob/item/bloco) e imagem-HUD são feitos pelo
> **editor** (`/npc editar`). Veja as seções 6–11.

### Ações de clique

| Comando | O que faz |
|---|---|
| `/npc acao adicionar <id> <tipo> <valor>` | Adiciona uma ação. Tipos na [seção 10](#10-ações-de-clique). |
| `/npc acao listar <id>` | Lista as ações do NPC com índice. |
| `/npc acao remover <id> <indice>` | Remove a ação pelo índice (veja em `acao listar`). |

### Comportamento

| Comando | O que faz |
|---|---|
| `/npc colidir <id>` | Liga/desliga colisão (o jogador atravessar o NPC ou não). |
| `/npc espelho <id>` | Liga/desliga **skin-espelho** (o NPC mostra a skin de quem está olhando). |
| `/npc visibilidade <id>` | Cicla **todos → permissao → manual**. |
| `/npc permissao <id> <perm>` | Define o nó de permissão usado quando a visibilidade é `permissao`. |
| `/npc ver <id> <jogador>` | Liga/desliga (toggle) se um jogador **online** vê o NPC quando a visibilidade é `manual`. Salvo por UUID. |

### HUD (imagens na tela)

| Comando | O que faz |
|---|---|
| `/npc hud` | Abre a GUI de configuração do HUD (escala/posição/onde exibir/recarregar). |
| `/npc hud recarregar` | Regenera o pack (depois de adicionar/trocar imagens ou mexer no `config.yml`). |
| `/npc hud <imagem> [segundos]` | **Testa** uma imagem em você por N segundos (padrão 10). |

---

## 6. Editor por GUI (`/npc editar`)

`/npc editar <id>` abre um menu de 54 slots com **tudo**. Itens read-only (não dá pra tirar). Resumo dos botões:

| Botão | Função |
|---|---|
| 🟢 Skin | Digita um nick (chat) pra trocar a skin. |
| 🏷️ Nome / Etiqueta | Digita o texto (use `|` entre linhas). |
| 👁️ Olhar o jogador | Liga/desliga o NPC seguir você com a cabeça. |
| 📖 Ações | Abre o submenu de ações (adicionar/remover/ordenar/trocar gatilho). |
| ✨ Brilho (glow) | Liga/desliga o contorno brilhante. |
| 🎨 Cor do brilho | Cicla entre 16 cores. |
| 🟩 Escala | Esq +0.1 / Dir −0.1 (de 0.1x a 10x). |
| 👤 Mostrar no tab | Liga/desliga o NPC aparecer na lista de jogadores (TAB). |
| 🛡️ Aparência | Escolhe player / mob / item / bloco (com pickers paginados). |
| 👢 Pose | normal / agachado / dormindo / nadando / sentado. |
| 🧰 Equipamento | Digita `slot ref` (ex: `capacete minecraft:DIAMOND_HELMET`). |
| 📄 Altura do nome | Sobe/desce a etiqueta (offset). |
| ⛏️ Altura do NPC (Y) | Ajuste fino de altura do NPC. |
| 🗺️ Imagem na tela (HUD) | Anexa uma imagem/gif ao clique do NPC (vira ação `imagem`). |
| 🧭 Mover / Centralizar / Virar | Posição rápida. |
| 📕 Copiar NPC | Digita um novo id pra duplicar. |
| 🧱 Colisão / Skin-espelho / Visibilidade | Alternar comportamento. |
| 🚫 Deletar | Remove o NPC. |

**Entrada por chat:** quando o editor pede um texto (skin, nome, equipamento, copiar...), ele fecha o
menu e espera a sua próxima mensagem no chat. Digite `cancelar` pra abortar. Funciona inclusive se você
começar com `/`.

---

## 7. Aparência: player, mob, item, bloco

No editor → **Aparência**. Quatro categorias:

- **Player (skin)** — o NPC é um jogador falso (usa a skin definida; veja seção 8).
- **Entidade / Mob** — qualquer entidade viva e spawnável do Minecraft (zumbi, aldeão, allay, vaca...).
  Picker paginado, ou "Digitar manualmente" (ex: `cow`, `zombie`, `allay`).
- **Item (display)** — qualquer item, exibido como display. Inclui `minecraft:ID`, **`nexo:id`**, **`itemsadder:id`**.
- **Bloco (display)** — qualquer bloco, exibido como display.

> Modelos do **ItemsAdder/Nexo** são resolvidos por reflexão: funciona se o plugin estiver instalado,
> e simplesmente cai pro item padrão se não estiver (sem erro).

---

## 8. Skin

- **Por nick**: `/npc skin <id> <nick>` ou editor → Skin → digita o nome de um jogador (a skin da conta
  Mojang dele é baixada, em cache, e aplicada). O download é assíncrono — a skin "chega" em instantes.
- **Por URL** (link de imagem): `/npc skin <id> https://.../skin.png` — se o valor começa com `http(s)`,
  o plugin resolve a skin pelo **MineSkin** automaticamente. Funciona sem configurar nada, mas com limite
  de taxa (pode falhar em uso intenso). Depende do MineSkin estar no ar.
- **Skin-espelho** (`/npc espelho <id>` ou botão no editor): o NPC mostra a **skin de quem está olhando**.
  Ótimo pra "seu personagem" em lobby.
- Ao **criar** um NPC, ele já nasce com a **sua** skin.

---

## 9. Nome / etiqueta (nameplate)

```
/npc nome <id> linha1|linha2|linha3
```

- Cada `|` vira uma **linha** flutuando acima da cabeça.
- Cores e formatação com `&` (ex: `&b&lLOJA`, `&7clique`).
- Com **PlaceholderAPI** instalado, placeholders são resolvidos **por jogador** (ex:
  `&aOlá %player_name%`) e atualizam sozinhos (sem piscar — só reenvia quando o texto muda).
- Texto vazio remove a etiqueta.
- A altura da etiqueta se ajusta no editor (**Altura do nome / offset**).

---

## 10. Ações de clique

Adicione com `/npc acao adicionar <id> <tipo> <valor>` ou pelo editor (submenu **Ações**).
As ações rodam **na ordem** da lista quando o NPC é clicado.

| Tipo | Valor | O que faz |
|---|---|---|
| `mensagem` | `&aOlá!` | Manda mensagem (cores `&`) pro jogador. |
| `comando` | `spawn` | Roda o comando **como o jogador** (sem a `/`). |
| `console` | `give %player% diamond 1` | Roda **no console**. `%player%` vira o nome de quem clicou. |
| `teleporte` | `world,100,64,200[,yaw,pitch]` | Teleporta o jogador. |
| `som` | `minecraft:ui.button.click` ou `ENTITY_VILLAGER_YES` | Toca um som. |
| `menu` | `loja` | Atalho pra `menu <valor>` (integra com plugins de menu). |
| `conectar` | `lobby2` | Manda o jogador pra outro servidor da rede (**exige `rede-proxy: true`**, seção 16). |
| `comando-op` | `warp vip` | Roda o comando **como o jogador com OP temporário** (sempre removido depois). %player% vira o nome. ⚠️ poderoso — só staff configura. |
| `imagem` | `qr` ou `qr;5` | Mostra a **imagem/GIF do HUD** `qr` por N segundos (padrão 5). Veja abaixo. |
| `esperar` | `5` / `5s` / `20t` | **Pausa** e adia o resto da lista: número/`s` = segundos, `t` = ticks. |
| `aleatorio` | (sem valor) | **Escolhe UMA** das ações listadas **depois** dela e roda só essa (sorteio). |
| `travar` | (sem valor) | **Barreira**: trava o NPC pra esse jogador até a sequência terminar (bloqueia spam em sequências com `esperar`). |

**Gatilho (botão) por ação:** cada ação tem um gatilho — `direito` (padrão), `esquerdo` ou `qualquer`.
Troque no editor (submenu Ações → clique esquerdo no item cicla o gatilho).

**Ordem e "ao terminar":** a ação `imagem` é especial — ela mostra a imagem e **adia** o resto da
lista até a imagem acabar. Ou seja, **tudo que vier depois de um `imagem` na lista é a "ação ao
terminar"**. Exemplo:

```
0. imagem  promo;5
1. mensagem &aObrigado por assistir!
2. comando loja
```
→ mostra `promo` por 5s; quando acaba, manda a mensagem e abre a loja.

**Sequências com tempo e sorteio:** `esperar` e `imagem` adiam o resto da lista; `aleatorio` ramifica.
Exemplos:

```
# Sequência cronometrada (trava o spam enquanto roda):
0. travar
1. mensagem &aPreparando seu prêmio...
2. esperar 3s
3. comando-op cratebox abrir vip

# Recompensa aleatória (sorteia 1 das 3 ações seguintes):
0. aleatorio
1. comando-op givdiamond %player%
2. comando-op giviron %player%
3. mensagem &cVocê não ganhou nada dessa vez!
```

> `comando-op` dá OP **temporário** ao jogador só durante aquele comando e **sempre** remove depois
> (mesmo se o comando falhar). Use pra comandos que exigem OP; configure com cuidado.

---

## 11. Brilho, escala, pose, equipamento

Tudo no editor (`/npc editar`):

- **Brilho (glow)** + **cor** — contorno brilhante; 16 cores (white, red, gold, aqua, blue, ...).
- **Escala** — de 0.1x a 10x (passo 0.1).
- **Pose** — normal / agachado / dormindo / nadando / sentado.
- **Equipamento** — 6 slots, formato `slot ref`:
  - slots: `capacete`, `peito`, `calca`, `bota`, `mao`, `secundaria`
  - ref: `minecraft:DIAMOND_HELMET`, `nexo:<id>`, `itemsadder:<id>`; `remover` limpa o slot.
  - ex: `capacete minecraft:NETHERITE_HELMET`

---

## 12. Visibilidade e colisão

- **Colisão** (`/npc colidir <id>`): se o jogador atravessa o NPC ou esbarra nele.
- **Visibilidade** (`/npc visibilidade <id>` cicla):
  - `todos` — todo mundo vê (padrão).
  - `permissao` — só quem tem o nó definido em `/npc permissao <id> <perm>` vê.
  - `manual` — escondido por padrão; só vê quem você libera com `/npc ver <id> <jogador>`.

**Visibilidade manual passo a passo:**
1. `/npc visibilidade <id>` até ficar em `manual`.
2. `/npc ver <id> <jogador>` (jogador **online**) — libera; rode de novo pra revogar (toggle).
3. A liberação é salva por **UUID**, então persiste reinício e vale quando o jogador reconectar.

Distância em que o NPC aparece: `distancia-visao` no `config.yml`.

---

## 13. HUD — imagens/logos/GIFs na tela

O HUD transforma `.png`/`.gif` em **glifos de fonte** dentro de um resource pack gerado na hora, e
mostra na tela do jogador (title/actionbar/chat). Serve pra logos, QR codes, banners, "assista e ganhe", etc.

### Como adicionar imagens

1. Pasta criada sozinha: `plugins/ArcherNpc/hud_imagens/`
   - Solte `.png` ou `.gif` aqui. O **nome do arquivo** (sem extensão, minúsculo) vira o **id** da imagem.
   - Subpasta `hud_imagens/hd/` → imagens grandes são **fatiadas em grade** (alta resolução na tela).
2. `/npc hud recarregar` (ou reinicie). O console mostra quais imagens entraram.
3. `/npc hud <id> [segundos]` pra testar em você.

### Como mostrar pro jogador

- **No clique de um NPC**: editor → **Imagem na tela (HUD)** → escolhe a imagem (esq=5s, dir=10s,
  shift=30s). Isso cria a ação `imagem <id>;<segundos>`.
- Ou direto: `/npc acao adicionar <id> imagem <imgId>;<segundos>`.

### Ajustes de tamanho/posição

GUI `/npc hud`, ou no `config.yml`:
- `hud-exibir`: `title` (centro) / `actionbar` / `chat`.
- `hud-altura` (altura do glifo) e `hud-ascent` (posição vertical; **mantenha ascent ≤ altura**).
- `hud-hd-tile`: tamanho na tela de cada tile das imagens HD.
- Depois de mexer: **APLICAR** na GUI ou `/npc hud recarregar`.

---

## 14. Hospedagem do pack

Pra o jogador ver as imagens, o cliente precisa **baixar o pack**. O `hud-modo` decide como ele é
publicado. Análise honesta de cada opção:

### `upload` (padrão, recomendado) — mc-packs.net
- ✅ **Zero configuração**, funciona atrás de NAT/firewall, **não precisa abrir porta**.
- ✅ Ideal pra host **compartilhada/limitada** (só usa HTTP de saída).
- ⚠️ Depende de um serviço **grátis de terceiros** (mc-packs.net) ficar no ar. Se ele cair, a HUD
  para até voltar. O pack vira público no CDN deles.
- **Use quando:** você não controla o servidor (host compartilhada) e quer que "simplesmente funcione".

### `auto` (upload com rede de segurança)
- Tenta `upload`; se falhar, usa o `hud-url` que você definiu; se não tiver, cai pro `local`.
- ✅ Melhor confiabilidade sem perder a praticidade do upload.
- **Use quando:** quer o upload, mas com um plano B automático.

### `manual` (você hospeda) — **mais confiável a longo prazo**
- Você sobe o `hud_pack.zip` (gerado em `plugins/ArcherNpc/`) num lugar **seu** e cola o link em `hud-url`.
- ✅ Controle total, permanente, rápido. **Recomendado para servidores sérios.**
- ⚠️ Você re-sobe o zip e atualiza o link **toda vez que o pack mudar** (adicionou imagem, mudou escala...).
- **Hospedagens grátis e estáveis recomendadas:**
  - **GitHub Releases** + **jsDelivr** (CDN grátis): suba o `hud_pack.zip` num release e use
    `https://cdn.jsdelivr.net/gh/<user>/<repo>@<tag>/hud_pack.zip`.
  - Qualquer servidor web/CDN seu, Cloudflare R2, etc. **Tem que ser link direto pro `.zip`.**

### `local` (servidor HTTP interno do plugin)
- O plugin sobe um HTTP na `hud-porta` servindo o zip, em **todas as interfaces**.
- ⚠️ Só funciona se os jogadores **alcançam o IP:porta** da sua máquina: exige a **porta aberta/
  encaminhada** e o **IP/domínio público** em `hud-host-publico`.
- ❌ Em host **compartilhada** normalmente **não** dá (sem porta extra).
- **Use quando:** VPS/dedicado com porta liberada e você não quer depender de serviço externo.

> **Correção importante nesta versão:** antes o modo `local` ficava preso em `127.0.0.1` (só
> funcionava no mesmo PC). Agora ele serve em todas as interfaces e monta o link a partir de
> `hud-host-publico` (ou do `server-ip`). Defina `hud-host-publico` com seu IP/domínio público.

### Recomendação rápida

| Sua situação | Use |
|---|---|
| Host compartilhada / não controla portas | `upload` (ou `auto`) |
| Quer máxima confiabilidade e controla um repositório/CDN | `manual` (GitHub Releases + jsDelivr) |
| VPS/dedicado com porta liberada | `local` (com `hud-host-publico`) ou `auto` |

---

## 15. Customizar o pack: texto, logo, nome, descrição

Tudo no `config.yml`, sem mexer em código. Aceita cores `&` e `\n` pra quebrar linha.

| Chave | O que controla | Onde o jogador vê |
|---|---|---|
| `hud-prompt` | Mensagem da tela **"Baixar pacote de recursos?"** | No popup ao entrar/receber o pack. |
| `hud-pack-nome` | **Nome** do pack (linha 1 da descrição) | Na lista de pacotes do cliente. |
| `hud-pack-descricao` | **Descrição** do pack (linha 2) | Embaixo do nome, na lista de pacotes. |
| `hud-logo` | Nome do PNG do **ícone/logo** | Como ícone do pacote na tela de recursos. |
| `hud-forcar` | Obrigar a aceitar (`true`) ou não (`false`) | Se `true`, quem recusa é desconectado. |

### Como pôr a logo

1. Salve um PNG **quadrado** (128×128 ou 256×256 fica ótimo).
2. Coloque em `plugins/ArcherNpc/` com o nome que estiver em `hud-logo` (padrão `hud_logo.png`).
3. `/npc hud recarregar`. Pronto — o ícone vai junto no pack (vira `pack.png`).

Se a imagem não for quadrada, o plugin centraliza num quadrado transparente (sem distorcer). Deixe
`hud-logo: ''` pra não ter ícone.

### Exemplo

```yaml
hud-prompt: '&b&lMEU SERVER &r&7» &fBaixe o pack pra ver as &alogos&f e &aimagens&f!'
hud-pack-nome: '&bMEU SERVER &fHUD'
hud-pack-descricao: '&7Logos, banners e GIFs oficiais'
hud-logo: 'hud_logo.png'
hud-forcar: false
```

> Depois de editar essas chaves, rode `/npc hud recarregar` (ou reinicie) pra regenerar o pack e
> reenviar pros jogadores online.

---

## 16. Rede / proxy

A ação `conectar <servidor>` (mandar o jogador pra outro backend) usa o canal `BungeeCord`
(Velocity também fala esse canal).

- `config.yml` → `rede-proxy: true`.
- Reinicie. O plugin registra o canal de saída.
- Use `/npc acao adicionar hub conectar lobby2` → clicar envia pro `lobby2`.

Com `rede-proxy: false` (servidor único), a ação `conectar` só avisa no log e não faz nada — sem erro.

---

## 17. Referência completa do `config.yml`

> **Importante:** todas as chaves têm valor-padrão no código. Se você atualizar o plugin e o seu
> `config.yml` não tiver uma chave nova, o plugin usa o padrão sem quebrar. Pra ganhar os comentários
> novos, apague o `config.yml` (ele é recriado) ou adicione a chave na mão.

### Geral

| Chave | Padrão | Significado |
|---|---|---|
| `lang` | `pt-br` | Idioma das mensagens de comando/log/HUD: `en` ou `pt-br`. Carrega `messages_<lang>.yml`. (As chaves do config seguem em pt-BR.) |
| `tracking-ativo` | `true` | Liga/desliga TODO o spawn de NPCs (freio de emergência — veja problemas). |
| `distancia-visao` | `48` | Distância (blocos) em que o jogador começa a ver o NPC. |
| `intervalo-tracking` | `10` | A cada quantos ticks o plugin recalcula quem vê cada NPC (20 ticks = 1s). |
| `spawn-grace-ms` | `1000` | Carência (ms) após o login antes de spawnar NPCs (evita travar o login). `0` = imediato. |
| `cooldown-clique` | `500` | Tempo mínimo (ms) entre dois cliques no mesmo NPC pelo mesmo jogador (mín. efetivo 200). |
| `rede-proxy` | `false` | `true` habilita a ação `conectar` (rede com proxy). |

### HUD — geral

| Chave | Padrão | Significado |
|---|---|---|
| `hud-ativo` | `true` | Liga/desliga o sistema de HUD. |
| `hud-exibir` | `title` | Onde a imagem aparece: `title` / `actionbar` / `chat`. |
| `hud-altura` | `128` | Altura (px) do glifo da imagem. |
| `hud-ascent` | `80` | Posição vertical do glifo. **Mantenha ≤ `hud-altura`.** |
| `hud-hd-tile` | `16` | Tamanho na tela de cada tile das imagens em `hud_imagens/hd/`. |

### HUD — hospedagem

| Chave | Padrão | Significado |
|---|---|---|
| `hud-modo` | `upload` | `upload` / `auto` / `manual` / `local` (veja seção 14). |
| `hud-url` | `''` | Link direto do `.zip` (usado por `manual`, e como fallback do `auto`). |
| `hud-porta` | `8123` | Porta do servidor HTTP interno (modo `local`). |
| `hud-host-publico` | `''` | IP/domínio público que os jogadores usam (modo `local`). Vazio = tenta `server-ip`, senão 127.0.0.1. |

### HUD — texto e visual do pack

| Chave | Padrão | Significado |
|---|---|---|
| `hud-prompt` | (texto exemplo) | Mensagem na tela "Baixar pacote?". `&` cores, `\n` quebra linha. Vazio = padrão do cliente. |
| `hud-forcar` | `false` | Obriga aceitar o pack (`true`) — quem recusa é desconectado. |
| `hud-pack-nome` | `&bSEU SERVER &fHUD` | Nome do pack (linha 1 da descrição). |
| `hud-pack-descricao` | (texto exemplo) | Descrição do pack (linha 2). |
| `hud-logo` | `hud_logo.png` | PNG do ícone do pack (em `plugins/ArcherNpc/`). Vazio = sem ícone. |

---

## 18. Onde os dados ficam salvos

```
plugins/ArcherNpc/
├── config.yml              # configurações (este manual, seção 17)
├── messages_pt-br.yml      # mensagens pt-BR (editável) — usado se lang: pt-br
├── messages_en.yml         # mensagens em inglês (editável) — usado se lang: en
├── npcs/<id>.yml           # 1 arquivo por NPC (posição, skin, ações, aparência...)
├── hud_imagens/            # solte .png/.gif aqui (id = nome do arquivo)
│   └── hd/                 # imagens grandes (fatiadas em grade de alta resolução)
├── hud_logo.png            # (opcional) ícone do pack — nome configurável em hud-logo
└── hud_pack.zip            # o resource pack gerado (use este no modo 'manual')
```

Cada NPC é um YAML com chaves em português (`mundo-uuid`, `x/y/z`, `skin.*`, `acoes`, `tipo`,
`brilho`, `escala`, `pose`, `equipamento.*`, `visibilidade`, etc). Dá pra editar na mão e usar
`/npc hud recarregar` / reiniciar, mas o **editor** é o caminho seguro.

---

## 19. Solução de problemas

**Jogadores caem com "network protocol error" ao entrar**
- Coloque `tracking-ativo: false`, reinicie, entre, conserte o NPC problemático, volte pra `true`.
- Aumente `spawn-grace-ms` (ex: `2000`) pra dar tempo do cliente carregar antes dos pacotes.

**O NPC não aparece**
- Você está dentro de `distancia-visao`? No **mesmo mundo**?
- `tracking-ativo` está `true`?
- A **visibilidade** não está `manual` (esconde de todos) nem `permissao` sem você ter o nó?

**A skin não carrega / fica Steve**
- Download é assíncrono — espere alguns segundos.
- O nick existe na Mojang? Skin por URL não é suportada (use nick ou espelho).

**O pack não baixa / fica "vermelho" (falhou)**
- O console loga o status de cada jogador: `[HUD] <jogador> resource pack: <STATUS>`.
- `upload`: o serviço mc-packs.net pode estar fora — tente `auto` ou `manual`.
- `local`: a `hud-porta` está **aberta/encaminhada**? `hud-host-publico` é alcançável de fora?
- `manual`: o `hud-url` é **link direto** pro `.zip` (não uma página HTML)?

**"porta já está em uso" no modo local**
- Troque `hud-porta` pra uma porta livre e `/npc hud recarregar`.

**A imagem aparece deslocada/cortada**
- Ajuste `hud-altura`/`hud-ascent` (lembre: `ascent ≤ altura`) e `hud-hd-tile`; depois **APLICAR**.

---

## 20. Status do projeto

### ✅ Pronto e funcionando
- Boot Paper-nativo + PacketEvents (com guarda pra não quebrar PacketEvents de outros plugins).
- NPCs por pacote: criar/deletar/listar/copiar, persistência por arquivo, visibilidade por distância
  e por viewer, limpeza de sessão ao sair.
- Skins por **nick** e por **URL/MineSkin** (assíncrono, cache, coalescência) e **skin-espelho**.
- Ações de clique (mensagem/comando/console/**comando-op**/teleporte/som/menu/conectar/**imagem**),
  e **ações avançadas** (`esperar`, `aleatorio`, `travar`), cooldown, anti-duplo-clique,
  gatilho por botão (direito/esquerdo/qualquer).
- Conector de rede (proxy BungeeCord/Velocity) com degradação limpa em servidor único.
- Etiquetas multi-linha com PlaceholderAPI (throttle + diff).
- Olhar o jogador (com gate de ângulo), brilho+cor, escala, pose, equipamento 6 slots.
- Tipos: player, **qualquer mob**, item/bloco em display, incluindo **ItemsAdder/Nexo** (por reflexão).
- Visibilidade `todos`/`permissao`/**`manual`** (com `/npc ver`), colisão, mostrar no tab.
- **Editor por GUI** completo (pt-BR) + entrada por chat.
- **Sistema de HUD**: png/gif/HD → resource pack gerado, hospedagem `upload`/`auto`/`manual`/`local`,
  e **customização do pack** (prompt, logo, nome, descrição, forçar).

### ⚠️ Observações
- **Skin por URL** depende do **MineSkin** estar no ar; sem API key há limite de taxa
  (defina `skin-mineskin-key`).
- **`comando-op`** dá OP temporário ao jogador durante o comando (sempre removido depois). Configure
  com cuidado — é um recurso de admin.
- Refactors internos/cosméticos pendentes (extrair `LookMath`, teste de mira, separar `GlowService`,
  `EquipmentData` como record) — **não afetam o uso**, só organização do código.

> Resumo: o plugin está **completo pra um lobby** — criar NPCs, aparências, ações simples e avançadas,
> nameplate, visibilidade manual/permissão, skins por nick/URL/espelho e HUD com logo customizada.
