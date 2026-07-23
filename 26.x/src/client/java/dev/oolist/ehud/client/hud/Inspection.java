package dev.oolist.ehud.client.hud;

import java.util.ArrayList;
import java.util.List;

public final class Inspection {
    private final String title;
    private final String subtitle;
    private final String namespace;
    private final List<InfoLine> lines = new ArrayList<>();

    public Inspection(String title, String subtitle) {
        this(title, subtitle, "general");
    }

    public Inspection(String title, String subtitle, String namespace) {
        this.title = title;
        this.subtitle = subtitle;
        this.namespace = namespace;
    }

    public Inspection add(String label, String value) {
        lines.add(new InfoLine(namespace + "." + keyOf(label), label, value, InfoLine.Severity.NORMAL));
        return this;
    }

    public Inspection add(String label, String value, InfoLine.Severity severity) {
        lines.add(new InfoLine(namespace + "." + keyOf(label), label, value, severity));
        return this;
    }

    public Inspection add(String key, String label, String value, InfoLine.Severity severity) {
        lines.add(new InfoLine(key, label, value, severity));
        return this;
    }

    public String title() {
        return title;
    }

    public String subtitle() {
        return subtitle;
    }

    public List<InfoLine> lines() {
        return lines;
    }

    private static String keyOf(String label) {
        return label.toLowerCase(java.util.Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "_")
                .replaceAll("^_+|_+$", "");
    }
}
