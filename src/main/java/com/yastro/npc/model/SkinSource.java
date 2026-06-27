package com.yastro.npc.model;

public record SkinSource(String type, String value) {
    public static final SkinSource NONE = new SkinSource("nome", "");
}
