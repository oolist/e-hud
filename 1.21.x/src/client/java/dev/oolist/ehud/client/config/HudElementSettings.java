package dev.oolist.ehud.client.config;

public final class HudElementSettings {
    public boolean enabled = true;
    public int xOffset = 0;
    public int yOffset = 0;
    public float scale = 1.0F;
    public int color = 0;
    public String condition = "RELEVANT";

    public void ensureDefaults() {
        xOffset = Math.max(-10_000, Math.min(10_000, xOffset));
        yOffset = Math.max(-10_000, Math.min(10_000, yOffset));
        scale = Float.isFinite(scale) ? Math.max(0.5F, Math.min(2.0F, scale)) : 1.0F;
        if (condition == null || condition.isBlank()) {
            condition = "RELEVANT";
        }
    }

    public HudElementSettings copy() {
        ensureDefaults();
        HudElementSettings copy = new HudElementSettings();
        copy.enabled = enabled;
        copy.xOffset = xOffset;
        copy.yOffset = yOffset;
        copy.scale = scale;
        copy.color = color;
        copy.condition = condition;
        return copy;
    }
}
