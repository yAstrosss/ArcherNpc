package com.yastro.npc.server;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public final class ProxyConnector implements ServerConnector {
    public static final String CHANNEL = "BungeeCord";
    private final Plugin plugin;

    public ProxyConnector(Plugin plugin) { this.plugin = plugin; }

    @Override
    public void connect(Player player, String server) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("Connect");
        out.writeUTF(server);
        player.sendPluginMessage(plugin, CHANNEL, out.toByteArray());
    }
}
