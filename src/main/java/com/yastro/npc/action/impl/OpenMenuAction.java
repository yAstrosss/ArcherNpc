package com.yastro.npc.action.impl;

import com.yastro.npc.action.NpcAction;
import org.bukkit.entity.Player;

public record OpenMenuAction(String menu) implements NpcAction {
    @Override
    public void execute(Player p) { p.performCommand("menu " + menu); }
}
