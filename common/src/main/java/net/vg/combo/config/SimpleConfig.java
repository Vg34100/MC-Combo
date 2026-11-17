package net.vg.combo.config;

/*
 * Copyright © 2021 magistermaks
 * Modified by VgLevelUp 2024
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * with the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or significant portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

import dev.architectury.platform.Platform;
import net.vg.combo.Combo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;

public class SimpleConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger("ComboConfig");
    private final LinkedHashMap<String, ConfigEntry> config = new LinkedHashMap<>();
    private final ConfigRequest request;

    public static final String MOD_VERSION = fetchModVersion();

    private boolean broken = false;

    private static String fetchModVersion() {
        String defaultVersion = "0.0.0";
        if (Platform.isModLoaded(Combo.MOD_ID)) {
            return Platform.getMod(Combo.MOD_ID).getVersion();
        }
        return defaultVersion;
    }

    public interface DefaultConfig {
        String get(String namespace);

        static String empty(String namespace) {
            return "";
        }
    }

    public static class ConfigRequest {

        private final File file;
        private final String filename;
        private DefaultConfig provider;

        private ConfigRequest(File file, String filename) {
            this.file = file;
            this.filename = filename;
            this.provider = DefaultConfig::empty;
        }

        public ConfigRequest provider(DefaultConfig provider) {
            this.provider = provider;
            return this;
        }

        public SimpleConfig request() {
            return new SimpleConfig(this);
        }

        private String getConfig() {
            return provider.get(filename) + "\n";
        }
    }

    /**
     * Stable {@code <filename>.properties}. Older releases wrote {@code <filename>-<version>.properties},
     * which reset settings on every update; the first start without the stable file copies the
     * newest of those (left untouched, so older jars keep working).
     */
    public static ConfigRequest of(String filename) {
        Path folder = Platform.getConfigFolder();
        String stableFilename = filename + ".properties";
        Path stable = folder.resolve(stableFilename);
        if (!Files.exists(stable)) {
            Path legacy = newestVersionedFile(folder, filename);
            if (legacy != null) {
                try {
                    Files.copy(legacy, stable);
                    LOGGER.info("Config '{}' carried over settings from {}", stableFilename, legacy.getFileName());
                } catch (IOException e) {
                    LOGGER.warn("Config '{}' could not copy settings from {}: {}", stableFilename, legacy.getFileName(), e.toString());
                }
            }
        }
        return new ConfigRequest(stable.toFile(), stableFilename);
    }

    /** Newest {@code <filename>-<version>.properties} by numeric version, or null. */
    static Path newestVersionedFile(Path folder, String filename) {
        String prefix = filename + "-", suffix = ".properties";
        Path newest = null;
        int[] newestVersion = null;
        try (java.nio.file.DirectoryStream<Path> files = Files.newDirectoryStream(folder, prefix + "*" + suffix)) {
            for (Path file : files) {
                String name = file.getFileName().toString();
                int[] version = parseVersion(name.substring(prefix.length(), name.length() - suffix.length()));
                if (version != null && (newestVersion == null || compareVersions(version, newestVersion) > 0)) {
                    newest = file;
                    newestVersion = version;
                }
            }
        } catch (IOException | RuntimeException ignored) {
            // Missing or unreadable folder: nothing to carry over.
        }
        return newest;
    }

    private static int[] parseVersion(String text) {
        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("^(\\d+(?:\\.\\d+)*)").matcher(text);
        if (!matcher.find()) return null;
        String[] parts = matcher.group(1).split("\\.");
        int[] version = new int[parts.length];
        for (int i = 0; i < parts.length; i++) {
            try {
                version[i] = Integer.parseInt(parts[i]);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return version;
    }

    private static int compareVersions(int[] a, int[] b) {
        for (int i = 0; i < Math.max(a.length, b.length); i++) {
            int left = i < a.length ? a[i] : 0, right = i < b.length ? b[i] : 0;
            if (left != right) return Integer.compare(left, right);
        }
        return 0;
    }

    private void createConfig() throws IOException {
        request.file.getParentFile().mkdirs();
        Files.createFile(request.file.toPath());

        PrintWriter writer = new PrintWriter(request.file, StandardCharsets.UTF_8);
        writer.write(request.getConfig());
        writer.close();
    }

    private void loadConfig() throws IOException {
        Scanner reader = new Scanner(request.file);
        StringBuilder currentComment = new StringBuilder();
        for (int line = 1; reader.hasNextLine(); line++) {
            currentComment = parseConfigEntry(reader.nextLine(), line, currentComment);
        }
    }

    private StringBuilder parseConfigEntry(String entry, int line, StringBuilder currentComment) {
        if (entry.startsWith("#")) {
            currentComment.append(entry).append("\n");
        } else if (!entry.isEmpty()) {
            String[] parts = entry.split("=", 2);
            if (parts.length == 2) {
                config.put(parts[0], new ConfigEntry(parts[1].split(" #")[0], currentComment.toString()));
                currentComment.setLength(0);
            } else {
                throw new RuntimeException("Syntax error in config file on line " + line + "!");
            }
        } else {
            currentComment.append("\n");
        }
        return currentComment;
    }

    private SimpleConfig(ConfigRequest request) {
        this.request = request;
        String identifier = "Config '" + request.filename + "'";

        if (!request.file.exists()) {
            LOGGER.info(identifier + " is missing, generating default one...");

            try {
                createConfig();
            } catch (IOException e) {
                LOGGER.error(identifier + " failed to generate!");
                LOGGER.trace(identifier, e);
                broken = true;
            }
        }

        if (!broken) {
            try {
                loadConfig();
            } catch (Exception e) {
                LOGGER.error(identifier + " failed to load!");
                LOGGER.trace(identifier, e);
                broken = true;
            }
        }
    }

    public String get(String key) {
        ConfigEntry entry = config.get(key);
        return entry != null ? entry.value : null;
    }

    public String getOrDefault(String key, String def) {
        String val = get(key);
        return val == null ? def : val;
    }

    public int getOrDefault(String key, int def) {
        try {
            return Integer.parseInt(get(key));
        } catch (Exception e) {
            return def;
        }
    }

    public boolean getOrDefault(String key, boolean def) {
        String val = get(key);
        if (val != null) {
            return val.equalsIgnoreCase("true");
        }
        return def;
    }

    public double getOrDefault(String key, double def) {
        try {
            return Double.parseDouble(get(key));
        } catch (Exception e) {
            return def;
        }
    }

    public boolean isBroken() {
        return broken;
    }

    public void set(String key, String value) {
        ConfigEntry entry = config.get(key);
        if (entry != null) {
            entry.value = value;
        } else {
            config.put(key, new ConfigEntry(value, ""));
        }
    }

    public void set(String key, int value) {
        set(key, Integer.toString(value));
    }

    public void set(String key, boolean value) {
        set(key, Boolean.toString(value));
    }

    public void set(String key, double value) {
        set(key, Double.toString(value));
    }

    public void save(String[] necessaryConfigs) {
        try {
            boolean needsRecreation = false;
            for (String key : necessaryConfigs) {
                if (!config.containsKey(key)) {
                    needsRecreation = true;
                    break;
                }
            }

            if (needsRecreation) {
                createConfig();
                loadConfig();
            } else {
                PrintWriter writer = new PrintWriter(request.file, StandardCharsets.UTF_8);
                for (Map.Entry<String, ConfigEntry> entry : config.entrySet()) {
                    if (entry.getValue() != null && !entry.getValue().comment.isEmpty()) {
                        writer.println(entry.getValue().comment.trim());
                    }
                    if (entry.getValue() != null) {
                        writer.println(entry.getKey() + "=" + entry.getValue().value);
                        writer.println();
                    }
                }
                writer.close();
            }
        } catch (IOException e) {
            LOGGER.error("Failed to save config!", e);
        }
    }

    private static class ConfigEntry {
        String value;
        String comment;

        ConfigEntry(String value, String comment) {
            this.value = value;
            this.comment = comment;
        }
    }
}
