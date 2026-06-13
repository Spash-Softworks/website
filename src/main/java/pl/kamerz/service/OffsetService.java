package pl.kamerz.service;

import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class OffsetService {

    /* im sooo gay */
    public enum Provider {
        SODA("Soda", "https://getsoda.netlify.app/versions/%s/%s"),
        THEO("Theo", "https://offsets.imtheo.lol/%s/%s");

        public final String label;
        private final String urlTemplate;

        Provider(String label, String urlTemplate) {
            this.label = label;
            this.urlTemplate = urlTemplate;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    public record Cat(String label, String file) { // *meow*
        @Override
        public String toString() {
            return label;
        }
    }

    public record Offset(String namespace, String name, String value) {
    }

    public record OffsetData(
            Map<String, List<Offset>> parsed,
            Map<String, String> raw,
            String offsetVersion,
            boolean current,
            boolean ok,
            String error
    ) {
        static OffsetData unavailable(String err) {
            return new OffsetData(Map.of(), Map.of(), "", false, false, err);
        }

        public List<Offset> entries(String file) {
            return parsed.getOrDefault(file, List.of());
        }

        public String source(String file) {
            return raw.getOrDefault(file, "");
        }
    }

    /**
     * Strips dumper banner and replaces it with the spash cat art header.
     */
    static final class Header {
        private Header() {
        }

        static String clean(String src, String hash) {
            if (src == null) return "";
            String s = src.replace("\r\n", "\n").replace("\r", "\n");
            s = s.replaceAll("(?s)/\\*.*?\\*/", "");

            StringBuilder sb = new StringBuilder();
            for (String line : s.split("\n", -1)) {
                String t = line.strip();
                if (t.startsWith("//")) continue;
                if (t.contains("version-") &&
                        (t.contains("roblox_version") || t.contains("ClientVersion"))) continue;
                sb.append(line).append("\n");
            }

            String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm, dd MMM yyyy"));
            String body = sb.toString().replaceAll("\n{2,}", "\n").stripLeading();
            return "/*             ╱|、\n" +
                    " *            (˚ˎ 。 7     kamerz is gay (version-" + hash + ")\n" +
                    " *             |、˜〵      time " + time + ", *meow*!\n" +
                    " *             じしˍ, )ノ   dsc.gg/spashapi\n" +
                    " */\n" + body;
        }
    }

    private static final List<String> SODA_VERSIONS = List.of(
            "ad5d3e2906444472", "a182ba0d4c6f483b", "460909c4fe904aae",
            "9377ee10133e4be3", "ec412128eba3476e", "bf6344c9c23446bf",
            "acc4b74f79e743b9", "390ba09e7e944154", "2e6461290a3541f5"
    );

    private static final long TTL_MS = 5 * 60 * 1000;

    private static final String UA =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 "
                    + "(KHTML, like Gecko) Chrome/124.0 Safari/537.36";

    private final HttpClient http = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL)
            .connectTimeout(Duration.ofSeconds(12))
            .build();

    private final VersionService versioning;
    private final Map<String, OffsetData> cache = new ConcurrentHashMap<>();
    private volatile List<String> theoCached;
    private volatile long theoCachedAt;

    public OffsetService(VersionService versioning) {
        this.versioning = versioning;
    }

    public List<Cat> categories(Provider provider) {
        return switch (provider) {
            case SODA -> List.of(
                    new Cat("External", "offsets.hpp"),
                    new Cat("Internal", "int.hpp"),
                    new Cat("CFG", "CFG.hpp"),
                    new Cat("FFlags", "fflags.hpp"));
            case THEO -> List.of(
                    new Cat("External", "offsets.hpp"),
                    new Cat("Structs", "struct.hpp"),
                    new Cat("FFlags", "fflags.hpp"));
        };
    }

    public List<String> versions(Provider provider) {
        return switch (provider) {
            case SODA -> SODA_VERSIONS;
            case THEO -> theoVersions();
        };
    }

    private List<String> theoVersions() {
        long now = System.currentTimeMillis();
        if (theoCached != null && now - theoCachedAt < TTL_MS) return theoCached;
        try {
            HttpRequest req = HttpRequest.newBuilder(URI.create("https://offsets.imtheo.lol/versions"))
                    .timeout(Duration.ofSeconds(12))
                    .header("Accept", "application/json")
                    .header("User-Agent", UA)
                    .GET().build();
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() == 200) {
                List<String> vs = new ArrayList<>();
                Matcher m = Pattern.compile("\"version\"\\s*:\\s*\"([^\"]+)\"").matcher(resp.body());
                while (m.find()) vs.add(m.group(1));
                if (!vs.isEmpty()) {
                    theoCached = vs;
                    theoCachedAt = now;
                    return vs;
                }
            }
        } catch (Exception ignored) {
        }
        return theoCached != null ? theoCached : List.of();
    }

    public OffsetData get() {
        return get(Provider.SODA, null);
    }

    public OffsetData get(Provider provider, String version) {
        if (version != null && !version.isBlank()) {
            return load(provider, version.strip());
        }

        String live = liveHash();
        List<String> candidates = new ArrayList<>();
        for (String v : versions(provider)) {
            if (bareHash(v).equals(live)) candidates.add(v);
        }
        for (String v : versions(provider)) {
            if (!candidates.contains(v)) candidates.add(v);
        }
        if (provider == Provider.SODA && !live.isBlank()
                && candidates.stream().noneMatch(c -> bareHash(c).equals(live))) {
            candidates.add(0, live);
        }

        for (String v : candidates) {
            OffsetData d = load(provider, v);
            if (d.ok()) return d;
        }
        return OffsetData.unavailable("No offset data available for " + provider.label);
    }

    private OffsetData load(Provider provider, String version) {
        String key = provider.name() + "|" + version;
        OffsetData hit = cache.get(key);
        if (hit != null && hit.ok()) return hit;

        OffsetData d = build(provider, version);
        if (d.ok()) cache.put(key, d);
        return d;
    }

    private OffsetData build(Provider provider, String version) {
        Map<String, List<Offset>> parsed = new LinkedHashMap<>();
        Map<String, String> raw = new LinkedHashMap<>();
        String bare = bareHash(version);
        boolean any = false;

        for (Cat cat : categories(provider)) {
            try {
                String src = Header.clean(fetch(provider, version, cat.file()), bare);
                raw.put(cat.file(), src);
                parsed.put(cat.file(), parseHpp(src));
                any = true;
            } catch (Exception e) {
                raw.put(cat.file(), "");
                parsed.put(cat.file(), List.of());
            }
        }

        if (!any) return OffsetData.unavailable("No offset data for version " + version);
        return new OffsetData(parsed, raw, bare, bare.equals(liveHash()), true, null);
    }

    private String fetch(Provider provider, String version, String file) throws Exception {
        String pathVersion = provider == Provider.SODA ? bareHash(version) : version;
        String url = String.format(provider.urlTemplate, pathVersion, file);
        HttpRequest req = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(12))
                .header("User-Agent", UA)
                .header("Accept", "text/plain, */*")
                .GET().build();
        HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() != 200) {
            throw new RuntimeException("HTTP " + resp.statusCode() + " fetching " + file);
        }
        return resp.body();
    }

    public String liveHash() {
        String upload = versioning.windows().clientVersionUpload();
        if (upload == null) return "";
        return upload.startsWith("version-") ? upload.substring(8) : upload;
    }

    private static String bareHash(String v) {
        if (v == null) return "";
        String s = v.strip();
        return s.startsWith("version-") ? s.substring(8) : s;
    }

    static List<Offset> parseHpp(String src) {
        if (src == null || src.isBlank()) return List.of();
        var result = new ArrayList<Offset>();

        src = src.replaceAll("(?m)\\bnamespace\\s+(\\w+)[ \\t]*\\r?\\n[ \\t]*\\{",
                "namespace $1 {");
        src = src.replaceAll("(?m)\\benum(?:\\s+class)?\\s+(\\w+)[^\\{\\n]*\\r?\\n[ \\t]*\\{",
                "enum $1 {");

        var nsPat = Pattern.compile("\\bnamespace\\s+(\\w+)\\s*\\{");
        var enumPat = Pattern.compile("\\benum(?:\\s+class)?\\s+(\\w+)[^\\{]*\\{");
        var entryPat = Pattern.compile("\\buintptr_t\\s+(\\w+)\\s*=\\s*(?:\\w+\\s*\\(\\s*)?(0x[0-9A-Fa-f]+)");
        var enumEntryPat = Pattern.compile("^(\\w+)\\s*=\\s*(0x[0-9A-Fa-f]+|\\d+)\\s*,?\\s*$");

        Deque<String[]> ns = new ArrayDeque<>();
        Deque<Integer> enums = new ArrayDeque<>();
        int depth = 0;

        for (String line : src.split("\n")) {
            String t = line.strip();
            if (t.startsWith("//") || t.startsWith("/*") || t.startsWith("*")) continue;

            long opens = t.chars().filter(c -> c == '{').count();
            long closes = t.chars().filter(c -> c == '}').count();

            Matcher en = enumPat.matcher(t);
            Matcher nm = nsPat.matcher(t);
            if (en.find()) {
                ns.push(new String[]{en.group(1), String.valueOf(depth + 1)});
                enums.push(depth + 1);
            } else if (nm.find()) {
                ns.push(new String[]{nm.group(1), String.valueOf(depth + 1)});
            }

            Matcher em = entryPat.matcher(t);
            while (em.find() && !ns.isEmpty()) {
                result.add(new Offset(ns.peek()[0], em.group(1), em.group(2)));
            }

            if (!enums.isEmpty() && !ns.isEmpty()) {
                Matcher ee = enumEntryPat.matcher(t);
                if (ee.find()) {
                    result.add(new Offset(ns.peek()[0], ee.group(1), ee.group(2)));
                }
            }

            depth += (int) (opens - closes);
            final int d = depth;
            ns.removeIf(frame -> Integer.parseInt(frame[1]) > d);
            enums.removeIf(open -> open > d);
        }
        return result;
    }
}
