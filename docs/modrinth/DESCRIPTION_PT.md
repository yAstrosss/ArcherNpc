# ArcherNpc

> ⚠️ **Cópia em pt-BR só para você conferir o que foi escrito.** O texto que vai para o Modrinth é o `DESCRIPTION_EN.md` (em inglês). As marcações de imagem já estão em português nos dois arquivos.

**NPCs por pacotes com um motor completo de imagens e GIFs na tela — 100% por GUI.**

NPCs de lobby leves e sem lag que não são entidades reais (enviados puramente por pacotes), mais um motor de HUD embutido que mostra **imagens estáticas, GIFs animados e fotos em alta resolução direto na tela do jogador** — perfeito para lojas, QR codes de pagamento, warps, painéis de informação e avisos. Tudo configurado clicando em menus; comandos só para o básico. O plugin vem em **inglês *e* português do Brasil**.

> 🖼️ **[ IMAGEM AQUI — banner principal (hero), bem largo: nome do plugin "ArcherNpc" + uma fileira de NPCs e um exemplo de imagem aparecendo na tela do jogador. Coloque no topo da página. ]**

---

## ✨ Por que o ArcherNpc?

- ⚡ **Zero lag, baseado em pacotes** — os NPCs nunca são entidades reais, então o servidor não os processa no tick e eles não aparecem no `/kill @e`. Ideal para hubs, lobbies e spawns cheios, com muitos NPCs e jogadores.
- 🧵 **Pronto para Folia & totalmente thread-safe** — feito para servidores modernos com threads por região (também roda em Paper normal).
- 🖱️ **GUI em primeiro lugar** — um editor rico, todo no clique. Sem brigar com arquivos de config.
- 🖼️ **Motor de imagens na HUD** — o recurso que nenhum outro plugin de NPC tem: desenhar imagens e GIFs na tela do jogador.
- 🌍 **Bilíngue de fábrica** — arquivos de mensagem completos em **inglês** e **português do Brasil**, trocáveis com uma linha de config.
- 🎨 **Suporte a ItemsAdder & Nexo** *(BETA)* — use seus modelos customizados de cara (sem dependência obrigatória).

---

## 🧍 Tipos de NPC

Transforme um NPC em qualquer coisa:

- **Player** (com skins reais)
- **Qualquer mob vivo** — zumbi, aldeão, esqueleto, allay, golem de ferro, vaca e todas as demais entidades spawnáveis
- **Item em display** — um item flutuante (vanilla, ItemsAdder ou Nexo)
- **Bloco em display** — qualquer bloco

> 🖼️ **[ IMAGEM AQUI — vários NPCs lado a lado: um player, um aldeão (villager), um diamante flutuando (item display) e um bloco (block display). ]**

---

## 🎨 Aparência

- **Skins** de três formas:
  - por **nick da Mojang** (baixada, em cache, aplicada de forma assíncrona),
  - por **link direto de imagem** (resolvida automaticamente pelo **MineSkin**),
  - ou **Skin-espelho** — o NPC copia a skin de quem está olhando (por jogador).
- NPCs novos nascem com a **sua** skin por padrão.
- **Etiquetas multi-linha** com suporte total a **MiniMessage** — hex RGB `<#ff0000>`, `<gradient:gold:red>`, formatação — *e* códigos de cor clássicos `&`. Use `|` para separar linhas.
- Suporte a **PlaceholderAPI** nas etiquetas, resolvido **por jogador** (`%player_name%`, etc.), com throttle inteligente (só reenvia quando o texto realmente muda — sem piscar).
- Efeito de **brilho (glow)** em **16 cores**.
- **Escala** de 0.1× a 10× (NPCs minúsculos ou gigantes).
- **Pose** — em pé, agachado, dormindo, nadando, sentado.
- **Equipamento** — 6 slots: capacete, peitoral, calça, bota, mão principal, mão secundária (itens vanilla, ItemsAdder ou Nexo).
- **Altura do NPC (Y)** e **offset da altura do nome** ajustáveis.

