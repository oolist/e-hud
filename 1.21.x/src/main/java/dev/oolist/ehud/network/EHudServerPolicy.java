package dev.oolist.ehud.network;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.oolist.ehud.EHud;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

final class EHudServerPolicy {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("ehud-server.json");
    private final Set<String> disabledModules = new LinkedHashSet<>();

    static EHudServerPolicy load() {
        try {
            if (Files.exists(FILE)) {
                EHudServerPolicy policy = GSON.fromJson(Files.readString(FILE), EHudServerPolicy.class);
                if (policy != null) return policy;
            }
        } catch (Exception exception) {
            EHud.LOGGER.warn("Could not load E HUD server policy.", exception);
        }
        return new EHudServerPolicy();
    }

    String encoded() { return disabledModules.stream().sorted().collect(Collectors.joining(",")); }

    void replace(String encoded) {
        disabledModules.clear();
        if (encoded != null && !encoded.isBlank()) {
            Arrays.stream(encoded.split(",")).map(String::trim).filter(value -> !value.isBlank())
                    .forEach(disabledModules::add);
        }
        save();
    }

    private void save() {
        try {
            Files.createDirectories(FILE.getParent());
            Files.writeString(FILE, GSON.toJson(this), StandardCharsets.UTF_8);
        } catch (IOException exception) {
            EHud.LOGGER.warn("Could not save E HUD server policy.", exception);
        }
    }
}
