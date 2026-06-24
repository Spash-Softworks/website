package pl.kamerz.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.kamerz.service.OffsetService;
import pl.kamerz.service.OffsetService.Cat;
import pl.kamerz.service.OffsetService.OffsetData;
import pl.kamerz.service.OffsetService.Provider;
import pl.kamerz.utils.Formatter;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@CrossOrigin("*")
public class Offset {

    private final OffsetService offsets;

    public Offset(OffsetService offsets) {
        this.offsets = offsets;
    }

    record OffsetEntry(String namespace, String name, String value) {}

    record OffsetResponse(
            String version,
            boolean current,
            Map<String, List<OffsetEntry>> categories
    ) {}

    // GET /api/offsets/versions?provider=soda
    @GetMapping("/offsets/versions")
    public ResponseEntity<?> versions(@RequestParam(defaultValue = "soda") String provider) {
        Provider p = resolve(provider);
        if (p == null) return badProvider(provider);
        return ResponseEntity.ok(offsets.versions(p));
    }

    // GET /api/offsets/categories?provider=soda
    @GetMapping("/offsets/categories")
    public ResponseEntity<?> categories(@RequestParam(defaultValue = "soda") String provider) {
        Provider p = resolve(provider);
        if (p == null) return badProvider(provider);
        return ResponseEntity.ok(offsets.categories(p).stream()
                .map(c -> Map.of("label", c.label(), "file", c.file()))
                .toList());
    }

    // GET /api/offsets?provider=soda&version=xxx&category=Classes&format=cs
    @GetMapping("/offsets")
    public ResponseEntity<?> offsetData(
            @RequestParam(defaultValue = "soda") String provider,
            @RequestParam(required = false) String version,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String format) {

        Provider p = resolve(provider);
        if (p == null) return badProvider(provider);

        OffsetData data = offsets.get(p, version);
        if (!data.ok()) return ResponseEntity.status(503).body(Map.of("error", data.error()));

        String resolvedFile = resolveCategory(p, category);
        String categoryLabel = resolvedFile != null
                ? labelFor(p, resolvedFile)
                : offsets.categories(p).stream().map(Cat::label).collect(Collectors.joining(", "));

        List<OffsetService.Offset> entries = resolvedFile != null
                ? data.entries(resolvedFile)
                : data.parsed().values().stream().flatMap(List::stream).toList();

        Formatter fmt = resolveFormatter(format);
        if (fmt != null) {
            String content = fmt.format(entries, data.offsetVersion(), categoryLabel);
            return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(content);
        }

        if (resolvedFile != null) {
            return ResponseEntity.ok(toEntries(entries));
        }
        return ResponseEntity.ok(toResponse(p, data));
    }

    // GET /api/offsets/raw?provider=soda&version=xxx&category=Classes
    @GetMapping(value = "/offsets/raw", produces = "text/plain;charset=UTF-8")
    public ResponseEntity<String> raw(
            @RequestParam(defaultValue = "soda") String provider,
            @RequestParam(required = false) String version,
            @RequestParam String category) {
        Provider p = resolve(provider);
        if (p == null) return ResponseEntity.badRequest().body("Unknown provider: " + provider);
        OffsetData data = offsets.get(p, version);
        if (!data.ok()) return ResponseEntity.status(503).body(data.error());
        String key = resolveCategory(p, category);
        if (key == null) return ResponseEntity.badRequest().body("Unknown category: " + category);
        String src = data.source(key);
        if (src.isBlank()) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(src);
    }

    private String resolveCategory(Provider p, String category) {
        if (category == null || category.isBlank()) return null;
        if (category.endsWith(".hpp")) return category;
        return offsets.categories(p).stream()
                .filter(c -> c.label().equalsIgnoreCase(category))
                .map(Cat::file)
                .findFirst()
                .orElse(null);
    }

    private String labelFor(Provider p, String file) {
        return offsets.categories(p).stream()
                .filter(c -> c.file().equals(file))
                .map(Cat::label)
                .findFirst()
                .orElse(file);
    }

    private static Formatter resolveFormatter(String s) {
        if (s == null || s.isBlank()) return null;
        for (Formatter f : Formatter.values())
            if (f.extension.equalsIgnoreCase(s) || f.name().equalsIgnoreCase(s))
                return f;
        return null;
    }

    private OffsetResponse toResponse(Provider p, OffsetData data) {
        var categories = new java.util.LinkedHashMap<String, List<OffsetEntry>>();
        for (Cat cat : offsets.categories(p))
            categories.put(cat.label(), toEntries(data.entries(cat.file())));
        return new OffsetResponse(data.offsetVersion(), data.current(), categories);
    }

    private static List<OffsetEntry> toEntries(List<OffsetService.Offset> list) {
        return list.stream()
                .map(o -> new OffsetEntry(o.namespace(), o.name(), o.value()))
                .collect(Collectors.toList());
    }

    private static Provider resolve(String s) {
        if (s == null) return Provider.SODA;
        return switch (s.toLowerCase()) {
            case "soda" -> Provider.SODA;
            case "theo" -> Provider.THEO;
            default -> null;
        };
    }

    private static ResponseEntity<Map<String, String>> badProvider(String s) {
        return ResponseEntity.badRequest()
                .body(Map.of("error", "Unknown provider '" + s + "'. Use 'soda' or 'theo'."));
    }
}
