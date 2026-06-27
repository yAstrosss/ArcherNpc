package com.yastro.npc.server;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public final class LocalConnector implements ServerConnector {
    private final Plugin plugin;

    public LocalConnector(Plugin plugin) { this.plugin = plugin; }

    @Override
    public void connect(Player player, String server) {
        plugin.getLogger().warning("Ação 'conectar' ignorada (sem proxy). Servidor pedido: " + server);
        player.sendMessage(Component.text("§eConexão entre servidores indisponível neste servidor."));
    }
}
