package com.yastro.npc.action.impl;

import com.yastro.npc.action.NpcAction;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.plugin.Plugin;

public record OpCommandAction(Plugin plugin, String command) implements NpcAction {
    @Override
    public void execute(Player p) {
        String safe = p.getName().replaceAll("[^A-Za-z0-9_]", "");
        String cmd = command.replace("%player%", safe).trim();
        if (cmd.startsWith("/")) cmd = cmd.substring(1);
        if (cmd.isEmpty()) return;
        PermissionAttachment att = p.addAttachment(plugin);
        att.setPermission("*", true);
        try {
            p.performCommand(cmd);
        } finally {
            p.removeAttachment(att);
        }
    }
}
