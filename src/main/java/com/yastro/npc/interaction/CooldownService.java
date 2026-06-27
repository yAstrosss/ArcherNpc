package com.yastro.npc.interaction;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.LongSupplier;

public final class CooldownService {
    private static final long DEBOUNCE_MS = 200;
    private final Map<String, Long> until = new ConcurrentHashMap<>();
    private final long cooldownMs;
    private final LongSupplier clock;

    public CooldownService(long cooldownMs, LongSupplier clock) {
        this.cooldownMs = cooldownMs;
        this.clock = clock;
    }

    public boolean tryUse(UUID player, String npcId) {
        String key = player + "|" + npcId;
        long now = clock.getAsLong();
        Long block = until.get(key);
        if (block != null && now < block) return false;
        until.put(key, now + Math.max(cooldownMs, DEBOUNCE_MS));
        return true;
    }

    public void clear(UUID player) { until.keySet().removeIf(k -> k.startsWith(player + "|")); }
}
