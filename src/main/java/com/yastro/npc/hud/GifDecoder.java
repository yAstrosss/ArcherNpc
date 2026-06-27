package com.yastro.npc.hud;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageInputStream;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Node;

public final class GifDecoder {
    private GifDecoder() {}

    private static final int MAX_DIM = 512;

    public record Result(List<BufferedImage> frames, List<Integer> delaysMs) {}

    public static Result decode(File file, int maxFrames) throws IOException {
        java.util.Iterator<ImageReader> it = ImageIO.getImageReadersByFormatName("gif");
        if (!it.hasNext()) throw new IOException("Nenhum ImageReader GIF disponivel (SPI removido?)");
        ImageReader reader = it.next();
        Graphics2D g = null;
        try (ImageInputStream iis = ImageIO.createImageInputStream(file)) {
            reader.setInput(iis, false);
            int n = Math.min(reader.getNumImages(true), Math.max(1, maxFrames));
            List<BufferedImage> frames = new ArrayList<>();
            List<Integer> delays = new ArrayList<>();

            int[] screen = screenSize(reader);
            BufferedImage canvas = null;

            for (int i = 0; i < n; i++) {
                BufferedImage frame = reader.read(i);
                IIOMetadataNode root = (IIOMetadataNode) reader.getImageMetadata(i)
                        .getAsTree("javax_imageio_gif_image_1.0");
                int x = intAttr(child(root, "ImageDescriptor"), "imageLeftPosition", 0);
                int y = intAttr(child(root, "ImageDescriptor"), "imageTopPosition", 0);
                IIOMetadataNode gce = child(root, "GraphicControlExtension");
                int delay = intAttr(gce, "delayTime", 10) * 10;
                if (delay <= 0) delay = 100;
                String disposal = gce != null ? gce.getAttribute("disposalMethod") : "none";

                if (canvas == null) {
                    int cw = screen[0] > 0 ? screen[0] : frame.getWidth();
                    int ch = screen[1] > 0 ? screen[1] : frame.getHeight();
                    int w = Math.max(cw, x + frame.getWidth());
                    int h = Math.max(ch, y + frame.getHeight());
                    if (w > MAX_DIM || h > MAX_DIM)
                        throw new IOException("GIF " + w + "x" + h + " excede o limite "
                                + MAX_DIM + "x" + MAX_DIM + " (reduza a resolução)");
                    canvas = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
                    g = canvas.createGraphics();
                }
                g.drawImage(frame, x, y, null);

                BufferedImage snap = new BufferedImage(canvas.getWidth(), canvas.getHeight(), BufferedImage.TYPE_INT_ARGB);
                Graphics2D sg = snap.createGraphics();
                sg.drawImage(canvas, 0, 0, null);
                sg.dispose();
                frames.add(snap);
                delays.add(delay);

                if ("restoreToBackgroundColor".equals(disposal)) {
                    g.clearRect(x, y, frame.getWidth(), frame.getHeight());
                }

            }
            return new Result(frames, delays);
        } finally {
            if (g != null) g.dispose();
            reader.dispose();
        }
    }

    private static int[] screenSize(ImageReader reader) {
        try {
            IIOMetadata sm = reader.getStreamMetadata();
            if (sm != null) {
                IIOMetadataNode root = (IIOMetadataNode) sm.getAsTree("javax_imageio_gif_stream_1.0");
                IIOMetadataNode lsd = child(root, "LogicalScreenDescriptor");
                return new int[]{intAttr(lsd, "logicalScreenWidth", -1), intAttr(lsd, "logicalScreenHeight", -1)};
            }
        } catch (IOException ignored) {}
        return new int[]{-1, -1};
    }

    private static IIOMetadataNode child(IIOMetadataNode parent, String name) {
        if (parent == null) return null;
        for (Node c = parent.getFirstChild(); c != null; c = c.getNextSibling()) {
            if (c.getNodeName().equalsIgnoreCase(name)) return (IIOMetadataNode) c;
        }
        return null;
    }

    private static int intAttr(IIOMetadataNode node, String attr, int def) {
        if (node == null) return def;
        String v = node.getAttribute(attr);
        if (v == null || v.isEmpty()) return def;
        try { return Integer.parseInt(v); } catch (NumberFormatException e) { return def; }
    }
}
