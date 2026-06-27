package com.yastro.npc.config;

import com.yastro.npc.model.NpcData;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public final class YamlNpcStorage implements NpcStorage {

    private final Plugin plugin;
    private final File dir;

    private final ConcurrentHashMap<String, NpcData> pending = new ConcurrentHashMap<>();

    private final ConcurrentHashMap<String, Object> locks = new ConcurrentHashMap<>();

    public YamlNpcStorage(Plugin plugin) {
        this.plugin = plugin;
        this.dir = new File(plugin.getDataFolder(), "npcs");
        if (!dir.exists() && !dir.mkdirs()) {
            plugin.getLogger().warning("Não consegui criar a pasta npcs/");
        }
    }

    @Override
    public List<NpcData> loadAll() {
        List<NpcData> out = new ArrayList<>();
        File[] files = dir.listFiles((d, n) -> n.endsWith(".yml"));
        if (files == null) return out;
        for (File f : files) {
            try {
                out.add(NpcYaml.read(f));
            } catch (Exception e) {
                plugin.getLogger().warning("NPC inválido em " + f.getName() + ": " + e.getMessage());
            }
        }
        return out;
    }

    @Override
    public void saveAsync(NpcData d) {
        pending.put(d.id(), d);
        plugin.getServer().getAsyncScheduler().runNow(plugin, t -> flush(d.id()));
    }

    private void flush(String id) {
        synchronized (locks.computeIfAbsent(id, k -> new Object())) {
            NpcData d = pending.remove(id);
            if (d == null) return;
            try { NpcYaml.write(d, new File(dir, id + ".yml")); }
            catch (Exception e) { plugin.getLogger().warning("Falha ao salvar NPC " + id + ": " + e.getMessage()); }
        }
    }

    @Override
    public void flushAll() {
        for (String id : new ArrayList<>(pending.keySet())) flush(id);
    }

    @Override
    public void deleteAsync(String id) {
        plugin.getServer().getAsyncScheduler().runNow(plugin, t -> {
            synchronized (locks.computeIfAbsent(id, k -> new Object())) {
                pending.remove(id);
                File f = new File(dir, id + ".yml");
                if (f.exists() && !f.delete()) plugin.getLogger().warning("Não consegui deletar " + f.getName());
            }
        });
    }
}
