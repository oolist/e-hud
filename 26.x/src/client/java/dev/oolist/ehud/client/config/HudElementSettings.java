package dev.oolist.ehud.client.config;

public final class HudElementSettings {
    public boolean enabled = true;
    public int xOffset = 0;
    public int yOffset = 0;
    public float scale = 1.0F;
    public int color = 0;
    public String condition = "RELEVANT";

    public HudElementSettings copy() {
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
