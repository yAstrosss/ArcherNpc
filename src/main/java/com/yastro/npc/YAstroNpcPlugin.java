package com.yastro.npc;

import com.github.retrooper.packetevents.PacketEvents;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import com.yastro.npc.action.ActionType;
import com.yastro.npc.action.NpcAction;
import com.yastro.npc.command.NpcCommandPhase2;
import com.yastro.npc.model.ActionData;
import com.yastro.npc.interaction.CooldownService;
import com.yastro.npc.interaction.NpcInteractListener;
import com.yastro.npc.server.LocalConnector;
import com.yastro.npc.server.ProxyConnector;
import com.yastro.npc.server.QueueService;
import com.yastro.npc.server.ServerConnector;
import com.yastro.npc.config.Messages;
import com.yastro.npc.config.NpcStorage;
import com.yastro.npc.config.YamlNpcStorage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import com.yastro.npc.gui.GuiService;
import com.yastro.npc.hud.HudService;
import com.yastro.npc.model.Npc;
import com.yastro.npc.model.NpcData;
import com.yastro.npc.model.SkinSource;
import com.yastro.npc.npc.LookController;
import com.yastro.npc.npc.NpcRegistry;
import com.yastro.npc.npc.NpcTracker;
import com.yastro.npc.npc.PacketNpc;
import com.yastro.npc.npc.PlayerSession;
import com.yastro.npc.npc.PlayerSessionManager;
import com.yastro.npc.npc.SessionListener;
import com.yastro.npc.nameplate.NameplateService;
import com.yastro.npc.nameplate.Placeholders;
import com.yastro.npc.skin.MojangApi;
import com.yastro.npc.skin.SkinData;
import com.yastro.npc.skin.SkinResolver;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public final class YAstroNpcPlugin extends JavaPlugin {

    private boolean ownsPacketEvents = false;
    private NpcRegistry registry;
    private PlayerSessionManager sessions;
    private NpcStorage storage;
    private NpcTracker tracker;
    private SkinResolver resolver;
    private ActionType actionType;
    private CooldownService cooldowns;
    private ServerConnector connector;
    private QueueService queue;
    private NameplateService nameplate;
    private LookController look;
    private GuiService gui;
    private HudService hud;
    private Messages messages;
    private ScheduledTask animTask;
    private NpcInteractListener interactListener;
    private final java.util.concurrent.atomic.AtomicInteger animPhase = new java.util.concurrent.atomic.AtomicInteger();

    @Override
    public void onLoad() {

        if (PacketEvents.getAPI() == null) {
            PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this));
            PacketEvents.getAPI().load();
            ownsPacketEvents = true;
        }
    }

    @Override
    public void onEnable() {
        if (ownsPacketEvents) PacketEvents.getAPI().init();

        saveDefaultConfig();
        this.messages = Messages.load(this, getConfig().getString("lang", "pt-br"));
        getLogger().info("Idioma/Language: " + messages.lang() + " (config.yml -> lang: en | pt-br)");
        this.registry = new NpcRegistry();
        this.sessions = new PlayerSessionManager();
        this.storage = new YamlNpcStorage(this);
        this.resolver = new SkinResolver(getLogger(), new MojangApi.Real(
                getConfig().getString("skin-mineskin-key", ""),
                getConfig().getString("skin-mineskin-url", "")));
        for (NpcData d : storage.loadAll()) {
            Npc npc = new Npc(d);
            registry.add(npc);
            if (d.tipo().equals("player") && d.skin().isEmpty()) resolveSkin(npc);
        }

        boolean papi = getServer().getPluginManager().isPluginEnabled("PlaceholderAPI");
        this.nameplate = new NameplateService(Placeholders.create(papi));
        debug(messages.get(papi ? "log-papi-on" : "log-papi-off"));

        this.look = new LookController(getConfig().getDouble("distancia-visao", 48));
        this.tracker = new NpcTracker(this, registry, sessions, nameplate, look,
                getConfig().getDouble("distancia-visao", 48),
                getConfig().getLong("intervalo-tracking", 10),
                getConfig().getLong("spawn-grace-ms", 1000));
        if (getConfig().getBoolean("tracking-ativo", true)) {
            tracker.start();
        } else {
            getLogger().warning(messages.get("log-tracking-off"));
        }

        boolean proxy = getConfig().getBoolean("rede-proxy", false);
        ServerConnector real = proxy ? new ProxyConnector(this) : new LocalConnector(this);
        if (proxy) getServer().getMessenger().registerOutgoingPluginChannel(this, ProxyConnector.CHANNEL);
        if (proxy && getConfig().getBoolean("fila-ativa", true)) {

            this.queue = new QueueService(this, real,
                    getConfig().getInt("fila-por-vez", 1),
                    getConfig().getLong("fila-intervalo", 20));
            getServer().getPluginManager().registerEvents(queue, this);
            queue.start();
            this.connector = queue;
            getLogger().info("Fila de transferência ativa: " + getConfig().getInt("fila-por-vez", 1)
                    + " por " + getConfig().getLong("fila-intervalo", 20) + " ticks.");
        } else {
            this.connector = real;
        }
        this.actionType = new ActionType(this, connector);
        this.cooldowns = new CooldownService(getConfig().getLong("cooldown-clique", 500),
                System::currentTimeMillis);

        this.gui = new GuiService(this);
        this.hud = new HudService(this);
        getServer().getPluginManager().registerEvents(hud, this);
        if (getConfig().getBoolean("hud-ativo", true)) hud.init();
        getServer().getPluginManager().registerEvents(gui, this);
        getServer().getPluginManager().registerEvents(new SessionListener(this, sessions, cooldowns, nameplate, look, tracker, hud), this);
        this.interactListener = new NpcInteractListener(this);
        PacketEvents.getAPI().getEventManager().registerListener(interactListener);

        animTask = getServer().getGlobalRegionScheduler().runAtFixedRate(this, t -> animateNameplates(), 2L, 2L);
        for (Player p : getServer().getOnlinePlayers()) sessions.open(p.getUniqueId());

        getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event ->
                event.registrar().register(NpcCommandPhase2.build(this).build(), List.of("npcs")));

        getLogger().info(messages.get("log-enabled"));
    }

    public Component msg(String key, Object... args) {
        return LegacyComponentSerializer.legacyAmpersand().deserialize(messages.get(key, args));
    }

    public void tell(Player p, String key, Object... args) { p.sendMessage(msg(key, args)); }

    public String tr(String key, Object... args) { return messages.get(key, args); }

    public boolean isDebug() { return getConfig().getBoolean("debug", false); }

    public void debug(String msg) { if (isDebug()) getLogger().info("[debug] " + msg); }

    private void animateNameplates() {
        if (nameplate == null || registry == null || sessions == null) return;
        java.util.Set<String> animated = null;
        for (Npc n : registry.all()) {
            for (String line : n.data().nameLines()) {
                if (com.yastro.npc.util.Colors.isAnimated(line)) {
                    if (animated == null) animated = new java.util.HashSet<>();
                    animated.add(n.id());
                    break;
                }
            }
        }
        if (animated == null) return;
        final java.util.Set<String> anim = animated;
        int tick = animPhase.incrementAndGet();
        double phase = -1.0 + 2.0 * ((tick % 40) / 40.0);
        for (Player viewer : getServer().getOnlinePlayers()) {
            PlayerSession s = sessions.get(viewer.getUniqueId());
            if (s == null || !intersects(s.spawned, anim)) continue;
            viewer.getScheduler().run(this, st -> {
                PlayerSession ses = sessions.get(viewer.getUniqueId());
                if (ses == null) return;
                for (String id : ses.spawned) {
                    if (!anim.contains(id)) continue;
                    Npc npc = registry.byId(id);
                    if (npc != null) nameplate.animate(viewer, npc, phase);
                }
            }, null);
        }
    }

    private static boolean intersects(java.util.Set<String> spawned, java.util.Set<String> animated) {
        for (String id : animated) if (spawned.contains(id)) return true;
        return false;
    }

    @Override
    public void onDisable() {

        HandlerList.unregisterAll(this);
        if (animTask != null) animTask.cancel();
        if (queue != null) queue.stop();
        if (tracker != null) tracker.stop();
        sequenceLocks.clear();
        if (!getServer().isStopping() && registry != null && sessions != null) {
            for (Player viewer : getServer().getOnlinePlayers()) {
                PlayerSession s = sessions.get(viewer.getUniqueId());
                if (s == null) continue;
                for (String id : s.spawned) {
                    Npc n = registry.byId(id);
                    if (n != null) {
                        PacketNpc.despawn(viewer, n);
                        if (nameplate != null) nameplate.despawnLines(viewer, n);
                    }
                }
            }
        }
        getServer().getMessenger().unregisterOutgoingPluginChannel(this);
        if (resolver != null) resolver.close();
        if (hud != null) hud.stop();
        if (storage != null) storage.flushAll();
        getServer().getAsyncScheduler().cancelTasks(this);
        getServer().getGlobalRegionScheduler().cancelTasks(this);

        if (interactListener != null && PacketEvents.getAPI() != null) {
            PacketEvents.getAPI().getEventManager().unregisterListener(interactListener);
            interactListener = null;
        }

        if (ownsPacketEvents && PacketEvents.getAPI() != null) {
            PacketEvents.getAPI().terminate();
            ownsPacketEvents = false;
        }
        if (messages != null) getLogger().info(messages.get("log-disabled"));
    }

    public NpcRegistry registry() { return registry; }

    private final java.util.Set<String> sequenceLocks = java.util.concurrent.ConcurrentHashMap.newKeySet();

    public void dispatch(Player player, Npc npc, String trigger) {
        if (!cooldowns.tryUse(player.getUniqueId(), npc.id())) return;
        String lockKey = player.getUniqueId() + "|" + npc.id();
        if (sequenceLocks.contains(lockKey)) return;

        List<ActionData> matching = new ArrayList<>();
        for (ActionData a : npc.data().actions()) if (a.firesOn(trigger)) matching.add(a);

        boolean lock = matching.stream().anyMatch(a -> {
            String t = a.type().toLowerCase();
            return t.equals("travar") || t.equals("esperar");
        });
        if (lock) sequenceLocks.add(lockKey);
        runActions(player, matching, 0, lock ? lockKey : null);
    }

    private void runActions(Player player, java.util.List<ActionData> actions, int from, String lockKey) {
        if (!isEnabled()) { if (lockKey != null) sequenceLocks.remove(lockKey); return; }
        for (int i = from; i < actions.size(); i++) {
            ActionData a = actions.get(i);
            if (!actionAllowed(player, a)) continue;
            try {
            switch (a.type().toLowerCase()) {
                case "imagem" -> {
                    String[] parts = a.value().split(";");
                    String id = parts[0].trim();
                    long secs = 5;
                    if (parts.length > 1) try { secs = Long.parseLong(parts[1].trim()); } catch (NumberFormatException ignored) {}
                    int next = i + 1;
                    hud.show(player, id, Math.max(20L, secs * 20L), () -> runActions(player, actions, next, lockKey));
                    return;
                }
                case "esperar" -> {
                    long ticks = parseDelayTicks(a.value());
                    int next = i + 1;

                    player.getScheduler().runDelayed(this, t -> runActions(player, actions, next, lockKey),
                            () -> { if (lockKey != null) sequenceLocks.remove(lockKey); }, ticks);
                    return;
                }
                case "aleatorio" -> {
                    int remaining = actions.size() - (i + 1);
                    if (remaining > 0) {
                        int pick = i + 1 + java.util.concurrent.ThreadLocalRandom.current().nextInt(remaining);

                        runActions(player, java.util.List.of(actions.get(pick)), 0, lockKey);
                        return;
                    }

                }
                case "travar" -> {  }
                default -> {
                    NpcAction action = actionType.create(a.type(), a.value());
                    if (action != null) action.execute(player);
                }
            }
            } catch (Exception e) {
                getLogger().warning("Falha ao executar acao '" + a.type() + "': " + e.getMessage());
                if (lockKey != null) sequenceLocks.remove(lockKey);
                return;
            }
        }
        if (lockKey != null) sequenceLocks.remove(lockKey);
    }

    public static final String PRIVILEGED_ACTION_PERMISSION = "archernpc.action.privileged";

    private boolean actionAllowed(Player player, ActionData a) {
        String perm = a.permission();
        if (perm == null || perm.isBlank()) {
            if (!isPrivilegedAction(a.type())) return true;
            perm = PRIVILEGED_ACTION_PERMISSION;
        }
        if (player.hasPermission(perm)) return true;
        debug("Acao '" + a.type() + "' bloqueada para " + player.getName() + " (falta permissao " + perm + ")");
        return false;
    }

    private static boolean isPrivilegedAction(String type) {
        String t = type == null ? "" : type.toLowerCase(java.util.Locale.ROOT);
        return t.equals("console") || t.equals("comando-op");
    }

    private static long parseDelayTicks(String v) {
        if (v == null) return 20;
        String s = v.trim().toLowerCase();
        try {
            if (s.endsWith("t")) return Math.max(1, Long.parseLong(s.substring(0, s.length() - 1).trim()));
            if (s.endsWith("s")) s = s.substring(0, s.length() - 1).trim();
            return Math.max(1, Math.round(Double.parseDouble(s) * 20));
        } catch (NumberFormatException e) { return 20; }
    }

    public void clearLocks(java.util.UUID player) {
        sequenceLocks.removeIf(k -> k.startsWith(player + "|"));
    }

    public void criarNpc(Player p, String id) {
        if (!id.matches("[a-z0-9_]{1,16}")) {
            tell(p, "id-invalido");
            return;
        }
        if (registry.exists(id)) { tell(p, "ja-existe", id); return; }
        Location l = p.getLocation();
        NpcData d = new NpcData(id, l.getWorld().getUID(), l.getX(), l.getY(), l.getZ(), l.getYaw(), l.getPitch(),
                new SkinSource("nome", p.getName()), SkinData.EMPTY, List.of(), true, "", List.of(),
                false, "white", 1.0, false, "player", 0.0, "normal", java.util.Map.of(),
                "todos", "", false, false, java.util.List.of(), java.util.List.of(), 0.0, 0.0, 0.0, 0.0, 0.0);
        Npc npc = new Npc(d);
        registry.add(npc);
        storage.saveAsync(d);
        resolveSkin(npc);
        tell(p, "criado", id);
    }

    public void deletarNpc(Player p, String id) {
        Npc npc = registry.remove(id);
        if (npc == null) { tell(p, "npc-nao-existe", id); return; }
        storage.deleteAsync(id);
        for (Player viewer : getServer().getOnlinePlayers()) {
            viewer.getScheduler().run(this, t -> {
                PlayerSession s = sessions.get(viewer.getUniqueId());
                if (s != null && s.spawned.remove(id)) {
                    PacketNpc.despawn(viewer, npc);
                    nameplate.despawnLines(viewer, npc);
                    look.forget(viewer, npc);
                }
            }, null);
        }
        tell(p, "removido", id);
    }

    public void definirNome(Player p, String id, String texto) {
        Npc npc = registry.byId(id);
        if (npc == null) { tell(p, "npc-nao-existe", id); return; }
        List<String> linhas = texto.isBlank() ? List.of() : List.of(texto.split("\\|"));
        npc.setData(npc.data().withNameLines(linhas));
        storage.saveAsync(npc.data());
        refreshNameplate(npc);
        p.sendMessage(linhas.isEmpty() ? msg("nome-removido", id) : msg("nome-definido", id, linhas.size()));
    }

    private void refreshNameplate(Npc npc) {
        for (Player viewer : getServer().getOnlinePlayers()) {
            viewer.getScheduler().run(this, t -> {
                PlayerSession s = sessions.get(viewer.getUniqueId());
                if (s != null && s.spawned.contains(npc.id())) {
                    nameplate.despawnLines(viewer, npc);
                    nameplate.spawnLines(viewer, npc);
                }
            }, null);
        }
    }

    public static final int MAX_NAME_LINES = 9;

    public List<String> nomeLinhas(String id) {
        Npc n = registry.byId(id);
        return n == null ? List.of() : n.data().nameLines();
    }
    public List<Double> nomeEscalas(String id) {
        Npc n = registry.byId(id);
        return n == null ? List.of() : padScales(n.data().nameScales(), n.data().nameLines().size());
    }

    private static List<Double> padScales(List<Double> s, int n) {
        List<Double> out = new ArrayList<>(s == null ? List.of() : s);
        while (out.size() < n) out.add(1.0);
        while (out.size() > n) out.remove(out.size() - 1);
        return out;
    }
    private void aplicarNome(Npc n, List<String> lines, List<Double> scales) {
        n.setData(n.data().withNameLines(lines).withNameScales(padScales(scales, lines.size())));
        storage.saveAsync(n.data());
        refreshNameplate(n);
    }
    public boolean nomeAdd(String id, String line) {
        Npc n = registry.byId(id); if (n == null) return false;
        List<String> l = new ArrayList<>(n.data().nameLines());
        if (l.size() >= MAX_NAME_LINES) return false;
        List<Double> s = padScales(n.data().nameScales(), l.size());
        l.add(line); s.add(1.0);
        aplicarNome(n, l, s);
        return true;
    }
    public void nomeSet(String id, int i, String line) {
        Npc n = registry.byId(id); if (n == null) return;
        List<String> l = new ArrayList<>(n.data().nameLines());
        if (i < 0 || i >= l.size()) return;
        l.set(i, line);
        aplicarNome(n, l, n.data().nameScales());
    }
    public void nomeRemove(String id, int i) {
        Npc n = registry.byId(id); if (n == null) return;
        List<String> l = new ArrayList<>(n.data().nameLines());
        List<Double> s = padScales(n.data().nameScales(), l.size());
        if (i < 0 || i >= l.size()) return;
        l.remove(i); s.remove(i);
        aplicarNome(n, l, s);
    }
    public void nomeMove(String id, int i, int delta) {
        Npc n = registry.byId(id); if (n == null) return;
        List<String> l = new ArrayList<>(n.data().nameLines());
        List<Double> s = padScales(n.data().nameScales(), l.size());
        int j = i + delta;
        if (i < 0 || i >= l.size() || j < 0 || j >= l.size()) return;
        java.util.Collections.swap(l, i, j); java.util.Collections.swap(s, i, j);
        aplicarNome(n, l, s);
    }

    public void nomeEscala(String id, int i, double scale) {
        Npc n = registry.byId(id); if (n == null) return;
        List<String> l = n.data().nameLines();
        if (i < 0 || i >= l.size()) return;
        List<Double> s = padScales(n.data().nameScales(), l.size());
        s.set(i, Math.max(0.25, Math.min(5.0, Math.round(scale * 100) / 100.0)));
        aplicarNome(n, new ArrayList<>(l), s);
    }
    public void nomeClear(String id) {
        Npc n = registry.byId(id); if (n == null) return;
        aplicarNome(n, new ArrayList<>(), new ArrayList<>());
    }

    public void definirSkin(Player p, String id, String valor) {
        Npc npc = registry.byId(id);
        if (npc == null) { tell(p, "npc-nao-existe", id); return; }
        String v = valor.trim();
        boolean url = v.startsWith("http://") || v.startsWith("https://");
        npc.setData(npc.data().withSkinSource(new SkinSource(url ? "url" : "nome", v)));
        storage.saveAsync(npc.data());
        resolveSkin(npc);
        p.sendMessage(url ? msg("skin-url", id) : msg("skin-nick", id, v));
    }

    public void verNpc(Player p, String id, String alvo) {
        Npc npc = registry.byId(id);
        if (npc == null) { tell(p, "npc-nao-existe", id); return; }
        Player target = getServer().getPlayerExact(alvo);
        if (target == null) {
            tell(p, "ver-precisa-online", alvo);
            return;
        }
        String uuid = target.getUniqueId().toString();
        List<String> viewers = new ArrayList<>(npc.data().manualViewers());
        boolean added;
        if (viewers.remove(uuid)) { added = false; }
        else { viewers.add(uuid); added = true; }
        npc.setData(npc.data().withManualViewers(viewers));
        storage.saveAsync(npc.data());
        refreshNpc(npc);
        String dica = npc.data().visibility().equalsIgnoreCase("manual") ? "" : messages.get("ver-dica", id);
        p.sendMessage(LegacyComponentSerializer.legacyAmpersand()
                .deserialize(messages.get(added ? "ver-add" : "ver-rem", alvo, id) + dica));
    }

    public boolean alternarOlhar(String id) {
        Npc npc = registry.byId(id);
        if (npc == null) return false;
        boolean novo = !npc.data().lookAtPlayer();
        npc.setData(npc.data().withLookAt(novo));
        storage.saveAsync(npc.data());
        return novo;
    }

    public boolean alternarGlow(String id) {
        Npc npc = registry.byId(id);
        if (npc == null) return false;
        boolean novo = !npc.data().glow();
        npc.setData(npc.data().withGlow(novo));
        storage.saveAsync(npc.data());
        refreshNpc(npc);
        return novo;
    }

    public void definirGlowCor(String id, String cor) {
        Npc npc = registry.byId(id);
        if (npc == null) return;
        npc.setData(npc.data().withGlowColor(cor));
        storage.saveAsync(npc.data());
        refreshNpc(npc);
    }

    public double ajustarEscala(String id, double delta) {
        Npc npc = registry.byId(id);
        if (npc == null) return 1.0;
        double s = Math.max(0.1, Math.min(10.0, npc.data().scale() + delta));
        s = Math.round(s * 10) / 10.0;
        npc.setData(npc.data().withScale(s));
        storage.saveAsync(npc.data());
        refreshNpc(npc);
        return s;
    }

    public boolean alternarTab(String id) {
        Npc npc = registry.byId(id);
        if (npc == null) return false;
        boolean novo = !npc.data().showInTab();
        npc.setData(npc.data().withShowInTab(novo));
        storage.saveAsync(npc.data());
        refreshNpc(npc);
        return novo;
    }

    public void definirTipo(String id, String tipo) {
        Npc npc = registry.byId(id);
        if (npc == null) return;
        npc.setData(npc.data().withTipo(tipo));
        storage.saveAsync(npc.data());
        refreshNpc(npc);
    }

    public void definirModelo(Player p, String id, String ref) {
        Npc npc = registry.byId(id);
        if (npc == null) return;
        npc.setData(npc.data().withModel(ref));
        storage.saveAsync(npc.data());
        refreshNpc(npc);
        tell(p, "modelo-definido", id, ref);
    }

    public void definirPose(String id, String pose) {
        Npc npc = registry.byId(id);
        if (npc == null) return;
        npc.setData(npc.data().withPose(pose));
        storage.saveAsync(npc.data());
        refreshNpc(npc);
    }

    public void definirEquip(Player p, String id, String slot, String ref) {
        Npc npc = registry.byId(id);
        if (npc == null) return;
        java.util.Map<String, String> eq = new java.util.TreeMap<>(npc.data().equipment());
        if (ref == null || ref.isBlank() || ref.equalsIgnoreCase("remover")) eq.remove(slot);
        else eq.put(slot, ref);
        npc.setData(npc.data().withEquipment(eq));
        storage.saveAsync(npc.data());
        refreshNpc(npc);
        tell(p, "equip-atualizado", slot, id);
    }

    public double ajustarOffset(String id, double delta) {
        Npc npc = registry.byId(id);
        if (npc == null) return 0;
        double o = Math.round((npc.data().nameOffset() + delta) * 10) / 10.0;
        npc.setData(npc.data().withNameOffset(o));
        storage.saveAsync(npc.data());
        refreshNpc(npc);
        return o;
    }

    public void moverNpc(Player p, String id) {
        Npc n = registry.byId(id);
        if (n == null) { tell(p, "npc-nao-existe", id); return; }
        Location l = p.getLocation();
        n.setData(n.data().withLocation(l.getWorld().getUID(), l.getX(), l.getY(), l.getZ(), l.getYaw(), l.getPitch()));
        storage.saveAsync(n.data());
        refreshNpc(n);
        tell(p, "movido", id);
    }

    public void centralizarNpc(Player p, String id) {
        Npc n = registry.byId(id);
        if (n == null) { tell(p, "npc-nao-existe", id); return; }
        NpcData d = n.data();
        n.setData(d.withLocation(d.world(), Math.floor(d.x()) + 0.5, d.y(), Math.floor(d.z()) + 0.5, d.yaw(), d.pitch()));
        storage.saveAsync(n.data());
        refreshNpc(n);
        tell(p, "centralizado", id);
    }

    public void rotacionarNpc(Player p, String id) {
        Npc n = registry.byId(id);
        if (n == null) { tell(p, "npc-nao-existe", id); return; }
        NpcData d = n.data();
        Location l = p.getLocation();

        double dx = l.getX() - d.x(), dz = l.getZ() - d.z();
        float yaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
        n.setData(d.withLocation(d.world(), d.x(), d.y(), d.z(), yaw, 0f));
        storage.saveAsync(n.data());
        refreshNpc(n);
        tell(p, "virado", id);
    }

    public double ajustarAltura(String id, double delta) {
        Npc n = registry.byId(id);
        if (n == null) return 0;
        NpcData d = n.data();
        double y = Math.round((d.y() + delta) * 10) / 10.0;
        n.setData(d.withLocation(d.world(), d.x(), y, d.z(), d.yaw(), d.pitch()));
        storage.saveAsync(n.data());
        refreshNpc(n);
        return y;
    }

    public double[] hitbox(String id) {
        Npc n = registry.byId(id);
        if (n == null) return new double[]{0, 0, 0, 0, 0};
        NpcData d = n.data();
        return new double[]{d.hitOffX(), d.hitOffY(), d.hitOffZ(), d.hitWidth(), d.hitHeight()};
    }

    public double ajustarHitbox(String id, int field, double delta) {
        Npc n = registry.byId(id);
        if (n == null) return 0;
        NpcData d = n.data();
        double ox = d.hitOffX(), oy = d.hitOffY(), oz = d.hitOffZ(), w = d.hitWidth(), h = d.hitHeight();
        double v = switch (field) { case 0 -> ox; case 1 -> oy; case 2 -> oz; case 3 -> w; default -> h; };
        v = Math.round((v + delta) * 100) / 100.0;
        v = (field <= 2) ? Math.max(-5, Math.min(5, v)) : Math.max(0, Math.min(8, v));
        switch (field) { case 0 -> ox = v; case 1 -> oy = v; case 2 -> oz = v; case 3 -> w = v; default -> h = v; }
        n.setData(d.withHitbox(ox, oy, oz, w, h));
        storage.saveAsync(n.data());
        refreshNpc(n);
        return v;
    }
    public void resetHitbox(String id) {
        Npc n = registry.byId(id);
        if (n == null) return;
        n.setData(n.data().withHitbox(0, 0, 0, 0, 0));
        storage.saveAsync(n.data());
        refreshNpc(n);
    }

    public void definirDisplay(Player p, String id, String tipo, String ref) {
        Npc npc = registry.byId(id);
        if (npc == null) { tell(p, "npc-nao-existe", id); return; }
        npc.setData(npc.data().withTipo(tipo).withModel(ref));
        storage.saveAsync(npc.data());
        refreshNpc(npc);
        tell(p, "display-definido", id, tipo, ref);
    }

    public void definirItemModelo(Player p, String id, String ref) {
        Npc n = registry.byId(id);
        if (n == null) { tell(p, "npc-nao-existe", id); return; }
        NpcData d = n.data();
        String tipo = (d.tipo().equals("item") || d.tipo().equals("bloco")) ? d.tipo() : "item";
        n.setData(d.withModel(ref).withTipo(tipo));
        storage.saveAsync(n.data());
        refreshNpc(n);
        tell(p, "itemmodelo-definido", id, ref, tipo);
    }

    public void copiarNpc(Player p, String origem, String novo) {
        if (!novo.matches("[a-z0-9_]{1,16}")) {
            tell(p, "id-invalido-curto"); return;
        }
        Npc o = registry.byId(origem);
        if (o == null) { tell(p, "npc-nao-existe", origem); return; }
        if (registry.exists(novo)) { tell(p, "ja-existe-curto", novo); return; }
        Location l = p.getLocation();
        NpcData d = o.data().withId(novo)
                .withLocation(l.getWorld().getUID(), l.getX(), l.getY(), l.getZ(), l.getYaw(), l.getPitch());
        Npc n = new Npc(d);
        registry.add(n);
        storage.saveAsync(d);
        if (d.tipo().equals("player") && d.skin().isEmpty()) resolveSkin(n);
        tell(p, "copiado", origem, novo);
    }

    public boolean alternarColidir(String id) {
        Npc n = registry.byId(id);
        if (n == null) return false;
        boolean v = !n.data().collidable();
        n.setData(n.data().withCollidable(v));
        storage.saveAsync(n.data());
        refreshNpc(n);
        return v;
    }

    public boolean alternarSkinEspelho(String id) {
        Npc n = registry.byId(id);
        if (n == null) return false;
        boolean v = !n.data().skinMirror();
        n.setData(n.data().withSkinMirror(v));
        storage.saveAsync(n.data());
        refreshNpc(n);
        return v;
    }

    public String cicloVisibilidade(String id) {
        Npc n = registry.byId(id);
        if (n == null) return "todos";
        String atual = n.data().visibility();
        String prox = switch (atual == null ? "todos" : atual) {
            case "todos" -> "permissao";
            case "permissao" -> "manual";
            default -> "todos";
        };
        n.setData(n.data().withVisibility(prox));
        storage.saveAsync(n.data());
        refreshNpc(n);
        return prox;
    }

    public void definirPermissao(Player p, String id, String perm) {
        Npc n = registry.byId(id);
        if (n == null) { tell(p, "npc-nao-existe", id); return; }
        n.setData(n.data().withPermissao(perm));
        storage.saveAsync(n.data());
        refreshNpc(n);
        tell(p, "permissao-definida", id, perm);
    }

    private void refreshNpc(Npc npc) {
        for (Player viewer : getServer().getOnlinePlayers()) {
            viewer.getScheduler().run(this, t -> {
                PlayerSession s = sessions.get(viewer.getUniqueId());
                if (s != null && s.spawned.contains(npc.id())) {
                    PacketNpc.despawn(viewer, npc);
                    nameplate.despawnLines(viewer, npc);
                    PacketNpc.spawn(viewer, npc);
                    nameplate.spawnLines(viewer, npc);
                }
            }, null);
        }
    }

    public void abrirEditor(Player p, String id) { gui.openEditor(p, id); }

    public void testarHud(Player p, String id, int segundos) {
        hud.show(p, id, Math.max(20L, segundos * 20L), () -> tell(p, "hud-terminou", id));
    }

    public void removerImagemNpc(String npcId, String token) {
        Npc n = registry.byId(npcId);
        if (n == null) return;
        List<ActionData> list = new ArrayList<>(n.data().actions());
        list.removeIf(a -> "imagem".equalsIgnoreCase(a.type()) && a.value().split(";")[0].trim().equals(token));
        n.setData(n.data().withActions(list));
        storage.saveAsync(n.data());
    }

    public void listarHud(Player p) {
        var ids = hud.ids();
        p.sendMessage(ids.isEmpty() ? msg("hud-nenhuma") : msg("hud-lista", String.join(", ", ids)));
    }

    public int hudCfg(String key, int def) { return getConfig().getInt(key, def); }

    public int hudImgAltura(String id)  { return hud.posicoes().altura(id, hudCfg("hud-altura", 128)); }
    public int hudImgAscent(String id)  { return hud.posicoes().ascent(id, hudCfg("hud-ascent", 80)); }
    public int hudImgX(String id)       { return hud.posicoes().x(id, hudCfg("hud-x", 0)); }
    public String hudImgExibir(String id) { return hud.posicoes().exibir(id, getConfig().getString("hud-exibir", "title")); }

    public String cicloHudExibir(String id) {
        String prox = switch (hudImgExibir(id).toLowerCase()) {
            case "title" -> "actionbar";
            case "actionbar" -> "bossbar";
            case "bossbar" -> "chat";
            default -> "title";
        };
        hud.posicoes().setString(id, "exibir", prox);
        return prox;
    }

    public void ajustarHudAltura(String id, int delta) {
        int alt = Math.max(16, Math.min(256, hudImgAltura(id) + delta));
        hud.posicoes().setInt(id, "altura", alt);
        if (hudImgAscent(id) > alt) hud.posicoes().setInt(id, "ascent", alt);
    }

    public void ajustarHudAscent(String id, int delta) {
        int alt = hudImgAltura(id);
        hud.posicoes().setInt(id, "ascent", Math.max(-2048, Math.min(alt, hudImgAscent(id) + delta)));
    }

    public void ajustarHudX(String id, int delta) {
        hud.posicoes().setInt(id, "x", Math.max(-4096, Math.min(4096, hudImgX(id) + delta)));
    }

    public void ajustarHudTile(int delta) {
        getConfig().set("hud-hd-tile", Math.max(4, Math.min(64, hudCfg("hud-hd-tile", 16) + delta)));
        saveConfig();
    }

    public void abrirHudConfig(Player p) { gui.openHudConfig(p); }

    public void recarregarHud(Player p) {
        reloadConfig();
        hud.stop();
        hud.init();
        hud.pushPack(p);
        tell(p, "hud-recarregado", getConfig().getString("hud-modo", "upload"), String.join(", ", hud.ids()));
    }

    public HudService hud() { return hud; }

    public void listarNpcs(Player p) {
        var all = registry.all();
        if (all.isEmpty()) { tell(p, "nenhum-npc"); return; }
        StringBuilder sb = new StringBuilder();
        for (Npc n : all) sb.append(n.id()).append(" ");
        tell(p, "lista-npcs", all.size(), sb.toString().trim());
    }

    public void acaoAdicionar(Player p, String id, String tipo, String valor) {
        Npc npc = registry.byId(id);
        if (npc == null) { tell(p, "npc-nao-existe", id); return; }
        if (!ActionType.isValid(tipo)) {
            tell(p, "acao-tipo-invalido", tipo);
            return;
        }
        List<ActionData> list = new ArrayList<>(npc.data().actions());
        list.add(new ActionData(tipo.toLowerCase(), valor));
        npc.setData(npc.data().withActions(list));
        storage.saveAsync(npc.data());
        tell(p, "acao-add", tipo, id);
    }

    public void acaoListar(Player p, String id) {
        Npc npc = registry.byId(id);
        if (npc == null) { tell(p, "npc-nao-existe", id); return; }
        var actions = npc.data().actions();
        if (actions.isEmpty()) { tell(p, "acao-sem", id); return; }
        tell(p, "acao-lista-cabecalho", id);
        for (int i = 0; i < actions.size(); i++) {
            ActionData a = actions.get(i);
            if (a.permission() != null && !a.permission().isBlank())
                tell(p, "acao-lista-item-perm", i, a.type(), a.value(), a.permission());
            else
                tell(p, "acao-lista-item", i, a.type(), a.value());
        }
    }

    public void acaoPermissao(Player p, String id, int index, String perm) {
        Npc npc = registry.byId(id);
        if (npc == null) { tell(p, "npc-nao-existe", id); return; }
        List<ActionData> list = new ArrayList<>(npc.data().actions());
        if (index < 0 || index >= list.size()) {
            tell(p, "acao-indice-invalido", index, id);
            return;
        }
        String norm = (perm == null || perm.isBlank()
                || perm.equalsIgnoreCase("remover") || perm.equalsIgnoreCase("nenhum")) ? "" : perm.trim();
        list.set(index, list.get(index).withPermission(norm));
        npc.setData(npc.data().withActions(list));
        storage.saveAsync(npc.data());
        if (norm.isEmpty()) tell(p, "acao-perm-limpa", index, id);
        else tell(p, "acao-perm-definida", index, id, norm);
    }

    public void acaoRemover(Player p, String id, int index) {
        Npc npc = registry.byId(id);
        if (npc == null) { tell(p, "npc-nao-existe", id); return; }
        List<ActionData> list = new ArrayList<>(npc.data().actions());
        if (index < 0 || index >= list.size()) {
            tell(p, "acao-indice-invalido", index, id);
            return;
        }
        ActionData removed = list.remove(index);
        npc.setData(npc.data().withActions(list));
        storage.saveAsync(npc.data());
        tell(p, "acao-removida", removed.type(), id);
    }

    public void acaoCicloGatilho(String id, int index) {
        Npc npc = registry.byId(id);
        if (npc == null) return;
        List<ActionData> list = new ArrayList<>(npc.data().actions());
        if (index < 0 || index >= list.size()) return;
        ActionData a = list.get(index);
        String prox = switch (a.trigger() == null ? "direito" : a.trigger()) {
            case "direito" -> "esquerdo";
            case "esquerdo" -> "qualquer";
            default -> "direito";
        };
        list.set(index, new ActionData(a.type(), a.value(), prox, a.permission()));
        npc.setData(npc.data().withActions(list));
        storage.saveAsync(npc.data());
    }

    public void acaoMover(String id, int index, int delta) {
        Npc npc = registry.byId(id);
        if (npc == null) return;
        List<ActionData> list = new ArrayList<>(npc.data().actions());
        int j = index + delta;
        if (index < 0 || index >= list.size() || j < 0 || j >= list.size()) return;
        ActionData tmp = list.get(index);
        list.set(index, list.get(j));
        list.set(j, tmp);
        npc.setData(npc.data().withActions(list));
        storage.saveAsync(npc.data());
    }

    private void resolveSkin(Npc npc) {
        resolver.resolve(npc.data().skinSource()).thenAccept(skin -> {
            if (!isEnabled() || skin.isEmpty()) return;
            getServer().getGlobalRegionScheduler().run(this, t -> applyResolvedSkin(npc, skin));
        });
    }

    private void applyResolvedSkin(Npc npc, SkinData skin) {
        npc.setData(npc.data().withSkin(skin));
        storage.saveAsync(npc.data());
        refreshNpc(npc);
    }
}
