package com.yastro.npc.skin;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public interface MojangApi {

    SkinData byName(String name) throws Exception;

    default SkinData byUrl(String url) throws Exception { return SkinData.EMPTY; }

    final class Real implements MojangApi {
        private static final String DEFAULT_MINESKIN = "https://api.mineskin.org/generate/url";
        private final HttpClient http = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5)).build();
        private final String mineskinUrl;
        private final String mineskinKey;

        public Real() { this("", ""); }

        public Real(String mineskinKey, String mineskinUrl) {
            this.mineskinKey = mineskinKey == null ? "" : mineskinKey.trim();
            this.mineskinUrl = (mineskinUrl == null || mineskinUrl.isBlank()) ? DEFAULT_MINESKIN : mineskinUrl.trim();
        }

        @Override
        public SkinData byUrl(String url) throws Exception {
            if (url == null || url.isBlank()) return SkinData.EMPTY;
            String body = "{\"url\":\"" + url.replace("\\", "\\\\").replace("\"", "\\\"") + "\"}";
            HttpRequest.Builder b = HttpRequest.newBuilder(URI.create(mineskinUrl))
                    .timeout(Duration.ofSeconds(40))
                    .header("Content-Type", "application/json")
                    .header("User-Agent", "ArcherNpc")
                    .POST(HttpRequest.BodyPublishers.ofString(body));
            if (!mineskinKey.isBlank()) b.header("Authorization", "Bearer " + mineskinKey);
            HttpResponse<String> r = http.send(b.build(), HttpResponse.BodyHandlers.ofString());
            if (r.statusCode() / 100 != 2 || r.body() == null || r.body().isBlank()) {
                throw new IllegalStateException("MineSkin HTTP " + r.statusCode()
                        + (r.statusCode() == 429 ? " (limite de taxa, tente uma API key em skin-mineskin-key)" : ""));
            }

            SkinData found = findTexture(JsonParser.parseString(r.body()));
            return found == null ? SkinData.EMPTY : found;
        }

        private static SkinData findTexture(JsonElement el) {
            if (el == null) return null;
            if (el.isJsonObject()) {
                JsonObject o = el.getAsJsonObject();
                if (o.has("value") && o.has("signature")
                        && o.get("value").isJsonPrimitive() && o.get("signature").isJsonPrimitive()) {
                    return new SkinData(o.get("value").getAsString(), o.get("signature").getAsString());
                }
                for (var e : o.entrySet()) { SkinData s = findTexture(e.getValue()); if (s != null) return s; }
            } else if (el.isJsonArray()) {
                for (JsonElement e : el.getAsJsonArray()) { SkinData s = findTexture(e); if (s != null) return s; }
            }
            return null;
        }

        @Override
        public SkinData byName(String name) throws Exception {
            String enc = java.net.URLEncoder.encode(name, java.nio.charset.StandardCharsets.UTF_8);
            String idJson = get("https://api.mojang.com/users/profiles/minecraft/" + enc);
            if (idJson.isBlank()) return SkinData.EMPTY;
            JsonObject idObj = JsonParser.parseString(idJson).getAsJsonObject();
            if (!idObj.has("id")) return SkinData.EMPTY;
            String uuid = idObj.get("id").getAsString();
            String profile = get("https://sessionserver.mojang.com/session/minecraft/profile/"
                    + uuid + "?unsigned=false");
            if (profile.isBlank()) return SkinData.EMPTY;
            JsonObject prof = JsonParser.parseString(profile).getAsJsonObject();
            if (!prof.has("properties") || !prof.get("properties").isJsonArray()) return SkinData.EMPTY;
            for (var el : prof.getAsJsonArray("properties")) {
                JsonObject p = el.getAsJsonObject();
                if (p.has("name") && "textures".equals(p.get("name").getAsString())
                        && p.has("value") && p.has("signature")) {
                    return new SkinData(p.get("value").getAsString(), p.get("signature").getAsString());
                }
            }
            return SkinData.EMPTY;
        }

        private String get(String url) throws Exception {
            HttpResponse<String> r = http.send(
                    HttpRequest.newBuilder(URI.create(url)).timeout(Duration.ofSeconds(5)).GET().build(),
                    HttpResponse.BodyHandlers.ofString());
            return r.statusCode() == 200 ? r.body() : "";
        }
    }
}
