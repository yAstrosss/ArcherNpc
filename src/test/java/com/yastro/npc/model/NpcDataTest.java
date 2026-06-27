package com.yastro.npc.model;

import com.yastro.npc.skin.SkinData;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class NpcDataTest {
    @Test
    void withSkin_replacesOnlyTheSkin() {
        UUID w = UUID.randomUUID();
        NpcData base = new NpcData("loja", w, 1, 2, 3, 90f, 0f,
                new SkinSource("nome", "Notch"), SkinData.EMPTY,
                List.of("&bLOJA"), true, "", List.of(new ActionData("comando", "spawn")),
                false, "white", 1.0, false, "player", 0.0, "normal", java.util.Map.of(),
                "todos", "", false, false, java.util.List.of(), java.util.List.of(), 0.0, 0.0, 0.0, 0.0, 0.0);

        NpcData updated = base.withSkin(new SkinData("v", "s"));

        assertEquals("loja", updated.id());
        assertEquals("Notch", updated.skinSource().value());
        assertFalse(updated.skin().isEmpty());
        assertTrue(base.skin().isEmpty());
    }
}
