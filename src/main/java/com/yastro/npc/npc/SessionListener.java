package com.yastro.npc.npc;

import com.yastro.npc.YAstroNpcPlugin;
import com.yastro.npc.hud.HudService;
import com.yastro.npc.interaction.CooldownService;
import com.yastro.npc.model.Npc;
import com.yastro.npc.nameplate.NameplateService;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import java.util.UUID;

public final class SessionListener implements Listener {
    private final YAstroNpcPlugin plugin;
    private final PlayerSessionManager sessions;
    private final CooldownService cooldowns;
    private final NameplateService nameplate;
    private final LookController look;
    private final NpcTracker tracker;
    private final HudService hud;

    public SessionListener(YAstroNpcPlugin plugin, PlayerSessionManager sessions, CooldownService cooldowns,
                           NameplateService nameplate, LookController look, NpcTracker tracker, HudService hud) {
        this.plugin = plugin;
        this.sessions = sessions;
        this.cooldowns = cooldowns;
        this.nameplate = nameplate;
        this.look = look;
        this.tracker = tracker;
        this.hud = hud;
    }

    @EventHandler public void onJoin(PlayerJoinEvent e) {
        sessions.open(e.getPlayer().getUniqueId());
        tracker.startFor(e.getPlayer());
        hud.pushPack(e.getPlayer());
    }

    @EventHandler public void onQuit(PlayerQuitEvent e) {
        var uuid = e.getPlayer().getUniqueId();
        tracker.stopFor(uuid);
        sessions.close(uuid);
        cooldowns.clear(uuid);
        nameplate.clear(uuid);
        look.clear(uuid);
        plugin.clearLocks(uuid);
    }

    @EventHandler public void onRespawn(PlayerRespawnEvent e) { scheduleResync(e.getPlayer()); }

    @EventHandler public void onWorldChange(PlayerChangedWorldEvent e) { scheduleResync(e.getPlayer()); }

    private void scheduleResync(Player p) {
        p.getScheduler().run(plugin, t -> resync(p), null);
    }

    private void resync(Player p) {
        UUID uuid = p.getUniqueId();
        PlayerSession s = sessions.get(uuid);
        if (s == null) return;
        for (String id : s.spawned) {
            Npc npc = plugin.registry().byId(id);
            if (npc != null) PacketNpc.despawn(p, npc);
        }
        s.resync();
        nameplate.clear(uuid);
        look.clear(uuid);
    }
}
