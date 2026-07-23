package dev.oolist.ehud.client.screen;

import dev.oolist.ehud.client.config.ConfigManager;
import dev.oolist.ehud.client.config.ConfigSession;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import dev.oolist.ehud.client.network.ClientServerState;

final class ProfilesScreen extends Screen {
    private final Screen parent;
    private final ConfigSession session;
    private EditBox name;
    private String status = "Presets are shareable text files";

    ProfilesScreen(Screen parent, ConfigSession session) {
        super(Component.literal("E HUD Profiles"));
        this.parent = parent; this.session = session;
    }

    @Override
    protected void init() {
        clearWidgets();
        name = new EditBox(font, width / 2 - 145, 56, 190, 20, Component.literal("Profile name"));
        name.setMaxLength(48); name.setValue(session.working().activeProfile); addRenderableWidget(name);
        addRenderableWidget(Button.builder(Component.literal("Save / export"), button -> export())
                .bounds(width / 2 + 51, 56, 94, 20).build());
        List<Path> presets = presets();
        for (int index = 0; index < Math.min(7, presets.size()); index++) {
            Path preset = presets.get(index);
            String file = preset.getFileName().toString();
            addRenderableWidget(Button.builder(Component.literal(file), button -> load(preset))
                    .bounds(width / 2 - 145, 88 + index * 25, 290, 20).build());
        }
        addRenderableWidget(Button.builder(Component.literal("Duplicate current"), button -> {
            name.setValue(session.working().activeProfile + " Copy"); export();
        }).bounds(width / 2 - 145, height - 55, 140, 20).build());
        Button link = Button.builder(Component.literal("Use in this world/server"), button -> linkCurrent())
                .bounds(width / 2 + 5, height - 55, 140, 20).build();
        link.active = minecraft != null && ClientServerState.currentProfileKey(minecraft) != null;
        addRenderableWidget(link);
        addRenderableWidget(Button.builder(Component.literal("Done"), button -> onClose())
                .bounds(width / 2 - 60, height - 30, 120, 20).build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        renderBackground(graphics, mouseX, mouseY, delta);
        EHudScreenStyle.header(graphics, font, width, session.working(), "PROFILES & SHARING");
        super.render(graphics, mouseX, mouseY, delta);
        if (presets().isEmpty()) graphics.drawCenteredString(font, "No exported presets yet", width / 2, 104, 0xFF94A89A);
        graphics.drawCenteredString(font, status, width / 2, height - 30, 0xFF9FD8AE);
    }

    @Override public void onClose() { minecraft.setScreen(parent); }

    private void export() {
        String requested = name.getValue().isBlank() ? "Default" : name.getValue().trim();
        session.working().activeProfile = requested;
        try {
            Path path = ConfigManager.exportPreset(session.working(), requested);
            session.changed(); status = "Exported " + path.getFileName(); rebuildWidgets();
        } catch (IOException exception) { status = "Export failed"; }
    }

    private void load(Path path) {
        try {
            var imported = ConfigManager.importPreset(path);
            String file = path.getFileName().toString();
            imported.activeProfile = file.substring(0, file.length() - 4);
            session.replaceWorking(imported); status = "Loaded " + file + " (staged)"; rebuildWidgets();
        } catch (IOException exception) { status = "That preset could not be imported"; }
    }

    private List<Path> presets() {
        try { return ConfigManager.listPresets(); } catch (IOException exception) { return List.of(); }
    }

    private void linkCurrent() {
        String key = ClientServerState.currentProfileKey(minecraft);
        if (key == null) { status = "Join a world or server first"; return; }
        String profile = name.getValue().isBlank() ? session.working().activeProfile : name.getValue().trim();
        session.working().serverProfiles.put(key, profile);
        session.changed();
        status = "Linked " + profile + " to this world/server (staged)";
    }
}
