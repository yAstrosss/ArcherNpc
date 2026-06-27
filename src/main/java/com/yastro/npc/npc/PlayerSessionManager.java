package com.yastro.npc.npc;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class PlayerSessionManager {
    private final Map<UUID, PlayerSession> sessions = new ConcurrentHashMap<>();
    public PlayerSession open(UUID player) { return sessions.computeIfAbsent(player, k -> new PlayerSession()); }
    public PlayerSession get(UUID player) { return sessions.get(player); }
    public void close(UUID player) { sessions.remove(player); }
}
