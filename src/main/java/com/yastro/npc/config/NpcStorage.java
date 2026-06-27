package com.yastro.npc.config;

import com.yastro.npc.model.NpcData;
import java.util.List;

public interface NpcStorage {
    List<NpcData> loadAll();
    void saveAsync(NpcData data);
    void deleteAsync(String id);
    default void flushAll() {}
}
