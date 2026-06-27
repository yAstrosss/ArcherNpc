package com.yastro.npc.skin;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.yastro.npc.model.SkinSource;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public final class SkinResolver {
    private final Logger log;
    private final MojangApi mojang;
    private final Cache<String, SkinData> cache = CacheBuilder.newBuilder()
            .maximumSize(500).expireAfterWrite(6, TimeUnit.HOURS).build();
    private final Map<String, CompletableFuture<SkinData>> inFlight = new ConcurrentHashMap<>();

    private final ExecutorService io = Executors.newFixedThreadPool(3, r -> {
        Thread t = new Thread(r, "archernpc-skin");
        t.setDaemon(true);
        return t;
    });

    public SkinResolver(Logger log, MojangApi mojang) { this.log = log; this.mojang = mojang; }

    public void close() { io.shutdownNow(); }

    public CompletableFuture<SkinData> resolve(SkinSource src) {

        if ("textura".equals(src.type())) {
            int sep = src.value().indexOf('|');
            return CompletableFuture.completedFuture(sep < 0 ? new SkinData(src.value(), "")
                    : new SkinData(src.value().substring(0, sep), src.value().substring(sep + 1)));
        }
        String key = src.type() + ":" + src.value();
        SkinData cached = cache.getIfPresent(key);
        if (cached != null) return CompletableFuture.completedFuture(cached);

        return inFlight.computeIfAbsent(key, k -> CompletableFuture.supplyAsync(() -> {
            try {
                if ("nome".equals(src.type())) return mojang.byName(src.value());
                if ("url".equals(src.type())) return mojang.byUrl(src.value());
                return SkinData.EMPTY;
            } catch (Exception e) {
                log.warning("Falha ao resolver skin " + key + ": " + e.getMessage());
                return SkinData.EMPTY;
            }
        }, io).whenComplete((data, err) -> {
            if (data != null && !data.isEmpty()) cache.put(key, data);
            inFlight.remove(key);
        }));
    }
}