> 🖼️ **[ IMAGEM AQUI — um NPC com nome em gradiente brilhando (glow), armadura customizada e uma diferença de escala visível ao lado de outro NPC. ]**

---

## 🕹️ Comportamento & Interação

- **Olhar o jogador** — o NPC vira suavemente para os jogadores próximos (com trava de ângulo para não rodar).
- **Gatilhos de clique por ação** — atribua cada ação ao **clique esquerdo**, **direito** ou **qualquer** clique.
- **Cooldown por NPC** contra spam de clique (anti duplo-clique embutido).
- Alternar **colisão** (deixar o jogador atravessar o NPC ou não).
- **Modos de visibilidade** — todos, por permissão, ou **manual** (escolhe a dedo quem vê, salvo por UUID, então sobrevive a reinícios e reconexões).
- Alternar **mostrar no tab** (tipo player).
- **Distância de visão configurável** — NPCs só renderizam dentro de um raio definido.

> 🖼️ **[ IMAGEM AQUI — um NPC virado para o jogador (look-at) com a etiqueta de nome visível acima da cabeça. ]**

---

## ⚡ O Motor de Ações

Encadeie quantas ações quiser por NPC — **reordenáveis** na GUI, cada uma com seu próprio gatilho de clique. É muito mais do que um simples "rodar comando ao clicar":

**Ações básicas**
- Rodar comando **como o jogador**
- Rodar comando **como console** (`%player%` → o nome de quem clicou)
- Rodar comando **como o jogador com OP temporário** (`comando-op`) — o OP é dado só naquele único comando e **sempre** removido depois, mesmo se o comando falhar. Ótimo para warps/crates que exigem OP; feito para uso de admin.
- Enviar uma **mensagem**
- **Teleportar** (com yaw/pitch opcionais)
- Tocar um **som**
- **Abrir um menu** (integra com plugins de menu)
- **Conectar** a outro servidor (proxy)
- **Mostrar uma imagem / GIF na HUD** — e rodar as próximas ações **quando ela terminar** (veja abaixo)

**Controle de fluxo avançado**
- 🖼️ **Imagem como barreira** — a ação `imagem` mostra a figura e **adia o resto da lista até ela acabar**. Tudo que vem depois de uma ação `imagem` é, na prática, a sequência "ao terminar".
- ⏳ **Esperar** (`esperar`) — pausa a sequência por segundos ou ticks (`5`, `5s`, `20t`) antes de continuar.
- 🎲 **Aleatório** (`aleatorio`) — sorteia **uma** das ações seguintes e roda só ela. Perfeito para recompensas aleatórias.
- 🔒 **Travar** (`travar`) — uma barreira que trava o NPC para aquele jogador até a sequência terminar, bloqueando spam durante sequências cronometradas/com `esperar`.

> 🎞️ **[ GIF AQUI — clicar no NPC → um QR code aparece na tela → some → mensagem de confirmação (demonstrando o encadeamento "imagem → ação ao terminar"). ]**

Exemplo — sequência de recompensa cronometrada e à prova de spam:

```
0. travar
1. mensagem  &aPreparando seu prêmio...
2. esperar   3s
3. comando-op  crate abrir vip
```

Exemplo — recompensa aleatória:

```
0. aleatorio
1. comando-op  da-diamante %player%
2. comando-op  da-ferro %player%
3. mensagem    &cNão foi dessa vez!
```

---

## 🖼️ O Motor de Imagens na HUD *(o recurso de destaque)*

Mostra imagens **na tela do jogador** — não presas a um bloco ou mapa. Solte um arquivo numa pasta e o plugin o transforma em glifos de fonte dentro de um resource pack gerado na hora.

