package com.yastro.npc.hud;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public final class HudPackGenerator {
    public static final String NAMESPACE = "yastronpc";
    public static final String FONT = "hud";
    private static final int PACK_FORMAT = 64;
    private static final int SUP_MIN = 1;
    private static final int SUP_MAX = 99;
    private static final int GLYPH_CP = 0xE000;
    private static final int SPACE_CP = 0xF000;
    private static final int MAX_TEX = 256;
    private static final int TILE_TEX = 256;
    private static final int MAX_FRAMES = 120;

    public record Result(File zip, byte[] sha1, Map<String, HudImage> images, Map<String, HudAnim> anims) {}

    private final int gHeight;
    private final int gAscent;
    private final int hdTile;
    private final int gX;
    private final HudConfig cfg;
    private final String packName;
    private final String packDesc;
    private final File icon;
    private final Consumer<String> log;

    private ZipOutputStream zip;
    private StringBuilder providers;
    private int cp;
    private int spcp;
    private final Map<Integer, String> spaceByAdvance = new LinkedHashMap<>();
    private final Map<String, Integer> spaceProvider = new LinkedHashMap<>();

    public HudPackGenerator(int gHeight, int gAscent, int hdTile, int gX, HudConfig cfg,
                            String packName, String packDesc, File icon, Consumer<String> log) {
        this.gHeight = gHeight;
        this.gAscent = gAscent;
        this.hdTile = Math.max(1, hdTile);
        this.gX = gX;
        this.cfg = cfg;
        this.packName = packName == null ? "" : packName;
        this.packDesc = packDesc == null ? "" : packDesc;
        this.icon = icon;
        this.log = log != null ? log : s -> {};
    }

    private int hOf(String id) { return cfg != null ? cfg.altura(id, gHeight) : gHeight; }
    private int ascOf(String id) { return cfg != null ? cfg.ascent(id, gAscent) : gAscent; }
    private int xOf(String id) { return cfg != null ? cfg.x(id, gX) : gX; }
    private String spaceFor(int adv) { return adv != 0 ? space(adv) : ""; }

    private static String baseOf(String id) { int at = id.indexOf('@'); return at < 0 ? id : id.substring(0, at); }

    public Result build(File imagesDir, File packZip, java.util.Set<String> variantIds) throws IOException {
        Map<String, HudImage> images = new LinkedHashMap<>();
        Map<String, HudAnim> anims = new LinkedHashMap<>();
        File[] flat = list(imagesDir, false);
        File[] hd = list(new File(imagesDir, "hd"), true);
        Map<String, File> flatSrc = new LinkedHashMap<>();
        for (File f : flat) flatSrc.put(baseName(f), f);
        Map<String, File> hdSrc = new LinkedHashMap<>();
        for (File f : hd) hdSrc.put(baseName(f), f);

        this.providers = new StringBuilder();
        this.cp = GLYPH_CP;
        this.spcp = SPACE_CP;
        spaceByAdvance.clear();
        spaceProvider.clear();

        try (ZipOutputStream z = new ZipOutputStream(new FileOutputStream(packZip))) {
            this.zip = z;
            put("pack.mcmeta", mcmeta().getBytes(StandardCharsets.UTF_8));
            writeIcon();

            for (File f : flat) bakeOne(images, anims, baseName(f), f);
            for (File f : hd) { HudImage img = addHd(f, baseName(f)); if (img != null) images.put(img.id(), img); }

            if (variantIds != null) for (String vid : variantIds) {
                if (images.containsKey(vid) || anims.containsKey(vid)) continue;
                String base = baseOf(vid);
                if (flatSrc.containsKey(base)) bakeOne(images, anims, vid, flatSrc.get(base));
                else if (hdSrc.containsKey(base)) { HudImage img = addHd(hdSrc.get(base), vid); if (img != null) images.put(vid, img); }
            }

            put("assets/" + NAMESPACE + "/font/" + FONT + ".json",
                    ("{\"providers\":[" + spaceProviderJson() + providers + "]}").getBytes(StandardCharsets.UTF_8));
        } finally {
            this.zip = null;
        }
        return new Result(packZip, sha1(packZip), images, anims);
    }

    private void bakeOne(Map<String, HudImage> images, Map<String, HudAnim> anims, String id, File f) throws IOException {
        if (f.getName().toLowerCase().endsWith(".gif")) {
            HudAnim a = addGif(f, id);
            if (a != null) anims.put(id, a);
        } else {
            byte[] png = encode(f, id);
            if (png != null) {
                int h = hOf(id), asc = ascOf(id);
                images.put(id, new HudImage(id, addGlyph(id, png, h, asc), h, asc, spaceFor(xOf(id))));
            }
        }
    }

    private String mcmeta() {
        return "{\"pack\":{\"description\":\"" + jsonEscape(description()) + "\""
                + ",\"pack_format\":" + PACK_FORMAT
                + ",\"supported_formats\":[" + SUP_MIN + "," + SUP_MAX + "]"
                + ",\"min_format\":[" + SUP_MIN + ",0],\"max_format\":[" + SUP_MAX + "," + SUP_MAX + "]}}";
    }

    private String description() {
        String name = color(packName.trim());
        String body = color(packDesc.trim());
        if (name.isEmpty() && body.isEmpty()) return "ArcherNpc HUD";
        if (body.isEmpty()) return name;
        if (name.isEmpty()) return body;
        return name + "\n" + body;
    }

    private static String color(String s) { return s.replace("\\n", "\n").replace('&', '§'); }

    private static String jsonEscape(String s) {
        StringBuilder sb = new StringBuilder(s.length() + 16);
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> { if (c < 0x20) sb.append(String.format("\\u%04x", (int) c)); else sb.append(c); }
            }
        }
        return sb.toString();
    }

    private void writeIcon() {
        if (icon == null || !icon.isFile()) return;
        try {
            BufferedImage src = ImageIO.read(icon);
            if (src == null) { log.accept("[HUD] logo " + icon.getName() + ": ImageIO não leu (PNG válido?)"); return; }
            int max = Math.max(src.getWidth(), src.getHeight());
            int side = Math.min(MAX_TEX, max);
            double s = (double) side / max;
            int nw = Math.max(1, (int) Math.round(src.getWidth() * s));
            int nh = Math.max(1, (int) Math.round(src.getHeight() * s));
            put("pack.png", draw(src, side, side, (side - nw) / 2, (side - nh) / 2, nw, nh));
            log.accept("[HUD] logo do pack: " + icon.getName() + " (" + side + "x" + side + ")");
        } catch (Exception e) {
            log.accept("[HUD] logo " + icon.getName() + ": falha (" + e.getMessage() + "), pack sem ícone.");
        }
    }

    private HudImage addHd(File f, String id) {
        try {
            BufferedImage src = ImageIO.read(f);
            if (src == null) { log.accept("[HUD] hd " + id + ": ImageIO não leu"); return null; }
            int w = src.getWidth(), h = src.getHeight();
            int cols = Math.max(1, (int) Math.ceil(w / (double) TILE_TEX));
            int rows = Math.max(1, (int) Math.ceil(h / (double) TILE_TEX));
            int H = hdTile;

            int half = cols * H / 2;
            StringBuilder render = new StringBuilder();
            for (int r = 0; r < rows; r++) {
                int y0 = r * h / rows, y1 = (r + 1) * h / rows;
                int rowAscent = H - r * H;
                render.append(space(-half));
                for (int c = 0; c < cols; c++) {
                    int x0 = c * w / cols, x1 = (c + 1) * w / cols;
                    BufferedImage cell = src.getSubimage(x0, y0, x1 - x0, y1 - y0);
                    byte[] png = encodeExact(cell, TILE_TEX);
                    String g = addGlyph(id + "_" + r + "_" + c, png, H, rowAscent);
                    render.append(g).append(space(-1));
                }
                render.append(space(-half));
            }
            log.accept("[HUD] hd " + id + ": grid " + cols + "x" + rows + " tiles, tile=" + H + "px");
            return new HudImage(id, render.toString(), H, H, spaceFor(xOf(id)));
        } catch (Exception e) {
            log.accept("[HUD] hd " + id + ": falha (" + e.getMessage() + ")");
            return null;
        }
    }

    private HudAnim addGif(File f, String id) {
        try {
            GifDecoder.Result gif = GifDecoder.decode(f, MAX_FRAMES);
            List<BufferedImage> fr = gif.frames();
            int k = fr.size();
            if (k == 0) return null;
            String[] glyphs = new String[k];
            int[] delays = new int[k];
            int h = hOf(id), asc = ascOf(id);
            for (int j = 0; j < k; j++) {
                glyphs[j] = addGlyph(id + "_" + j, encodeImage(fr.get(j)), h, asc);
                delays[j] = gif.delaysMs().get(j);
            }
            log.accept("[HUD] gif " + id + ": " + k + " frames");
            return new HudAnim(id, glyphs, delays, spaceFor(xOf(id)));
        } catch (Exception e) {
            log.accept("[HUD] gif " + id + ": falha (" + e.getMessage() + ")");
            return null;
        }
    }

    private String addGlyph(String texName, byte[] png, int h, int asc) throws IOException {
        String glyph = new String(Character.toChars(cp));
        String escaped = String.format("\\u%04X", cp);
        put("assets/" + NAMESPACE + "/textures/font/" + texName + ".png", png);
        if (providers.length() > 0) providers.append(',');
        providers.append("{\"type\":\"bitmap\",\"file\":\"").append(NAMESPACE).append(":font/").append(texName)
                .append(".png\",\"ascent\":").append(asc).append(",\"height\":").append(h)
                .append(",\"chars\":[\"").append(escaped).append("\"]}");
        cp++;
        return glyph;
    }

    private String space(int advance) {
        return spaceByAdvance.computeIfAbsent(advance, adv -> {
            String ch = new String(Character.toChars(spcp));
            spaceProvider.put(String.format("\\u%04X", spcp), adv);
            spcp++;
            return ch;
        });
    }

    private String spaceProviderJson() {
        if (spaceProvider.isEmpty()) return "";
        StringBuilder sb = new StringBuilder("{\"type\":\"space\",\"advances\":{");
        boolean first = true;
        for (Map.Entry<String, Integer> e : spaceProvider.entrySet()) {
            if (!first) sb.append(',');
            sb.append('"').append(e.getKey()).append("\":").append(e.getValue());
            first = false;
        }
        return sb.append("}},").toString();
    }

    private byte[] encode(File f, String id) {
        try {
            BufferedImage src = ImageIO.read(f);
            if (src == null) { log.accept("[HUD] " + id + ": ImageIO não leu, copiando cru."); return Files.readAllBytes(f.toPath()); }
            log.accept("[HUD] " + id + ": " + src.getWidth() + "x" + src.getHeight() + " -> RGBA PoT<=256");
            return encodeImage(src);
        } catch (Exception e) {
            log.accept("[HUD] " + id + ": falha (" + e.getMessage() + "), copiando cru.");
            try { return Files.readAllBytes(f.toPath()); } catch (IOException io) { return null; }
        }
    }

    private byte[] encodeImage(BufferedImage src) throws IOException {
        int w = src.getWidth(), h = src.getHeight();
        double s = Math.min(1.0, (double) MAX_TEX / Math.max(w, h));
        int nw = Math.max(1, (int) Math.round(w * s)), nh = Math.max(1, (int) Math.round(h * s));
        int pw = pot(nw), ph = pot(nh);
        return draw(src, pw, ph, (pw - nw) / 2, (ph - nh) / 2, nw, nh);
    }

    private byte[] encodeExact(BufferedImage src, int size) throws IOException {
        return draw(src, size, size, 0, 0, size, size);
    }

    private byte[] draw(BufferedImage src, int cw, int ch, int dx, int dy, int dw, int dh) throws IOException {
        BufferedImage out = new BufferedImage(cw, ch, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = out.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(src, dx, dy, dw, dh, null);
        g.dispose();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ImageIO.write(out, "png", bos);
        return bos.toByteArray();
    }

    private static File[] list(File dir, boolean pngOnly) {
        File[] fs = dir.listFiles((d, n) -> {
            String s = n.toLowerCase();
            return s.endsWith(".png") || (!pngOnly && s.endsWith(".gif"));
        });
        if (fs == null) return new File[0];
        Arrays.sort(fs, (a, b) -> a.getName().compareToIgnoreCase(b.getName()));
        return fs;
    }

    private static String baseName(File f) {
        String n = f.getName();
        return n.substring(0, n.lastIndexOf('.')).toLowerCase();
    }

    private static int pot(int n) { return n <= 1 ? 1 : Integer.highestOneBit(n - 1) << 1; }

    private void put(String path, byte[] data) throws IOException {
        zip.putNextEntry(new ZipEntry(path));
        zip.write(data);
        zip.closeEntry();
    }

    private static byte[] sha1(File f) throws IOException {
        try {
            return MessageDigest.getInstance("SHA-1").digest(Files.readAllBytes(f.toPath()));
        } catch (Exception e) {
            throw new IOException("SHA-1 falhou: " + e.getMessage(), e);
        }
    }
}
