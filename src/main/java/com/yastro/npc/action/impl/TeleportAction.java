package com.yastro.npc.action.impl;

import com.yastro.npc.action.NpcAction;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public record TeleportAction(String dest) implements NpcAction {
    @Override
    public void execute(Player p) {
        String[] a = dest.split(",");
        if (a.length < 4) return;
        var w = Bukkit.getWorld(a[0].trim());
        if (w == null) return;
        try {
            double x = Double.parseDouble(a[1].trim()), y = Double.parseDouble(a[2].trim()), z = Double.parseDouble(a[3].trim());
            if (!Double.isFinite(x) || !Double.isFinite(y) || !Double.isFinite(z)) return;
            p.teleportAsync(new Location(w, x, y, z,
                    a.length > 4 ? Float.parseFloat(a[4].trim()) : 0f,
                    a.length > 5 ? Float.parseFloat(a[5].trim()) : 0f));
        } catch (NumberFormatException ignored) {  }
    }
}
