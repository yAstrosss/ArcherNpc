package com.yastro.npc.gui;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public final class GuiHolder implements InventoryHolder {
    public enum Type { EDITOR, ACOES, ITENS, HUD, HUDPICK, APARENCIA, ENTIDADES, BLOCOS, NOME, HITBOX }

    private final Type type;
    private final String npcId;
    private final int page;
    private Inventory inventory;

    public GuiHolder(Type type, String npcId) { this(type, npcId, 0); }
    public GuiHolder(Type type, String npcId, int page) { this.type = type; this.npcId = npcId; this.page = page; }

    public Type type() { return type; }
    public String npcId() { return npcId; }
    public int page() { return page; }
    void attach(Inventory inv) { this.inventory = inv; }

    @Override public Inventory getInventory() { return inventory; }
}
