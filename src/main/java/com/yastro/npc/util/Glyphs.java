package com.yastro.npc.util;

import org.bukkit.Bukkit;

import java.lang.reflect.Method;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Glyphs {
    private Glyphs() {}

    private static final Pattern NEXO = Pattern.compile("<nexo:([^>]+)>");
    private static final Method IA_REPLACE = method("dev.lone.itemsadder.api.FontImages.FontImageWrapper", "replaceFontImages", String.class);
    private static final Method NEXO_GLYPH_FROM_NAME = method("com.nexomc.nexo.api.NexoGlyphs", "glyphFromName", String.class);

    private static Method method(String cls, String name, Class<?>... args) {
        try { return Class.forName(cls).getMethod(name, args); }
        catch (Throwable t) { return null; }
    }

    public static String apply(String text) {
        if (text == null || text.isEmpty()) return text;
        String out = text;
        if (out.indexOf(':') >= 0 && enabled("ItemsAdder")) out = itemsAdder(out);
        if (out.contains("<nexo:") && enabled("Nexo")) out = nexo(out);
        return out;
    }

    private static boolean enabled(String plugin) {
        try { return Bukkit.getPluginManager().isPluginEnabled(plugin); }
        catch (Throwable t) { return false; }
    }

    private static String itemsAdder(String text) {
        if (IA_REPLACE == null) return text;
        try {
            Object r = IA_REPLACE.invoke(null, text);
            return r == null ? text : (String) r;
        } catch (Throwable t) {
            return text;
        }
    }

    private static String nexo(String text) {
        Matcher m = NEXO.matcher(text);
        StringBuilder sb = new StringBuilder(text.length() + 16);
        while (m.find()) {
            String repl = nexoGlyph(m.group(1).trim());
            m.appendReplacement(sb, Matcher.quoteReplacement(repl != null ? repl : m.group()));
        }
        m.appendTail(sb);
        return sb.toString();
    }

    private static String nexoGlyph(String id) {
        if (NEXO_GLYPH_FROM_NAME == null) return null;
        try {
            Object glyph = NEXO_GLYPH_FROM_NAME.invoke(null, id);
            if (glyph == null) return null;
            String ch = String.valueOf(glyph.getClass().getMethod("getCharacter").invoke(glyph));
            String font = null;
            try {
                Object f = glyph.getClass().getMethod("getFont").invoke(glyph);
                if (f != null) font = String.valueOf(f);
            } catch (Throwable ignored) {  }
            return (font != null && !font.isBlank()) ? "<font:" + font + ">" + ch + "</font>" : ch;
        } catch (Throwable t) {
            return null;
        }
    }
}
