package com.yastro.npc.config;

import com.yastro.npc.model.ActionData;
import com.yastro.npc.model.NpcData;
import com.yastro.npc.model.SkinSource;
import com.yastro.npc.skin.SkinData;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import org.bukkit.configuration.ConfigurationSection;

public final class NpcYaml {
    private NpcYaml() {}

    public static NpcData read(File f) {
        YamlConfiguration y = YamlConfiguration.loadConfiguration(f);
        String id = f.getName().substring(0, f.getName().length() - 4);
        String wid = y.getString("mundo-uuid", "");
        UUID world;
        try { world = UUID.fromString(wid); }
        catch (IllegalArgumentException e) { throw new IllegalArgumentException("mundo-uuid ausente/invalido: '" + wid + "'"); }
        List<ActionData> actions = new ArrayList<>();
        for (Map<?, ?> raw : y.getMapList("acoes")) {
            Object trig = raw.get("gatilho");
            Object perm = raw.get("permissao");
            actions.add(new ActionData(String.valueOf(raw.get("tipo")), String.valueOf(raw.get("valor")),
                    trig == null ? "direito" : String.valueOf(trig),
                    perm == null ? "" : String.valueOf(perm)));
        }
        return new NpcData(
                id, world,
                y.getDouble("x"), y.getDouble("y"), y.getDouble("z"),
                (float) y.getDouble("yaw"), (float) y.getDouble("pitch"),
                new SkinSource(y.getString("skin.tipo", "nome"), y.getString("skin.valor", "")),
                new SkinData(y.getString("skin.textura.valor", ""), y.getString("skin.textura.assinatura", "")),
                new ArrayList<>(y.getStringList("nome")),
                y.getBoolean("olhar-jogador", true),
                y.getString("modelo", ""),
                actions,
                y.getBoolean("brilho", false),
                y.getString("brilho-cor", "white"),
                y.getDouble("escala", 1.0),
                y.getBoolean("mostrar-no-tab", false),
                y.getString("tipo", "player"),
                y.getDouble("nome-offset", 0.0),
                y.getString("pose", "normal"),
                readEquipment(y),
                y.getString("visibilidade", "todos"),
                y.getString("permissao", ""),
                y.getBoolean("skin-espelho", false),
                y.getBoolean("colidir", false),
                new ArrayList<>(y.getStringList("ver-manual")),
                new ArrayList<>(y.getDoubleList("nome-escala")),
                y.getDouble("hit-off-x"), y.getDouble("hit-off-y"), y.getDouble("hit-off-z"),
                y.getDouble("hit-largura"), y.getDouble("hit-altura"));
    }

    private static Map<String, String> readEquipment(YamlConfiguration y) {
        Map<String, String> eq = new TreeMap<>();
        ConfigurationSection sec = y.getConfigurationSection("equipamento");
        if (sec != null) for (String slot : sec.getKeys(false)) eq.put(slot, sec.getString(slot, ""));
        return eq;
    }

    public static void write(NpcData d, File f) throws Exception {
        YamlConfiguration y = new YamlConfiguration();
        y.set("mundo-uuid", d.world().toString());
        y.set("x", d.x()); y.set("y", d.y()); y.set("z", d.z());
        y.set("yaw", (double) d.yaw()); y.set("pitch", (double) d.pitch());
        y.set("skin.tipo", d.skinSource().type());
        y.set("skin.valor", d.skinSource().value());
        y.set("skin.textura.valor", d.skin().value());
        y.set("skin.textura.assinatura", d.skin().signature());
        y.set("nome", d.nameLines());
        y.set("olhar-jogador", d.lookAtPlayer());
        y.set("modelo", d.model());
        y.set("brilho", d.glow());
        y.set("brilho-cor", d.glowColor());
        y.set("escala", d.scale());
        y.set("mostrar-no-tab", d.showInTab());
        y.set("tipo", d.tipo());
        y.set("nome-offset", d.nameOffset());
        y.set("pose", d.pose());
        for (Map.Entry<String, String> e : d.equipment().entrySet()) y.set("equipamento." + e.getKey(), e.getValue());
        y.set("visibilidade", d.visibility());
        y.set("permissao", d.permissao());
        y.set("skin-espelho", d.skinMirror());
        y.set("colidir", d.collidable());
        y.set("ver-manual", d.manualViewers());
        y.set("nome-escala", d.nameScales());
        y.set("hit-off-x", d.hitOffX());
        y.set("hit-off-y", d.hitOffY());
        y.set("hit-off-z", d.hitOffZ());
        y.set("hit-largura", d.hitWidth());
        y.set("hit-altura", d.hitHeight());
        List<Object> acoes = new ArrayList<>();
        for (ActionData a : d.actions()) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("tipo", a.type());
            m.put("valor", a.value());
            m.put("gatilho", a.trigger());
            if (a.permission() != null && !a.permission().isBlank()) m.put("permissao", a.permission());
            acoes.add(m);
        }
        y.set("acoes", acoes);
        y.save(f);
    }
}
