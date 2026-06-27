package com.yastro.npc.hud;

import com.sun.net.httpserver.HttpServer;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public final class HudPackServer {
    private HttpServer server;
    private ExecutorService pool;
    private volatile byte[] bytes = new byte[0];

    public void start(int port, File zip) throws IOException {
        this.bytes = Files.readAllBytes(zip.toPath());
        if (server != null) return;
        server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/pack.zip", exchange -> {
            try {
                byte[] b = bytes;
                exchange.getResponseHeaders().set("Content-Type", "application/zip");
                exchange.sendResponseHeaders(200, b.length);
                try (var os = exchange.getResponseBody()) { os.write(b); }
            } finally {
                exchange.close();
            }
        });

        pool = Executors.newFixedThreadPool(4, daemonFactory());
        server.setExecutor(pool);
        server.start();
    }

    public void stop() {
        if (server != null) { server.stop(0); server = null; }
        if (pool != null) { pool.shutdownNow(); pool = null; }
    }

    private static ThreadFactory daemonFactory() {
        AtomicInteger n = new AtomicInteger();
        return r -> {
            Thread th = new Thread(r, "yastronpc-hudpack-" + n.incrementAndGet());
            th.setDaemon(true);
            return th;
        };
    }
}
