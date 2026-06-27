package com.yastro.npc.nameplate;

import org.bukkit.entity.Player;

public interface Placeholders {
    String apply(Player player, String text);

    static Placeholders create(boolean papiPresent) {
        return papiPresent ? new Papi() : (p, t) -> t;
    }

    final class Papi implements Placeholders {
        @Override public String apply(Player player, String text) {
            return me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player, text);
        }
    }
}
