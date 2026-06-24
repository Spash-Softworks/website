package pl.kamerz.utils;

import pl.kamerz.service.OffsetService.Offset;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public enum Formatter {

    C_HEADER("C Header (.h)", "h", "cpp") {
        @Override
        public String format(List<Offset> offsets, String version, String category) {
            var sb = new StringBuilder();
            sb.append(blockComment(version)).append("\n");
            sb.append("#pragma once\n\n");
            byNamespace(offsets).forEach((ns, list) -> {
                sb.append("// ").append(ns).append("\n");
                for (Offset o : list)
                    sb.append("#define ").append(ns).append("_").append(o.name())
                      .append(" ").append(o.value()).append("\n");
                sb.append("\n");
            });
            return sb.toString();
        }
    },

    JAVA("Java (.java)", "java", "java") {
        @Override
        public String format(List<Offset> offsets, String version, String category) {
            var sb = new StringBuilder();
            sb.append(blockComment(version)).append("\n");
            sb.append("public interface Offsets {\n\n");
            byNamespace(offsets).forEach((ns, list) -> {
                sb.append("    interface ").append(ns).append(" {\n");
                for (Offset o : list)
                    sb.append("        long ").append(o.name())
                      .append(" = ").append(o.value()).append("L;\n");
                sb.append("    }\n\n");
            });
            sb.append("}\n");
            return sb.toString();
        }
    },

    CSHARP("C# / .NET (.cs)", "cs", "csharp") {
        @Override
        public String format(List<Offset> offsets, String version, String category) {
            var sb = new StringBuilder();
            sb.append(blockComment(version)).append("\n");
            sb.append("public static class Offsets\n{\n");
            byNamespace(offsets).forEach((ns, list) -> {
                sb.append("    public static class ").append(ns).append("\n    {\n");
                for (Offset o : list)
                    sb.append("        public const ulong ").append(o.name())
                      .append(" = ").append(o.value()).append(";\n");
                sb.append("    }\n\n");
            });
            sb.append("}\n");
            return sb.toString();
        }
    },

    PYTHON("Python (.py)", "py", "python") {
        @Override
        public String format(List<Offset> offsets, String version, String category) {
            var sb = new StringBuilder();
            sb.append(hashComment(version)).append("\n");
            byNamespace(offsets).forEach((ns, list) -> {
                sb.append("class ").append(ns).append(":\n");
                for (Offset o : list)
                    sb.append("    ").append(o.name())
                      .append(" = ").append(o.value()).append("\n");
                sb.append("\n");
            });
            return sb.toString();
        }
    },

    CPP("C++ (.hpp)", "hpp", "cpp") {
        @Override
        public String format(List<Offset> offsets, String version, String category) {
            var sb = new StringBuilder();
            sb.append(blockComment(version)).append("\n");
            sb.append("#pragma once\n#include <cstdint>\n\n");
            byNamespace(offsets).forEach((ns, list) -> {
                sb.append("namespace ").append(ns).append(" {\n");
                for (Offset o : list)
                    sb.append("    constexpr uintptr_t ").append(o.name())
                      .append(" = ").append(o.value()).append(";\n");
                sb.append("}\n\n");
            });
            return sb.toString();
        }
    },

    RUST("Rust (.rs)", "rs", "rust") {
        @Override
        public String format(List<Offset> offsets, String version, String category) {
            var sb = new StringBuilder();
            sb.append(blockComment(version)).append("\n");
            sb.append("#![allow(dead_code, non_upper_case_globals)]\n\n");
            byNamespace(offsets).forEach((ns, list) -> {
                sb.append("pub mod ").append(ns).append(" {\n");
                for (Offset o : list)
                    sb.append("    pub const ").append(o.name())
                      .append(": usize = ").append(o.value()).append(";\n");
                sb.append("}\n\n");
            });
            return sb.toString();
        }
    },

    LUA("Lua (.lua)", "lua", "lua") {
        @Override
        public String format(List<Offset> offsets, String version, String category) {
            var sb = new StringBuilder();
            sb.append(hashComment(version)).append("\n");
            sb.append("local Offsets = {}\n\n");
            byNamespace(offsets).forEach((ns, list) -> {
                sb.append("Offsets.").append(ns).append(" = {\n");
                for (Offset o : list)
                    sb.append("    ").append(o.name())
                      .append(" = ").append(o.value()).append(",\n");
                sb.append("}\n\n");
            });
            sb.append("return Offsets\n");
            return sb.toString();
        }
    },

    TYPESCRIPT("TypeScript (.ts)", "ts", "typescript") {
        @Override
        public String format(List<Offset> offsets, String version, String category) {
            var sb = new StringBuilder();
            sb.append(blockComment(version)).append("\n");
            byNamespace(offsets).forEach((ns, list) -> {
                sb.append("export namespace ").append(ns).append(" {\n");
                for (Offset o : list)
                    sb.append("    export const ").append(o.name())
                      .append(" = ").append(o.value()).append(";\n");
                sb.append("}\n\n");
            });
            return sb.toString();
        }
    },

    JSON("JSON (.json)", "json", "json") {
        @Override
        public String format(List<Offset> offsets, String version, String category) {
            var sb = new StringBuilder();
            sb.append("{\n");
            sb.append("  \"version\": \"").append(version).append("\",\n");
            sb.append("  \"category\": \"").append(category).append("\",\n");
            sb.append("  \"offsets\": {\n");
            var ns = byNamespace(offsets);
            var nsEntries = new ArrayList<>(ns.entrySet());
            for (int i = 0; i < nsEntries.size(); i++) {
                var entry = nsEntries.get(i);
                sb.append("    \"").append(entry.getKey()).append("\": {\n");
                var list = entry.getValue();
                for (int j = 0; j < list.size(); j++) {
                    var o = list.get(j);
                    sb.append("      \"").append(o.name()).append("\": \"").append(o.value()).append("\"");
                    if (j < list.size() - 1) sb.append(",");
                    sb.append("\n");
                }
                sb.append("    }");
                if (i < nsEntries.size() - 1) sb.append(",");
                sb.append("\n");
            }
            sb.append("  }\n}\n");
            return sb.toString();
        }
    },

    GO("Go (.go)", "go", "go") {
        @Override
        public String format(List<Offset> offsets, String version, String category) {
            var sb = new StringBuilder();
            sb.append(blockComment(version)).append("\n");
            sb.append("package offsets\n\nconst (\n");
            byNamespace(offsets).forEach((ns, list) -> {
                sb.append("\t// ").append(ns).append("\n");
                for (Offset o : list)
                    sb.append("\t").append(ns).append("_").append(o.name())
                      .append(" uintptr = ").append(o.value()).append("\n");
                sb.append("\n");
            });
            sb.append(")\n");
            return sb.toString();
        }
    },

    JAVASCRIPT("JavaScript (.js)", "js", "javascript") {
        @Override
        public String format(List<Offset> offsets, String version, String category) {
            var sb = new StringBuilder();
            sb.append(blockComment(version)).append("\n");
            byNamespace(offsets).forEach((ns, list) -> {
                sb.append("export const ").append(ns).append(" = {\n");
                for (Offset o : list)
                    sb.append("    ").append(o.name()).append(": ").append(o.value()).append(",\n");
                sb.append("};\n\n");
            });
            return sb.toString();
        }
    },

    ZIG("Zig (.zig)", "zig", "plaintext") {
        @Override
        public String format(List<Offset> offsets, String version, String category) {
            var sb = new StringBuilder();
            sb.append(blockComment(version)).append("\n");
            byNamespace(offsets).forEach((ns, list) -> {
                sb.append("pub const ").append(ns).append(" = struct {\n");
                for (Offset o : list)
                    sb.append("    pub const ").append(o.name())
                      .append(": usize = ").append(o.value()).append(";\n");
                sb.append("};\n\n");
            });
            return sb.toString();
        }
    },

    KOTLIN("Kotlin (.kt)", "kt", "kotlin") {
        @Override
        public String format(List<Offset> offsets, String version, String category) {
            var sb = new StringBuilder();
            sb.append(blockComment(version)).append("\n");
            byNamespace(offsets).forEach((ns, list) -> {
                sb.append("object ").append(ns).append(" {\n");
                for (Offset o : list)
                    sb.append("    const val ").append(o.name())
                      .append(": Long = ").append(o.value()).append("L\n");
                sb.append("}\n\n");
            });
            return sb.toString();
        }
    },

    SWIFT("Swift (.swift)", "swift", "swift") {
        @Override
        public String format(List<Offset> offsets, String version, String category) {
            var sb = new StringBuilder();
            sb.append(blockComment(version)).append("\n");
            byNamespace(offsets).forEach((ns, list) -> {
                sb.append("enum ").append(ns).append(" {\n");
                for (Offset o : list)
                    sb.append("    static let ").append(o.name())
                      .append(": UInt = ").append(o.value()).append("\n");
                sb.append("}\n\n");
            });
            return sb.toString();
        }
    },

    RUBY("Ruby (.rb)", "rb", "ruby") {
        @Override
        public String format(List<Offset> offsets, String version, String category) {
            var sb = new StringBuilder();
            sb.append(hashComment(version)).append("\n");
            byNamespace(offsets).forEach((ns, list) -> {
                sb.append("module ").append(ns).append("\n");
                for (Offset o : list)
                    sb.append("  ").append(o.name().toUpperCase())
                      .append(" = ").append(o.value()).append("\n");
                sb.append("end\n\n");
            });
            return sb.toString();
        }
    },

    PHP("PHP (.php)", "php", "php") {
        @Override
        public String format(List<Offset> offsets, String version, String category) {
            var sb = new StringBuilder();
            sb.append("<?php\n").append(blockComment(version)).append("\n");
            byNamespace(offsets).forEach((ns, list) -> {
                sb.append("class ").append(ns).append(" {\n");
                for (Offset o : list)
                    sb.append("    const ").append(o.name())
                      .append(" = ").append(o.value()).append(";\n");
                sb.append("}\n\n");
            });
            return sb.toString();
        }
    },

    DART("Dart (.dart)", "dart", "dart") {
        @Override
        public String format(List<Offset> offsets, String version, String category) {
            var sb = new StringBuilder();
            sb.append(blockComment(version)).append("\n");
            byNamespace(offsets).forEach((ns, list) -> {
                sb.append("abstract class ").append(ns).append(" {\n");
                for (Offset o : list)
                    sb.append("  static const int ").append(o.name())
                      .append(" = ").append(o.value()).append(";\n");
                sb.append("}\n\n");
            });
            return sb.toString();
        }
    },

    SCALA("Scala (.scala)", "scala", "scala") {
        @Override
        public String format(List<Offset> offsets, String version, String category) {
            var sb = new StringBuilder();
            sb.append(blockComment(version)).append("\n");
            byNamespace(offsets).forEach((ns, list) -> {
                sb.append("object ").append(ns).append(" {\n");
                for (Offset o : list)
                    sb.append("  val ").append(o.name())
                      .append(": Long = ").append(o.value()).append("L\n");
                sb.append("}\n\n");
            });
            return sb.toString();
        }
    },

    FSHARP("F# (.fs)", "fs", "fsharp") {
        @Override
        public String format(List<Offset> offsets, String version, String category) {
            var sb = new StringBuilder();
            sb.append(blockComment(version)).append("\n");
            byNamespace(offsets).forEach((ns, list) -> {
                sb.append("module ").append(ns).append(" =\n");
                for (Offset o : list)
                    sb.append("    let ").append(o.name())
                      .append(" = ").append(o.value()).append("UL\n");
                sb.append("\n");
            });
            return sb.toString();
        }
    },

    NIM("Nim (.nim)", "nim", "plaintext") {
        @Override
        public String format(List<Offset> offsets, String version, String category) {
            var sb = new StringBuilder();
            sb.append(hashComment(version)).append("\n");
            byNamespace(offsets).forEach((ns, list) -> {
                sb.append("# ").append(ns).append("\nconst\n");
                for (Offset o : list)
                    sb.append("  ").append(ns).append("_").append(o.name())
                      .append("* = ").append(o.value()).append("\n");
                sb.append("\n");
            });
            return sb.toString();
        }
    },

    POWERSHELL("PowerShell (.ps1)", "ps1", "powershell") {
        @Override
        public String format(List<Offset> offsets, String version, String category) {
            var sb = new StringBuilder();
            sb.append(hashComment(version)).append("\n");
            byNamespace(offsets).forEach((ns, list) -> {
                sb.append("# ").append(ns).append("\n");
                for (Offset o : list)
                    sb.append("$").append(ns).append("_").append(o.name())
                      .append(" = ").append(o.value()).append("\n");
                sb.append("\n");
            });
            return sb.toString();
        }
    },

    NASM("NASM (.asm)", "asm", "plaintext") {
        @Override
        public String format(List<Offset> offsets, String version, String category) {
            var sb = new StringBuilder();
            sb.append("; ╱|、\n");
            sb.append("; (˚ˎ 。 7     kamerz is gay (version-").append(version).append(")\n");
            String time = java.time.LocalDateTime.now()
                    .format(java.time.format.DateTimeFormatter.ofPattern("HH:mm, dd MMM yyyy"));
            sb.append("; |、˜〵      time ").append(time).append(", *meow*!\n");
            sb.append("; じしˍ, )ノ   dsc.gg/spashapi\n\n");
            sb.append("BITS 64\n\n");
            byNamespace(offsets).forEach((ns, list) -> {
                sb.append("; ").append(ns).append("\n");
                for (Offset o : list)
                    sb.append("%define ").append(ns).append("_").append(o.name())
                      .append(" ").append(o.value()).append("\n");
                sb.append("\n");
            });
            return sb.toString();
        }
    },

    FASM("FASM (.fasm)", "fasm", "plaintext") {
        @Override
        public String format(List<Offset> offsets, String version, String category) {
            var sb = new StringBuilder();
            sb.append("; ╱|、\n");
            sb.append("; (˚ˎ 。 7     kamerz is gay (version-").append(version).append(")\n");
            String time = java.time.LocalDateTime.now()
                    .format(java.time.format.DateTimeFormatter.ofPattern("HH:mm, dd MMM yyyy"));
            sb.append("; |、˜〵      time ").append(time).append(", *meow*!\n");
            sb.append("; じしˍ, )ノ   dsc.gg/spashapi\n\n");
            byNamespace(offsets).forEach((ns, list) -> {
                sb.append("; ").append(ns).append("\n");
                for (Offset o : list)
                    sb.append(ns).append("_").append(o.name())
                      .append(" = ").append(o.value()).append("\n");
                sb.append("\n");
            });
            return sb.toString();
        }
    },

    YAML("YAML (.yaml)", "yaml", "yaml") {
        @Override
        public String format(List<Offset> offsets, String version, String category) {
            var sb = new StringBuilder();
            sb.append(hashComment(version)).append("\n");
            sb.append("version: \"").append(version).append("\"\n");
            sb.append("category: \"").append(category).append("\"\n");
            sb.append("offsets:\n");
            byNamespace(offsets).forEach((ns, list) -> {
                sb.append("  ").append(ns).append(":\n");
                for (Offset o : list)
                    sb.append("    ").append(o.name())
                      .append(": \"").append(o.value()).append("\"\n");
            });
            return sb.toString();
        }
    },

    TOML("TOML (.toml)", "toml", "plaintext") {
        @Override
        public String format(List<Offset> offsets, String version, String category) {
            var sb = new StringBuilder();
            sb.append(hashComment(version)).append("\n");
            sb.append("version = \"").append(version).append("\"\n");
            sb.append("category = \"").append(category).append("\"\n\n");
            byNamespace(offsets).forEach((ns, list) -> {
                sb.append("[").append(ns).append("]\n");
                for (Offset o : list)
                    sb.append(o.name()).append(" = \"").append(o.value()).append("\"\n");
                sb.append("\n");
            });
            return sb.toString();
        }
    },

    CSV("CSV (.csv)", "csv", "plaintext") {
        @Override
        public String format(List<Offset> offsets, String version, String category) {
            var sb = new StringBuilder();
            sb.append("namespace,name,value\n");
            for (Offset o : offsets)
                sb.append(o.namespace()).append(",")
                  .append(o.name()).append(",")
                  .append(o.value()).append("\n");
            return sb.toString();
        }
    };

    public final String label;
    public final String extension;
    public final String monacoLanguage;

    Formatter(String label, String extension, String monacoLanguage) {
        this.label = label;
        this.extension = extension;
        this.monacoLanguage = monacoLanguage;
    }

    public abstract String format(List<Offset> offsets, String version, String category);

    @Override
    public String toString() {
        return label;
    }

    static String blockComment(String version) {
        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm, dd MMM yyyy"));
        return "/*             ╱|、\n" +
               " *            (˚ˎ 。 7     kamerz is gay (version-" + version + ")\n" +
               " *             |、˜〵      time " + time + ", *meow*!\n" +
               " *             じしˍ, )ノ   dsc.gg/spashapi\n" +
               " */";
    }

    static String hashComment(String version) {
        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm, dd MMM yyyy"));
        return "#             ╱|、\n" +
               "#            (˚ˎ 。 7     kamerz is gay (version-" + version + ")\n" +
               "#             |、˜〵      time " + time + ", *meow*!\n" +
               "#             じしˍ, )ノ   dsc.gg/spashapi";
    }

    static Map<String, List<Offset>> byNamespace(List<Offset> offsets) {
        var map = new LinkedHashMap<String, List<Offset>>();
        for (Offset o : offsets)
            map.computeIfAbsent(o.namespace(), k -> new ArrayList<>()).add(o);
        return map;
    }
}
