package dev.oolist.ehud.client.hud;

public record InfoLine(String key, String label, String value, Severity severity) {
    public enum Severity {
        NORMAL,
        INFORMATION,
        CAUTION,
        DANGER,
        CRITICAL
    }
}
