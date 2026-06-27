package com.yastro.npc.npc;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class PlayerSession {

    public final Set<String> spawned = ConcurrentHashMap.newKeySet();

    public volatile long openedAtMs = System.currentTimeMillis();

    public long ticks;

    public void resync() {
        spawned.clear();
        openedAtMs = System.currentTimeMillis();
    }
}
