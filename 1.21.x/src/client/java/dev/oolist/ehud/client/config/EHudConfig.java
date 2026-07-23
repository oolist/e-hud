package dev.oolist.ehud.client.config;

import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class EHudConfig {
    public static final int CURRENT_SCHEMA = 2;

    public int schema = CURRENT_SCHEMA;
    public boolean enabled = true;
    public boolean advancedMode = false;
    public boolean debugMode = false;
    public boolean animations = true;
    public boolean warningIcons = true;
    public boolean warningSounds = true;
    public boolean automaticInspection = true;
    public boolean showTechnicalDetails = true;
    public boolean showRegistryIds = false;
    public boolean showLivePreview = true;
    public boolean saveGlobally = true;
    public boolean adaptivePerformance = true;
    public boolean weeklyBackups = true;
    public boolean confirmLargeChanges = true;
    public boolean showVanillaServerWarning = true;
    public boolean doNotShowVanillaServerWarningAgain = false;
    public boolean showIcons = true;
    public boolean textShadow = true;
    public boolean compactMode = false;
    public boolean animatedGradient = true;
    public boolean rememberPinnedTarget = false;
    public boolean perServerProfiles = true;
    public boolean respectServerPolicy = true;
    public boolean performanceWarnings = true;

    public int primaryColor = 0xFF42F57B;
    public int accentColor = 0xFFFF8A21;
    public int panelColor = 0xDD0A1610;
    public int textColor = 0xFFF4FFF7;
    public int warningColor = 0xFFFFB13B;
    public int dangerColor = 0xFFFF4D4D;

    public float hudScale = 1.0F;
    public float panelOpacity = 0.86F;
    public float neonIntensity = 0.55F;
    public float animationSpeed = 1.0F;
    public float lineSpacing = 1.0F;
    public int hudYOffset = 44;
    public int hudXOffset = 0;
    public int panelPadding = 9;
    public int maximumLines = 8;
    public int scanDistance = -1;
    public int scanIntervalTicks = 10;
    public int maximumCheckedBlocks = 4096;
    public int backupIntervalDays = 7;

    public String activeProfile = "Default";
    public String gradientName = "Neon Grove";
    public String anchor = "HOTBAR_TOP";
    public String fontStyle = "MINECRAFT";
    public String iconStyle = "NEON";
    public String borderStyle = "THIN_GLOW";
    public String backgroundStyle = "DARK_GLASS";
    public String alertSound = "EXPERIENCE_ORB";
    public List<Integer> gradientColors = new java.util.ArrayList<>(List.of(0xFFFF8A21, 0xFFFFC23B, 0xFF42F57B, 0xFF20D978));
    public Map<HudModule, Boolean> modules = new EnumMap<>(HudModule.class);
    public Map<String, Boolean> individualSettings = new LinkedHashMap<>();
    public Map<String, HudElementSettings> elementSettings = new LinkedHashMap<>();
    public Map<String, String> serverProfiles = new LinkedHashMap<>();
    public List<String> favoriteSettings = new java.util.ArrayList<>();
    public List<String> recentSettings = new java.util.ArrayList<>();

    public EHudConfig() {
        ensureDefaults();
    }

    public void ensureDefaults() {
        if (modules == null) modules = new EnumMap<>(HudModule.class);
        if (individualSettings == null) individualSettings = new LinkedHashMap<>();
        if (elementSettings == null) elementSettings = new LinkedHashMap<>();
        if (serverProfiles == null) serverProfiles = new LinkedHashMap<>();
        if (gradientColors == null || gradientColors.size() < 2) {
            gradientColors = new java.util.ArrayList<>(List.of(0xFFFF8A21, 0xFFFFC23B, 0xFF42F57B, 0xFF20D978));
        }
        if (favoriteSettings == null) favoriteSettings = new java.util.ArrayList<>();
        if (recentSettings == null) recentSettings = new java.util.ArrayList<>();
        for (HudModule module : HudModule.values()) {
            modules.putIfAbsent(module, true);
        }
        individualSettings.putIfAbsent("entity.health", true);
        individualSettings.putIfAbsent("entity.armor", true);
        individualSettings.putIfAbsent("entity.effects", true);
        individualSettings.putIfAbsent("entity.age", true);
        individualSettings.putIfAbsent("block.hardness", true);
        individualSettings.putIfAbsent("block.light", true);
        individualSettings.putIfAbsent("block.redstone", true);
        individualSettings.putIfAbsent("world.coordinates", true);
        individualSettings.putIfAbsent("world.biome", true);
        individualSettings.putIfAbsent("player.inventory_space", true);
    }

    public EHudConfig copy() {
        EHudConfig copy = new EHudConfig();
        copy.schema = schema;
        copy.enabled = enabled;
        copy.advancedMode = advancedMode;
        copy.debugMode = debugMode;
        copy.animations = animations;
        copy.warningIcons = warningIcons;
        copy.warningSounds = warningSounds;
        copy.automaticInspection = automaticInspection;
        copy.showTechnicalDetails = showTechnicalDetails;
        copy.showRegistryIds = showRegistryIds;
        copy.showLivePreview = showLivePreview;
        copy.saveGlobally = saveGlobally;
        copy.adaptivePerformance = adaptivePerformance;
        copy.weeklyBackups = weeklyBackups;
        copy.confirmLargeChanges = confirmLargeChanges;
        copy.showVanillaServerWarning = showVanillaServerWarning;
        copy.doNotShowVanillaServerWarningAgain = doNotShowVanillaServerWarningAgain;
        copy.showIcons = showIcons;
        copy.textShadow = textShadow;
        copy.compactMode = compactMode;
        copy.animatedGradient = animatedGradient;
        copy.rememberPinnedTarget = rememberPinnedTarget;
        copy.perServerProfiles = perServerProfiles;
        copy.respectServerPolicy = respectServerPolicy;
        copy.performanceWarnings = performanceWarnings;
        copy.primaryColor = primaryColor;
        copy.accentColor = accentColor;
        copy.panelColor = panelColor;
        copy.textColor = textColor;
        copy.warningColor = warningColor;
        copy.dangerColor = dangerColor;
        copy.hudScale = hudScale;
        copy.panelOpacity = panelOpacity;
        copy.neonIntensity = neonIntensity;
        copy.animationSpeed = animationSpeed;
        copy.lineSpacing = lineSpacing;
        copy.hudYOffset = hudYOffset;
        copy.hudXOffset = hudXOffset;
        copy.panelPadding = panelPadding;
        copy.maximumLines = maximumLines;
        copy.scanDistance = scanDistance;
        copy.scanIntervalTicks = scanIntervalTicks;
        copy.maximumCheckedBlocks = maximumCheckedBlocks;
        copy.backupIntervalDays = backupIntervalDays;
        copy.activeProfile = activeProfile;
        copy.gradientName = gradientName;
        copy.anchor = anchor;
        copy.fontStyle = fontStyle;
        copy.iconStyle = iconStyle;
        copy.borderStyle = borderStyle;
        copy.backgroundStyle = backgroundStyle;
        copy.alertSound = alertSound;
        copy.gradientColors = new java.util.ArrayList<>(gradientColors == null ? List.of(primaryColor, accentColor) : gradientColors);
        copy.modules = new EnumMap<>(modules);
        copy.individualSettings = new LinkedHashMap<>(individualSettings);
        copy.elementSettings = new LinkedHashMap<>();
        elementSettings.forEach((key, value) -> copy.elementSettings.put(key, value.copy()));
        copy.serverProfiles = new LinkedHashMap<>(serverProfiles);
        copy.favoriteSettings = new java.util.ArrayList<>(favoriteSettings);
        copy.recentSettings = new java.util.ArrayList<>(recentSettings);
        return copy;
    }

    public HudElementSettings settingsFor(String key) {
        return elementSettings.computeIfAbsent(key, ignored -> new HudElementSettings());
    }

    public boolean isLineEnabled(String key) {
        HudElementSettings exact = elementSettings.get(key);
        if (exact != null) {
            return exact.enabled;
        }
        Boolean legacy = individualSettings.get(key);
        if (legacy != null) {
            return legacy;
        }
        for (Map.Entry<String, Boolean> entry : individualSettings.entrySet()) {
            if (entry.getKey().endsWith("." + key)) {
                return entry.getValue();
            }
        }
        return true;
    }
}
