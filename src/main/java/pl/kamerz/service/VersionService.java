package pl.kamerz.service;

import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class VersionService {

    private static final String URL =
            "https://clientsettingscdn.roblox.com/v2/client-version/WindowsPlayer";
    private static final long TTL_MS = 5 * 60 * 1000;

    private final HttpClient http = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL)
            .connectTimeout(Duration.ofSeconds(8))
            .build();

    private volatile Version cached;
    private volatile long lastFetch;

    public record Version(String version, String clientVersionUpload,
                          String bootstrapperVersion, boolean ok) {
        static Version unavailable() {
            return new Version("unavailable", "", "", false);
        }
    }

    public synchronized Version windows() {
        long now = System.currentTimeMillis();
        if (cached != null && cached.ok() && now - lastFetch < TTL_MS) return cached;
        try {
            HttpRequest req = HttpRequest.newBuilder(URI.create(URL))
                    .timeout(Duration.ofSeconds(8))
                    .header("Accept", "application/json")
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/124.0 Safari/537.36")
                    .GET().build();
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() == 200) {
                String body = resp.body();
                String version = extract(body, "version");
                String upload = extract(body, "clientVersionUpload");
                String boot = extract(body, "bootstrapperVersion");
                cached = new Version(version, upload, boot, !version.isEmpty());
                lastFetch = now;
                return cached;
            }
        } catch (Exception ignored) {
        }
        return cached != null ? cached : Version.unavailable();
    }

    private String extract(String json, String key) {
        Matcher m = Pattern.compile("\"" + key + "\"\\s*:\\s*\"([^\"]*)\"").matcher(json);
        return m.find() ? m.group(1) : "";
    }
}
