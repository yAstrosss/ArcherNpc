package com.yastro.npc.config;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public final class Messages {
    private final YamlConfiguration file;
    private final YamlConfiguration jarDefaults;
    private final String lang;

    private Messages(String lang, YamlConfiguration file, YamlConfiguration jarDefaults) {
        this.lang = lang;
        this.file = file;
        this.jarDefaults = jarDefaults;
    }

    public String lang() { return lang; }

    public static Messages load(Plugin plugin, String langRaw) {
        String lang = "en".equalsIgnoreCase(langRaw == null ? "" : langRaw.trim()) ? "en" : "pt-br";
        for (String f : new String[]{"messages_pt-br.yml", "messages_en.yml"}) {
            if (!new File(plugin.getDataFolder(), f).exists()) {
                try { plugin.saveResource(f, false); } catch (IllegalArgumentException ignored) {}
            }
        }
        String name = "messages_" + lang + ".yml";
        YamlConfiguration onDisk = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), name));
        YamlConfiguration defaults = null;
        try (InputStream in = plugin.getResource(name)) {
            if (in != null) defaults = YamlConfiguration.loadConfiguration(new InputStreamReader(in, StandardCharsets.UTF_8));
        } catch (Exception ignored) {}
        return new Messages(lang, onDisk, defaults);
    }

    public String raw(String key) {
        String v = file.getString(key);
        if (v == null && jarDefaults != null) v = jarDefaults.getString(key);
        return v == null ? key : v;
    }

    public String get(String key, Object... args) {
        String s = raw(key);
        for (int i = 0; i < args.length; i++) s = s.replace("{" + i + "}", String.valueOf(args[i]));
        return s;
    }
}
