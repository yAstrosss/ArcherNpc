package com.yastro.npc.interaction;

import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;

class CooldownServiceTest {
    @Test void blocksWithinWindowThenAllows() {
        AtomicLong now = new AtomicLong(1000);
        CooldownService cd = new CooldownService(500, now::get);
        UUID p = UUID.randomUUID();
        assertTrue(cd.tryUse(p, "loja"));
        assertFalse(cd.tryUse(p, "loja"));
        now.addAndGet(600);
        assertTrue(cd.tryUse(p, "loja"));
    }

    @Test void clearRemovesPlayerEntries() {
        AtomicLong now = new AtomicLong(0);
        CooldownService cd = new CooldownService(500, now::get);
        UUID p = UUID.randomUUID();
        assertTrue(cd.tryUse(p, "loja"));
        assertFalse(cd.tryUse(p, "loja"));
        cd.clear(p);
        assertTrue(cd.tryUse(p, "loja"));
    }
}
