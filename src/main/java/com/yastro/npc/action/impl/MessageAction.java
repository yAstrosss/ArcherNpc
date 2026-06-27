package com.yastro.npc.action.impl;

import com.yastro.npc.action.NpcAction;
import com.yastro.npc.util.Colors;
import org.bukkit.entity.Player;

public record MessageAction(String legacy) implements NpcAction {
    @Override
    public void execute(Player p) {
        p.sendMessage(Colors.render(legacy));
    }
}
