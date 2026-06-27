package com.yastro.npc.action.impl;

import com.yastro.npc.action.NpcAction;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.bukkit.entity.Player;

public record SoundAction(String sound) implements NpcAction {
    @Override
    public void execute(Player p) {
        String key = sound.trim();
        if (key.contains(":")) key = key.toLowerCase();
        else key = "minecraft:" + key.toLowerCase().replace('_', '.');
        try { p.playSound(Sound.sound(Key.key(key), Sound.Source.MASTER, 1f, 1f)); }
        catch (Exception ignored) {}
    }
}
