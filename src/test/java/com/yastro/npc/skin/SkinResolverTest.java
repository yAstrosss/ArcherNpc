package com.yastro.npc.skin;

import com.yastro.npc.model.SkinSource;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

class SkinResolverTest {

    private static SkinResolver resolver(MojangApi api) {
        return new SkinResolver(Logger.getLogger("test"), api);
    }

    @Test
    void cachesAndCoalesces() throws Exception {
        AtomicInteger calls = new AtomicInteger();
        MojangApi fake = name -> { calls.incrementAndGet(); Thread.sleep(20); return new SkinData("v", "s"); };
        SkinResolver r = resolver(fake);

        var a = r.resolve(new SkinSource("nome", "Notch"));
        var b = r.resolve(new SkinSource("nome", "Notch"));
        assertEquals(new SkinData("v", "s"), a.get());
        assertEquals(new SkinData("v", "s"), b.get());

        r.resolve(new SkinSource("nome", "Notch")).get();
        assertEquals(1, calls.get(), "one HTTP call for 3 resolves of the same name");
    }

    @Test
    void rawTextureNeedsNoFetch() throws Exception {
        AtomicInteger calls = new AtomicInteger();
        MojangApi fake = name -> { calls.incrementAndGet(); return SkinData.EMPTY; };
        SkinResolver r = resolver(fake);
        assertEquals(new SkinData("abc", "sig"), r.resolve(new SkinSource("textura", "abc|sig")).get());
        assertEquals(0, calls.get());
    }
}
