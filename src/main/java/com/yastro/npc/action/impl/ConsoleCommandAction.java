package com.yastro.npc.action.impl;

import com.yastro.npc.action.NpcAction;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public record ConsoleCommandAction(String command) implements NpcAction {
    @Override
    public void execute(Player p) {

        String safe = p.getName().replaceAll("[^A-Za-z0-9_]", "");
        String cmd = command.replace("%player%", safe).trim();
        if (cmd.startsWith("/")) cmd = cmd.substring(1);
        if (cmd.isEmpty()) return;
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
    }
}
