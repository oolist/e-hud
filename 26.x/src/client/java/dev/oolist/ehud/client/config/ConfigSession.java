package dev.oolist.ehud.client.config;

public final class ConfigSession {
    private final EHudConfig original;
    private EHudConfig working;
    private int changes;

    public ConfigSession() {
        original = ConfigManager.get().copy();
        working = original.copy();
    }

    public EHudConfig original() {
        return original;
    }

    public EHudConfig working() {
        return working;
    }

    public int changes() {
        return changes;
    }

    public void changed() {
        changes++;
    }

    public void replaceWorking(EHudConfig replacement) {
        working = replacement;
        working.ensureDefaults();
        changed();
    }
}
