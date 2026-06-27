package com.yastro.npc.hud;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Duration;

public final class HudPackUploader {
    private HudPackUploader() {}

    public static String upload(File zip, String sha1Hex) throws IOException, InterruptedException {
        String boundary = "yastronpc" + Long.toHexString(System.identityHashCode(zip)) + zip.length();
        byte[] file = Files.readAllBytes(zip.toPath());
        String pre = "--" + boundary + "\r\n"
                + "Content-Disposition: form-data; name=\"file\"; filename=\"pack.zip\"\r\n"
                + "Content-Type: application/zip\r\n\r\n";
        String post = "\r\n--" + boundary + "--\r\n";

        ByteArrayOutputStream body = new ByteArrayOutputStream();
        body.write(pre.getBytes(StandardCharsets.UTF_8));
        body.write(file);
        body.write(post.getBytes(StandardCharsets.UTF_8));

        HttpRequest req = HttpRequest.newBuilder(URI.create("https://mc-packs.net/"))
                .timeout(Duration.ofSeconds(30))
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .header("User-Agent", "ArcherNpc")
                .POST(HttpRequest.BodyPublishers.ofByteArray(body.toByteArray()))
                .build();

        String downloadUrl = "https://download.mc-packs.net/pack/" + sha1Hex + ".zip";
        try (HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build()) {
            HttpResponse<String> r = client.send(req, HttpResponse.BodyHandlers.ofString());
            if (r.statusCode() / 100 != 2) {
                throw new IOException("mc-packs.net respondeu HTTP " + r.statusCode());
            }
            HttpRequest head = HttpRequest.newBuilder(URI.create(downloadUrl))
                    .timeout(Duration.ofSeconds(10)).method("HEAD", HttpRequest.BodyPublishers.noBody()).build();
            int code = client.send(head, HttpResponse.BodyHandlers.discarding()).statusCode();
            if (code != 200) {
                throw new IOException("upload aceito mas pack não ficou disponível (HTTP " + code
                        + " em " + downloadUrl + ")");
            }
        }
        return downloadUrl;
    }
}
