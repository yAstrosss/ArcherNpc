package com.yastro.npc.model;

import io.github.retrooper.packetevents.util.SpigotReflectionUtil;
import java.util.UUID;

public final class Npc {
    private final int entityId;
    private final int interactionId;
    private final UUID profileId;
    private volatile NpcData data;

    public Npc(NpcData data) {
        this.entityId = SpigotReflectionUtil.generateEntityId();
        this.interactionId = SpigotReflectionUtil.generateEntityId();
        this.profileId = UUID.randomUUID();
        this.data = data;
    }

    public int entityId() { return entityId; }
    public int interactionId() { return interactionId; }
    public UUID profileId() { return profileId; }
    public NpcData data() { return data; }
    public void setData(NpcData data) { this.data = data; }
    public String id() { return data.id(); }
}
