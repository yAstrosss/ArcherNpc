package com.yastro.npc.display;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Method;

public final class ItemModels {
    private ItemModels() {}

    private static final Method IA_GET_INSTANCE = m("dev.lone.itemsadder.api.CustomStack", "getInstance", String.class);
    private static final Method IA_GET_ITEMSTACK = m("dev.lone.itemsadder.api.CustomStack", "getItemStack");
    private static final Method NEXO_FROM_ID = m("com.nexomc.nexo.api.NexoItems", "itemFromId", String.class);

    private static Method m(String cls, String name, Class<?>... args) {
        try { return Class.forName(cls).getMethod(name, args); }
        catch (Throwable t) { return null; }
    }

    public static ItemStack resolve(String ref) {
        if (ref == null || ref.isBlank()) return null;
        String r = ref.trim();
        int sep = r.indexOf(':');
        String ns = sep < 0 ? "minecraft" : r.substring(0, sep).toLowerCase();
        String id = sep < 0 ? r : r.substring(sep + 1);
        return switch (ns) {
            case "itemsadder", "ia" -> itemsAdder(id);
            case "nexo" -> nexo(id);
            default -> vanilla(id);
        };
    }

    private static ItemStack vanilla(String id) {
        Material m = Material.matchMaterial(id);
        return m == null || !m.isItem() ? null : new ItemStack(m);
    }

    private static ItemStack itemsAdder(String id) {
        if (IA_GET_INSTANCE == null || IA_GET_ITEMSTACK == null) return null;
        try {
            Object inst = IA_GET_INSTANCE.invoke(null, id);
            return inst == null ? null : (ItemStack) IA_GET_ITEMSTACK.invoke(inst);
        } catch (Throwable t) {
            return null;
        }
    }

    private static ItemStack nexo(String id) {
        if (NEXO_FROM_ID == null) return null;
        try {
            Object builder = NEXO_FROM_ID.invoke(null, id);
            return builder == null ? null : (ItemStack) builder.getClass().getMethod("build").invoke(builder);
        } catch (Throwable t) {
            return null;
        }
    }
}
