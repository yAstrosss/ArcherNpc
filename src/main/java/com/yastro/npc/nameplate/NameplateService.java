package com.yastro.npc.nameplate;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.util.Vector3f;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDestroyEntities;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnEntity;
import com.yastro.npc.model.Npc;
import com.yastro.npc.util.Colors;
import io.github.retrooper.packetevents.util.SpigotReflectionUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class NameplateService {
    private final Placeholders placeholders;
    private final Map<UUID, Map<String, int[]>> ids = new ConcurrentHashMap<>();
    private final Map<UUID, Map<String, String[]>> lastText = new ConcurrentHashMap<>();

    public NameplateService(Placeholders placeholders) { this.placeholders = placeholders; }

    private String resolve(Player viewer, String line) {
        try { return placeholders.apply(viewer, line); }
        catch (Exception e) { return line; }
    }

    public void spawnLines(Player viewer, Npc npc) {
        List<String> lines = npc.data().nameLines();
        if (lines.isEmpty()) return;
        int n = lines.size();
        int[] entityIds = new int[n];

        double[] ys = new double[n];
        double cur = npc.data().y() + 2.05 + npc.data().nameOffset();
        for (int i = n - 1; i >= 0; i--) { ys[i] = cur; cur += 0.27 * Math.max(0.25, scaleOf(npc, i)); }
        for (int i = 0; i < n; i++) {
            int eid = SpigotReflectionUtil.generateEntityId();
            entityIds[i] = eid;
            send(viewer, new WrapperPlayServerSpawnEntity(eid, Optional.of(UUID.randomUUID()),
                    EntityTypes.TEXT_DISPLAY, new Vector3d(npc.data().x(), ys[i], npc.data().z()),
                    0f, 0f, 0f, 0, Optional.empty()));
            sendText(viewer, eid, resolve(viewer, lines.get(i)), scaleOf(npc, i), 0.0);
        }
        ids.computeIfAbsent(viewer.getUniqueId(), k -> new ConcurrentHashMap<>()).put(npc.id(), entityIds);
    }

    private static double scaleOf(Npc npc, int i) {
        List<Double> s = npc.data().nameScales();
        return (s != null && i < s.size() && s.get(i) != null && s.get(i) > 0) ? s.get(i) : 1.0;
    }

    public void despawnLines(Player viewer, Npc npc) {
        Map<String, int[]> byNpc = ids.get(viewer.getUniqueId());
        if (byNpc != null) {
            int[] eids = byNpc.remove(npc.id());
            if (eids != null) send(viewer, new WrapperPlayServerDestroyEntities(eids));
        }
        Map<String, String[]> lt = lastText.get(viewer.getUniqueId());
        if (lt != null) lt.remove(npc.id());
    }

    public void refresh(Player viewer, Npc npc) {
        Map<String, int[]> byNpc = ids.get(viewer.getUniqueId());
        if (byNpc == null) return;
        int[] eids = byNpc.get(npc.id());
        if (eids == null) return;
        List<String> lines = npc.data().nameLines();
        String[] prev = lastText.computeIfAbsent(viewer.getUniqueId(), k -> new ConcurrentHashMap<>())
                .computeIfAbsent(npc.id(), k -> new String[lines.size()]);
        for (int i = 0; i < eids.length && i < lines.size() && i < prev.length; i++) {
            String resolved = resolve(viewer, lines.get(i));
            if (!resolved.equals(prev[i])) {
                sendText(viewer, eids[i], resolved, scaleOf(npc, i), 0.0);
                prev[i] = resolved;
            }
        }
    }

    public void clear(UUID viewer) { ids.remove(viewer); lastText.remove(viewer); }

    public void animate(Player viewer, Npc npc, double phase) {
        Map<String, int[]> byNpc = ids.get(viewer.getUniqueId());
        if (byNpc == null) return;
        int[] eids = byNpc.get(npc.id());
        if (eids == null) return;
        List<String> lines = npc.data().nameLines();
        for (int i = 0; i < eids.length && i < lines.size(); i++) {
            if (Colors.isAnimated(lines.get(i))) sendText(viewer, eids[i], resolve(viewer, lines.get(i)), scaleOf(npc, i), phase);
        }
    }

    private void sendText(Player viewer, int entityId, String text, double scale, double phase) {
        Component comp = Colors.render(text, phase);
        List<EntityData<?>> meta = new ArrayList<>();
        meta.add(new EntityData<>(DisplayMeta.BILLBOARD_INDEX, EntityDataTypes.BYTE, (byte) 3));
        if (scale != 1.0) {
            float s = (float) scale;
            meta.add(new EntityData<>(DisplayMeta.SCALE_INDEX, EntityDataTypes.VECTOR3F, new Vector3f(s, s, s)));
        }
        meta.add(new EntityData<>(DisplayMeta.TEXT_INDEX, EntityDataTypes.ADV_COMPONENT, comp));
        send(viewer, new WrapperPlayServerEntityMetadata(entityId, meta));
    }

    private void send(Player viewer, PacketWrapper<?> w) {
        PacketEvents.getAPI().getPlayerManager().sendPacket(viewer, w);
    }
}