- **Imagens estáticas** (PNG) — banners de loja, **QR codes de pagamento**, logos.
- **GIFs animados** — reprodução frame a frame, na velocidade original do GIF.
- **Modo alta resolução** — imagens grandes soltas na subpasta `hd/` são automaticamente **fatiadas numa grade de tiles** para detalhe nítido.
- **Resource pack gerado automaticamente** — solte um `.png` ou `.gif` em `hud_imagens/` (o nome do arquivo vira o id da imagem) e o plugin monta, hospeda e serve o pack para você.
- **Onde exibir** — mostre a imagem como `title` (centralizada), na `actionbar` ou no `chat`.
- **GUI de ajuste em jogo** (`/npc hud`) — ajuste a altura do glifo, a posição vertical (ascent) e o tamanho do tile HD ao vivo, e depois **Aplicar**.
- **Convive bem com outros packs** — empilha junto com packs do ItemsAdder / Oraxen em vez de substituí-los.

### Hospedagem flexível — quatro modos, com prós e contras honestos

- **`upload`** *(padrão, recomendado)* — faz upload automático para o mc-packs.net. **Zero config**, funciona atrás de NAT/firewall, **não precisa abrir porta** — ideal para host compartilhada/limitada. (Depende de um serviço grátis de terceiros ficar no ar.)
- **`auto`** — tenta `upload`, cai para o seu `hud-url`, depois para o `local`. Melhor confiabilidade sem perder a praticidade.
- **`manual`** *(mais confiável a longo prazo)* — você mesmo hospeda o `hud_pack.zip` gerado e cola o link direto. Controle total; recomendado para servidores sérios (ex.: **GitHub Releases + jsDelivr**, CDN grátis e estável). Você re-sobe quando o pack muda.
- **`local`** — o plugin sobe seu próprio servidor HTTP numa porta configurável, em **todas as interfaces**, usando seu IP/domínio público. Para VPS/dedicado com porta aberta.

### Customize o próprio pack

Tudo sem mexer em código — definido no `config.yml`:

- **Texto do prompt** na tela "Baixar pacote de recursos?".
- **Nome** e **descrição** do pack na lista de pacotes do cliente.
- Um **logo/ícone** customizado (solte um PNG quadrado; imagens não quadradas são centralizadas, sem distorcer).
- Alternar **forçar aceitação** (expulsar quem recusar, ou deixar opcional).

> 🎞️ **[ GIF AQUI — um GIF animado tocando centralizado na tela do jogador (HUD). ]**
>
> 🖼️ **[ IMAGEM AQUI — uma imagem em alta resolução exibida na tela para mostrar a qualidade do fatiamento em grade (HD tiling). ]**

---

## 🧩 Integração ItemsAdder & Nexo *(BETA)*

> ⚠️ **BETA** — integração ainda em testes. Funciona, mas pode ter arestas.

Use seu conteúdo customizado existente sem setup extra:

- Defina o item/bloco em display de um NPC para qualquer modelo do **ItemsAdder** (`itemsadder:id`) ou **Nexo** (`nexo:id`).
- Equipe NPCs com itens customizados.
- Detectado automaticamente por reflexão — **sem dependência obrigatória**, e nada quebra se eles estiverem ausentes (cai para o item vanilla em silêncio).

> 🖼️ **[ IMAGEM AQUI — um NPC exibindo um modelo customizado do ItemsAdder/Nexo. ]**

---

## 🌐 Cross-Server & Rede *(BungeeCord / Velocity)*

- A ação `conectar` manda o jogador para outro backend pelo canal `BungeeCord` (o Velocity também fala esse canal).
- **Fila de transferência embutida** — em vez de socar o backend de destino quando uma multidão clica de uma vez, as transferências são **limitadas por taxa e enfileiradas por destino**, com feedback de posição ao vivo na action bar. Sem avalanche em lobbies grandes.
- Degrada de forma limpa em servidor único: com o proxy desligado, `conectar` só registra no log e não faz nada (sem erros).

---

## 🌍 Idiomas

- Arquivos de mensagem completos em **inglês** (`messages_en.yml`) e **português do Brasil** (`messages_pt-br.yml`).
- Troque com uma única linha de config: `lang: en` ou `lang: pt-br`.
- Todos os arquivos são editáveis, então dá para retraduzir ou reescrever inteiro para combinar com o tom do seu servidor.

---

## 🖱️ A GUI do Editor

