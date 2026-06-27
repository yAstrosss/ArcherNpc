package com.yastro.npc.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.yastro.npc.YAstroNpcPlugin;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.entity.Player;

public final class NpcCommandPhase2 {
    private NpcCommandPhase2() {}

    public static LiteralArgumentBuilder<CommandSourceStack> build(YAstroNpcPlugin plugin) {
        return Commands.literal("npc")
            .requires(s -> s.getSender().hasPermission("archernpc.admin"))
            .then(Commands.literal(lit(plugin, "cmd-criar", "criar")).then(Commands.argument("id", StringArgumentType.word())
                .executes(c -> {
                    if (!(c.getSource().getSender() instanceof Player p)) return 0;
                    plugin.criarNpc(p, StringArgumentType.getString(c, "id"));
                    return 1;
                })))
            .then(Commands.literal(lit(plugin, "cmd-deletar", "deletar")).then(Commands.argument("id", StringArgumentType.word())
                .executes(c -> {
                    if (!(c.getSource().getSender() instanceof Player p)) return 0;
                    plugin.deletarNpc(p, StringArgumentType.getString(c, "id"));
                    return 1;
                })))
            .then(Commands.literal(lit(plugin, "cmd-listar", "listar")).executes(c -> {
                if (!(c.getSource().getSender() instanceof Player p)) return 0;
                plugin.listarNpcs(p);
                return 1;
            }))
            .then(Commands.literal(lit(plugin, "cmd-editar", "editar")).then(Commands.argument("id", StringArgumentType.word())
                .executes(c -> {
                    if (!(c.getSource().getSender() instanceof Player p)) return 0;
                    plugin.abrirEditor(p, StringArgumentType.getString(c, "id"));
                    return 1;
                })))
            .then(Commands.literal(lit(plugin, "cmd-hud", "hud"))
                .executes(c -> {
                    if (!(c.getSource().getSender() instanceof Player p)) return 0;
                    plugin.abrirHudConfig(p);
                    return 1;
                })
                .then(Commands.literal(lit(plugin, "cmd-recarregar", "recarregar")).executes(c -> {
                    if (!(c.getSource().getSender() instanceof Player p)) return 0;
                    plugin.recarregarHud(p);
                    return 1;
                }))
                .then(Commands.argument("imagem", StringArgumentType.word())
                    .executes(c -> {
                        if (!(c.getSource().getSender() instanceof Player p)) return 0;
                        plugin.testarHud(p, StringArgumentType.getString(c, "imagem"), 10);
                        return 1;
                    })
                    .then(Commands.argument("segundos", IntegerArgumentType.integer(1, 60))
                        .executes(c -> {
                            if (!(c.getSource().getSender() instanceof Player p)) return 0;
                            plugin.testarHud(p, StringArgumentType.getString(c, "imagem"),
                                    IntegerArgumentType.getInteger(c, "segundos"));
                            return 1;
                        }))))
            .then(Commands.literal(lit(plugin, "cmd-nome", "nome")).then(Commands.argument("id", StringArgumentType.word())
                .then(Commands.argument("texto", StringArgumentType.greedyString())
                    .executes(c -> {
                        if (!(c.getSource().getSender() instanceof Player p)) return 0;
                        plugin.definirNome(p, StringArgumentType.getString(c, "id"),
                                StringArgumentType.getString(c, "texto"));
                        return 1;
                    }))))
            .then(Commands.literal(lit(plugin, "cmd-acao", "acao"))
                .then(Commands.literal(lit(plugin, "cmd-adicionar", "adicionar")).then(Commands.argument("id", StringArgumentType.word())
                    .then(Commands.argument("tipo", StringArgumentType.word())
                    .then(Commands.argument("valor", StringArgumentType.greedyString())
                        .executes(c -> {
                            if (!(c.getSource().getSender() instanceof Player p)) return 0;
                            plugin.acaoAdicionar(p, StringArgumentType.getString(c, "id"),
                                    StringArgumentType.getString(c, "tipo"),
                                    StringArgumentType.getString(c, "valor"));
                            return 1;
                        })))))
                .then(Commands.literal(lit(plugin, "cmd-listar", "listar")).then(Commands.argument("id", StringArgumentType.word())
                    .executes(c -> {
                        if (!(c.getSource().getSender() instanceof Player p)) return 0;
                        plugin.acaoListar(p, StringArgumentType.getString(c, "id"));
                        return 1;
                    })))
                .then(Commands.literal(lit(plugin, "cmd-remover", "remover")).then(Commands.argument("id", StringArgumentType.word())
                    .then(Commands.argument("indice", IntegerArgumentType.integer(0))
                        .executes(c -> {
                            if (!(c.getSource().getSender() instanceof Player p)) return 0;
                            plugin.acaoRemover(p, StringArgumentType.getString(c, "id"),
                                    IntegerArgumentType.getInteger(c, "indice"));
                            return 1;
                        }))))
                .then(Commands.literal(lit(plugin, "cmd-perm", "perm")).then(Commands.argument("id", StringArgumentType.word())
                    .then(Commands.argument("indice", IntegerArgumentType.integer(0))
                    .then(Commands.argument("permissao", StringArgumentType.word())
                        .executes(c -> {
                            if (!(c.getSource().getSender() instanceof Player p)) return 0;
                            plugin.acaoPermissao(p, StringArgumentType.getString(c, "id"),
                                    IntegerArgumentType.getInteger(c, "indice"),
                                    StringArgumentType.getString(c, "permissao"));
                            return 1;
                        }))))))
            .then(Commands.literal(lit(plugin, "cmd-mover", "mover")).then(idArg(plugin).executes(c -> run(c, p -> plugin.moverNpc(p, id(c))))))
            .then(Commands.literal(lit(plugin, "cmd-centralizar", "centralizar")).then(idArg(plugin).executes(c -> run(c, p -> plugin.centralizarNpc(p, id(c))))))
            .then(Commands.literal(lit(plugin, "cmd-rotacionar", "rotacionar")).then(idArg(plugin).executes(c -> run(c, p -> plugin.rotacionarNpc(p, id(c))))))
            .then(Commands.literal(lit(plugin, "cmd-copiar", "copiar")).then(idArg(plugin)
                .then(Commands.argument("novo", StringArgumentType.word())
                    .executes(c -> run(c, p -> plugin.copiarNpc(p, id(c), StringArgumentType.getString(c, "novo")))))))
            .then(Commands.literal(lit(plugin, "cmd-colidir", "colidir")).then(idArg(plugin).executes(c -> run(c, p -> {
                boolean v = plugin.alternarColidir(id(c));
                p.sendMessage(plugin.msg(v ? "colidir-on" : "colidir-off"));
            }))))
            .then(Commands.literal(lit(plugin, "cmd-espelho", "espelho")).then(idArg(plugin).executes(c -> run(c, p -> {
                boolean v = plugin.alternarSkinEspelho(id(c));
                p.sendMessage(plugin.msg(v ? "espelho-on" : "espelho-off"));
            }))))
            .then(Commands.literal(lit(plugin, "cmd-visibilidade", "visibilidade")).then(idArg(plugin).executes(c -> run(c, p -> {
                String v = plugin.cicloVisibilidade(id(c));
                p.sendMessage(plugin.msg("visibilidade-ciclo", v));
            }))))
            .then(Commands.literal(lit(plugin, "cmd-permissao", "permissao")).then(idArg(plugin)
                .then(Commands.argument("perm", StringArgumentType.word())
                    .executes(c -> run(c, p -> plugin.definirPermissao(p, id(c), StringArgumentType.getString(c, "perm")))))))
            .then(Commands.literal(lit(plugin, "cmd-ver", "ver")).then(idArg(plugin)
                .then(Commands.argument("jogador", StringArgumentType.word()).suggests((ctx, b) -> {
                        String pre = b.getRemaining().toLowerCase();
                        for (Player on : org.bukkit.Bukkit.getOnlinePlayers())
                            if (on.getName().toLowerCase().startsWith(pre)) b.suggest(on.getName());
                        return b.buildFuture();
                    })
                    .executes(c -> run(c, p -> plugin.verNpc(p, id(c), StringArgumentType.getString(c, "jogador")))))))
            .then(Commands.literal(lit(plugin, "cmd-skin", "skin")).then(idArg(plugin)
                .then(Commands.argument("valor", StringArgumentType.greedyString())
                    .executes(c -> run(c, p -> plugin.definirSkin(p, id(c), StringArgumentType.getString(c, "valor")))))));
    }

    private static String id(com.mojang.brigadier.context.CommandContext<CommandSourceStack> c) {
        return StringArgumentType.getString(c, "id");
    }

    private static int run(com.mojang.brigadier.context.CommandContext<CommandSourceStack> c,
                           java.util.function.Consumer<Player> action) {
        if (!(c.getSource().getSender() instanceof Player p)) return 0;
        action.accept(p);
        return 1;
    }

    private static String lit(YAstroNpcPlugin plugin, String key, String fallback) {
        String v = plugin.tr(key);
        if (v == null) return fallback;
        v = v.trim();
        if (v.isEmpty() || v.equals(key) || v.indexOf(' ') >= 0) return fallback;
        return v;
    }

    private static RequiredArgumentBuilder<CommandSourceStack, String> idArg(YAstroNpcPlugin plugin) {
        return Commands.argument("id", StringArgumentType.word()).suggests((ctx, b) -> {
            String pre = b.getRemaining().toLowerCase();
            for (var n : plugin.registry().all()) if (n.id().startsWith(pre)) b.suggest(n.id());
            return b.buildFuture();
        });
    }
}
