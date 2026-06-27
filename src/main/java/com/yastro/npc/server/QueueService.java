package com.yastro.npc.server;

import com.yastro.npc.YAstroNpcPlugin;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Deque;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

public final class QueueService implements ServerConnector, Listener {
    private final YAstroNpcPlugin plugin;
    private final ServerConnector delegate;
    private final int perBatch;
    private final long intervalTicks;
    private final Map<String, Deque<UUID>> queues = new ConcurrentHashMap<>();
    private final Set<UUID> queued = ConcurrentHashMap.newKeySet();
    private ScheduledTask task;

    public QueueService(YAstroNpcPlugin plugin, ServerConnector delegate, int perBatch, long intervalTicks) {
        this.plugin = plugin;
        this.delegate = delegate;
        this.perBatch = Math.max(1, perBatch);
        this.intervalTicks = Math.max(1, intervalTicks);
    }

    public void start() {
        task = plugin.getServer().getGlobalRegionScheduler()
                .runAtFixedRate(plugin, t -> drain(), intervalTicks, intervalTicks);
    }

    public void stop() {
        if (task != null) task.cancel();
        queues.clear();
        queued.clear();
    }

    @Override
    public void connect(Player player, String server) {
        String key = server.toLowerCase();
        UUID u = player.getUniqueId();
        Deque<UUID> q = queues.computeIfAbsent(key, k -> new ConcurrentLinkedDeque<>());
        if (!queued.add(u)) {
            Deque<UUID> cur = queueOf(u);
            int total = cur == null ? 0 : cur.size();
            player.sendMessage(plugin.msg("fila-ja", positionIn(cur, u), total));
            return;
        }
        q.addLast(u);
        player.sendMessage(plugin.msg("fila-entrou", server, q.size()));
    }

    private void drain() {
        for (Map.Entry<String, Deque<UUID>> e : queues.entrySet()) {
            String server = e.getKey();
            Deque<UUID> q = e.getValue();
            int sent = 0;
            while (sent < perBatch && !q.isEmpty()) {
                UUID u = q.pollFirst();
                queued.remove(u);
                Player p = Bukkit.getPlayer(u);
                if (p == null || !p.isOnline()) continue;
                Player fp = p;
                p.getScheduler().run(plugin, st -> {
                    fp.sendMessage(plugin.msg("fila-enviando", server));
                    delegate.connect(fp, server);
                }, null);
                sent++;
            }
            int total = q.size();
            int pos = 1;
            for (UUID u : q) {
                Player p = Bukkit.getPlayer(u);
                if (p != null && p.isOnline()) {
                    int fpos = pos;
                    p.getScheduler().run(plugin, st -> p.sendActionBar(plugin.msg("fila-posicao", server, fpos, total)), null);
                }
                pos++;
            }
        }
    }

    private Deque<UUID> queueOf(UUID u) {
        for (Deque<UUID> q : queues.values()) if (q.contains(u)) return q;
        return null;
    }

    private static int positionIn(Deque<UUID> q, UUID u) {
        if (q == null) return 0;
        int pos = 1;
        for (UUID x : q) { if (x.equals(u)) return pos; pos++; }
        return 0;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        UUID u = e.getPlayer().getUniqueId();
        if (queued.remove(u)) for (Deque<UUID> q : queues.values()) q.remove(u);
    }
}
