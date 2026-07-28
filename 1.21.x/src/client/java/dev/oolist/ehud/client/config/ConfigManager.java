package dev.oolist.ehud.client.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.oolist.ehud.EHud;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

public final class ConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final DateTimeFormatter BACKUP_NAME =
            DateTimeFormatter.ofPattern("uuuu-MM-dd_HH-mm-ss").withZone(ZoneOffset.UTC);
    private static final Path ROOT = FabricLoader.getInstance().getConfigDir().resolve("ehud");
    private static final Path CONFIG_FILE = ROOT.resolve("config.json");
    private static final Path PRESET_DIR = ROOT.resolve("presets");
    private static final Path BACKUP_DIR = ROOT.resolve("backups");

    private static EHudConfig config = new EHudConfig();

    private ConfigManager() {
    }

    public static EHudConfig get() {
        return config;
    }

    public static synchronized void replace(EHudConfig replacement) {
        if (replacement == null) {
            EHud.LOGGER.warn("Tried to apply an empty E HUD configuration; using defaults instead.");
            replacement = new EHudConfig();
        }
        replacement.ensureDefaults();
        config = replacement;
    }

    public static synchronized void load() {
        try {
            createDirectories();
            if (Files.exists(CONFIG_FILE)) {
                try {
                    EHudConfig loaded = GSON.fromJson(
                            Files.readString(CONFIG_FILE, StandardCharsets.UTF_8), EHudConfig.class);
                    if (loaded == null) {
                        throw new IOException("The configuration file was empty.");
                    }
                    loaded.ensureDefaults();
                    config = loaded;
                } catch (Exception invalidConfig) {
                    Path backup = preserveInvalidConfig();
                    EHud.LOGGER.error("Could not read E HUD configuration; preserved it at {} and restored defaults.",
                            backup, invalidConfig);
                    config = new EHudConfig();
                    writeAtomically(CONFIG_FILE, GSON.toJson(config));
                }
            } else {
                save();
            }
        } catch (Exception exception) {
            EHud.LOGGER.error("Could not load E HUD configuration; using defaults.", exception);
            config = new EHudConfig();
        }
    }

    public static synchronized void save() {
        try {
            createDirectories();
            config.ensureDefaults();
            maybeCreateScheduledBackup();
            writeAtomically(CONFIG_FILE, GSON.toJson(config));
        } catch (Exception exception) {
            EHud.LOGGER.error("Could not save E HUD configuration.", exception);
        }
    }

    public static Path exportPreset(String requestedName) throws IOException {
        return exportPreset(config, requestedName);
    }

    public static Path exportPreset(EHudConfig source, String requestedName) throws IOException {
        Files.createDirectories(PRESET_DIR);
        String safeName = sanitizeName(requestedName == null || requestedName.isBlank()
                ? source.activeProfile : requestedName);
        Path destination = PRESET_DIR.resolve(safeName + ".txt");
        writeAtomically(destination, GSON.toJson(source));
        return destination;
    }

    public static List<Path> listPresets() throws IOException {
        Files.createDirectories(PRESET_DIR);
        try (Stream<Path> stream = Files.list(PRESET_DIR)) {
            return stream.filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().endsWith(".txt"))
                    .sorted(Comparator.comparing(path -> path.getFileName().toString().toLowerCase()))
                    .toList();
        }
    }

    public static Path presetDirectory() {
        return PRESET_DIR;
    }

    public static Path presetByName(String name) throws IOException {
        String expected = sanitizeName(name) + ".txt";
        return listPresets().stream()
                .filter(path -> path.getFileName().toString().equalsIgnoreCase(expected))
                .findFirst().orElseThrow(() -> new IOException("Preset not found: " + name));
    }

    public static EHudConfig importPreset(Path source) throws IOException {
        try {
            EHudConfig imported = GSON.fromJson(
                    Files.readString(source, StandardCharsets.UTF_8), EHudConfig.class);
            if (imported == null) {
                throw new IOException("Preset did not contain a valid E HUD configuration.");
            }
            imported.ensureDefaults();
            return imported;
        } catch (RuntimeException exception) {
            throw new IOException("Preset contains invalid E HUD configuration data.", exception);
        }
    }

    public static Path createBackup(String reason) throws IOException {
        Files.createDirectories(BACKUP_DIR);
        Path destination = uniqueBackupPath(sanitizeName(reason));
        if (Files.exists(CONFIG_FILE)) {
            Files.copy(CONFIG_FILE, destination, StandardCopyOption.REPLACE_EXISTING);
        } else {
            Files.writeString(destination, GSON.toJson(config), StandardCharsets.UTF_8);
        }
        return destination;
    }

    public static Path rootDirectory() {
        return ROOT;
    }

    private static void maybeCreateScheduledBackup() throws IOException {
        if (!config.weeklyBackups || !Files.exists(CONFIG_FILE)) {
            return;
        }
        long newestBackup = 0L;
        if (Files.exists(BACKUP_DIR)) {
            try (Stream<Path> stream = Files.list(BACKUP_DIR)) {
                newestBackup = stream.filter(Files::isRegularFile)
                        .map(path -> {
                            try {
                                return Files.getLastModifiedTime(path).toMillis();
                            } catch (IOException ignored) {
                                return 0L;
                            }
                        })
                        .max(Comparator.naturalOrder())
                        .orElse(0L);
            }
        }
        long interval = Math.max(1, config.backupIntervalDays) * 86_400_000L;
        if (System.currentTimeMillis() - newestBackup >= interval) {
            createBackup("scheduled");
        }
    }

    private static void writeAtomically(Path destination, String content) throws IOException {
        Files.createDirectories(destination.getParent());
        Path temporary = Files.createTempFile(
                destination.getParent(), destination.getFileName().toString() + ".", ".tmp");
        try {
            Files.writeString(temporary, content, StandardCharsets.UTF_8);
            try {
                Files.move(temporary, destination, StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.ATOMIC_MOVE);
            } catch (IOException ignored) {
                Files.move(temporary, destination, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temporary);
        }
    }

    private static String sanitizeName(String input) {
        if (input == null) {
            return "preset";
        }
        String sanitized = input.replaceAll("[^a-zA-Z0-9._-]", "_");
        return sanitized.isBlank() ? "preset" : sanitized;
    }

    private static void createDirectories() throws IOException {
        Files.createDirectories(ROOT);
        Files.createDirectories(PRESET_DIR);
        Files.createDirectories(BACKUP_DIR);
    }

    private static Path preserveInvalidConfig() throws IOException {
        Path destination = uniqueBackupPath("invalid-config");
        Files.copy(CONFIG_FILE, destination, StandardCopyOption.REPLACE_EXISTING);
        return destination;
    }

    private static Path uniqueBackupPath(String reason) {
        String prefix = BACKUP_NAME.format(Instant.now()) + "_" + reason;
        Path destination = BACKUP_DIR.resolve(prefix + ".json");
        int duplicate = 2;
        while (Files.exists(destination)) {
            destination = BACKUP_DIR.resolve(prefix + "_" + duplicate++ + ".json");
        }
        return destination;
    }
}
