package com.yastro.npc.server;

import org.bukkit.entity.Player;

@FunctionalInterface
public interface ServerConnector {
    void connect(Player player, String server);
}
