package com.yastro.npc.npc;

import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import java.util.ArrayList;
import java.util.List;

public final class PlayerMeta {
    private PlayerMeta() {}

    public static final int SKIN_LAYERS_INDEX = 16;
    public static final byte ALL_LAYERS = 0x7F;

    public static final int ENTITY_FLAGS_INDEX = 0;
    public static final byte FLAG_GLOWING = 0x40;

    public static List<EntityData<?>> baseMeta(boolean glow) {
        List<EntityData<?>> meta = new ArrayList<>();
        meta.add(new EntityData<>(ENTITY_FLAGS_INDEX, EntityDataTypes.BYTE, (byte) (glow ? FLAG_GLOWING : 0)));
        meta.add(new EntityData<>(SKIN_LAYERS_INDEX, EntityDataTypes.BYTE, ALL_LAYERS));
        return meta;
    }
}
