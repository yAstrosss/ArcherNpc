package com.yastro.npc.hud;

public record HudAnim(String id, String[] glyphs, int[] delaysMs, String xSpace) {
    public int frameCount() { return glyphs.length; }
}
