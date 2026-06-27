package com.yastro.npc.config;

import com.yastro.npc.model.*;
import com.yastro.npc.skin.SkinData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class YamlNpcStorageTest {
    @Test
    void writeThenRead_roundTrips(@TempDir Path tmp) throws Exception {
        UUID w = UUID.randomUUID();
        NpcData in = new NpcData("loja", w, 1.5, 64, -8.5, 90f, 0f,
                new SkinSource("nome", "Notch"), new SkinData("val", "sig"),
                List.of("&bLOJA", "&7clique"), true, "nexo:espada",
                List.of(new ActionData("comando", "loja"), new ActionData("mensagem", "&aoi")),
                true, "aqua", 1.5, true, "item", 0.5,
                "sentado", java.util.Map.of("capacete", "minecraft:DIAMOND_HELMET"),
                "permissao", "yastro.vip", true, true,
                List.of("11111111-1111-1111-1111-111111111111", "22222222-2222-2222-2222-222222222222"),
                List.of(1.0, 1.5), 0.3, -0.5, 0.0, 1.2, 2.0);
        File f = new File(tmp.toFile(), "loja.yml");

        NpcYaml.write(in, f);
        NpcData out = NpcYaml.read(f);

        assertEquals(in, out);
    }
}