Rode `/npc editar <id>` e configure **tudo** clicando num menu de 54 slots:

- Skin · Nome/etiqueta · Olhar o jogador · Ações (adicionar/remover/reordenar/trocar gatilho) · Brilho & cor · Escala · Mostrar no tab · Tipo (player/mob/item/bloco) · Pose · Equipamento
- Menus de **seleção de tipo** e **seleção de item/bloco** (paginados)
- Mover aqui · Centralizar · Virar pra mim · Copiar · Colisão · Skin-espelho · Visibilidade
- Anexar uma imagem na HUD · Ajustar altura do NPC & offset do nome · Deletar

Campos de texto (nome da skin, ids de modelos customizados, comandos, id de cópia) são digitados no chat depois que o menu fecha — digite `cancelar` para abortar. Sem precisar editar config.

> 🖼️ **[ IMAGEM AQUI — o menu principal do editor com todos os ícones de opções. ]**
>
> 🖼️ **[ IMAGEM AQUI — o menu de seleção de item/bloco (picker paginado). ]**

---

## ⌨️ Comandos

Todos os comandos têm **autocompletar** (TAB) para os IDs de NPC. Permissão: `archernpc.admin`.

| Comando | Descrição |
|---|---|
| `/npc criar <id>` | Cria um NPC onde você está (com a sua skin) |
| `/npc editar <id>` | Abre a GUI do editor |
| `/npc deletar <id>` | Remove o NPC |
| `/npc listar` | Lista todos os NPCs |
| `/npc copiar <id> <novo>` | Duplica um NPC |
| `/npc mover · centralizar · rotacionar <id>` | Posiciona o NPC (mover aqui / centralizar no bloco / virar pra você) |
| `/npc nome <id> <texto>` | Define a etiqueta (`|` separa linhas) |
| `/npc skin <id> <nick\|url>` | Define a skin (detecta nick vs URL sozinho) |
| `/npc acao adicionar · listar · remover <id> ...` | Gerencia ações de clique |
| `/npc colidir · espelho · visibilidade <id>` | Alterna colisão / skin-espelho / cicla visibilidade |
| `/npc permissao <id> <perm>` · `/npc ver <id> <jogador>` | Nó de permissão / visibilidade manual por jogador |
| `/npc hud` | Abre o menu de configuração da HUD |
| `/npc hud recarregar` | Regenera & re-serve o pack após trocar imagens/config |
| `/npc hud <imagem> [segundos]` | Testa uma imagem da HUD em você mesmo |

---

## 🔧 Destaques do Config

- `lang` — `en` ou `pt-br`.
- `tracking-ativo` — interruptor mestre de todo o spawn de NPC (freio de emergência).
- `distancia-visao` — distância de renderização em blocos.
- `intervalo-tracking` — de quanto em quanto tempo (em ticks) os viewers são recalculados.
- `spawn-grace-ms` — carência após o login antes de spawnar NPCs (evita travar o login).
- `cooldown-clique` — mínimo de ms entre cliques no mesmo NPC.
- `rede-proxy` — habilita a ação `conectar` cross-server e a fila.
- Bloco completo de HUD (`hud-modo`, `hud-url`, `hud-porta`, `hud-host-publico`, `hud-exibir`, `hud-altura`, `hud-ascent`, `hud-hd-tile`, `hud-prompt`, `hud-pack-nome`, `hud-pack-descricao`, `hud-logo`, `hud-forcar`).

> Chaves faltando sempre caem em padrões seguros — atualizar o plugin nunca quebra o seu `config.yml` existente.

---

## ✅ Requisitos

- **Paper / Folia 1.21+** (Java 21)
- **PacketEvents** (renderização dos NPCs — obrigatório)
- *Opcional:* PlaceholderAPI, ItemsAdder, Nexo

---

## 📝 Observações

- Vem em **inglês e português do Brasil** — defina `lang` no `config.yml`.
- Armazenamento é **YAML simples por NPC** — sem banco de dados.

> 🖼️ **[ IMAGEM AQUI — rodapé opcional: créditos / banner do Discord. ]**
