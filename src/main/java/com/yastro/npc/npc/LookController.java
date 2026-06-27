package com.yastro.npc.npc;

import com.yastro.npc.model.Npc;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class LookController {
    private static final float EPSILON_DEG = 4f;
    private static final double NPC_EYE = 1.62;
    private final double rangeSq;
    private final Map<UUID, Map<String, float[]>> last = new ConcurrentHashMap<>();

    public LookController(double range) { this.rangeSq = range * range; }

    public void tick(Player viewer, Npc npc) {
        if (!npc.data().lookAtPlayer()) return;
        double dx = viewer.getX() - npc.data().x();
        double dz = viewer.getZ() - npc.data().z();
        double flatSq = dx * dx + dz * dz;
        if (flatSq > rangeSq) return;
        double dy = viewer.getEyeLocation().getY() - (npc.data().y() + NPC_EYE);
        double flat = Math.sqrt(flatSq);
        float yaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
        float pitch = (float) -Math.toDegrees(Math.atan2(dy, flat));

        Map<String, float[]> byNpc = last.computeIfAbsent(viewer.getUniqueId(), k -> new ConcurrentHashMap<>());
        float[] prev = byNpc.get(npc.id());
        if (prev != null && Math.abs(prev[0] - yaw) < EPSILON_DEG && Math.abs(prev[1] - pitch) < EPSILON_DEG) return;
        PacketNpc.rotate(viewer, npc, yaw, pitch);
        byNpc.put(npc.id(), new float[]{yaw, pitch});
    }

    public void forget(Player viewer, Npc npc) {
        Map<String, float[]> byNpc = last.get(viewer.getUniqueId());
        if (byNpc != null) byNpc.remove(npc.id());
    }

    public void clear(UUID viewer) { last.remove(viewer); }
}
