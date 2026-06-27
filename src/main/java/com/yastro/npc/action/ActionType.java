package com.yastro.npc.action;

import com.yastro.npc.action.impl.*;
import com.yastro.npc.server.ServerConnector;
import org.bukkit.plugin.Plugin;

public final class ActionType {
    private final Plugin plugin;
    private final ServerConnector connector;

    public ActionType(Plugin plugin, ServerConnector connector) { this.plugin = plugin; this.connector = connector; }

    public NpcAction create(String token, String value) {
        return switch (token.toLowerCase(java.util.Locale.ROOT)) {
            case "comando"   -> new RunCommandAction(value);
            case "console"   -> new ConsoleCommandAction(value);
            case "mensagem"  -> new MessageAction(value);
            case "teleporte" -> new TeleportAction(value);
            case "som"       -> new SoundAction(value);
            case "menu"      -> new OpenMenuAction(value);
            case "conectar"  -> new ConnectServerAction(value, connector);
            case "comando-op" -> new OpCommandAction(plugin, value);
            default          -> null;
        };
    }

    public static boolean isValid(String token) {
        return switch (token.toLowerCase(java.util.Locale.ROOT)) {

            case "comando", "console", "mensagem", "teleporte", "som", "menu", "conectar", "comando-op",
                 "imagem", "esperar", "aleatorio", "travar" -> true;
            default -> false;
        };
    }
}
