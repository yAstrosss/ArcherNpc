package com.yastro.npc.hud;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public final class HudConfig {
    private final Plugin plugin;
    private final File file;

    private final Object lock = new Object();
    private final AtomicInteger saveGen = new AtomicInteger();
    private YamlConfiguration y;

    public HudConfig(Plugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "hud_posicoes.yml");
        reload();
    }

    public void reload() { synchronized (lock) { this.y = YamlConfiguration.loadConfiguration(file); } }

    private static String baseOf(String id) {
        int at = id.indexOf('@');
        return at < 0 ? id : id.substring(0, at);
    }
    private int getInt(String id, String prop, int def) {
        synchronized (lock) {
            if (y.contains(id + "." + prop)) return y.getInt(id + "." + prop);
            String base = baseOf(id);
            if (!base.equals(id) && y.contains(base + "." + prop)) return y.getInt(base + "." + prop);
            return def;
        }
    }
    private String getStr(String id, String prop, String def) {
        synchronized (lock) {
            if (y.contains(id + "." + prop)) return y.getString(id + "." + prop);
            String base = baseOf(id);
            if (!base.equals(id) && y.contains(base + "." + prop)) return y.getString(base + "." + prop);
            return def;
        }
    }

    public int altura(String id, int def)        { return getInt(id, "altura", def); }
    public int ascent(String id, int def)        { return getInt(id, "ascent", def); }
    public int x(String id, int def)             { return getInt(id, "x", def); }
    public String exibir(String id, String def)  { return getStr(id, "exibir", def); }

    public void setInt(String id, String prop, int v)     { synchronized (lock) { y.set(id + "." + prop, v); } scheduleSave(); }
    public void setString(String id, String prop, String v) { synchronized (lock) { y.set(id + "." + prop, v); } scheduleSave(); }

    private void scheduleSave() {
        final int my = saveGen.incrementAndGet();
        plugin.getServer().getAsyncScheduler().runDelayed(plugin, t -> {
            if (my != saveGen.get()) return;
            writeNow();
        }, 1, TimeUnit.SECONDS);
    }

    public void flushNow() { saveGen.incrementAndGet(); writeNow(); }

    private void writeNow() {
        String data;
        synchronized (lock) { data = y.saveToString(); }
        try { Files.writeString(file.toPath(), data, StandardCharsets.UTF_8); }
        catch (Exception ignored) {}
    }
}
