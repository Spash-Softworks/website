package pl.kamerz.service;

import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class VersionService {

    private static final String URL =
            "https://clientsettingscdn.roblox.com/v2/client-version/WindowsPlayer";
    private static final long TTL_MS = 5 * 60 * 1000;

    private static final Pattern VERSION_PAT =
            Pattern.compile("\"version\"\\s*:\\s*\"([^\"]*)\"");
    private static final Pattern UPLOAD_PAT =
            Pattern.compile("\"clientVersionUpload\"\\s*:\\s*\"([^\"]*)\"");
    private static final Pattern BOOT_PAT =
            Pattern.compile("\"bootstrapperVersion\"\\s*:\\s*\"([^\"]*)\"");

    private final HttpClient http = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL)
            .connectTimeout(Duration.ofSeconds(8))
            .build();

    private final ExecutorService refreshPool = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "version-refresh");
        t.setDaemon(true);
        return t;
    });

    private volatile Version cached;
    private volatile long lastFetch;
    private volatile boolean refreshing;

    public record Version(String version, String clientVersionUpload,
                          String bootstrapperVersion, boolean ok) {
        static Version unavailable() {
            return new Version("unavailable", "", "", false);
        }
    }

    public Version windows() {
        long now = System.currentTimeMillis();
        Version v = cached;
        if (v != null && v.ok() && now - lastFetch < TTL_MS) return v;

        if (v != null) {
            // Stale but usable — kick off a background refresh and return immediately
            if (!refreshing) {
                refreshing = true;
                refreshPool.submit(this::doFetch);
            }
            return v;
        }

        // Nothing cached yet — block once on the very first call
        return doFetch();
    }

    private synchronized Version doFetch() {
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
                String version = extract(VERSION_PAT, body);
                String upload  = extract(UPLOAD_PAT,  body);
                String boot    = extract(BOOT_PAT,    body);
                cached    = new Version(version, upload, boot, !version.isEmpty());
                lastFetch = System.currentTimeMillis();
                return cached;
            }
        } catch (Exception ignored) {
        } finally {
            refreshing = false;
        }
        return cached != null ? cached : Version.unavailable();
    }

    private static String extract(Pattern p, String json) {
        Matcher m = p.matcher(json);
        return m.find() ? m.group(1) : "";
    }
}
