package com.yastro.npc.action;

import com.yastro.npc.action.impl.MessageAction;
import com.yastro.npc.action.impl.RunCommandAction;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ActionTypeTest {
    private final ActionType t = new ActionType(null, (player, server) -> {});

    @Test void mapsKnownTokens() {
        assertInstanceOf(RunCommandAction.class, t.create("comando", "spawn"));
        assertInstanceOf(MessageAction.class, t.create("MENSAGEM", "&aoi"));
    }

    @Test void unknownTokenIsNull() { assertNull(t.create("xpto", "x")); }

    @Test void validityCheck() {
        assertTrue(ActionType.isValid("conectar"));
        assertFalse(ActionType.isValid("foo"));
    }
}
