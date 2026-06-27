package com.yastro.npc.gui;

import com.yastro.npc.YAstroNpcPlugin;
import com.yastro.npc.model.ActionData;
import com.yastro.npc.model.Npc;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public final class GuiService implements Listener {
    private static final LegacyComponentSerializer AMP = LegacyComponentSerializer.legacyAmpersand();
    private static final PlainTextComponentSerializer PLAIN = PlainTextComponentSerializer.plainText();
    private static final List<String> GLOW_CORES = List.of(
            "white", "red", "gold", "yellow", "green", "aqua", "blue", "light_purple",
            "dark_purple", "dark_red", "dark_green", "dark_aqua", "dark_blue", "gray", "dark_gray", "black");
    private static final List<String> POSES = List.of("normal", "agachado", "dormindo", "nadando", "sentado");
    private static final Material[] ITEM_PALETTE = {
            Material.DIAMOND_SWORD, Material.DIAMOND, Material.EMERALD, Material.GOLD_INGOT, Material.IRON_INGOT,
            Material.NETHERITE_INGOT, Material.NETHER_STAR, Material.TOTEM_OF_UNDYING, Material.ENDER_PEARL,
            Material.DIAMOND_PICKAXE, Material.DIAMOND_AXE, Material.BOW, Material.TRIDENT, Material.SHIELD,
            Material.ELYTRA, Material.DIAMOND_HELMET, Material.DIAMOND_CHESTPLATE, Material.DIAMOND_BOOTS,
            Material.APPLE, Material.GOLDEN_APPLE, Material.BREAD, Material.COOKED_BEEF, Material.CAKE,
            Material.GRASS_BLOCK, Material.STONE, Material.OAK_LOG, Material.DIAMOND_BLOCK, Material.GOLD_BLOCK,
            Material.EMERALD_BLOCK, Material.CHEST, Material.FURNACE, Material.CRAFTING_TABLE, Material.TNT,
            Material.BEACON, Material.GLOWSTONE, Material.REDSTONE, Material.TORCH, Material.BOOK, Material.PAPER,
            Material.MAP, Material.COMPASS, Material.CLOCK, Material.BELL, Material.BARRIER};
    private final YAstroNpcPlugin plugin;
    private final Map<UUID, Consumer<String>> chatCallbacks = new ConcurrentHashMap<>();

    public GuiService(YAstroNpcPlugin plugin) { this.plugin = plugin; }

    public void openEditor(Player p, String npcId) {
        Npc npc = plugin.registry().byId(npcId);
        if (npc == null) { p.sendMessage(plugin.msg("npc-nao-existe", npcId)); return; }
        GuiHolder holder = new GuiHolder(GuiHolder.Type.EDITOR, npcId);
        Inventory inv = Bukkit.createInventory(holder, 54, text(t("gui-editor-titulo", npcId)));
        holder.attach(inv);
        fill(inv);

        var d = npc.data();
        String skinNick = d.skinSource().value();
        inv.setItem(10, item(Material.PLAYER_HEAD, t("gui-skin"), t("gui-atual", skinNick.isBlank() ? t("gui-skin-padrao") : skinNick), t("gui-skin-dica")));
        inv.setItem(11, item(Material.NAME_TAG, t("gui-nome"), t("gui-nome-linhas", d.nameLines().size()), t("gui-nome-dica")));
        inv.setItem(12, item(d.lookAtPlayer() ? Material.ENDER_EYE : Material.ENDER_PEARL,
                t("gui-olhar"), t("gui-estado", onOff(d.lookAtPlayer())), t("gui-clique-alternar")));
        inv.setItem(13, item(Material.WRITABLE_BOOK, t("gui-acoes"), t("gui-acoes-total", d.actions().size()), t("gui-acoes-dica")));
        inv.setItem(14, item(Material.GLOWSTONE_DUST, t("gui-glow"), t("gui-estado", onOff(d.glow())), t("gui-clique-alternar")));
        inv.setItem(15, item(Material.LIGHT_BLUE_DYE, t("gui-glowcor"), t("gui-atual", d.glowColor()), t("gui-glowcor-dica")));
        inv.setItem(16, item(Material.SLIME_BALL, t("gui-escala"), t("gui-escala-atual", d.scale()), t("gui-pm01")));
        inv.setItem(19, item(Material.PLAYER_HEAD, t("gui-tab"), t("gui-estado", onOff(d.showInTab())), t("gui-clique-alternar")));
        inv.setItem(20, item(Material.ARMOR_STAND, t("gui-aparencia"),
                t("gui-atual", d.tipo() + (d.model().isBlank() ? "" : " §7→ §f" + d.model())),
                t("gui-aparencia-dica")));
        inv.setItem(21, item(Material.LEATHER_BOOTS, t("gui-pose"), t("gui-atual", d.pose()), t("gui-pose-dica")));
        inv.setItem(22, item(Material.IRON_CHESTPLATE, t("gui-equip"), t("gui-equip-pecas", d.equipment().size()),
                t("gui-equip-dica"), t("gui-equip-slots")));
        inv.setItem(23, item(Material.PAPER, t("gui-offset"), t("gui-atual", d.nameOffset()), t("gui-pm01")));
        inv.setItem(24, item(Material.PISTON, t("gui-altura"), t("gui-altura-y", String.format("%.1f", d.y())), t("gui-pm01")));
        inv.setItem(25, item(Material.MAP, t("gui-hud"), t("gui-hud-l1"), t("gui-hud-l2"), t("gui-hud-dica")));

        inv.setItem(28, item(Material.COMPASS, t("gui-mover"), t("gui-mover-dica")));
        inv.setItem(29, item(Material.STRUCTURE_VOID, t("gui-centralizar"), t("gui-clique")));
        inv.setItem(30, item(Material.CLOCK, t("gui-virar"), t("gui-virar-dica")));
        inv.setItem(31, item(Material.BOOK, t("gui-copiar"), t("gui-copiar-dica")));
        inv.setItem(32, item(d.collidable() ? Material.COBBLESTONE_WALL : Material.GLASS,
                t("gui-colisao"), t("gui-estado", onOff(d.collidable())), t("gui-clique-alternar")));
        inv.setItem(33, item(Material.PLAYER_HEAD, t("gui-espelho"), t("gui-espelho-l"),
                t("gui-estado", onOff(d.skinMirror())), t("gui-clique-alternar")));
        inv.setItem(34, item(Material.SPYGLASS, t("gui-visib"), t("gui-atual", d.visibility()),
                t("gui-visib-perm", d.permissao().isBlank() ? "-" : d.permissao()), t("gui-visib-dica")));
        inv.setItem(40, item(Material.SHULKER_BOX, t("gui-editor-hitbox"), t("gui-editor-hitbox-l1"), t("gui-editor-hitbox-l2")));
        inv.setItem(49, item(Material.BARRIER, t("gui-deletar"), t("gui-deletar-l"), t("gui-deletar-dica")));
        inv.setItem(53, item(Material.BELL, "&6&lArcherNpc", t("gui-credito-l")));
        p.openInventory(inv);
    }

    public void openAcoes(Player p, String npcId) {
        Npc npc = plugin.registry().byId(npcId);
        if (npc == null) return;
        GuiHolder holder = new GuiHolder(GuiHolder.Type.ACOES, npcId);
        Inventory inv = Bukkit.createInventory(holder, 27, text(t("gui-acoes-titulo", npcId)));
        holder.attach(inv);
        fill(inv);
        List<ActionData> acoes = npc.data().actions();
        for (int i = 0; i < acoes.size() && i < 18; i++) {
            ActionData a = acoes.get(i);
            inv.setItem(i, item(Material.PAPER, t("gui-acao-item-nome", i, a.type()),
                    t("gui-acao-valor", a.value()), t("gui-acao-gatilho", a.trigger()),
                    t("gui-acao-item-dica1"), t("gui-acao-item-dica2")));
        }
        inv.setItem(22, item(Material.LIME_DYE, t("gui-acao-add"), t("gui-acao-add-l1"),
                t("gui-acao-add-l2"), t("gui-acao-add-l3")));
        inv.setItem(18, item(Material.ARROW, t("gui-voltar")));
        p.openInventory(inv);
    }

    private static final java.util.regex.Pattern WRAP =
            java.util.regex.Pattern.compile("^<[^>]+>(.*?)(?:</[^>]+>)?$", java.util.regex.Pattern.DOTALL);

    public void openNome(Player p, String npcId) { openNome(p, npcId, 0); }

    public void openNome(Player p, String npcId, int sel) {
        Npc npc = plugin.registry().byId(npcId);
        if (npc == null) { p.sendMessage(plugin.msg("npc-nao-existe", npcId)); return; }
        List<String> lines = plugin.nomeLinhas(npcId);
        List<Double> scales = plugin.nomeEscalas(npcId);
        sel = lines.isEmpty() ? 0 : Math.max(0, Math.min(sel, lines.size() - 1));
        GuiHolder holder = new GuiHolder(GuiHolder.Type.NOME, npcId, sel);
        Inventory inv = Bukkit.createInventory(holder, 54, text(t("gui-nome-titulo", npcId)));
        holder.attach(inv);
        fill(inv);

        for (int i = 0; i < 9; i++) {
            if (i < lines.size()) {
                double sc = i < scales.size() ? scales.get(i) : 1.0;
                String name = t("gui-nome-linha", i + 1, lines.get(i));
                if (i == sel) inv.setItem(i, itemGlow(Material.NAME_TAG, name, t("gui-nome-linha-escala", sc), t("gui-nome-sel-atual")));
                else inv.setItem(i, item(Material.NAME_TAG, name, t("gui-nome-linha-escala", sc), t("gui-nome-sel-click")));
            } else if (i == lines.size()) {
                inv.setItem(i, item(Material.LIME_DYE, t("gui-nome-add"), t("gui-nome-add-l")));
            } else {
                inv.setItem(i, item(Material.GRAY_STAINED_GLASS_PANE, t("gui-nome-slot-vazio")));
            }
        }

        if (!lines.isEmpty()) inv.setItem(9 + sel, item(Material.LIME_STAINED_GLASS_PANE, t("gui-nome-marcador", sel + 1)));

        inv.setItem(19, item(Material.WRITABLE_BOOK, t("gui-nome-act-texto"), t("gui-nome-act-texto-l")));
        inv.setItem(20, item(Material.RED_DYE, t("gui-nome-act-solida"), t("gui-nome-act-solida-l")));
        inv.setItem(21, item(Material.MAGENTA_DYE, t("gui-nome-act-gradiente"), t("gui-nome-act-gradiente-l")));
        inv.setItem(22, item(Material.FIREWORK_STAR, t("gui-nome-act-rainbow"), t("gui-nome-act-rainbow-l")));
        inv.setItem(23, item(Material.CLOCK, t("gui-nome-act-wave"), t("gui-nome-act-wave-l")));
        inv.setItem(24, item(Material.ITEM_FRAME, t("gui-nome-act-texture"), t("gui-nome-act-texture-l")));
        inv.setItem(25, item(Material.SLIME_BALL, t("gui-nome-act-scale"), t("gui-nome-act-scale-l")));

        inv.setItem(31, item(Material.BARRIER, t("gui-nome-remover-sel"), t("gui-nome-remover-sel-l")));

        inv.setItem(45, item(Material.ARROW, t("gui-voltar-editor")));
        inv.setItem(49, item(Material.LAVA_BUCKET, t("gui-nome-limpar"), t("gui-nome-limpar-l")));
        inv.setItem(53, item(Material.BOOK, t("gui-nome-ajuda"),
                t("gui-nome-ajuda-1"), t("gui-nome-ajuda-2"), t("gui-nome-ajuda-3"), t("gui-nome-ajuda-4"), t("gui-nome-ajuda-5")));
        p.openInventory(inv);
    }

    private void handleNome(Player p, String id, int slot, org.bukkit.event.inventory.ClickType click, int sel) {
        List<String> lines = plugin.nomeLinhas(id);
        if (slot >= 0 && slot < 9) {
            if (slot < lines.size()) { openNome(p, id, slot); }
            else if (slot == lines.size()) {
                prompt(p, t("gui-nome-prompt-texto"), txt -> {
                    if (!plugin.nomeAdd(id, txt)) p.sendMessage(plugin.msg("gui-nome-cheio"));
                    reopenNome(p, id, plugin.nomeLinhas(id).size() - 1);
                });
            }
            return;
        }

        boolean hasSel = sel >= 0 && sel < lines.size();
        if (!hasSel && (slot == 19 || slot == 20 || slot == 21 || slot == 22 || slot == 23 || slot == 24 || slot == 25 || slot == 31)) {
            p.sendMessage(plugin.msg("gui-nome-sem-sel"));
            return;
        }
        String cur = hasSel ? lines.get(sel) : "";
        int fsel = sel;
        switch (slot) {
            case 19 -> prompt(p, t("gui-nome-prompt-texto"), txt -> { plugin.nomeSet(id, fsel, txt); reopenNome(p, id, fsel); });
            case 20 -> prompt(p, t("gui-nome-prompt-cor"), c -> {
                if (!isColor(c.trim())) { p.sendMessage(plugin.msg("gui-cor-invalida")); reopenNome(p, id, fsel); return; }
                plugin.nomeSet(id, fsel, "<" + c.trim() + ">" + unwrap(cur)); reopenNome(p, id, fsel);
            });
            case 21 -> prompt(p, t("gui-nome-prompt-grad"), c -> {
                String[] parts = c.trim().split("\\s+");
                if (parts.length < 2 || !isColor(parts[0]) || !isColor(parts[1])) { p.sendMessage(plugin.msg("gui-cor-invalida")); reopenNome(p, id, fsel); return; }
                plugin.nomeSet(id, fsel, "<gradient:" + parts[0] + ":" + parts[1] + ">" + unwrap(cur)); reopenNome(p, id, fsel);
            });
            case 22 -> { plugin.nomeSet(id, fsel, wrapToggle(cur, "rainbow")); openNome(p, id, fsel); }
            case 23 -> { plugin.nomeSet(id, fsel, wrapToggle(cur, "wave")); openNome(p, id, fsel); }
            case 24 -> prompt(p, t("gui-nome-prompt-glyph"), g -> {
                String tok = glyphToken(g);
                if (tok == null) p.sendMessage(plugin.msg("gui-nome-glyph-erro"));
                else plugin.nomeSet(id, fsel, cur + tok);
                reopenNome(p, id, fsel);
            });
            case 25 -> prompt(p, t("gui-nome-prompt-scale1"), s -> {
                try { plugin.nomeEscala(id, fsel, Double.parseDouble(s.trim().replace(',', '.'))); }
                catch (Exception e) { p.sendMessage(plugin.msg("gui-nome-escala-erro")); }
                reopenNome(p, id, fsel);
            });
            case 31 -> { plugin.nomeRemove(id, fsel); openNome(p, id, Math.max(0, fsel - 1)); }
            case 45 -> openEditor(p, id);
            case 49 -> { plugin.nomeClear(id); openNome(p, id, 0); }
            default -> {  }
        }
    }

    private void reopenNome(Player p, String id, int sel) { p.getScheduler().run(plugin, t -> openNome(p, id, sel), null); }

    public void openHitbox(Player p, String npcId) {
        Npc npc = plugin.registry().byId(npcId);
        if (npc == null) { p.sendMessage(plugin.msg("npc-nao-existe", npcId)); return; }
        double[] hb = plugin.hitbox(npcId);
        GuiHolder holder = new GuiHolder(GuiHolder.Type.HITBOX, npcId);
        Inventory inv = Bukkit.createInventory(holder, 27, text(t("gui-hit-titulo", npcId)));
        holder.attach(inv);
        fill(inv);
        Object w = hb[3] > 0 ? hb[3] : t("gui-hit-auto-val");
        Object h = hb[4] > 0 ? hb[4] : t("gui-hit-auto-val");
        inv.setItem(10, item(Material.SLIME_BALL, t("gui-hit-offy"), t("gui-atual", hb[1]), t("gui-pm01")));
        inv.setItem(11, item(Material.SLIME_BALL, t("gui-hit-offx"), t("gui-atual", hb[0]), t("gui-pm01")));
        inv.setItem(12, item(Material.SLIME_BALL, t("gui-hit-offz"), t("gui-atual", hb[2]), t("gui-pm01")));
        inv.setItem(14, item(Material.STRING, t("gui-hit-larg"), t("gui-atual", w), t("gui-hit-auto"), t("gui-pm01")));
        inv.setItem(15, item(Material.STRING, t("gui-hit-alt"), t("gui-atual", h), t("gui-hit-auto"), t("gui-pm01")));
        inv.setItem(16, item(Material.LAVA_BUCKET, t("gui-hit-reset"), t("gui-hit-reset-l")));
        inv.setItem(22, item(Material.ARROW, t("gui-voltar-editor")));
        p.openInventory(inv);
    }

    private void handleHitbox(Player p, String id, int slot, boolean right) {
        double delta = right ? -0.1 : 0.1;
        switch (slot) {
            case 10 -> { plugin.ajustarHitbox(id, 1, delta); openHitbox(p, id); }
            case 11 -> { plugin.ajustarHitbox(id, 0, delta); openHitbox(p, id); }
            case 12 -> { plugin.ajustarHitbox(id, 2, delta); openHitbox(p, id); }
            case 14 -> { plugin.ajustarHitbox(id, 3, delta); openHitbox(p, id); }
            case 15 -> { plugin.ajustarHitbox(id, 4, delta); openHitbox(p, id); }
            case 16 -> { plugin.resetHitbox(id); openHitbox(p, id); }
            case 22 -> openEditor(p, id);
            default -> { }
        }
    }

    private static String glyphToken(String input) {
        String[] parts = input.trim().split("\\s+", 2);
        if (parts.length < 2) return null;
        String name = parts[1].trim().split("\\s+")[0];
        return switch (parts[0].toLowerCase()) {
            case "ia", "itemsadder" -> ":" + name + ":";
            case "nexo" -> "<nexo:" + name + ">";
            default -> null;
        };
    }

    private static String wrapToggle(String text, String tag) {
        if (text.startsWith("<" + tag)) return unwrap(text);
        return "<" + tag + ">" + unwrap(text);
    }

    private static String unwrap(String t) {
        java.util.regex.Matcher m = WRAP.matcher(t);
        return m.matches() ? m.group(1) : t;
    }

    private static boolean isColor(String tok) {
        return tok.startsWith("#") ? tok.matches("#[0-9a-fA-F]{6}") : tok.matches("[a-zA-Z_]{3,}");
    }

    private static final int PER_PAGE = 45;

    private static final List<org.bukkit.entity.EntityType> ENTITY_LIST = buildEntityList();
    private static final List<Material> ITEM_LIST = buildItemList();
    private static final List<Material> BLOCK_LIST = buildBlockList();

    private static List<org.bukkit.entity.EntityType> entityList() { return ENTITY_LIST; }
    private static List<Material> itemList() { return ITEM_LIST; }
    private static List<Material> blockList() { return BLOCK_LIST; }

    private static List<org.bukkit.entity.EntityType> buildEntityList() {
        List<org.bukkit.entity.EntityType> l = new java.util.ArrayList<>();
        for (var e : org.bukkit.entity.EntityType.values())
            if (e.isAlive() && e.isSpawnable() && e != org.bukkit.entity.EntityType.PLAYER) l.add(e);
        l.sort((a, b) -> a.name().compareTo(b.name()));
        return l;
    }
    private static List<Material> buildItemList() {
        List<Material> l = new java.util.ArrayList<>();
        for (Material m : Material.values()) if (!m.isLegacy() && m.isItem() && !m.isAir()) l.add(m);
        return l;
    }
    private static List<Material> buildBlockList() {
        List<Material> l = new java.util.ArrayList<>();
        for (Material m : Material.values()) if (!m.isLegacy() && m.isBlock() && m.isItem() && !m.isAir()) l.add(m);
        return l;
    }

    public void openAparencia(Player p, String npcId) {
        Npc npc = plugin.registry().byId(npcId);
        if (npc == null) return;
        GuiHolder holder = new GuiHolder(GuiHolder.Type.APARENCIA, npcId);
        Inventory inv = Bukkit.createInventory(holder, 27, text(t("gui-aparencia-titulo", npcId)));
        holder.attach(inv);
        fill(inv);
        String atual = npc.data().tipo();
        inv.setItem(10, item(Material.PLAYER_HEAD, t("gui-ap-player"), t("gui-atual", atual), t("gui-ap-player-dica")));
        inv.setItem(12, item(Material.ZOMBIE_HEAD, t("gui-ap-mob"), t("gui-ap-mob-l"), t("gui-clique")));
        inv.setItem(14, item(Material.DIAMOND, t("gui-ap-item"), t("gui-ap-item-l"), t("gui-clique")));
        inv.setItem(16, item(Material.GRASS_BLOCK, t("gui-ap-bloco"), t("gui-ap-bloco-l"), t("gui-clique")));
        inv.setItem(22, item(Material.ARROW, t("gui-voltar-editor")));
        p.openInventory(inv);
    }

    private void handleAparencia(Player p, String id, int slot) {
        switch (slot) {
            case 10 -> { plugin.definirTipo(id, "player"); openEditor(p, id); }
            case 12 -> openEntidades(p, id, 0);
            case 14 -> openItens(p, id, 0);
            case 16 -> openBlocos(p, id, 0);
            case 22 -> openEditor(p, id);
            default -> { }
        }
    }

    public void openEntidades(Player p, String id, int page) {
        var list = entityList();
        int max = Math.max(0, (list.size() - 1) / PER_PAGE);
        page = Math.max(0, Math.min(page, max));
        GuiHolder holder = new GuiHolder(GuiHolder.Type.ENTIDADES, id, page);
        Inventory inv = Bukkit.createInventory(holder, 54, text(t("gui-pag-titulo", t("gui-cat-entidades"), page + 1, max + 1)));
        holder.attach(inv);
        for (int i = 0; i < PER_PAGE; i++) {
            int idx = page * PER_PAGE + i;
            if (idx >= list.size()) break;
            var e = list.get(idx);
            inv.setItem(i, item(mat(e.name() + "_SPAWN_EGG", Material.EGG), "&a" + nice2(e.name()), t("gui-clique-usar")));
        }
        navRow(inv, page, max);
        p.openInventory(inv);
    }

    public void openItens(Player p, String id, int page) { openMatPage(p, id, page, GuiHolder.Type.ITENS, itemList(), t("gui-cat-itens")); }
    public void openBlocos(Player p, String id, int page) { openMatPage(p, id, page, GuiHolder.Type.BLOCOS, blockList(), t("gui-cat-blocos")); }

    private void openMatPage(Player p, String id, int page, GuiHolder.Type type, List<Material> list, String titulo) {
        int max = Math.max(0, (list.size() - 1) / PER_PAGE);
        page = Math.max(0, Math.min(page, max));
        GuiHolder holder = new GuiHolder(type, id, page);
        Inventory inv = Bukkit.createInventory(holder, 54, text(t("gui-pag-titulo", titulo, page + 1, max + 1)));
        holder.attach(inv);
        for (int i = 0; i < PER_PAGE; i++) {
            int idx = page * PER_PAGE + i;
            if (idx >= list.size()) break;
            Material m = list.get(idx);
            inv.setItem(i, item(m, "&a" + nice(m), t("gui-clique-usar")));
        }
        navRow(inv, page, max);
        p.openInventory(inv);
    }

    private void navRow(Inventory inv, int page, int max) {
        if (page > 0) inv.setItem(45, item(Material.ARROW, t("gui-nav-anterior")));
        if (page < max) inv.setItem(53, item(Material.ARROW, t("gui-nav-proxima")));
        inv.setItem(46, item(Material.BARRIER, t("gui-nav-voltar-cat")));
        inv.setItem(49, item(Material.NAME_TAG, t("gui-nav-manual"), t("gui-nav-manual-dica")));
        inv.setItem(51, item(Material.PAPER, t("gui-nav-pagina", page + 1, max + 1)));
    }

    private void handleEntidades(Player p, String id, int slot, int page) {
        if (slot == 46) { openAparencia(p, id); return; }
        if (slot == 45) { openEntidades(p, id, page - 1); return; }
        if (slot == 53) { openEntidades(p, id, page + 1); return; }
        if (slot == 49) { prompt(p, t("gui-prompt-entidade"), txt -> {
            plugin.definirTipo(id, txt.toLowerCase().replace("minecraft:", "")); reopenEditor(p, id); }); return; }
        var list = entityList();
        int idx = page * PER_PAGE + slot;
        if (slot >= 0 && slot < PER_PAGE && idx < list.size()) {
            plugin.definirTipo(id, list.get(idx).name().toLowerCase());
            openEditor(p, id);
        }
    }

    private void handleMat(Player p, String id, int slot, int page, String tipo, List<Material> list,
                           java.util.function.IntConsumer reopenPage) {
        if (slot == 46) { openAparencia(p, id); return; }
        if (slot == 45) { reopenPage.accept(page - 1); return; }
        if (slot == 53) { reopenPage.accept(page + 1); return; }
        if (slot == 49) { prompt(p, t("gui-prompt-display"), ref -> {
            plugin.definirDisplay(p, id, tipo, ref); reopenEditor(p, id); }); return; }
        int idx = page * PER_PAGE + slot;
        if (slot >= 0 && slot < PER_PAGE && idx < list.size()) {
            plugin.definirDisplay(p, id, tipo, "minecraft:" + list.get(idx).name());
            openEditor(p, id);
        }
    }

    public void openHudAttach(Player p, String npcId) {
        Npc npc = plugin.registry().byId(npcId);
        if (npc == null) return;
        GuiHolder holder = new GuiHolder(GuiHolder.Type.HUDPICK, npcId);
        Inventory inv = Bukkit.createInventory(holder, 54, text(t("gui-anexar-titulo", npcId)));
        holder.attach(inv);
        fill(inv);
        int slot = 0;
        for (String hudId : plugin.hud().ids()) {
            if (slot >= 45) break;
            inv.setItem(slot++, item(Material.PAINTING, "&a" + hudId,
                    t("gui-anexar-dica"), t("gui-anexar-l")));
        }
        if (slot == 0) inv.setItem(22, item(Material.BARRIER, t("gui-nenhuma-img"),
                t("gui-nenhuma-img-l1"), t("gui-nenhuma-img-l2")));
        inv.setItem(45, item(Material.ARROW, t("gui-voltar")));
        inv.setItem(49, item(Material.COMPARATOR, t("gui-ajustar-escala"), t("gui-ajustar-escala-l")));
        p.openInventory(inv);
    }

    private void handleHudPick(Player p, String npcId, int slot, org.bukkit.event.inventory.ClickType click) {
        if (slot == 45 || slot == 49) { openHudConfig(p, 0, npcId); return; }
        java.util.List<String> ids = new java.util.ArrayList<>(plugin.hud().ids());
        if (slot >= 0 && slot < ids.size() && slot < 45) {
            int secs = click.isShiftClick() ? 30 : (click.isRightClick() ? 10 : 5);

            plugin.acaoAdicionar(p, npcId, "imagem", ids.get(slot) + "@" + npcId + ";" + secs);
            openHudConfig(p, 0, npcId);
        }
    }

    public void openHudConfig(Player p) { openHudConfig(p, 0, null); }

    private List<String> hudIds(String npcId) {
        if (npcId == null) return new java.util.ArrayList<>(plugin.hud().ids());
        List<String> out = new java.util.ArrayList<>();
        Npc npc = plugin.registry().byId(npcId);
        if (npc != null) for (ActionData a : npc.data().actions())
            if ("imagem".equalsIgnoreCase(a.type())) out.add(a.value().split(";")[0].trim());
        return out;
    }
    private static String shortId(String tok) { int at = tok.indexOf('@'); return at < 0 ? tok : tok.substring(0, at); }

    public void openHudConfig(Player p, int sel, String npcId) {
        List<String> ids = hudIds(npcId);
        int n = ids.size();
        sel = n == 0 ? 0 : Math.max(0, Math.min(sel, n - 1));
        GuiHolder holder = new GuiHolder(GuiHolder.Type.HUD, npcId, sel);
        Inventory inv = Bukkit.createInventory(holder, 54,
                text(npcId == null ? t("gui-hudcfg-titulo") : t("gui-hudcfg-titulo-npc", npcId)));
        holder.attach(inv);
        fill(inv);
        for (int i = 0; i < 9; i++) {
            if (i < n) {
                String nm = shortId(ids.get(i));
                if (i == sel) inv.setItem(i, itemGlow(Material.PAINTING, "&a" + nm, t("gui-hudcfg-sel-atual")));
                else inv.setItem(i, item(Material.PAINTING, "&7" + nm, t("gui-hudcfg-sel-click")));
            } else {
                inv.setItem(i, item(Material.GRAY_STAINED_GLASS_PANE, t("gui-nome-slot-vazio")));
            }
        }
        inv.setItem(31, item(Material.EMERALD_BLOCK, t("gui-hudcfg-aplicar"), t("gui-hudcfg-aplicar-l1"), t("gui-hudcfg-aplicar-l2")));
        if (npcId != null) {
            inv.setItem(45, item(Material.ARROW, t("gui-voltar-editor")));
            inv.setItem(49, item(Material.LIME_DYE, t("gui-hudcfg-add"), t("gui-hudcfg-add-l")));
        }
        if (n == 0) {
            inv.setItem(22, item(Material.BARRIER, t("gui-hudcfg-sem-img"), t("gui-hudcfg-sem-img-l")));
            p.openInventory(inv);
            return;
        }
        String id = ids.get(sel);
        inv.setItem(9 + sel, item(Material.LIME_STAINED_GLASS_PANE, t("gui-nome-marcador", sel + 1)));
        inv.setItem(19, item(Material.SLIME_BALL, t("gui-hudcfg-escala"), t("gui-atual", plugin.hudImgAltura(id)), t("gui-hudcfg-escala-dica")));
        inv.setItem(20, item(Material.SPECTRAL_ARROW, t("gui-hudcfg-posx"), t("gui-atual", plugin.hudImgX(id)), t("gui-hudcfg-posx-dica")));
        inv.setItem(21, item(Material.FEATHER, t("gui-hudcfg-posy"), t("gui-atual", plugin.hudImgAscent(id)), t("gui-hudcfg-posy-dica")));
        inv.setItem(22, item(Material.OAK_SIGN, t("gui-hudcfg-exibir"), t("gui-atual", plugin.hudImgExibir(id)), t("gui-hudcfg-exibir-dica")));
        inv.setItem(23, item(Material.ENDER_EYE, t("gui-hudcfg-testar-btn"), t("gui-hudcfg-testar")));
        inv.setItem(25, item(Material.MAP, t("gui-hudcfg-tile"), t("gui-atual", plugin.hudCfg("hud-hd-tile", 16)), t("gui-hudcfg-tile-dica")));
        if (npcId != null) inv.setItem(51, item(Material.CAULDRON, t("gui-hudcfg-remover"), t("gui-hudcfg-remover-l")));
        p.openInventory(inv);
    }

    private void handleHud(Player p, int slot, org.bukkit.event.inventory.ClickType click, String npcId, int sel) {
        List<String> ids = hudIds(npcId);
        if (slot >= 0 && slot < 9) { if (slot < ids.size()) openHudConfig(p, slot, npcId); return; }
        if (slot == 31) { plugin.recarregarHud(p); p.closeInventory(); return; }
        if (npcId != null && slot == 45) { openEditor(p, npcId); return; }
        if (npcId != null && slot == 49) { openHudAttach(p, npcId); return; }
        if (ids.isEmpty()) return;
        String id = sel < ids.size() ? ids.get(sel) : ids.get(0);
        boolean r = click.isRightClick();
        int big = click.isShiftClick() ? 16 : 1;
        switch (slot) {
            case 19 -> { plugin.ajustarHudAltura(id, (r ? -8 : 8) * big); openHudConfig(p, sel, npcId); }
            case 20 -> { plugin.ajustarHudX(id, (r ? 8 : -8) * big); openHudConfig(p, sel, npcId); }
            case 21 -> { plugin.ajustarHudAscent(id, (r ? -8 : 8) * big); openHudConfig(p, sel, npcId); }
            case 22 -> { plugin.cicloHudExibir(id); openHudConfig(p, sel, npcId); }
            case 23 -> { p.closeInventory(); plugin.testarHud(p, id, 8); }
            case 25 -> { plugin.ajustarHudTile(r ? -2 : 2); openHudConfig(p, sel, npcId); }
            case 51 -> { if (npcId != null) { plugin.removerImagemNpc(npcId, id); openHudConfig(p, 0, npcId); } }
            default -> { }
        }
    }

    public void prompt(Player p, String instrucao, Consumer<String> onConfirm) {
        chatCallbacks.put(p.getUniqueId(), onConfirm);
        p.closeInventory();
        p.sendMessage(text(t("gui-prompt-cabecalho", instrucao)));
        p.sendMessage(text(t("gui-prompt-rodape")));
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncChatEvent e) {
        Consumer<String> cb = chatCallbacks.remove(e.getPlayer().getUniqueId());
        if (cb == null) return;
        e.setCancelled(true);
        String msg = PLAIN.serialize(e.message()).trim();
        if (msg.equalsIgnoreCase("cancelar") || msg.isBlank()) {
            e.getPlayer().sendMessage(text(t("gui-cancelado")));
            return;
        }

        e.getPlayer().getScheduler().run(plugin, t -> cb.accept(msg), null);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onCommandInput(PlayerCommandPreprocessEvent e) {
        Consumer<String> cb = chatCallbacks.remove(e.getPlayer().getUniqueId());
        if (cb == null) return;
        e.setCancelled(true);
        String msg = e.getMessage();
        if (msg.startsWith("/")) msg = msg.substring(1);
        msg = msg.trim();
        if (msg.equalsIgnoreCase("cancelar") || msg.isBlank()) { e.getPlayer().sendMessage(text(t("gui-cancelado"))); return; }
        String fmsg = msg;
        e.getPlayer().getScheduler().run(plugin, t -> cb.accept(fmsg), null);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) { chatCallbacks.remove(e.getPlayer().getUniqueId()); }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!(e.getInventory().getHolder() instanceof GuiHolder holder)) return;
        e.setCancelled(true);
        if (!(e.getWhoClicked() instanceof Player p)) return;

        switch (holder.type()) {
            case EDITOR -> handleEditor(p, holder.npcId(), e.getRawSlot(), e.getClick().isRightClick());
            case ACOES -> handleAcoes(p, holder.npcId(), e.getRawSlot(), e.getClick());
            case NOME -> handleNome(p, holder.npcId(), e.getRawSlot(), e.getClick(), holder.page());
            case HITBOX -> handleHitbox(p, holder.npcId(), e.getRawSlot(), e.getClick().isRightClick());
            case APARENCIA -> handleAparencia(p, holder.npcId(), e.getRawSlot());
            case ENTIDADES -> handleEntidades(p, holder.npcId(), e.getRawSlot(), holder.page());
            case ITENS -> handleMat(p, holder.npcId(), e.getRawSlot(), holder.page(), "item", itemList(),
                    pg -> openItens(p, holder.npcId(), pg));
            case BLOCOS -> handleMat(p, holder.npcId(), e.getRawSlot(), holder.page(), "bloco", blockList(),
                    pg -> openBlocos(p, holder.npcId(), pg));
            case HUD -> handleHud(p, e.getRawSlot(), e.getClick(), holder.npcId(), holder.page());
            case HUDPICK -> handleHudPick(p, holder.npcId(), e.getRawSlot(), e.getClick());
            default -> {  }
        }
    }

    private void handleEditor(Player p, String id, int slot, boolean right) {
        switch (slot) {
            case 10 -> prompt(p, t("gui-prompt-skin"), nick -> {
                plugin.definirSkin(p, id, nick);
                reopenEditor(p, id);
            });
            case 11 -> openNome(p, id);
            case 12 -> { plugin.alternarOlhar(id); openEditor(p, id); }
            case 13 -> openAcoes(p, id);
            case 14 -> { plugin.alternarGlow(id); openEditor(p, id); }
            case 15 -> { plugin.definirGlowCor(id, nextCor(id)); openEditor(p, id); }
            case 16 -> { plugin.ajustarEscala(id, right ? -0.1 : 0.1); openEditor(p, id); }
            case 19 -> { plugin.alternarTab(id); openEditor(p, id); }
            case 20 -> openAparencia(p, id);
            case 21 -> { plugin.definirPose(id, nextPose(id)); openEditor(p, id); }
            case 22 -> prompt(p, t("gui-prompt-equip"), txt -> {
                String[] parts = txt.split(" ", 2);
                plugin.definirEquip(p, id, parts[0], parts.length > 1 ? parts[1] : "");
                reopenEditor(p, id);
            });
            case 23 -> { plugin.ajustarOffset(id, right ? -0.1 : 0.1); openEditor(p, id); }
            case 24 -> { plugin.ajustarAltura(id, right ? -0.1 : 0.1); openEditor(p, id); }
            case 25 -> openHudConfig(p, 0, id);
            case 28 -> { plugin.moverNpc(p, id); openEditor(p, id); }
            case 29 -> { plugin.centralizarNpc(p, id); openEditor(p, id); }
            case 30 -> { plugin.rotacionarNpc(p, id); openEditor(p, id); }
            case 31 -> prompt(p, t("gui-prompt-copiar"), novo -> {
                plugin.copiarNpc(p, id, novo);
                reopenEditor(p, novo);
            });
            case 32 -> { plugin.alternarColidir(id); openEditor(p, id); }
            case 33 -> { plugin.alternarSkinEspelho(id); openEditor(p, id); }
            case 34 -> { plugin.cicloVisibilidade(id); openEditor(p, id); }
            case 40 -> openHitbox(p, id);
            case 49 -> { plugin.deletarNpc(p, id); p.closeInventory(); }
            default -> {  }
        }
    }

    private String nextPose(String id) {
        Npc npc = plugin.registry().byId(id);
        String atual = npc == null ? "normal" : npc.data().pose();
        int i = POSES.indexOf(atual);
        return POSES.get((i + 1) % POSES.size());
    }

    private String nextCor(String id) {
        Npc npc = plugin.registry().byId(id);
        String atual = npc == null ? "white" : npc.data().glowColor();
        int i = GLOW_CORES.indexOf(atual);
        return GLOW_CORES.get((i + 1) % GLOW_CORES.size());
    }

    private String onOff(boolean v) { return v ? t("gui-on") : t("gui-off"); }

    private static Material mat(String name, Material fallback) {
        try { return Material.valueOf(name); } catch (IllegalArgumentException e) { return fallback; }
    }

    private static String nice(Material m) {
        return m.name().toLowerCase().replace('_', ' ');
    }

    private static String nice2(String name) {
        return name.toLowerCase().replace('_', ' ');
    }

    private void handleAcoes(Player p, String id, int slot, org.bukkit.event.inventory.ClickType click) {
        if (slot == 18) { openEditor(p, id); return; }
        if (slot == 22) {
            prompt(p, t("gui-prompt-acao"), txt -> {
                String[] parts = txt.split(" ", 2);
                if (parts.length == 2) plugin.acaoAdicionar(p, id, parts[0], parts[1]);
                else p.sendMessage(plugin.msg("gui-formato-acao"));
                reopenAcoes(p, id);
            });
            return;
        }
        Npc npc = plugin.registry().byId(id);
        if (npc == null || slot < 0 || slot >= npc.data().actions().size()) return;
        if (click.isShiftClick()) plugin.acaoMover(id, slot, click.isRightClick() ? 1 : -1);
        else if (click.isRightClick()) plugin.acaoRemover(p, id, slot);
        else plugin.acaoCicloGatilho(id, slot);
        openAcoes(p, id);
    }

    private void reopenEditor(Player p, String id) { p.getScheduler().run(plugin, t -> openEditor(p, id), null); }
    private void reopenAcoes(Player p, String id) { p.getScheduler().run(plugin, t -> openAcoes(p, id), null); }

    private void fill(Inventory inv) {
        ItemStack pane = item(Material.GRAY_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < inv.getSize(); i++) inv.setItem(i, pane);
    }

    private ItemStack item(Material mat, String name, String... lore) {
        ItemStack is = new ItemStack(mat);
        ItemMeta meta = is.getItemMeta();
        meta.displayName(text(name).decoration(TextDecoration.ITALIC, false));
        if (lore.length > 0) {
            List<Component> lines = new ArrayList<>();
            for (String l : lore) lines.add(text(l).decoration(TextDecoration.ITALIC, false));
            meta.lore(lines);
        }
        is.setItemMeta(meta);
        return is;
    }

    private ItemStack itemGlow(Material mat, String name, String... lore) {
        ItemStack is = item(mat, name, lore);
        ItemMeta meta = is.getItemMeta();
        meta.addEnchant(org.bukkit.enchantments.Enchantment.UNBREAKING, 1, true);
        meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
        is.setItemMeta(meta);
        return is;
    }

    private Component text(String legacy) { return AMP.deserialize(legacy); }

    private String t(String key, Object... a) { return plugin.tr(key, a); }
}
