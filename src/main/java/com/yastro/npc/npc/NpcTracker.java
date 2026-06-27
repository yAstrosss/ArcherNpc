package com.yastro.npc.npc;

import com.yastro.npc.model.Npc;
import com.yastro.npc.model.NpcData;
import com.yastro.npc.nameplate.NameplateService;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class NpcTracker {
    private final Plugin plugin;
    private final NpcRegistry registry;
    private final PlayerSessionManager sessions;
    private final NameplateService nameplate;
    private final LookController look;
    private final double rangeSq;
    private final long intervalTicks;
    private final long graceMs;
    private final long refreshEvery;
    private final ConcurrentHashMap<UUID, ScheduledTask> tasks = new ConcurrentHashMap<>();

    public NpcTracker(Plugin plugin, NpcRegistry registry, PlayerSessionManager sessions,
                      NameplateService nameplate, LookController look, double range, long intervalTicks, long graceMs) {
        this.plugin = plugin; this.registry = registry; this.sessions = sessions; this.nameplate = nameplate;
        this.look = look;
        this.rangeSq = range * range; this.intervalTicks = Math.max(1, intervalTicks); this.graceMs = graceMs;
        this.refreshEvery = Math.max(1, 20 / this.intervalTicks);
    }

    public void start() { for (Player p : plugin.getServer().getOnlinePlayers()) startFor(p); }

    public void startFor(Player viewer) {
        stopFor(viewer.getUniqueId());
        ScheduledTask t = viewer.getScheduler().runAtFixedRate(plugin,
                st -> tickViewer(viewer), null, intervalTicks, intervalTicks);
        if (t != null) tasks.put(viewer.getUniqueId(), t);
    }

    public void stopFor(UUID viewer) {
        ScheduledTask t = tasks.remove(viewer);
        if (t != null) t.cancel();
    }

    public void stop() {
        tasks.values().forEach(ScheduledTask::cancel);
        tasks.clear();
    }

    private void tickViewer(Player viewer) {
        PlayerSession s = sessions.get(viewer.getUniqueId());
        if (s == null) return;

        if (graceMs > 0 && System.currentTimeMillis() - s.openedAtMs < graceMs) return;
        boolean doRefresh = (++s.ticks % refreshEvery) == 0;
        UUID world = viewer.getWorld().getUID();
        Location loc = viewer.getLocation();
        for (Npc npc : registry.all()) {
            NpcData d = npc.data();
            boolean visible = canSee(viewer, d) && d.world().equals(world) && distSq(loc, d) <= rangeSq;
            boolean shown = s.spawned.contains(npc.id());
            try {
                if (visible && !shown) {
                    PacketNpc.spawn(viewer, npc); nameplate.spawnLines(viewer, npc); s.spawned.add(npc.id());
                } else if (!visible && shown) {
                    PacketNpc.despawn(viewer, npc); nameplate.despawnLines(viewer, npc);
                    look.forget(viewer, npc); s.spawned.remove(npc.id());
                } else if (visible && shown) {
                    look.tick(viewer, npc);
                    if (doRefresh) nameplate.refresh(viewer, npc);
                }
            } catch (Exception e) {

                plugin.getLogger().warning("Falha ao renderizar NPC " + npc.id() + ": " + e.getMessage());
            }
        }
    }

    private static boolean canSee(Player viewer, NpcData d) {
        return switch (d.visibility() == null ? "todos" : d.visibility().toLowerCase()) {
            case "permissao" -> !d.permissao().isBlank() && viewer.hasPermission(d.permissao());

            case "manual" -> d.manualViewers() != null && d.manualViewers().contains(viewer.getUniqueId().toString());
            default -> true;
        };
    }

    private static double distSq(Location l, NpcData d) {
        double dx = l.getX() - d.x(), dy = l.getY() - d.y(), dz = l.getZ() - d.z();
        return dx * dx + dy * dy + dz * dz;
    }
}
