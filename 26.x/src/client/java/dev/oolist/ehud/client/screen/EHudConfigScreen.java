package dev.oolist.ehud.client.screen;

import dev.oolist.ehud.client.config.ConfigManager;
import dev.oolist.ehud.client.config.ConfigSession;
import dev.oolist.ehud.client.config.EHudConfig;
import dev.oolist.ehud.client.config.FeatureCatalog;
import dev.oolist.ehud.client.config.HudModule;
import dev.oolist.ehud.client.hud.EHudRenderer;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.io.IOException;

public final class EHudConfigScreen extends Screen {
    private final Screen parent;
    private final ConfigSession session;
    private String status = "Changes are staged until Save";

    public EHudConfigScreen(Screen parent) {
        this(parent, new ConfigSession());
    }

    EHudConfigScreen(Screen parent, ConfigSession session) {
        super(Component.translatable("ehud.config.title"));
        this.parent = parent;
        this.session = session;
    }

    @Override
    protected void init() {
        clearWidgets();
        EHudConfig working = session.working();
        int columns = width >= 620 ? 3 : 2;
        int gap = 8;
        int cardWidth = Math.min(190, (width - 36 - gap * (columns - 1)) / columns);
        int left = (width - (cardWidth * columns + gap * (columns - 1))) / 2;
        int top = 47;
        int cardHeight = 24;

        HudModule[] modules = HudModule.values();
        for (int index = 0; index < modules.length; index++) {
            HudModule module = modules[index];
            int x = left + (index % columns) * (cardWidth + gap);
            int y = top + (index / columns) * 30;
            addRenderableWidget(Button.builder(moduleLabel(module), button ->
                    VersionClientUi.setScreen(minecraft, new HudCategoryScreen(this, session, module)))
                    .bounds(x, y, cardWidth, cardHeight).build());
        }

        int rows = (modules.length + columns - 1) / columns;
        int utilityY = Math.min(height - 118, top + rows * 30 + 7);
        int utilityWidth = Math.min(132, (width - 52) / 4);
        int utilityLeft = (width - (utilityWidth * 4 + 18)) / 2;
        addRenderableWidget(Button.builder(Component.literal("Appearance"), button ->
                VersionClientUi.setScreen(minecraft, new AppearanceScreen(this, session)))
                .bounds(utilityLeft, utilityY, utilityWidth, 22).build());
        addRenderableWidget(Button.builder(Component.literal("HUD Editor"), button ->
                VersionClientUi.setScreen(minecraft, new HudEditorScreen(this, session)))
                .bounds(utilityLeft + utilityWidth + 6, utilityY, utilityWidth, 22).build());
        addRenderableWidget(Button.builder(Component.literal("Profiles"), button ->
                VersionClientUi.setScreen(minecraft, new ProfilesScreen(this, session)))
                .bounds(utilityLeft + (utilityWidth + 6) * 2, utilityY, utilityWidth, 22).build());
        addRenderableWidget(Button.builder(Component.literal("Performance"), button ->
                VersionClientUi.setScreen(minecraft, new PerformanceScreen(this, session)))
                .bounds(utilityLeft + (utilityWidth + 6) * 3, utilityY, utilityWidth, 22).build());

        addRenderableWidget(Button.builder(Component.literal("Favorites"), button ->
                VersionClientUi.setScreen(minecraft, new FeatureListScreen(this, session, true)))
                .bounds(width / 2 - 226, utilityY + 27, 112, 22).build());
        addRenderableWidget(Button.builder(Component.literal("Compatibility & server"), button ->
                VersionClientUi.setScreen(minecraft, new CompatibilityScreen(this, session)))
                .bounds(width / 2 - 100, utilityY + 27, 200, 22).build());
        addRenderableWidget(Button.builder(Component.literal("Recently changed"), button ->
                VersionClientUi.setScreen(minecraft, new FeatureListScreen(this, session, false)))
                .bounds(width / 2 + 114, utilityY + 27, 112, 22).build());

        int bottom = height - 27;
        addRenderableWidget(Button.builder(Component.literal("Save"), button -> requestSave())
                .bounds(width / 2 - 188, bottom, 72, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Cancel"), button -> onClose())
                .bounds(width / 2 - 111, bottom, 72, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Reset"), button -> requestReset())
                .bounds(width / 2 - 34, bottom, 72, 20).build());
        addRenderableWidget(Button.builder(advancedLabel(), button -> {
            working.advancedMode = !working.advancedMode;
            session.changed();
            status = working.advancedMode ? "Advanced mode unlocked" : "Basic mode active";
            rebuildWidgets();
        }).bounds(width / 2 + 43, bottom, 128, 20).build());

        addRenderableWidget(Button.builder(Component.literal("Export"), button -> exportPreset())
                .bounds(8, bottom, 62, 20).build());
        addRenderableWidget(Button.builder(Component.literal((working.debugMode ? "[ON] " : "") + "Bug Debug"), button -> {
            working.debugMode = !working.debugMode;
            session.changed();
            rebuildWidgets();
        }).bounds(width - 94, 8, 86, 20).build());
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        EHudScreenStyle.background(graphics, width, height);
        EHudScreenStyle.header(graphics, font, width, session.working(), "SETTINGS");
        super.extractRenderState(graphics, mouseX, mouseY, delta);
        int previewY = height - 34;
        if (session.working().showLivePreview && height >= 430) {
            EHudRenderer.renderPreview(graphics, width - 128, previewY, session.working());
        }
        graphics.text(font, "Profile: " + session.working().activeProfile + "  |  "
                + session.changes() + " changes", 78, height - 21, 0xFFB7C8BC, false);
        graphics.centeredText(font, status, width / 2, height - 42, 0xFF86A98E);
    }

    @Override
    public void onClose() {
        if (minecraft != null) VersionClientUi.setScreen(minecraft, parent);
    }

    private Component moduleLabel(HudModule module) {
        boolean enabled = session.working().modules.getOrDefault(module, true);
        return Component.literal((enabled ? "[ON] " : "[OFF] ") + module.title()
                + " (" + FeatureCatalog.count(module) + ")");
    }

    private Component advancedLabel() {
        return Component.literal(session.working().advancedMode ? "Advanced: ON" : "Advanced Mode");
    }

    private void requestSave() {
        if (session.working().confirmLargeChanges && session.changes() > 10) {
            VersionClientUi.setScreen(minecraft, new ConfirmScreen(confirmed -> {
                if (confirmed) saveNow();
                else VersionClientUi.setScreen(minecraft, this);
            }, Component.literal("Save " + session.changes() + " E HUD changes?"),
                    Component.literal("All staged changes will become active.")));
        } else {
            saveNow();
        }
    }

    private void saveNow() {
        ConfigManager.replace(session.working().copy());
        ConfigManager.save();
        if (minecraft != null) VersionClientUi.setScreen(minecraft, parent);
    }

    private void requestReset() {
        VersionClientUi.setScreen(minecraft, new ConfirmScreen(confirmed -> {
            if (confirmed) {
                try {
                    ConfigManager.createBackup("before-reset");
                    status = "Backup created before reset";
                } catch (IOException exception) {
                    status = "Backup failed; reset cancelled";
                    VersionClientUi.setScreen(minecraft, this);
                    return;
                }
                session.replaceWorking(new EHudConfig());
            }
            VersionClientUi.setScreen(minecraft, this);
        }, Component.literal("Reset E HUD settings?"),
                Component.literal("A recoverable backup will be created first.")));
    }

    private void exportPreset() {
        try {
            var path = ConfigManager.exportPreset(session.working(), session.working().activeProfile);
            status = "Exported " + path.getFileName();
        } catch (IOException exception) {
            status = "Could not export preset";
        }
    }
}
