package com.yastro.npc.npc;

import com.yastro.npc.model.Npc;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class NpcRegistry {
    private final Map<String, Npc> byId = new ConcurrentHashMap<>();
    private final Map<Integer, Npc> byEntityId = new ConcurrentHashMap<>();

    public void add(Npc npc) {
        byId.put(npc.id(), npc);
        byEntityId.put(npc.entityId(), npc);
        byEntityId.put(npc.interactionId(), npc);
    }
    public Npc remove(String id) {
        Npc n = byId.remove(id);
        if (n != null) { byEntityId.remove(n.entityId()); byEntityId.remove(n.interactionId()); }
        return n;
    }
    public Npc byId(String id) { return byId.get(id); }
    public Npc byEntityId(int entityId) { return byEntityId.get(entityId); }
    public Collection<Npc> all() { return byId.values(); }
    public boolean exists(String id) { return byId.containsKey(id); }
}
