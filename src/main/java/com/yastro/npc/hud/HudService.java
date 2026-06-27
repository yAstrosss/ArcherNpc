package com.yastro.npc.hud;

import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.net.BindException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public final class HudService implements Listener {
    private final com.yastro.npc.YAstroNpcPlugin plugin;
    private final HudPackServer server = new HudPackServer();
    private volatile Map<String, HudImage> glyphs = Map.of();
    private volatile Map<String, HudAnim> anims = Map.of();
    private volatile byte[] hash = new byte[0];
    private volatile String url = "";
    private volatile boolean ready;
    private final Map<UUID, Integer> playGen = new ConcurrentHashMap<>();
    private final Map<UUID, BossBar> bars = new ConcurrentHashMap<>();
    private final AtomicInteger gen = new AtomicInteger();
    private final HudConfig posicoes;

    public HudService(com.yastro.npc.YAstroNpcPlugin plugin) {
        this.plugin = plugin;
        this.posicoes = new HudConfig(plugin);
    }

    public HudConfig posicoes() { return posicoes; }

    private record GenCfg(int height, int ascent, int hdTile, int x, String packName, String packDesc,
                          String logo, String mode, String url, int port, String hostPublico) {}

    public void init() {
        int my = gen.incrementAndGet();
        this.ready = false;
        posicoes.reload();
        var cfg = plugin.getConfig();
        GenCfg snap = new GenCfg(
                cfg.getInt("hud-altura", 128), cfg.getInt("hud-ascent", 80), cfg.getInt("hud-hd-tile", 16),
                cfg.getInt("hud-x", 0),
                cfg.getString("hud-pack-nome", "ArcherNpc HUD"), cfg.getString("hud-pack-descricao", ""),
                cfg.getString("hud-logo", "hud_logo.png"), cfg.getString("hud-modo", "local").toLowerCase(),
                cfg.getString("hud-url", ""), cfg.getInt("hud-porta", 8123), cfg.getString("hud-host-publico", ""));
        plugin.getServer().getAsyncScheduler().runNow(plugin, t -> generate(my, snap));
    }

    private void generate(int my, GenCfg c) {
        try {
            File imagesDir = new File(plugin.getDataFolder(), "hud_imagens");
            if (!imagesDir.exists() && !imagesDir.mkdirs()) plugin.getLogger().warning("Não criei hud_imagens/");
            File hdDir = new File(imagesDir, "hd");
            if (!hdDir.exists()) hdDir.mkdirs();
            File zip = new File(plugin.getDataFolder(), "hud_pack.zip");
            File icon = (c.logo() == null || c.logo().isBlank()) ? null : new File(plugin.getDataFolder(), c.logo());

            java.util.Set<String> variantIds = new java.util.HashSet<>();
            if (plugin.registry() != null) {
                for (var npc : plugin.registry().all())
                    for (var a : npc.data().actions())
                        if ("imagem".equalsIgnoreCase(a.type())) {
                            String tok = a.value().split(";")[0].trim();
                            if (!tok.isEmpty()) variantIds.add(tok);
                        }
            }
            HudPackGenerator.Result r = new HudPackGenerator(c.height(), c.ascent(), c.hdTile(), c.x(), posicoes,
                    c.packName(), c.packDesc(), icon, plugin::debug).build(imagesDir, zip, variantIds);
            if (stale(my)) return;
            this.glyphs = r.images();
            this.anims = r.anims();
            this.hash = r.sha1();
            String hex = hex(hash);
            plugin.debug("HUD pack gerado: " + glyphs.size() + " img + " + anims.size()
                    + " gif, sha1=" + hex + ". Estáticas: " + glyphs.keySet() + " | Gifs: " + anims.keySet());

            switch (c.mode()) {

                case "link", "manual", "nuvem" -> {
                    this.url = c.url();
                    this.ready = !url.isBlank();
                    if (!ready) plugin.getLogger().warning("HUD modo 'link' mas 'hud-url' está vazio, cole o link DIRETO do .zip "
                            + "(ex: Dropbox com ?dl=1, Google Drive direto, GitHub Releases, MediaFire direto).");
                    else plugin.debug("HUD modo link: " + url);
                }

                case "pasta", "off", "manual-cliente" -> {
                    this.url = ""; this.ready = false;
                    plugin.debug("HUD modo pasta: pack gerado em " + zip.getAbsolutePath()
                            + ", NÃO empurrado. Distribua o .zip; o jogador coloca em .minecraft e ativa, ou você usa no server.properties.");
                }

                case "local", "localhost" -> { if (!startLocal(c, zip)) return; }

                case "auto" -> {
                    if (!c.url().isBlank()) {
                        this.url = c.url(); this.ready = true;
                        plugin.debug("HUD auto: usando 'hud-url' (link).");
                    } else if (startLocal(c, zip)) {
                        plugin.debug("HUD auto: usando servidor local.");
                    } else {
                        try {
                            String u = HudPackUploader.upload(zip, hex);
                            if (stale(my)) return;
                            this.url = u; this.ready = true;
                            plugin.debug("HUD auto: upload externo OK -> " + u);
                        } catch (Exception up) {
                            plugin.getLogger().warning("HUD auto: nenhuma opção funcionou (" + up.getMessage()
                                    + "). Defina 'hud-url' (link) ou 'hud-host-publico' (local), ou use 'pasta'. HUD desligado.");
                            return;
                        }
                    }
                }

                default -> {
                    plugin.getLogger().warning("HUD modo 'upload' (mc-packs.net) é instável e pode falhar (404). "
                            + "Prefira 'local', 'link' ou 'pasta'. Tentando assim mesmo...");
                    try {
                        String u = HudPackUploader.upload(zip, hex);
                        if (stale(my)) return;
                        this.url = u; this.ready = true;
                        plugin.debug("HUD pronto: " + u);
                    } catch (Exception up) {
                        plugin.getLogger().warning("HUD upload falhou (" + up.getMessage()
                                + "). Troque 'hud-modo' para 'local', 'link' ou 'pasta' no config.yml.");
                        return;
                    }
                }
            }
            if (ready && !stale(my)) pushAll();
        } catch (Exception e) {
            plugin.getLogger().warning("Falha ao iniciar HUD: " + e.getMessage());
        }
    }

    private boolean stale(int my) { return my != gen.get() || !plugin.isEnabled(); }

    private boolean startLocal(GenCfg c, File zip) {
        int port = c.port();
        try {
            server.start(port, zip);
        } catch (BindException be) {
            plugin.getLogger().warning("HUD: porta " + port + " já está em uso, troque 'hud-porta' no config.");
            return false;
        } catch (Exception e) {
            plugin.getLogger().warning("HUD: não consegui abrir o servidor HTTP: " + e.getMessage());
            return false;
        }
        String host = c.hostPublico();
        host = host == null ? "" : host.trim();
        if (host.isBlank()) host = plugin.getServer().getIp();
        if (host == null || host.isBlank()) {
            host = "127.0.0.1";
            plugin.debug("HUD modo local SEM 'hud-host-publico', usando 127.0.0.1 (só funciona no MESMO PC). "
                    + "Defina 'hud-host-publico' com o IP/domínio público e abra/encaminhe a porta " + port + ".");
        }
        this.url = "http://" + host + ":" + port + "/pack.zip";
        this.ready = true;
        plugin.debug("HUD modo local: servindo em " + url + " (a porta " + port + " precisa estar aberta).");
        return true;
    }

    private void pushAll() {
        plugin.getServer().getGlobalRegionScheduler().run(plugin, t -> {
            for (Player p : plugin.getServer().getOnlinePlayers()) p.getScheduler().run(plugin, x -> pushPack(p), null);
        });
    }

    private static String hex(byte[] b) {
        StringBuilder sb = new StringBuilder(b.length * 2);
        for (byte x : b) sb.append(Character.forDigit((x >> 4) & 0xF, 16)).append(Character.forDigit(x & 0xF, 16));
        return sb.toString();
    }

    private static final UUID PACK_ID = UUID.nameUUIDFromBytes("yastronpc-hud".getBytes(StandardCharsets.UTF_8));

    public void pushPack(Player p) {
        if (ready && !url.isBlank() && hash.length > 0) {
            p.setResourcePack(PACK_ID, url, hash, promptComponent(),
                    plugin.getConfig().getBoolean("hud-forcar", false));
        }
    }

    private Component promptComponent() {
        String s = plugin.getConfig().getString("hud-prompt", "");
        if (s == null || s.isBlank()) return null;
        return net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacyAmpersand()
                .deserialize(s.replace("\\n", "\n"));
    }

    public Set<String> ids() {
        java.util.Set<String> all = new java.util.TreeSet<>(glyphs.keySet());
        all.addAll(anims.keySet());
        return all;
    }

    public boolean has(String id) { return glyphs.containsKey(id) || anims.containsKey(id); }

    private Component glyph(String ch, String xSpace) {
        return Component.text(xSpace + ch).font(Key.key(HudPackGenerator.NAMESPACE, HudPackGenerator.FONT));
    }

    public Component glyphComponent(String id) {
        HudImage img = glyphs.get(id);
        if (img != null) return glyph(img.glyph(), img.xSpace());
        HudAnim a = anims.get(id);
        if (a != null && a.frameCount() > 0) return glyph(a.glyphs()[0], a.xSpace());
        return null;
    }

    private String mode(String id) {
        return posicoes.exibir(id, plugin.getConfig().getString("hud-exibir", "title")).toLowerCase();
    }

    private void display(Player p, Component g, String mode, long stayMs) {
        switch (mode) {
            case "actionbar" -> p.sendActionBar(g);
            case "chat" -> p.sendMessage(g);
            case "bossbar" -> {
                BossBar bar = bars.get(p.getUniqueId());
                if (bar == null) {
                    bar = BossBar.bossBar(g, bossProgress(), bossColor(), BossBar.Overlay.PROGRESS);
                    bars.put(p.getUniqueId(), bar);
                    p.showBossBar(bar);
                } else {
                    bar.name(g);
                }
            }
            default -> p.showTitle(Title.title(g, Component.empty(),
                    Title.Times.times(Duration.ZERO, Duration.ofMillis(Math.max(1000, stayMs)), Duration.ofMillis(200))));
        }
    }

    private void clearVehicle(Player p, String mode) {
        if (mode.equals("title")) p.clearTitle();
        else if (mode.equals("actionbar")) p.sendActionBar(Component.empty());
        else if (mode.equals("bossbar")) {
            BossBar b = bars.remove(p.getUniqueId());
            if (b != null) p.hideBossBar(b);
        }
    }

    private float bossProgress() {
        return (float) Math.max(0.0, Math.min(1.0, plugin.getConfig().getDouble("hud-bossbar-progresso", 1.0)));
    }
    private BossBar.Color bossColor() {
        try { return BossBar.Color.valueOf(plugin.getConfig().getString("hud-bossbar-cor", "PINK").toUpperCase()); }
        catch (Exception e) { return BossBar.Color.PINK; }
    }

    public void show(Player p, String id, long durationTicks, Runnable onEnd) {

        if (!glyphs.containsKey(id) && !anims.containsKey(id)) {
            int at = id.indexOf('@');
            if (at > 0) id = id.substring(0, at);
        }
        HudAnim anim = anims.get(id);
        if (anim != null) { playAnim(p, anim, durationTicks, onEnd); return; }
        HudImage img = glyphs.get(id);
        if (img == null) {
            p.sendMessage(Component.text("§cImagem HUD não encontrada: §e" + id));
            if (onEnd != null) onEnd.run();
            return;
        }
        String mode = mode(id);
        display(p, glyph(img.glyph(), img.xSpace()), mode, durationTicks * 50);

        p.getScheduler().runDelayed(plugin,
                t -> { clearVehicle(p, mode); if (onEnd != null) onEnd.run(); },
                () -> { if (onEnd != null) onEnd.run(); },
                Math.max(1, durationTicks));
    }

    private void playAnim(Player p, HudAnim a, long durationTicks, Runnable onEnd) {
        UUID u = p.getUniqueId();
        int gen = playGen.merge(u, 1, Integer::sum);
        long endAtMs = System.currentTimeMillis() + durationTicks * 50;
        frame(p, a, 0, endAtMs, gen, onEnd);
    }

    private void frame(Player p, HudAnim a, int idx, long endAtMs, int gen, Runnable onEnd) {
        if (!p.isOnline() || !Integer.valueOf(gen).equals(playGen.get(p.getUniqueId()))) return;
        int delayMs = a.delaysMs()[idx];
        String mode = mode(a.id());
        display(p, glyph(a.glyphs()[idx], a.xSpace()), mode, Math.max(1000, delayMs * 2L));
        if (System.currentTimeMillis() >= endAtMs) {
            playGen.remove(p.getUniqueId());
            clearVehicle(p, mode);
            if (onEnd != null) onEnd.run();
            return;
        }
        long delayTicks = Math.max(1, Math.round(delayMs / 50.0));
        int next = (idx + 1) % a.frameCount();
        p.getScheduler().runDelayed(plugin, st -> frame(p, a, next, endAtMs, gen, onEnd),
                () -> { playGen.remove(p.getUniqueId()); if (onEnd != null) onEnd.run(); }, delayTicks);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) { playGen.remove(e.getPlayer().getUniqueId()); bars.remove(e.getPlayer().getUniqueId()); }

    @EventHandler
    public void onPackStatus(PlayerResourcePackStatusEvent e) {
        plugin.debug("[HUD] " + e.getPlayer().getName() + " resource pack: " + e.getStatus());
    }

    public void stop() {
        gen.incrementAndGet();
        if (plugin.isEnabled()) {
            for (Map.Entry<UUID, BossBar> en : bars.entrySet()) {
                Player p = plugin.getServer().getPlayer(en.getKey());
                if (p != null) p.getScheduler().run(plugin, t -> p.hideBossBar(en.getValue()), null);
            }
        }
        bars.clear();
        playGen.clear();
        posicoes.flushNow();
        server.stop();
    }
}
