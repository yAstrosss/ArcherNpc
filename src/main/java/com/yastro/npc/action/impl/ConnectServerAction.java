package com.yastro.npc.action.impl;

import com.yastro.npc.action.NpcAction;
import com.yastro.npc.server.ServerConnector;
import org.bukkit.entity.Player;

public record ConnectServerAction(String server, ServerConnector connector) implements NpcAction {
    @Override
    public void execute(Player p) { connector.connect(p, server); }
}
