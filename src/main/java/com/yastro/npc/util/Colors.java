package com.yastro.npc.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Colors {
    private Colors() {}

    private static final MiniMessage MM = MiniMessage.miniMessage();
    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacyAmpersand();
    private static final Pattern WAVE = Pattern.compile("<wave(:[^>]*)?>");
    private static final String RAINBOW = "#ff0000:#ff7f00:#ffff00:#00ff00:#00bfff:#0000ff:#8b00ff";

    public static Component render(String text) { return render(text, 0.0); }

    public static Component render(String text, double wavePhase) {
        String shown = applyWave(Glyphs.apply(text), wavePhase);
        try {
            return shown.indexOf('<') >= 0 ? MM.deserialize(shown) : LEGACY.deserialize(shown);
        } catch (Exception ex) {
            return LEGACY.deserialize(text);
        }
    }

    public static boolean isAnimated(String text) { return text != null && text.contains("<wave"); }

    public static String applyWave(String text, double phase) {
        if (text == null || text.indexOf("<wave") < 0) return text;
        String ph = String.format(Locale.ROOT, "%.3f", Math.max(-1.0, Math.min(1.0, phase)));
        Matcher m = WAVE.matcher(text);
        StringBuilder sb = new StringBuilder(text.length() + 32);
        while (m.find()) {
            String colours = m.group(1);
            String c = (colours == null || colours.length() <= 1) ? RAINBOW : colours.substring(1);
            if (c.indexOf(':') < 0) c = c + ":" + c;
            m.appendReplacement(sb, Matcher.quoteReplacement("<gradient:" + c + ":" + ph + ">"));
        }
        m.appendTail(sb);

        return sb.toString().replace("</wave>", "</gradient>");
    }
}
